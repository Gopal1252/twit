package com.gopal.twit.core.objects;

public class GitBlob extends GitObject{
    private byte[] blobData;

    public GitBlob(){
        this.blobData = new byte[0];
    }

    public GitBlob(byte[] data){
        deserialize(data);
    }

    @Override
    public String getFormat() {
        return "blob";
    }

    @Override
    public byte[] serialize() {
        return blobData;
    }

    @Override
    public void deserialize(byte[] data) {
        this.blobData = data;
    }

    public byte[] getBlobData(){
        return blobData;
    }

    public void setBlobData(byte[] data){
        this.blobData = data;
    }
}
