package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.ignore.GitIgnore;
import com.gopal.twit.util.IgnoreParser;

/**
 * This command takes a list of paths and outputs back those of those paths that should be ignored {on the basis of various rules stored in the various .gitignore files}
 */
public class CheckIgnoreCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        if(args.length == 0){
            System.out.println("Usage: twit check-ignore <path>...");
            return;
        }

        GitRepository repo = GitRepository.find();
        GitIgnore rules = IgnoreParser.gitIgnoreRead(repo);

        for(String path : args){
            if(IgnoreParser.checkIgnore(rules, path)){
                System.out.println(path);
            }
        }
    }
}
