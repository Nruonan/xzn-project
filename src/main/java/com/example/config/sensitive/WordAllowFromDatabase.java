package com.example.config.sensitive;

import com.github.houbb.sensitive.word.api.IWordAllow;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * @author Nruonan
 */
@Component
public class WordAllowFromDatabase implements IWordAllow {

    @Override
    public List<String> allow() {
        return List.of();
    }

}
