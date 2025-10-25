package com.gopal.twit.core.objects;

/**
 * Base class for all the Git objects (blob, commit, tree, tag)
 */
public abstract class GitObject {

    //Get the format/type of this object (eg: "blob", "commit")
    public abstract String getFormat();

    //Serialize this object to bytes
    public abstract byte[] serialize();
    //Deserialize bytes into this object
    public abstract void deserialize(byte[] data);

    //Initialize an empty object (for creating new objects)
    public void init(){

    }
}
