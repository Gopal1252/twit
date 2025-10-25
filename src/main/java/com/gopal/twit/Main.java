package com.gopal.twit;

import com.gopal.twit.commands.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//Main Entry point for twit
public class Main {
    public static final Map<String, Command> commands = new HashMap<>();

    static {
        commands.put("init", new InitCommand());
        commands.put("cat-file", new CatFileCommand());
        commands.put("hash-object", new HashObjectCommand());
        //TODO: Add rest of the commands
    }

    public static void main(String[] args) {
        if(args.length == 0){
            System.err.println("Usage: twit <commands> [<args>]");
            System.err.println("\nAvailable commands:");
            commands.keySet().stream().sorted().forEach(cmd ->
                    System.err.println(" " + cmd));
            System.exit(1);
        }

        String commandName = args[0];
        String[] commandArgs = Arrays.copyOfRange(args,1,args.length);

        Command command = commands.get(commandName);
        if(command == null){
            System.err.println("Unknown command: " + commandName);
            System.exit(1);
        }

        try{
            command.execute(commandArgs);
        } catch (Exception e){
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
