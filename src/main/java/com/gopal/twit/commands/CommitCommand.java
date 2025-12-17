package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;
import com.gopal.twit.core.objects.GitCommit;
import com.gopal.twit.core.objects.GitTree;
import com.gopal.twit.core.objects.GitTreeLeaf;
import com.gopal.twit.util.IndexIO;
import com.gopal.twit.util.ObjectIO;
import com.gopal.twit.util.RefResolver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

/**
 * Using add and rm commands, we modified the index (i.e. staged the changes)
 * Next, to actually commit, following steps are followed:
 * 1) Read git's config file using configparser to get the author and commiter details
 * we first need to convert the index into a tree object, generate and store the corresponding commit object, and update the HEAD branch to the new commit (remember: a branch is just a ref to a commit)
 */
public class CommitCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        String message = null;

        for(int i=0;i< args.length;i++){
            if(args[i].equals("-m")){
                message = args[++i];
            }
        }

        if(message == null){
            System.err.println("Usage: twit commit -m <message>");
            return;
        }

        GitRepository repo = GitRepository.find();
        commit(repo, message);
    }

    private void commit(GitRepository repo, String message) throws Exception{
        GitIndex index = IndexIO.indexRead(repo);

        //Build tree from index
        String treeSha = treeFromIndex(repo, index);

        //Create commit object
        GitCommit commit = new GitCommit();
        commit.init();
        Map<String, Object> kvlm = commit.getKvlm();//kvlm is ref to the commit object's kvlm hashmap

        //Set tree
        kvlm.put("tree", treeSha.getBytes(StandardCharsets.UTF_8));

        //Add parent if not initial commit
        try{
            String head = RefResolver.refResolve(repo, "HEAD");
            if(head != null){
                kvlm.put("parent", head.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e){
            //Initial commit, no parent
        }

        //Get user identity from git config
        String author = gitconfigUserGet();
        long timestamp = Instant.now().getEpochSecond();
        String timezone = "+0000"; // Simplified
        String authorLine = author + " " + timestamp + " " + timezone;

        kvlm.put("author", authorLine.getBytes(StandardCharsets.UTF_8));
        kvlm.put("committer", authorLine.getBytes(StandardCharsets.UTF_8));
        kvlm.put(null, (message + "\n").getBytes(StandardCharsets.UTF_8));

        //Write commit object
        String commitSha = ObjectIO.objectWrite(commit, repo);

        //Update HEAD (or the branch HEAD points to)
        Path headFile = repo.repoFile("HEAD");
        String headContent = Files.readString(headFile).trim();

        if (headContent.startsWith("ref: ")) {
            // HEAD points to a branch, update the branch
            String ref = headContent.substring(5);
            Path refFile = repo.repoFile(true, ref);
            Files.writeString(refFile, commitSha + "\n");
        } else {
            // Detached HEAD, update HEAD directly
            Files.writeString(headFile, commitSha + "\n");
        }

        System.out.println("[" + commitSha.substring(0, 7) + "] " + message);
    }

    /**
     * This function build a tree from the index and returns its sha
     * Since index is flattened while tree is not, so we need to "unflatten" the index. We do this as follows:
     * 1) Build a dictionary (hashmap) of directories. Keys are full paths from worktree root and values are list of GitIndexEntry — files in the directory. At this point, our dictionary only contains files: directories are only its keys.
     * 2) Traverse this list, going bottom-up, that is, from the deepest directories up to root (depth doesn’t really matter: we just want to see each directory before its parent. To do that, we just sort them by full path length, from longest to shortest — parents are obviously always shorter)
     * 3) At each directory, we build a tree with its contents and then we write the new tree to the repository {i.e. actually make the changes in our repo}
     * 4) We then add this tree to this directory’s parent
     * 5) And we iterate over the next directory in the parent
     * 6) Since trees are recursive, so the last tree we'll build will be the one for the root {it's key's length will 0}. and this key {the root tree's sha} is the one that will be returned
     */
    private String treeFromIndex(GitRepository repo, GitIndex index) throws Exception{
        //Dictionary: path -> List of entries in that directory
        Map<String, List<Object>> contents = new HashMap<>();
        contents.put("", new ArrayList<>());

        //Enumerate entries, and turn them into a dictionary where keys
        //are directories, and values are lists of directory contents
        //Here we're grouping all the entries by their parent directory
        for(GitIndexEntry entry : index.getEntries()){
            String dirname = "";//name of the directory containing the entry.getName() file
            int lastSlash = entry.getName().lastIndexOf('/');
            if(lastSlash >= 0){
                dirname = entry.getName().substring(0, lastSlash);
            }

            String key = dirname;
            while(!key.isEmpty()){//this loop adds every directory on the path from the file’s directory up to the root in the contents as a key
                if(!contents.containsKey(key)){
                    contents.put(key, new ArrayList<>());
                }

                if(key.isEmpty()){
                    break;
                }

                //move to parent directory
                int idx = key.lastIndexOf('/');
                key = idx >= 0 ? key.substring(0,idx) : "";
            }
            //Add the file index entry to the list of entries for its immediate directory
            //At this point contents contains only file index entries in the lists (no subtree entries yet)
            contents.get(dirname).add(entry);
        }

        //Sort by path length (deepest first) {i.e. in descending order of length}
        //This means that we'll always encounter a given path before its
        //parent, which is all we need, since for each directory D we'll
        //need to modify its parent P to add D's tree
        List<String> sorted = new ArrayList<>(contents.keySet());
        sorted.sort((a, b) -> Integer.compare(b.length(), a.length()));

        //Map to store tree SHAs: path -> tree SHA
        Map<String, String> sha = new HashMap<>();

        //Create tree objects bottom up
        for(String path : sorted){
            GitTree tree = new GitTree();
            tree.init();

            for(Object obj : contents.get(path)){
                if(obj instanceof GitIndexEntry entry){
                    //This is a file entry from the index
                    String leaf = entry.getName();

                    //String the parent path to get just the name of the file
                    if(!path.isEmpty()){
                        leaf = leaf.substring(path.length()+1);
                    }

                    //Skip if this contains a slash (it's in a subdirectory)
                    if(leaf.contains("/")){
                        continue;
                    }

                    //Build mode string  (6 digits octal)
                    String mode = String.format("%06o", (entry.getModeType() << 12) | entry.getModePerms());

                    // Get SHA - either from the entry itself, or from a subtree we created
                    String leafSha = sha.getOrDefault(entry.getName(), entry.getSha());

                    tree.getItems().add(new GitTreeLeaf(mode, leaf, leafSha));

                } else if(obj instanceof String dirName){
                    //This is a directory entry we need to add
                    String treeSha = sha.get(dirName);
                    if(treeSha != null){
                        //Get just the directory name (not full path)
                        String name = dirName;
                        int lastSlash = name.lastIndexOf('/');
                        if (lastSlash >= 0) {
                            name = name.substring(lastSlash + 1);
                        }

                        tree.getItems().add(new GitTreeLeaf("040000", name, treeSha));
                    }
                }

                //Write this tree and save its SHA
                String treeSha = ObjectIO.objectWrite(tree, repo);
                sha.put(path, treeSha);

                // Add this directory to its parent's contents
                if (!path.isEmpty()) {
                    int lastSlash = path.lastIndexOf('/');
                    String parent = lastSlash >= 0 ? path.substring(0, lastSlash) : "";
                    contents.get(parent).add(path);
                }
            }
        }
        //Return the root tree SHA
        return sha.get("");
    }

    /**
     * This function returns the name and email of the user {used as the author and committer}
     */
    private String gitconfigUserGet() throws Exception{
        String configHome = System.getenv().getOrDefault("XDG_CONFIG_HOME", System.getProperty("user.home") + "/.config");

        Path globalConfig = Paths.get(configHome, "git/config");
        Path userConfig = Paths.get(System.getProperty("user.home"), ".gitconfig");

        Properties config = new Properties();

        if(Files.exists(globalConfig)){
            try(var in = Files.newInputStream(globalConfig)){
                config.load(in);
            }
        }
        if(Files.exists(userConfig)){
            try (var in = Files.newInputStream(userConfig)) {
                config.load(in);
            }
        }

        String name = config.getProperty("user.name", "Unknown");
        String email = config.getProperty("user.email", "unknown@example.com");

        return name + " <" + email + ">";
    }
}
