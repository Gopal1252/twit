package com.gopal.twit.core.ref;

import com.gopal.twit.core.GitRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RefResolver {

    /**
     * Resolve a reference to a SHA-1
     */
    public static String refResolve(GitRepository repo, String ref) throws IOException{
        Path path = repo.repoFile(ref);

        if(!Files.exists(path)){
            return null;
        }

        String data = Files.readString(path).trim();

        if(data.startsWith("ref: ")) {
            return refResolve(repo, data.substring(5));
        }

        return data;
    }

    /**
     * Resolve name to object hash
     */
    public static String objectFind(GitRepository repo, String name) throws Exception{
        return objectFind(repo, name, null, true);
    }

    public static String objectFind(GitRepository repo, String name, String fmt, boolean follow) throws Exception{
        //TODO
        return "";
    }
}
