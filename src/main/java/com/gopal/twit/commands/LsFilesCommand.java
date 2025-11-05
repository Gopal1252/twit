package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.index.GitIndex;
import com.gopal.twit.core.index.GitIndexEntry;
import com.gopal.twit.util.IndexIO;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * this command displays the names of the files in the staging area.
 * Git's original ls-files has a ton of options
 * ours ls-files command will be much simpler with only --verbose option (doesn't exist in git)
 * --verbose option -> display every single bit of info in the index file
 */
public class LsFilesCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        boolean verbose = false;

        for(String arg : args){
            if(arg.equals("--verbose")){
                verbose = true;
            }
        }

        GitRepository repo = GitRepository.find();
        GitIndex index = IndexIO.indexRead(repo);

        if(verbose){
            System.out.println("Index file format v" + index.getVersion() + ", containing " + index.getEntries().size() + " entries.");
        }

        for(GitIndexEntry e : index.getEntries()){
            System.out.println(e.getName());

            if(verbose){
                String entryType = switch (e.getModeType()){
                    case 0b1000 -> "regular file";
                    case 0b1010 -> "symlink";
                    case 0b1110 -> "git link";
                    default -> "unknown";
                };

                System.out.println(" " + entryType + " with perms: " + String.format("%o", e.getModePerms()));
                System.out.println(" on blob: " + e.getSha());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
                String ctime = formatter.format(Instant.ofEpochSecond(e.getCtime()[0]));
                String mtime = formatter.format(Instant.ofEpochSecond(e.getMtime()[0]));

                System.out.println("  created: " + ctime + "." + e.getCtime()[1] + ", modified: " + mtime + "." + e.getMtime()[1]);
                System.out.println("  device: " + e.getDev() + ", inode: " + e.getIno());
                System.out.println("  user: " + e.getUid() + " group: " + e.getGid());
                System.out.println("  flags: stage=" + e.getFlagStage() + " assume_valid=" + e.isFlagAssumeValid());
            }
        }
    }
}
