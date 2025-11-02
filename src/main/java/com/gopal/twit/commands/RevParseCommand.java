package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.ref.RefResolver;

public class RevParseCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        String type = null;
        String name = null;

        for(int i=0;i<args.length;i++){
            if(args[i].equals("--twit-type")){
                type = args[++i];
            }
            else{
                name = args[i];
            }
        }

        if(name == null){
            System.err.println("Usage: twit rev-parse [--twit-type TYPE] <name>");
            return;
        }

        GitRepository repo = GitRepository.find();
        String sha = RefResolver.objectFind(repo, name, type, true);
        System.out.println(sha);
    }
}
