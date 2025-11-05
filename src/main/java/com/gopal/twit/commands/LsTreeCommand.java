package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.objects.GitObject;
import com.gopal.twit.core.objects.GitTree;
import com.gopal.twit.core.objects.GitTreeLeaf;
import com.gopal.twit.util.RefResolver;
import com.gopal.twit.util.ObjectIO;

/**
 * simply prints the contents of a tree, recursively with the -r flag
 * In recursive mode, it doesn’t show subtrees, just final objects with their full paths.
 */
public class LsTreeCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        boolean recursive = false;
        String tree = null;

        for(String arg : args){
            if(args.equals("-r")){
                recursive = true;
            }
            else{
                tree = arg;
            }
        }

        if (tree == null) {
            System.err.println("Usage: twit ls-tree [-r] <tree>");
            return;
        }

        GitRepository repo = GitRepository.find();
        lsTree(repo, tree, recursive, "");
    }

    private void lsTree(GitRepository repo, String ref, boolean recursive, String prefix) throws Exception{
        String sha = RefResolver.objectFind(repo, ref, "tree", false);
        GitObject obj = ObjectIO.objectRead(repo, sha);

        if(!(obj instanceof GitTree tree)){
            throw new Exception("Not a tree: " + ref);
        }

        for(GitTreeLeaf item : tree.getItems()){
            String mode = item.getMode();
            String type;

            if (mode.startsWith("04")) {
                type = "tree";
            } else if (mode.startsWith("10")) {
                type = "blob";
            } else if (mode.startsWith("12")) {
                type = "blob"; // symlink
            } else if (mode.startsWith("16")) {
                type = "commit"; // submodule
            } else {
                type = "unknown";
            }

            if (!recursive || !type.equals("tree")) {
                System.out.println(String.format("%06d %s %s\t%s%s",
                        Integer.parseInt(mode, 8), type, item.getSha(), prefix, item.getPath()));
            } else {
                lsTree(repo, item.getSha(), recursive, prefix + item.getPath() + "/");
            }
        }
    }
}
