package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;

import java.nio.file.Paths;

public class InitCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        String path = args.length > 0 ? args[0] : ".";
        GitRepository.create(Paths.get(path));
        System.out.println("Initialized an empty Git repository in " + path);
    }
}
