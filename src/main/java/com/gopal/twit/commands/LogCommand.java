package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.objects.GitCommit;
import com.gopal.twit.core.objects.GitObject;
import com.gopal.twit.core.ref.RefResolver;
import com.gopal.twit.util.ObjectIO;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Logs the commit history in reverse chronological order
 */
public class LogCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        String commit = args.length > 0 ? args[0] : "HEAD";

        GitRepository repo = GitRepository.find();

        System.out.println("digraph twitlog{");
        System.out.println("  node[shape=rect]");

        logGraphviz(repo, RefResolver.objectFind(repo, commit), new HashSet<>());

        System.out.println("}");
    }

    private void logGraphviz(GitRepository repo, String sha, Set<String> seen) throws Exception{
        if(seen.contains(sha)) return;
        seen.add(sha);

        GitObject obj = ObjectIO.objectRead(repo, sha);
        if(!(obj instanceof GitCommit commit)) return;

        byte[] msgBytes = (byte[]) commit.getKvlm().get(null);
        String message = new String(msgBytes, StandardCharsets.UTF_8).strip();
        message = message.replace("\\", "\\\\").replace("\"", "\\\"");

        if (message.contains("\n")) {
            message = message.substring(0, message.indexOf("\n"));
        }

        System.out.println("  c_" + sha + " [label=\"" + sha.substring(0, 7) + ": " + message + "\"]");

        Object parentObj = commit.getKvlm().get("parent");
        if (parentObj == null) return;

        List<byte[]> parents;
        if (parentObj instanceof List) {
            parents = (List<byte[]>) parentObj;
        } else {
            parents = Collections.singletonList((byte[]) parentObj);
        }

        for (byte[] p : parents) {
            String parent = new String(p, StandardCharsets.UTF_8);
            System.out.println("  c_" + sha + " -> c_" + parent + ";");
            logGraphviz(repo, parent, seen);
        }
    }
}
