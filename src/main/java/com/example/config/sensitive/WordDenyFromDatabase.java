package com.example.config.sensitive;

import com.github.houbb.sensitive.word.api.IWordDeny;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Nruonan
 */
@Component
public class WordDenyFromDatabase implements IWordDeny {

    @Override
    public List<String> deny() {
        return List.of();
    }

}
