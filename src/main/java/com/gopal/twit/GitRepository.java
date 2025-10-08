package com.gopal.twit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class GitRepository {
    private Path workTree;
    private Path gitDir;
    private Properties config;

    public GitRepository(Path path, boolean force) throws IOException {

        this.workTree = path;
        this.gitDir = path.resolve(".twit");

        if(!force && !Files.isDirectory(this.gitDir)){
            throw new IllegalStateException("Not a twit repository: " + path);
        }

        // Reading config file in .twit/config
        config = new Properties();
        Path configFile = RepoUtils.repoFile(this,false,"config");
        if(Files.exists(configFile)){
            try(InputStream in = Files.newInputStream(configFile)){
                config.load(in);
            }
        }
        else if(!force){
            throw new IllegalStateException("Configuration file is missing!");
        }

        if(!force){
            int vers = Integer.parseInt(config.getProperty("core.repositoryformatversion"));
            if(vers != 0){
                throw new IllegalStateException("Unsupported repositoryformatversion: " + vers);
            }
        }
    }

    Path getGitDir(){
        return gitDir;
    }

    Path getWorkTree(){
        return workTree;
    }
}
