package com.gopal.twit.core.ref;

import com.gopal.twit.core.GitRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * REFERENCES
 * they’re text files, in the .git/refs hierarchy;
 * they hold the SHA-1 identifier of an object, or a reference to another reference, ultimately to a SHA-1 (no loops!)
 */

public class RefResolver {

    /**
     * Resolve a reference to a SHA-1
     */
    public static String refResolve(GitRepository repo, String ref) throws IOException{
        Path path = repo.repoFile(ref);


        //Sometimes, an indirect reference may be broken.  This is normal
        //in one specific case: we're looking for HEAD on a new repository
        //with no commits.  In that case, .git/HEAD points to "ref:
        //refs/heads/main", but .git/refs/heads/main doesn't exist yet
        //(since there's no commit for it to refer to).
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
     * List all references
     * it is a recursive function to collect refs and return them as a dict
     */
    public static Map<String, Object> refList(GitRepository repo, Path path) throws IOException{
        if(path == null){
            path = repo.repoDir("refs");
        }

        //return the refs in a treemap since git stores refs in sorted order
        Map<String, Object> ret = new TreeMap<>();
        try(Stream<Path> files = Files.list(path).sorted()){
            for(Path f : files.toList()){
                String name = f.getFileName().toString();
                if(Files.isDirectory(f)){
                    ret.put(name,refList(repo, f));
                }else {
                    //f.toString().substring(repo.getGitdir().toString().length() + 1) -> removes the .git/ prefix from the path
                    ret.put(name, refResolve(repo, f.toString().substring(repo.getGitDir().toString().length() + 1)));
                }
            }
        }
        return ret;
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
