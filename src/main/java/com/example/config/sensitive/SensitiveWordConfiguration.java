package com.example.config.sensitive;

import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Nruonan
 * @description
 */
@Configuration
public class SensitiveWordConfiguration {

    private final WordAllowFromDatabase wordAllowFromDatabase;

    private final WordDenyFromDatabase wordDenyFromDatabase;

    public SensitiveWordConfiguration(WordAllowFromDatabase wordAllowFromDatabase,
        WordDenyFromDatabase wordDenyFromDatabase) {
        this.wordAllowFromDatabase = wordAllowFromDatabase;
        this.wordDenyFromDatabase = wordDenyFromDatabase;
    }

    @Bean
    public SensitiveWordBs sensitiveWordBs() {
        return SensitiveWordBs.newInstance()
            .wordAllow(WordAllows.chains(WordAllows.defaults(), wordAllowFromDatabase))
            .wordDeny(WordDenys.chains(WordDenys.defaults(), wordDenyFromDatabase))
            // 各种其他配置
            .init();
    }
}
