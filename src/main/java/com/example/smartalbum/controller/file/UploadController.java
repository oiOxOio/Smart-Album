package com.example.smartalbum.controller.file;

import com.example.smartalbum.exception.TemporaryFileDoesNotExistException;
import com.example.smartalbum.service.SearchService;
import com.example.smartalbum.service.UploadService;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 文件上传、上传进度条回显、重置进度条
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/file")
@Slf4j
public class UploadController {

    @Resource
    private UploadService uploadService;
    @Resource
    private SearchService searchService;
    @Resource
    private ImageDataService imageDataService;

    private static final int UPLOAD_BASE_SIZE = 10;

    /**
     * 多文件上传<br/>
     * 上传的过程中会生成原图和缩略图，原图是采用分片上传，缩略图采用简易上传，上传到oss对象存储桶的同时，把数据整理插入到数据库中。
     *
     * @param multipartFiles 前端传来的文件数组
     * @return 成功上传的文件列表
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("files") MultipartFile[] multipartFiles,
                         HttpSession session) throws IOException, ExecutionException, InterruptedException {

        //判断上传的文件是否为空
        if (multipartFiles.length <= 0 || multipartFiles[0].isEmpty()) {
            return ResponseMsgUtil.failure("上传文件为空!");
        }

        //去重 去空
        List<MultipartFile> fileList = Arrays.stream(multipartFiles)
                .distinct()
                .filter((item) -> !item.isEmpty())
                .collect(Collectors.toList());

        if (fileList.size() > UPLOAD_BASE_SIZE) {
            return ResponseMsgUtil.failure("最多只能上传10张图片！");
        }
        long currentSize = 0;
        String checkFilename;
        for (MultipartFile multipartFile : fileList) {
            //上传的文件名
            checkFilename = multipartFile.getOriginalFilename();

            //当前大小
            currentSize += multipartFile.getSize();

            //验证是否符合上传需求
            uploadService.checkUploadFile(checkFilename, currentSize, session);
        }

        CountDownLatch countDownLatch = new CountDownLatch(fileList.size());

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        //所有文件检测完后再上传
        for (MultipartFile multipartFile : fileList) {

            Runnable thread = Executors.defaultThreadFactory().newThread(() -> {
                try {
                    uploadService.upload(multipartFile, session);
                } catch (InterruptedException | FileExistsException | TemporaryFileDoesNotExistException | ExecutionException e) {
                    log.error("上传错误！", e);
                } finally {
                    countDownLatch.countDown();
                }
            });
            executorService.execute(thread);
        }

        countDownLatch.await();

        return ResponseMsgUtil.success("上传图片成功");
    }

    /**
     * 实时验证能否执行上传操作,每添加一个文件调用一次
     *
     * @param filenames 文件名字数组
     * @param size      所有待传文件大小总和
     */
    @GetMapping("/checkUpload")
    public String checkUpload(@RequestParam("filenames") String[] filenames,
                              @RequestParam("size") long size,
                              HttpSession session) throws FileExistsException {
        for (String filename : filenames) {
            if (!uploadService.checkUploadFile(filename, size, session)) {
                return ResponseMsgUtil.failure("未通过验证");
            }
        }
        return ResponseMsgUtil.success("已通过验证");
    }

    /**
     * 上传的同时进度条回显
     *
     * @param session 用来获取进度
     * @return 上传进度，取值[0,1]
     */
    @GetMapping("/uploadProgress")
    public double getProgress(HttpSession session) {
        Object uploadPercent = session.getAttribute("uploadPercent");
        return uploadPercent == null ? 0 : Double.parseDouble(uploadPercent + "");
    }

}
