package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.ignore.GitIgnore;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;
import com.gopal.twit.core.objects.GitTree;
import com.gopal.twit.util.IgnoreParser;
import com.gopal.twit.util.IndexIO;
import com.gopal.twit.util.ObjectIO;
import com.gopal.twit.util.RefResolver;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This command is used to know which files were added, removed or modified since the last commit, and which of these changes are actually staged, and will make it to the next commit.
 * This command compares:
 * 1) HEAD with the STAGING AREA (INDEX)
 * 2) WORKTREE with the STAGING AREA (INDEX)
 */
public class StatusCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        GitRepository repo = GitRepository.find();
        GitIndex index = IndexIO.indexRead(repo);

        //find out which branch we're currently on
        statusBranch(repo);
    }

    /**
     * Prints the current branch we're on
     * Finds the current branch by looking at .git/HEAD
     * .git/HEAD either contains an hexadecimal ID (a ref to a commit, in detached HEAD state), or an indirect reference to something in refs/heads/: the active branch
     */
    private void statusBranch(GitRepository repo) throws IOException{
        Path headFile = repo.repoFile("HEAD");
        String head = Files.readString(headFile).trim();

        if(head.startsWith("ref: refs/heads/")){
            String branch = head.substring(16);
            System.out.println("On branch " + branch + ".");
        }
        else{
            System.out.println("HEAD detached at " + head.substring(0,7));
        }
    }

    /**
     * Displays diff between HEAD and the STAGING AREA (index)
     * Displays the second block of the status output i.e. "changes to be commited"
     * Basically shows how the staging area differs from HEAD
     * In this function we do the following:
     * 1) Read the HEAD tree and flatten it as a single dict (hashmap) with full paths as keys, so it's closer to the (flat) index associating paths to blobs
     * 2) Just compare then and output the differences
     */
    private void statusHeadIndex(GitRepository repo, GitIndex index) throws Exception{
        System.out.println("Changes to be committed:");

        //function to convert head tree(recursive) to a (flat) dict
        Map<String, String> head = treeToDict(repo, "HEAD", "");

        for(GitIndexEntry entry : index.getEntries()){
            if(head.containsKey(entry.getName())){
                if(!head.get(entry.getName()).equals(entry.getSha())){
                    System.out.println(" modified: " + entry.getName());
                }
                head.remove(entry.getName());
            }
            else{
                System.out.println("  added:    " + entry.getName());
            }
        }

        for(String deleted : head.keySet()){
            System.out.println("  deleted:  " + deleted);
        }
    }

    /**
     * Displays diff between STAGING AREA (index) and WORKTREE
     */
    private void statusIndexWorktree(GitRepository repo, GitIndex index) throws Exception{
        System.out.println("Changes not staged for commit:");

        GitIgnore ignore = IgnoreParser.gitIgnoreRead(repo);
        Set<String> allFiles = new HashSet<>();

        //Walk filesystem
        Path worktree = repo.getWorktree();
        Path gitdir = repo.getGitDir();

        /**
         * This portion walks through the working directory (the actual files on disk) to find all files that exist in the project
         * It recursively walks through the working directory, collecting all file paths (except those inside the .git folder), and stores them in allFiles
         */
        Files.walkFileTree(worktree, new SimpleFileVisitor<Path>(){

            //this method runs before entering each directory
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                if(dir.equals(gitdir) || dir.startsWith(gitdir)){
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }

            //this method runs for every file that's visited
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String relPath = worktree.relativize(file).toString();
                allFiles.add(relPath);
                return FileVisitResult.CONTINUE;
            }
        });

        //We now traverse the index, and compare real files with the cached versions
        for(GitIndexEntry entry : index.getEntries()){
            Path fullPath = worktree.resolve(entry.getName());

            if(!Files.exists(fullPath)){
                System.out.println("  deleted:  " + entry.getName());
            }
            else{
                //Compare the metadata

                //metadata from worktree
                BasicFileAttributes attrs = Files.readAttributes(fullPath, BasicFileAttributes.class);
                long ctimeNs = attrs.creationTime().toInstant().getEpochSecond() * 1_000_000_000L + attrs.creationTime().toInstant().getNano();
                long mtimeNs = attrs.lastModifiedTime().toInstant().getEpochSecond() * 1_000_000_000L + attrs.lastModifiedTime().toInstant().getNano();

                //metadata from index
                long storedCtimeNs = entry.getCtime()[0] * 1_000_000_000L + entry.getCtime()[1];
                long storedMtimeNs = entry.getMtime()[0] * 1_000_000_000L + entry.getMtime()[1];

                if (ctimeNs != storedCtimeNs || mtimeNs != storedMtimeNs){
                    //if different, deep compare {check the actual content}
                    try (var in = Files.newInputStream(fullPath)) {
                        String newSha = ObjectIO.objectHash(in, "blob", null);
                        if (!newSha.equals(entry.getSha())) {
                            System.out.println("  modified:  " + entry.getName());
                        }
                    }
                }
            }
            allFiles.remove(entry.getName());
        }

        System.out.println();
        System.out.println("Untracked Files:");
        for(String f : allFiles){
            if(!IgnoreParser.checkIgnore(ignore, f)){
                System.out.println("  " + f);
            }
        }
    }

    /**
     * Converts a tree(recursive) to a (flat) dict
     * Returns a flattened hashmap of the type {completePath, object's sha}
     */
    private Map<String, String> treeToDict(GitRepository repo, String ref, String prefix) throws Exception{
        Map<String, String> ret = new HashMap<>();
        String treeSha = RefResolver.objectFind(repo, ref, "tree", false);
        var tree = (GitTree) ObjectIO.objectRead(repo, treeSha);

        for(var leaf : tree.getItems()){
            String fullPath = prefix.isEmpty() ? leaf.getPath() : prefix + "/" + leaf.getPath();

            if(leaf.getMode().startsWith("04")){//meaning the path is to a directory (tree)
                //subtree
                ret.putAll(treeToDict(repo, leaf.getSha(), fullPath));
            }
            else{
                ret.put(fullPath, leaf.getSha());
            }
        }
        return ret;
    }
}
