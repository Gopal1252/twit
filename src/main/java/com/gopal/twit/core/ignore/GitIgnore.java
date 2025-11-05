package com.gopal.twit.core.ignore;

import java.util.List;
import java.util.Map;

/**
 * Represents gitignore rules {absolute and scoped}
 * SCOPED IGNORE FILES/RULES:
 * Some of the ignore files live in the index: they’re the various gitignore files.
 * Emphasis on the plural; although there often is only one such file, at the root, there can be one in each directory, and it applies to this directory and its subdirectories.
 * They're being those scoped, here because they only apply to paths under their directory.
 *
 * ABSOLUTE IGNORE FILES/RULES:
 * These live outside the index.
 * They’re the global ignore file (usually in ~/.config/git/ignore) and the repository-specific .git/info/exclude.
 * They are being called absolute here, because they apply everywhere, but at a lower priority.
 */
public class GitIgnore {
    private final List<List<IgnoreRule>> absolute;
    private final Map<String, List<IgnoreRule>> scoped;

    public GitIgnore(List<List<IgnoreRule>> absolute, Map<String, List<IgnoreRule>> scoped) {
        this.absolute = absolute;
        this.scoped = scoped;
    }

    public List<List<IgnoreRule>> getAbsolute() {
        return absolute;
    }

    public Map<String, List<IgnoreRule>> getScoped() {
        return scoped;
    }

    /**
     * Represents a single ignore rule
     */
    public static class IgnoreRule{
        private final String pattern;
        private final boolean include; //false = exclude, true = include

        public IgnoreRule(String pattern, boolean include){
            this.pattern = pattern;
            this.include = include;
        }

        public String getPattern() {
            return pattern;
        }

        public boolean isInclude() {
            return include;
        }
    }
}
