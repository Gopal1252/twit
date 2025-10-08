package com.gopal.twit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class RepoUtils {

    //builds/computes the path within .git directory
    public static Path repoPath(GitRepository repo, String... path){
        Path p = repo.getGitDir();
        for(String s : path){
            p = p.resolve(s);
        }
        return p;
    }

    //returns and optionally creates a path to a file
    public static Path repoFile(GitRepository repo, boolean mkdir, String... path) throws IOException{
        if (repoDir(repo, mkdir, Arrays.copyOf(path, path.length - 1)) != null) {
            return repoPath(repo, path);
        }
        return null;
    }

    //returns and optionally creates a path to a directory
    public static Path repoDir(GitRepository repo, boolean mkdir, String... path) throws IOException{
        Path p = repoPath(repo,path);
        if(Files.exists(p)){
            if(!Files.isDirectory(p)){
                throw new IOException("Not a directory: " + p);
            }
        }

        if(mkdir){
            Files.createDirectories(p);
        }
        return p;
    }
}
