package com.gopal.twit.core.objects;

import com.gopal.twit.util.KVLMParser;

import java.util.LinkedHashMap;
import java.util.Map;

public class GitCommit extends GitObject {
    private Map<String, Object> kvlm;

    public GitCommit(){
        init();
    }

    public GitCommit(byte[] data){
        deserialize(data);
    }

    @Override
    public String getFormat() {
        return "commit";
    }

    @Override
    public byte[] serialize() {
        return KVLMParser.serialize(kvlm);
    }

    @Override
    public void deserialize(byte[] data) {
        this.kvlm = KVLMParser.parse(data);
    }

    @Override
    public void init(){
        this.kvlm = new LinkedHashMap<>();
    }

    public Map<String, Object> getKvlm(){
        return kvlm;
    }

    public void setKvlm(Map<String, Object> kvlm){
        this.kvlm = kvlm;
    }
}
