package com.gopal.twit.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GitRepository {
    private final Path worktree;
    private final Path gitDir;
    private final Properties config;

    /**
     * Create or open a repository
     * @param path Path to the repository root
     * @param force If true, skip the validation checks
     */
    public GitRepository(Path path, boolean force) throws IOException {
        this.worktree = path;
        this.gitDir = path.resolve(".git");

        if(!force){
            //throw an exception if it's not a git repo
            if(!Files.isDirectory(gitDir)){
                throw new IOException("Not a Git repository: " + path);
            }

            //Reading config file in .git/config
            Path configFile = repoFile("config");

            //if config file doesn't exist, throw an exception
            if(!Files.exists(configFile)){
                throw new IOException("Configuration file missing");
            }

            //read from the config file if it exists
            config = new Properties();
            try(InputStream in = Files.newInputStream(configFile)){
                config.load(in);
            }

            //Check repository format version
            String version = config.getProperty("core.repositoryformatversion");
            if(version == null || !version.equals("0")){
                throw new IOException("Unsupported repositoryformatversion: " + version);
            }
        }
        else{
            config = new Properties();
        }
    }

    /**
     * Computes the path within .git directory
     */
    public Path repoPath(String ...parts){
        Path result = gitDir;
        for(String part : parts){
            result = result.resolve(part);
        }
        return result;
    }

    /**
     * returns and optionally creates a path to a file
     */
    public Path repoFile(String ...parts) throws IOException {
        return repoFile(false, parts);
    }

    public Path repoFile(boolean mkdir, String ...parts) throws IOException{
        if(mkdir && parts.length > 1){
            // Create all the directories except for the last component {which is a file}
            String[] dirParts = new String[parts.length-1];
            System.arraycopy(parts,0,dirParts,0,parts.length-1);
            repoDir(true,dirParts);
        }
        return repoPath(parts);
    }

    /**
     * returns and optionally creates a path to a directory
     */
    public Path repoDir(String ...parts) throws IOException{
        return repoDir(false, parts);
    }

    public Path repoDir(boolean mkdir, String ...parts) throws IOException {
        Path path = repoPath(parts);

        if(Files.exists(path)){
            if(!Files.isDirectory(path)){
                throw new IOException("Not a directory: " + path);
            }
            return path;
        }

        if(mkdir){
            Files.createDirectories(path);
            return path;
        }

        return null;
    }


    /**
     * Create a new repository at the give path
     */
    public static GitRepository create(Path path) throws IOException{
        GitRepository repo = new GitRepository(path, true);

        //create worktree if needed
        if(Files.exists(repo.worktree)){
            if(!Files.isDirectory(repo.worktree)){
                throw new IOException(path + " is not a directory!");
            }
            if(Files.exists(repo.gitDir) && Files.list(repo.gitDir).findAny().isPresent()){//gitDir directory should be empty
                throw new IOException(path + " is not empty!");
            }
        }else{
            Files.createDirectories(repo.worktree);//create the worktree if it doesn't exist at path
        }

        //Create directory structure
        repo.repoDir(true, "branches");
        repo.repoDir(true, "objects");
        repo.repoDir(true, "refs", "tags");
        repo.repoDir(true, "refs", "heads");

        //Create description file {description file is created if doesn't exist}
        Files.writeString(repo.repoFile("description"), "Unnamed repository; edit this file 'description' to name the repository.\n");

        //Create HEAD file
        Files.writeString(repo.repoFile("HEAD"), "ref: refs/heads/master\n");

        //Create config file
        Properties config = defaultConfig();
        try (OutputStream out = Files.newOutputStream(repo.repoFile("config"))){
            config.store(out, "Git configuration");
        }
        return repo;
    }

    /**
     * Default repository configuration
     */
    private static Properties defaultConfig(){
        Properties config = new Properties();
        config.setProperty("core.repositoryformatversion", "0");
        config.setProperty("core.filemode", "false");
        config.setProperty("core.bare", "false");
        return config;
    }

    /**
     * Find repository root starting from current directory
     * function tries to find the Git repository starting from the current working directory
     */
    public static GitRepository find() throws IOException{
        return find(Paths.get(".").toAbsolutePath().normalize(),true);
    }

    //required -> it is required to find a Git repository. If none is found, it will throw an IOException.
    public static GitRepository find(Path path, boolean required) throws IOException{
        path = path.toAbsolutePath().normalize();

        //if .git directory exists then we’ve found the root of the repository
        if(Files.isDirectory(path.resolve(".git"))){
            return new GitRepository(path, false);
        }

        //if the currect directory doesn't contain a .git folder, it looks at the parent directory
        Path parent = path.getParent();
        if(parent == null || parent.equals(path)){
            if(required){//never found a git repository, i.e reached the filesystem root / or drive root
                throw new IOException("No git directory.");
            }
            return null;
        }

        return find(parent, required);
    }

    public Path getWorktree(){
        return this.worktree;
    }

    public Path getGitDir(){
        return this.gitDir;
    }

    public Properties getConfig(){
        return config;
    }
}
