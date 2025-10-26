package com.gopal.twit.util;

import com.gopal.twit.core.objects.GitTreeLeaf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the tree object
 * Tree is an array of 3 element tuples of the format: [mode] space [path] 0x00 [sha-1]
 */
public class TreeParser {

    public static List<GitTreeLeaf> parse(byte[] raw){
        List<GitTreeLeaf> ret = new ArrayList<>();
        int pos = 0;

        while(pos < raw.length){
            ParseResult result = parseOne(raw, pos);
            ret.add(result.leaf);
            pos = result.newPos;
        }
        return ret;
    }

    private static class ParseResult {
        int newPos;
        GitTreeLeaf leaf;

        ParseResult(int newPos, GitTreeLeaf leaf){
            this.newPos = newPos;
            this.leaf = leaf;
        }
    }

    /**
     * parses a single record
     */
    private static ParseResult parseOne(byte[] raw, int start){
        //Find space (end of mode)
        int x = findByte(raw, (byte) ' ', start);
        String mode = new String(raw, start, x - start, StandardCharsets.UTF_8);

        //Normalize to 6 digits
        if(mode.length() == 5){
            mode = "0" + mode;
        }

        //Find null (end of path)
        int y = findByte(raw, (byte) 0, x);
        String path = new String(raw, x+1, y-x+1, StandardCharsets.UTF_8);

        //Read the SHA
        byte[] shaBytes = new byte[20];
        System.arraycopy(raw, y + 1, shaBytes, 0, 20);
        BigInteger shaInt = new BigInteger(1, shaBytes);
        String sha = String.format("%040x", shaInt);

        return new ParseResult(y + 21, new GitTreeLeaf(mode, path, sha));
    }

    public static byte[] serialize(List<GitTreeLeaf> items){
        //sort items {Because we may have added or modified entries, we need to sort them again}
        items.sort((a, b) -> {
            String aKey = a.getMode().startsWith("04") ? a.getPath() + "/" : a.getPath();
            String bKey = b.getMode().startsWith("04") ? b.getPath() + "/" : b.getPath();
            return aKey.compareTo(bKey);
        });

        ByteArrayOutputStream ret = new ByteArrayOutputStream();

        try {
            for (GitTreeLeaf item : items) {
                ret.write(item.getMode().getBytes(StandardCharsets.UTF_8));
                ret.write(' ');
                ret.write(item.getPath().getBytes(StandardCharsets.UTF_8));
                ret.write(0);

                // Convert hex SHA to binary
                BigInteger shaInt = new BigInteger(item.getSha(), 16);
                byte[] shaBytes = shaInt.toByteArray();

                // Pad to 20 bytes if needed
                if (shaBytes.length < 20) {
                    byte[] padded = new byte[20];
                    System.arraycopy(shaBytes, 0, padded, 20 - shaBytes.length, shaBytes.length);
                    shaBytes = padded;
                } else if (shaBytes.length > 20) {
                    // Remove sign byte
                    byte[] trimmed = new byte[20];
                    System.arraycopy(shaBytes, shaBytes.length - 20, trimmed, 0, 20);
                    shaBytes = trimmed;
                }

                ret.write(shaBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ret.toByteArray();
    }

    private static int findByte(byte[] array, byte target, int start){
        for(int i=start; i<array.length; i++){
            if(array[i] == target) return i;
        }
        return -1;
    }
}
