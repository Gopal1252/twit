package com.gopal.twit;

import org.apache.commons.cli.*;

public class LibTwit {
    public static void main(String[] args) {
        Options options = new Options();
        CommandLineParser parser = new DefaultParser();//for later usage for parsing

        if(args.length == 0){
            System.out.println("No command used");
            System.exit(1);
        }

        String cmd = args[0];

        switch(cmd){
            case "init":
                System.out.println("init");
                break;
            case "add":
                System.out.println("add");
                break;
            // TODO: Add all the commands
            default:
                System.out.println("Unknown command: " + cmd);
        }
    }
}
