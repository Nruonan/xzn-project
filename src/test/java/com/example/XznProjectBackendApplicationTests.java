package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XznProjectBackendApplicationTests {

    @Test
    void contextLoads() {
        for(int i = 20; i < 1000; i++){
            System.out.println("INSERT INTO `monitor`.`db_follow` (`id`, `uid`, `fid`, `status`, `time`) VALUES ("+i+", "+i+", 11, 1, '2025-03-13 12:02:34');");

        }
    }

}
