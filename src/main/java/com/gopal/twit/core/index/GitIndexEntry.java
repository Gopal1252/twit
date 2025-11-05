package com.gopal.twit.core.index;

public class GitIndexEntry {

    private final long[] ctime;//The last time a file's metadata changed (timestamp in seconds, nanoseconds) {it's a pair}
    private final long[] mtime;//The last time a file's data changed (timestamp in seconds, nanoseconds) {it's a pair}
    private final long dev;//The ID of device containing this file
    private final long ino;//The file's inode number {a unique integer identifier assigned by the operating system to a specific file, directory, or other file system object}
    private final int modeType;//The object type, either b1000 (regular), b1010 (symlink), b1110 (gitlink)
    private final int modePerms;//The object permissions, an integer
    private final long uid;//User ID of owner
    private final long gid;//Group ID of owner
    private final long fsize;//Size of this object, in bytes
    private final String sha;//The object's SHA
    private final boolean flagAssumeValid;
    private final int flagStage;
    private final String name;//Name of the object (full path this time!)

    public GitIndexEntry(long[] ctime, long[] mtime, long dev, long ino,
                         int modeType, int modePerms, long uid, long gid,
                         long fsize, String sha, boolean flagAssumeValid,
                         int flagStage, String name) {
        this.ctime = ctime;
        this.mtime = mtime;
        this.dev = dev;
        this.ino = ino;
        this.modeType = modeType;
        this.modePerms = modePerms;
        this.uid = uid;
        this.gid = gid;
        this.fsize = fsize;
        this.sha = sha;
        this.flagAssumeValid = flagAssumeValid;
        this.flagStage = flagStage;
        this.name = name;
    }

    // Getters
    public long[] getCtime() { return ctime; }
    public long[] getMtime() { return mtime; }
    public long getDev() { return dev; }
    public long getIno() { return ino; }
    public int getModeType() { return modeType; }
    public int getModePerms() { return modePerms; }
    public long getUid() { return uid; }
    public long getGid() { return gid; }
    public long getFsize() { return fsize; }
    public String getSha() { return sha; }
    public boolean isFlagAssumeValid() { return flagAssumeValid; }
    public int getFlagStage() { return flagStage; }
    public String getName() { return name; }
}
