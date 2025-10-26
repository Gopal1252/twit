package com.gopal.twit.core.objects;

import com.gopal.twit.util.TreeParser;

import java.util.ArrayList;
import java.util.List;

public class GitTree extends GitObject{
    private List<GitTreeLeaf> items;

    public GitTree(){
        init();
    }

    public GitTree(byte[] data){
        deserialize(data);
    }
    @Override
    public String getFormat() {
        return "tree";
    }

    @Override
    public byte[] serialize() {
        return TreeParser.serialize(items);
    }

    @Override
    public void deserialize(byte[] data) {
        this.items = TreeParser.parse(data);
    }

    @Override
    public void init(){
        this.items = new ArrayList<>();
    }

    public List<GitTreeLeaf> getItems(){
        return items;
    }
}
