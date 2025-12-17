package com.gopal.twit.util;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.objects.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class ObjectIO {
    /**
     * Reads the object sha from the git repository and
     * returns a GitObject whose exact type depends on the object
     */
    public static GitObject objectRead(GitRepository repo, String sha) throws Exception{
        Path path = repo.repoFile("objects", sha.substring(0,2), sha.substring(2));

        if(!Files.exists(path)){
            return null;
        }

        //Decompress the binary file {Git stores data compressed in a binary format}
        byte[] raw;
        try(InputStream in = new InflaterInputStream(Files.newInputStream(path))){
            raw = in.readAllBytes();
        }

        /*
        * HEADER FORMAT: <header>␣<size>\0<content>
        * GIT OBJECT STORAGE FORMAT
        * OBJECT TYPE (blob, commit, tag, tree)
        * ASCII SPACE (0x20)
        * SIZE OF OBJECT in bytes as an ASCII number
        * null (0x00)
        * CONTENTS OF THE OBJECT
         * */

        //Parse the header { and is followed by an ASCII space}
        int spaceIdx = findByte(raw, (byte) ' ', 0);
        String fmt = new String(raw, 0, spaceIdx, "ASCII");

        int nullIdx = findByte(raw, (byte) 0,spaceIdx);
        int size = Integer.parseInt(new String(raw,spaceIdx+1,nullIdx-spaceIdx-1,"ASCII"));

        if(size != raw.length-nullIdx-1){
            throw new Exception("Malformed object " + sha + ": bad length");
        }

        //extract data
        byte[] data = new byte[size];
        System.arraycopy(raw, nullIdx+1, data, 0 , size);

        //Create appropriate object
        GitObject obj = switch (fmt) {
            case "blob" -> new GitBlob();
            case "commit" -> new GitCommit();
            case "tree" -> new GitTree();
            case "tag" -> new GitTag();
            default -> throw new Exception("Unknown type " + fmt + "for object " + sha);
        };

        obj.deserialize(data);
        return obj;
    }

    private static int findByte(byte[] array, byte target, int start){
        for(int i=start; i<array.length; i++){
            if(array[i] == target) return i;
        }
        return -1;
    }

    /**
     * Write an object to the git repository
     * It's opposite of reading the object from the repository
     * we compute the hash, insert the header, zlib-compress everything and write the result in the correct location
     * and the function returns the sha
     */
    public static String objectWrite(GitObject obj, GitRepository repo) throws Exception{
        //Serialize object data
        byte[] data = obj.serialize();

        //Add header
        String header = obj.getFormat() + " " + data.length + "\0";
        byte[] headerBytes = header.getBytes("ASCII");

        byte[] full = new byte[headerBytes.length + data.length];
        System.arraycopy(headerBytes,0,full,0,headerBytes.length);
        System.arraycopy(data,0,full,headerBytes.length,data.length);

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = md.digest(full);//hashBytes -> a binary array {the SHA-1 digest (20 bytes long)}
        String sha = bytesToHex(hashBytes);

        if(repo != null){
            //write to repository
            Path path = repo.repoFile(true,"objects", sha.substring(0,2),sha.substring(2));

            if(!Files.exists(path)){
                try(OutputStream out = new DeflaterOutputStream(Files.newOutputStream(path))){//compress using zlib (DeflaterOutputStream)
                    out.write(full);
                }
            }
        }

        return sha;
    }

    //convert the sha bytes array to hexadecimal
    private static String bytesToHex(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Hash a file into a Git Object
     * Used by hash-object command to hash a file
     */
    public static String objectHash(InputStream in , String fmt, GitRepository repo) throws Exception{
        byte[] data = in.readAllBytes();

        //Create appropriate object
        GitObject obj = switch (fmt) {
            case "commit" -> new GitCommit(data);
            case "tree" -> new GitTree(data);
            case "tag" -> new GitTag();
            case "blob" -> new GitBlob(data);
            default -> throw new Exception("Unknown type " + fmt);
        };

        return objectWrite(obj, repo);
    }
}
