package com.example.smartalbum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 启动类
 *
 * @author Administrator
 */
@SpringBootApplication
@MapperScan("com.example.smartalbum.dao")
@EnableTransactionManagement
@EnableScheduling
public class SmartAlbumApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAlbumApplication.class, args);
    }

}
