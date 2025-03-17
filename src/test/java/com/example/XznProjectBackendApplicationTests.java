package com.example;

import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.github.houbb.sensitive.word.support.result.WordResultHandlers;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XznProjectBackendApplicationTests {

    @Test
    void contextLoads() {
        //是否有敏感词存在
        String text = "操死你妈士大夫是。";
        boolean isFalse = SensitiveWordHelper.contains(text);
        System.out.println("是否有敏感词存在====>>>>"+isFalse);

        //返回所有敏感词
        List<String> wordList = SensitiveWordHelper.findAll(text);
        System.out.println("返回所有敏感词====>>>>"+wordList);

        //返回所有敏感词 等价于SensitiveWordHelper.findAll(text)
        wordList = SensitiveWordHelper.findAll(text, WordResultHandlers.word());
        System.out.println("返回所有敏感词====>>>>"+wordList);

        //返回字符串中第一个敏感词
        String first = SensitiveWordHelper.findFirst(text);
        System.out.println("返回字符串中第一个敏感词====>>>>"+first);

        //返回字符串中第一个敏感词 等价于SensitiveWordHelper.findFirst（text）
        first = SensitiveWordHelper.findFirst(text,WordResultHandlers.word());
        System.out.println("返回字符串中第一个敏感词====>>>>"+first);

        //默认的替换策略
        String result = SensitiveWordHelper.replace(text);
        System.out.println("默认的替换策略====>>>>"+result);

        //指定替换的内容 这里可以自定义替换内容
        result = SensitiveWordHelper.replace(text, '0');
        System.out.println("指定替换的内容====>>>>"+result);
    }

}
