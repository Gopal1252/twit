package com.gopal.twit.core.index;

import java.util.ArrayList;
import java.util.List;

public class GitIndex {
    private int version;
    private List<GitIndexEntry> entries;

    public GitIndex(){
        this.version = 2;
        this.entries = new ArrayList<>();
    }

    public GitIndex(int version, List<GitIndexEntry> entries) {
        this.version = version;
        this.entries = entries;
    }

    public int getVersion() {
        return version;
    }

    public List<GitIndexEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<GitIndexEntry> entries) {
        this.entries = entries;
    }
}
