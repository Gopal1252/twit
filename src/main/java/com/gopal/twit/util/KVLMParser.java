package com.gopal.twit.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Parser for Key-value list with message (KVLM) format (used by commits and tags)
 * Used for parsing commits {basically a key-value store kind of this with a message}
 */
public class KVLMParser {
    public static Map<String, Object> parse(byte[] raw){
        return parse(raw, 0, new LinkedHashMap<>());
    }

    /**
     * parses or deserializes the bytes into the kvlm object
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parse(byte[] raw, int start, Map<String, Object> dict){
        //Find space and newline
        int spc = findByte(raw, (byte) ' ', start);
        int nl = findByte(raw, (byte) '\n', start);

        // If space appears before newline, we have a keyword.
        // Otherwise, it's the final message, which we just read to the end of the file.

        //BASE CASE: blank line means message start
        // If newline appears first (or there's no space at all, in which
        // case find returns -1), we assume a blank line. A blank line
        // means the remainder of the data is the message. We store it in
        // the map, with None as the key, and return.
        if(spc < 0 || nl < spc){
            byte[] message = new byte[raw.length - start - 1];
            System.arraycopy(raw, start + 1, message, 0, message.length);
            dict.put(null, message);
            return dict;
        }

        //RECURSIVE CASE: we read a key-value pair and recurse for the next
        String key = new String(raw, start, spc - start, StandardCharsets.UTF_8);

        //Find the end of value
        //Continuation lines begin with a space, so we loop until we find a "\n" not followed by a space
        int end = nl;
        while(end + 1 < raw.length && raw[end+1] == ' '){
            end = findByte(raw, (byte) '\n', end + 1);
        }

        //Extract the value
        //Also, drop the leading space on continuation lines
        String valueStr = new String(raw, spc+1, end-spc, StandardCharsets.UTF_8);
        byte[] value = valueStr.replace("\n ", "\n").getBytes(StandardCharsets.UTF_8);

        //Store in dict (handle multiple values for same key {there can be multiple values for the parent key})
        if(dict.containsKey(key)){//implies multiple values for the same key
            Object existing = dict.get(key);
            if(existing instanceof List){//list already exists, so just add the current value to that list
                ((List<byte[]>) existing).add(value);
            }
            else{//only a single value so far for that key, so now need to create the list to add the current value as well
                List<byte[]> list = new ArrayList<>();
                list.add((byte[]) existing);//add the existing value
                list.add(value);//add the new value
                dict.put(key, list);
            }
        }
        else{
            dict.put(key, value);
        }

        return parse(raw, end + 1, dict);
    }

    /**
     * takes the key value list with the message
     * returns them in the byte format {basically the commit format {without the header}}
     */
    @SuppressWarnings("unchecked")
    public static byte[] serialize(Map<String, Object> kvlm){
        ByteArrayOutputStream ret = new ByteArrayOutputStream();

        try{
            //Output fields
            for(Map.Entry<String, Object> entry : kvlm.entrySet()){
                String k = entry.getKey();
                if(k == null) continue; //skip the message for now

                Object val = entry.getValue();
                List<byte[]> values;

                if(val instanceof List){
                    values = (List<byte[]>) val;
                }
                else{
                    values = Collections.singletonList((byte[]) val);
                }

                for(byte[] v : values){
                    ret.write(k.getBytes(StandardCharsets.UTF_8));
                    ret.write(' ');
                    String valStr = new String(v, StandardCharsets.UTF_8);
                    ret.write(valStr.replace("\n", "\n ").getBytes(StandardCharsets.UTF_8));
                    ret.write('\n');
                }

                //Append message
                ret.write('\n');
                byte[] message = (byte[]) kvlm.get(null);
                if(message != null){
                    ret.write(message);
                }
            }
        } catch (IOException e){
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
