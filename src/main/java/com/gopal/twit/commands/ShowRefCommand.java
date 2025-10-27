package com.gopal.twit.commands;

import com.gopal.twit.core.GitRepository;
import com.gopal.twit.core.ref.RefResolver;

import java.util.Map;

/**
 * it just lists all references in a repository
 */
public class ShowRefCommand implements Command{
    @Override
    public void execute(String[] args) throws Exception {
        GitRepository repo = GitRepository.find();
        Map<String, Object> refs = RefResolver.refList(repo, null);
        showRef(refs, true, "refs");
    }

    @SuppressWarnings("unchecked")
    private void showRef(Map<String, Object> refs, boolean withHash, String prefix){
        if(!prefix.isEmpty()){
            prefix += "/";
        }

        for(Map.Entry<String, Object> entry : refs.entrySet()){
            String k = entry.getKey();
            Object v = entry.getValue();

            if(v instanceof String sha){
                if(withHash){
                    System.out.println(sha + " " + prefix + k);
                }
                else{
                    System.out.println(prefix + k);
                }
            }
            else{
                showRef((Map<String, Object>) v, withHash, prefix + k);
            }
        }
    }
}
