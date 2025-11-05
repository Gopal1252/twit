package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;
import com.gopal.twit.util.IndexIO;
import com.gopal.twit.util.ObjectIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * STEPS:
 * 1) Firstly, remove the existing index entry, if there’s one, without removing the file itself {basically remove the path' entry from the index if already there}
 * 2) Then hash the file into a glob object
 * 3) Then create its entry
 * 4) Finally write the modified index back
 */
public class AddCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        if(args.length == 0){
            System.err.println("Usage: twit add <path>...");
            return;
        }

        GitRepository repo = GitRepository.find();
        add(repo, Arrays.asList(args));
    }

    private void add(GitRepository repo, List<String> paths) throws Exception{
        //first remove paths from index, if already exist
        rm(repo, paths, false, true);

        Path worktree = repo.getWorktree();
        Set<PathPair> cleanPaths = new HashSet<>(); //stores the paths as pairs {absolute, relative_to_worktree}

        //Convert the paths to pairs: (absolute, relative_to_worktree).
        for(String path : paths){
            Path absPath = Paths.get(path).toAbsolutePath();

            if (!absPath.startsWith(worktree) || !Files.isRegularFile(absPath)) {
                throw new Exception("Not a file, or outside the worktree: " + path);
            }

            Path relPath = worktree.relativize(absPath);
            cleanPaths.add(new PathPair(absPath, relPath.toString()));
        }

        // Read index (It was modified by rm) {This is not optimal, but good enough for twit}
        GitIndex index = IndexIO.indexRead(repo);

        for(PathPair pair : cleanPaths){
            String sha;
            try (var in = Files.newInputStream(pair.absolute)) {
                sha = ObjectIO.objectHash(in, "blob", repo);
            }

            BasicFileAttributes stat = Files.readAttributes(pair.absolute, BasicFileAttributes.class);

            long ctimeS = stat.creationTime().toInstant().getEpochSecond();
            long ctimeNs = stat.creationTime().toInstant().getNano();
            long mtimeS = stat.lastModifiedTime().toInstant().getEpochSecond();
            long mtimeNs = stat.lastModifiedTime().toInstant().getNano();

            GitIndexEntry entry = new GitIndexEntry(
                    new long[]{ctimeS, ctimeNs},
                    new long[]{mtimeS, mtimeNs},
                    0, // dev - simplified
                    0, // ino - simplified
                    0b1000, // regular file
                    0644, // permissions
                    0, // uid - simplified
                    0, // gid - simplified
                    stat.size(),
                    sha,
                    false,
                    0,
                    pair.relative
            );

            index.getEntries().add(entry);
        }
        IndexIO.indexWrite(repo, index);
    }

    private void rm(GitRepository repo, List<String> paths, boolean delete, boolean skipMissing) throws Exception{
        GitIndex index = IndexIO.indexRead(repo);
        Path worktree = repo.getWorktree();

        //Make paths absolute
        Set<Path> absPaths = new HashSet<>();
        for (String path : paths) {
            Path absPath = Paths.get(path).toAbsolutePath();
            if (absPath.startsWith(worktree)) {
                absPaths.add(absPath);
            }
        }

        List<GitIndexEntry> kept = new ArrayList<>();//The list of entries to *keep*, which we will write back to the index
        List<Path> toRemove = new ArrayList<>();//The list of removed paths, which we'll use after index update to physically remove the actual paths from the filesystem

        for (GitIndexEntry e : index.getEntries()) {
            Path fullPath = worktree.resolve(e.getName());
            if (absPaths.contains(fullPath)) {
                toRemove.add(fullPath);
                absPaths.remove(fullPath);
            } else {
                kept.add(e);
            }
        }

        //If absPaths is not empty and not allowed to skip paths missing in the index, it means some paths weren't in the index
        if (!absPaths.isEmpty() && !skipMissing) {
            throw new Exception("Cannot remove paths not in index: " + absPaths);
        }

        //physically delete the paths from the filesystem/worktree
        if (delete) {
            for (Path path : toRemove) {
                Files.deleteIfExists(path);
            }
        }

        index.setEntries(kept);
        IndexIO.indexWrite(repo, index);
    }

    private record PathPair(Path absolute, String relative) {}
}
