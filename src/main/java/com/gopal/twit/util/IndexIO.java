package com.gopal.twit.util;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IndexIO {

    /**
     * Reads the .git/index file (if it exists), parses its binary structure, and returns a GitIndex Object
     * From article: parses/reads the index files into respective objects and prepare the GitIndex object
     * After reading the 12-bytes header, we just parse entries in the order they appear.
     * An entry begins with a set of fixed-length data, followed by a variable-length name.
     */
    public static GitIndex indexRead(GitRepository repo) throws IOException{
        Path indexFile = repo.repoFile("index");

        if(!Files.exists(indexFile)){
            return new GitIndex();
        }

        try(DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(indexFile)))){

            //Read header
            byte[] sig = new byte[4];
            in.readFully(sig);
            if(!new String(sig, "ASCII").equals("DIRC")){
                throw new IOException("Invalid index signature");
            }

            int version = in.readInt();
            if(version != 2){
                throw new IOException("Unsupported index version: " + version);
            }

            int count = in.readInt();

            List<GitIndexEntry> entries = new ArrayList<>();

            for(int i=0;i<count;i++){
                // Read entry

                //read timestamps and metadata
                long ctimeS = Integer.toUnsignedLong(in.readInt());
                long ctimeNs = Integer.toUnsignedLong(in.readInt());
                long mtimeS = Integer.toUnsignedLong(in.readInt());
                long mtimeNs = Integer.toUnsignedLong(in.readInt());

                //read filesystem info
                long dev = Integer.toUnsignedLong(in.readInt());
                long ino = Integer.toUnsignedLong(in.readInt());

                //skip unused field
                int unused = in.readUnsignedShort();

                //read mode (file type and permissions)
                int mode = in.readUnsignedShort();//git stores mode in 16 bits
                int modeType = mode >> 12;//top 4 bits -> file type (normal file, symlink, etc)
                int modePerms = mode & 0b0000000111111111;//lower 9 bits

                //read user/group ids and file size
                long uid = Integer.toUnsignedLong(in.readInt());
                long gid = Integer.toUnsignedLong(in.readInt());
                long fsize = Integer.toUnsignedLong(in.readInt());

                //read SHA
                byte[] shaBytes = new byte[20];
                in.readFully(shaBytes);
                String sha = bytesToHex(shaBytes);

                //read flags
                int flags = in.readUnsignedShort();//flags is 16-bit field
                boolean flagAssumeValid = (flags & 0b1000000000000000) != 0;//bit 15 -> assume valid flag (Git optimization)
                int flagStage = (flags & 0b0011000000000000) >> 12;//bits 12-13 -> stage (used for merges)
                int nameLength = flags & 0b0000111111111111;// bits 0-11 -> filename length (up to 4095 bytes)

                //read name
                String name;
                if(nameLength < 0xFFF){//If the filename length fits in the 12 bits (< 4095), read it directly and stop at the null terminator (0x00)
                    byte[] nameBytes = new byte[nameLength];
                    in.readFully(nameBytes);
                    name = new String(nameBytes, "UTF-8");
                    in.readByte();//null terminator
                }
                else{//If longer, read bytes one by one until reaching 0x00
                    ByteArrayOutputStream nameOut = new ByteArrayOutputStream();
                    int b;
                    while((b = in.readByte()) != 0){
                        nameOut.write(b);
                    }
                    name = nameOut.toString("UTF-8");
                }

                //skip padding bytes {Git’s index entries are padded so that each entry starts at an address that is a multiple of 8 bytes}
                //Git aligns each entry to an 8-byte boundary for performance
                //We compute how many padding bytes to skip to reach that boundary
                int entryLen = 62 + name.length() + 1;
                int padLen = (8 - (entryLen % 8)) % 8;
                in.skipBytes(padLen);

                entries.add(new GitIndexEntry(
                        new long[]{ctimeS, ctimeNs},
                        new long[]{mtimeS, mtimeNs},
                        dev, ino, modeType, modePerms, uid, gid, fsize, sha,
                        flagAssumeValid, flagStage, name
                ));
            }

            return new GitIndex(version, entries);
        }
    }

    public static void indexWrite(GitRepository repo, GitIndex index) throws IOException{
        Path indexFile = repo.repoFile("index");

        try(DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(indexFile)))){

            //write header
            out.write("DIRC".getBytes("ASCII"));
            out.writeInt(index.getVersion());
            out.writeInt(index.getEntries().size());

            //write entries
            for(GitIndexEntry e : index.getEntries()){
                out.writeInt((int) e.getCtime()[0]);
                out.writeInt((int) e.getCtime()[1]);
                out.writeInt((int) e.getMtime()[0]);
                out.writeInt((int) e.getMtime()[1]);
                out.writeInt((int) e.getDev());
                out.writeInt((int) e.getIno());

                //mode
                int mode = (e.getModeType() << 12) | e.getModePerms();
                out.writeInt(mode);

                //user/group ids and file size
                out.writeInt((int) e.getUid());
                out.writeInt((int) e.getGid());
                out.writeInt((int) e.getFsize());

                //sha
                out.write(hexToBytes(e.getSha()));

                //flags
                int flagAssumeValid = e.isFlagAssumeValid() ? 0x8000 : 0;
                byte[] nameBytes = e.getName().getBytes("UTF-8");
                int nameLength = Math.min(nameBytes.length, 0xFFF);
                int flags = flagAssumeValid | (e.getFlagStage() << 12) | nameLength;
                out.writeShort(flags);

                //name
                out.write(nameBytes);
                out.writeByte(0);

                //padding bytes
                int entryLen = 62 + nameBytes.length + 1;
                int padLen = (8 - (entryLen % 8)) % 8;
                for (int i = 0; i < padLen; i++) {
                    out.writeByte(0);
                }
            }
        }
    }

    private static String bytesToHex(byte[] shaBytes){
        StringBuilder sb = new StringBuilder();
        for(byte b : shaBytes){
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}

