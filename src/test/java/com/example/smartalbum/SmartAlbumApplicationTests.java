package com.example.smartalbum;

import com.example.smartalbum.scheduled.VideoScheduled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;

@SpringBootTest
class SmartAlbumApplicationTests {

    @Resource
    private VideoScheduled videoScheduled;

    @Test
    void contextLoads() throws IOException {
        long start = System.currentTimeMillis();

//        imageUtil.imagesToVideo(wonderfulPath + "image/", wonderfulPath + "video/");

        videoScheduled.createVideo();

        System.out.println("耗时 " + (System.currentTimeMillis() - start) + "ms");
    }

}


