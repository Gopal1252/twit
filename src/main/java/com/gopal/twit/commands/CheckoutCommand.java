package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.objects.*;
import com.gopal.twit.core.ref.RefResolver;
import com.gopal.twit.util.ObjectIO;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * It's a simpler version of what git provides
 * Normally, git checkout instantiates a commit in the worktree
 * normal git checkout only takes a commit as argument
 * our version takes a commit and a directory and then
 * instantiates the tree in the directory, if and only if the directory is empty.
 */
public class CheckoutCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: twit checkout <commit> <path>");
            return;
        }

        String commit = args[0];
        String pathStr = args[1];

        GitRepository repo = GitRepository.find();
        String sha = RefResolver.objectFind(repo, commit);
        GitObject obj = ObjectIO.objectRead(repo, sha);

        // If commit, get its tree
        if (obj instanceof GitCommit commitObj) {
            byte[] treeBytes = (byte[]) commitObj.getKvlm().get("tree");
            String treeSha = new String(treeBytes, "UTF-8");
            obj = ObjectIO.objectRead(repo, treeSha);
        }

        Path path = Paths.get(pathStr);

        // Verify path is empty
        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                throw new Exception("Not a directory: " + path);
            }
            if (Files.list(path).findAny().isPresent()) {
                throw new Exception("Not empty: " + path);
            }
        } else {
            Files.createDirectories(path);
        }

        treeCheckout(repo, (GitTree) obj, path);
    }

    private void treeCheckout(GitRepository repo, GitTree tree, Path path) throws Exception{
        for (GitTreeLeaf item : tree.getItems()) {
            GitObject obj = ObjectIO.objectRead(repo, item.getSha());
            Path dest = path.resolve(item.getPath());

            if (obj instanceof GitTree subTree) {
                Files.createDirectory(dest);
                treeCheckout(repo, subTree, dest);
            } else if (obj instanceof GitBlob blob) {
                Files.write(dest, blob.getBlobData());
            }
        }
    }
}
