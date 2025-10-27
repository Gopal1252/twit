package com.gopal.twit.core.objects;

/**
 * A tag is just a user-defined name for an object, often a commit
 * Tags are actually refs
 * They live in the .git/refs/tags/ hierarchy
 * Tow types of tags: lightweight tags and tag objects
 * "lightweight" tags -> just regular refs to a commit, a tree or a blob
 * Tag Object are regular refs pointing to an object of type tag.
 * Unlike lightweight tags, tag objects have an author, a date, an optional PGP signature and an optional annotation.
 * heir format is the same as a commit object.
 */
public class GitTag extends GitCommit{
    @Override
    public String getFormat() {
        return "tag";
    }
}
