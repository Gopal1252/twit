package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;
import com.gopal.twit.util.IndexIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Used to remove an entry from the index {so that the next commit won't include it} and also from the filesystem/worktree
 * Need to take care since our command also removes it from the worktree (like the actual one), but doesn't care if it isn't saved
 */
public class RmCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: twit rm <path>...");
            return;
        }

        GitRepository repo = GitRepository.find();
        rm(repo, Arrays.asList(args), true, false);
    }

    private void rm(GitRepository repo, List<String> paths, boolean delete, boolean skipMissing) throws Exception{
        GitIndex index = IndexIO.indexRead(repo);
        Path worktree = repo.getWorktree();

        //Make paths absolute
        Set<Path> absPaths = new HashSet<>();
        for (String path : paths) {
            Path absPath = Paths.get(path).toAbsolutePath();
            if (!absPath.startsWith(worktree)) {
                throw new Exception("Cannot remove paths outside of worktree: " + path);
            }
            absPaths.add(absPath);
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
}
