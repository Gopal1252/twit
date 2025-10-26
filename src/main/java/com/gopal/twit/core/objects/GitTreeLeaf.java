package com.gopal.twit.core.objects;

/**
 * Tree describes the content of the worktree i.e. it associates blobs to paths
 * It is an array of three-element tuples -> file mode, path (relative to worktree), SHA-1
 * Trees are binary objects which is basically an array of 3 element tuples of the format: [mode] space [path] 0x00 [sha-1]
 * We represent a single record in the tree using the GitTreeLeaf object
 */
public class GitTreeLeaf {
    private final String mode;
    private final String path;
    private final String sha;

    public GitTreeLeaf(String mode, String path, String sha){
        this.mode = mode;
        this.path = path;
        this.sha = sha;
    }

    public String getMode(){
        return mode;
    }

    public String getPath(){
        return path;
    }

    public String getSha(){
        return sha;
    }
}
