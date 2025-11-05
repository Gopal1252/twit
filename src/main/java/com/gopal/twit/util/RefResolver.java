package com.gopal.twit.util;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.objects.GitCommit;
import com.gopal.twit.core.objects.GitObject;
import com.gopal.twit.core.objects.GitTag;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
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
     * Resolve name to object hash
     * This function has 2 parts:
     * 1) First it uses the objectResolve function to resolve the name to a hash or a set of candidates of hashes {if more than one candidate, just raise an exception}
     * 2) Second, we get the actual object using the sha hash we obtained in the previous step
     * ->if the format matches, we return the sha
     * ->check the follows parameter {The parameter follow=True controls whether the function should follow the chain of references to reach the desired object type (fmt)}
     * After this we check the following:
     * If we have a tag and fmt is anything else, we follow the tag.
     * If we have a commit and fmt is tree, we return this commit’s tree object
     * In all other situations, we bail out: nothing else makes sense.
     */
    public static String objectFind(GitRepository repo, String name) throws Exception{
        return objectFind(repo, name, null, true);
    }

    public static String objectFind(GitRepository repo, String name, String fmt, boolean follow) throws Exception{
        List<String> candidates = objectResolve(repo, name);

        if(candidates.isEmpty()){
            throw new Exception("No such references: " + name);
        }

        if(candidates.size() > 1){
            throw new Exception("Ambiguous reference " + name + ": Candidates are:\n - " + String.join("\n - ", candidates));
        }

        String sha = candidates.get(0);

        if(fmt == null){
            return sha;
        }

        while(true){
            GitObject obj = ObjectIO.objectRead(repo, sha);

            if(obj.getFormat().equals(fmt)){
                return sha;
            }

            if(!follow){
                return null;
            }

            //Follow tags
            if(obj instanceof GitTag tag){
                byte[] target = (byte[]) tag.getKvlm().get("object");
                sha = new String(target, "UTF-8");
            }else if (obj instanceof GitCommit commit && fmt.equals("tree")) {
                byte[] tree = (byte[]) commit.getKvlm().get("tree");
                sha = new String(tree, "UTF-8");
            } else {
                return null;
            }
        }
    }

    /**
     * Resolve name to object hash
     * Name Resolution works like this:
     *      * 1) If name is HEAD, it will just resolve .git/HEAD; {i.e the head of the current branch}
     *      * 2) If name is a full hash, this hash is returned unmodified.
     *      * 3) If name looks like a short hash, it will collect objects whose full hash begin with this short hash.
     *      * 4) At last, it will resolve tags and branches matching name.
     *      * For 3) and 4), i.e short hashes or branch names can be ambiguous, we want to enumerate all possible meanings of the name and raise an error if we’ve found more than 1
     */
    public static List<String> objectResolve(GitRepository repo, String name) throws IOException {
        List<String> candidates = new ArrayList<>();

        if (name.trim().isEmpty()) {
            return candidates;
        }

        //HEAD is ambiguous
        if (name.equals("HEAD")) {
            String resolved = refResolve(repo, "HEAD");
            if (resolved != null) {
                candidates.add(resolved);
            }
            return candidates;
        }

        //Try as hash
        Pattern hashRE = Pattern.compile("^[0-9A-Fa-f]{4,40}$");
        if (hashRE.matcher(name).matches()) {
            //This may be a hash, either small or full.  4 seems to be the
            //minimal length for git to consider something a short hash.
            name = name.toLowerCase();
            String prefix = name.substring(0, 2);
            Path path = repo.repoDir("objects", prefix);

            if (path != null && Files.exists(path)) {
                String rem = name.substring(2);
                try (Stream<Path> files = Files.list(path)) {
                    files.filter(f -> f.getFileName().toString().startsWith(rem))
                            .forEach(f -> candidates.add(prefix + f.getFileName().toString()));
                }
            }
        }

        //Try as tag
        String asTag = refResolve(repo, "ref/tags/" + name);
        if (asTag != null) candidates.add(asTag);

        //Try as branch
        String asBranch = refResolve(repo, "refs/heads" + name);
        if (asBranch != null) candidates.add(asBranch);

        return candidates;
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
}
