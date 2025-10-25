package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.util.ObjectIO;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * just hashes an object
 * it reads a file, computes its hash as an object, either storing it in the repo (-w flag used) or just printing its hash
 */
public class HashObjectCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        boolean write = false;
        String type = "blob";
        String path = null;

        for(int i=0;i<args.length;i++){
            switch(args[i]){
                case "-w" -> write = true;
                case "-t" -> type = args[++i];
                default -> path = args[i];
            }
        }

        //path is the only mandatory argument
        //-w and -t type arguments are optional
        if(path == null){
            System.err.println("Usage: twit hash-object [-w] [-t TYPE] FILE");
            return;
        }

        GitRepository repo = write? GitRepository.find() : null;

        try(var in = Files.newInputStream(Paths.get(path))){
            String sha = ObjectIO.objectHash(in,type,repo);
            System.out.println(sha);
        }
    }
}
