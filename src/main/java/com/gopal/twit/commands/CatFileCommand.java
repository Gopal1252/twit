package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.objects.GitObject;
import com.gopal.twit.util.ObjectIO;

/**
 * It simply prints the raw contents of an object to stdout, uncompressed and without the git header
 */
public class CatFileCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        if(args.length < 2){
            System.err.println("Usage: twit cat-file <type> <object>");
            return;
        }

        String type = args[0];
        String object = args[1];//basically the hash of object

        GitRepository repo = GitRepository.find();
        GitObject obj = ObjectIO.objectRead(repo, object);

        if(obj == null){
            System.err.println("Object not found: " + object);
            return;
        }

        if(!obj.getFormat().equals(type)){
            System.err.println("Expected " + type + " but got " + obj.getFormat());
            return;
        }

        System.out.write(obj.serialize());
    }
}
