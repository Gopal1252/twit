package com.gopal.twit.core.objects;

public class GitTag extends GitObject{
    @Override
    public String getFormat() {
        return "tag";
    }

    @Override
    public byte[] serialize() {
        return new byte[0];
    }

    @Override
    public void deserialize(byte[] data) {

    }
}
