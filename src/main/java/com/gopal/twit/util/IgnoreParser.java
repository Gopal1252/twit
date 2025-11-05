package com.gopal.twit.util;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.ignore.GitIgnore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.gopal.twit.core.ignore.GitIgnore.IgnoreRule;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;
import com.gopal.twit.core.objects.GitBlob;
import com.gopal.twit.core.objects.GitObject;

public class IgnoreParser {

    /**
     * Read all the gitignore rules in a repository
     */
    public static GitIgnore gitIgnoreRead(GitRepository repo) throws Exception{
        List<List<IgnoreRule>> absolute = new ArrayList<>();
        Map<String, List<IgnoreRule>> scoped = new HashMap<>();

        // Read local configuration in .git/info/exclude
        Path repoFile = repo.repoFile("info", "exclude");
        if(Files.exists(repoFile)){
            List<String> lines = Files.readAllLines(repoFile);
            absolute.add(gitignoreParse(lines));
        }

        //Global configuration {global ignore file (in ~/.config/git/ignore)}
        String configHome = System.getenv().getOrDefault("XDG_CONFIG_HOME", System.getProperty("user.home") + "/.config");
        Path globalFile = Paths.get(configHome, "git/ignore");

        if(Files.exists(globalFile)){
            List<String> lines = Files.readAllLines(globalFile);
            absolute.add(gitignoreParse(lines));
        }

        // .gitignore files in the index (scoped)
        GitIndex index = IndexIO.indexRead(repo);
        for(GitIndexEntry entry : index.getEntries()){
            if(entry.getName().equals(".gitignore") || entry.getName().endsWith("/.gitignore")){
                String dirName = "";
                int lastSlash = entry.getName().lastIndexOf('/');
                if(lastSlash > 0){
                    dirName = entry.getName().substring(0,lastSlash);
                }

                GitObject obj = ObjectIO.objectRead(repo, entry.getSha());
                if(obj instanceof GitBlob blob){
                    String content = new String(blob.getBlobData(), "UTF-8");
                    List<String> lines = Arrays.asList(content.split("\n"));
                    scoped.put(dirName, gitignoreParse(lines));
                }
            }
        }
        return new GitIgnore(absolute, scoped);
    }

    /**
     * Parses list of gitignore lines
     */
    private static List<IgnoreRule>  gitignoreParse(List<String> lines){
        List<IgnoreRule> ret = new ArrayList<>();

        for(String line : lines){
            IgnoreRule parsed = gitignoreParse1(line);
            if(parsed != null){
                ret.add(parsed);
            }
        }
        return ret;
    }

    /**
     * Parses a single gitignore pattern
     */
    private static IgnoreRule gitignoreParse1(String raw){
        raw = raw.trim();

        if(raw.isEmpty() || raw.startsWith("#")){
            return null;
        }

        if(raw.startsWith("!")){
            return new IgnoreRule(raw.substring(1), true);
        }

        if (raw.startsWith("\\")) {
            return new IgnoreRule(raw.substring(1), false);
        }

        return new IgnoreRule(raw, false);
    }

    /**
     * Check if a path should be ignored
     */
    public static boolean checkIgnore(GitIgnore rules, String path){
        //First check scoped rules (most specific first)
        Boolean result = checkIgnoreScoped(rules.getScoped(), path);
        if(result != null){
            return result;
        }

        //Then check absolute rules
        return checkIgnoreAbsolute(rules.getAbsolute(), path);
    }

    /**
     * Check against scoped rules (directory-specific .gitignore files)
     */
    private static Boolean checkIgnoreScoped(Map<String, List<IgnoreRule>> rules, String path){
        String parent = "";
        int lastSlash = path.lastIndexOf('/');
        if(lastSlash > 0){
            parent = path.substring(0, lastSlash);
        }

        while(true){
            if(rules.containsKey(parent)){
                Boolean result = checkIgnore1(rules.get(parent), path);
                if(result != null){
                    return result;
                }
            }

            if(parent.isEmpty()){
                break;
            }

            lastSlash = parent.lastIndexOf('/');
            parent = lastSlash >= 0 ? parent.substring(0, lastSlash) : "";
        }
        return null;
    }

    /**
     * Check against absolute rules (global and repo-level)
     */
    private static boolean checkIgnoreAbsolute(List<List<IgnoreRule>> rules, String path){
        for (List<IgnoreRule> ruleset : rules) {
            Boolean result = checkIgnore1(ruleset, path);
            if (result != null) {
                return result;
            }
        }
        return false; // Default: not ignored
    }

    /**
     * Check a path against a single ruleset
     * Returns null if no rule matches
     */
    private static Boolean checkIgnore1(List<IgnoreRule> rules, String path){
        Boolean result = null;

        for(IgnoreRule rule : rules){
            if(fnmatch(path, rule.getPattern())){
                result = !rule.isInclude();//if include = true, don't ignore
            }
        }
        return result;
    }

    /**
     * Simple glob pattern matching (similar to fnmatch)
     * Supports: * (any string), ? (any char), [abc] (char class)
     * make a regex from the pattern string and then check if the path matches with this regex
     */
    private static boolean fnmatch(String path, String pattern){
        //Convert glob pattern to regex
        StringBuilder regex = new StringBuilder("^");

        for(int i=0;i<pattern.length();i++){
            char c = pattern.charAt(i);

            switch (c){
                case '*' -> {
                    if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                        // ** matches any number of directories
                        regex.append(".*");
                        i++; // Skip next *
                    } else {
                        // * matches anything except /
                        regex.append("[^/]*");
                    }
                }
                case '?' -> regex.append("[^/]");
                case '.' -> regex.append("\\.");
                case '\\' -> {
                    // Escape next character
                    if (i + 1 < pattern.length()) {
                        i++;
                        regex.append(java.util.regex.Pattern.quote(String.valueOf(pattern.charAt(i))));
                    }
                }
                case '[' -> {
                    // Character class
                    int end = pattern.indexOf(']', i);
                    if (end > i) {
                        regex.append(pattern, i, end + 1);
                        i = end;
                    } else {
                        regex.append("\\[");
                    }
                }
                default -> {
                    // Escape regex special characters
                    if ("^$+{}|()".indexOf(c) >= 0) {
                        regex.append('\\');
                    }
                    regex.append(c);
                }
            }
        }
        regex.append("$");

        try {
            return java.util.regex.Pattern.matches(regex.toString(), path);
        } catch (Exception e) {
            return false;
        }
    }
}
