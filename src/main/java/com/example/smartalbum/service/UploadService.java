package com.example.smartalbum.service;

import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.TemporaryFileDoesNotExistException;
import com.example.smartalbum.exception.UploadFileException;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Administrator
 * @date 2021/5/30 20:37
 */
@Slf4j
@Service
public class UploadService {

    @Resource
    private ImageService imageService;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private OssService ossService;
    @Resource
    private UpdateService updateService;
    @Resource
    private ImageUtil imageUtil;

    /**
     * 创建固定数线程量的可重用线程池
     */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(20);

    public void upload(MultipartFile multipartFile, HttpSession session)
            throws InterruptedException, FileExistsException, TemporaryFileDoesNotExistException, ExecutionException {

        User userInfo = (User) session.getAttribute("userInfo");
        //每个用户的根路径
        String depositoryName = userInfo.getDepository().getName();
        //上传的文件名
        String fileName = multipartFile.getOriginalFilename();
        //oss对象名字
        String objectName = depositoryName + "/" + fileName;

        //图片源文件
        byte[] bytes;
        try {
            //上传原图(分片上传)和缩略图(简单上传)
            bytes = multipartFile.getBytes();
        } catch (IOException e) {
            throw new TemporaryFileDoesNotExistException();
        }

        List<Callable<List<String>>> callables = new LinkedList<>();

        //线程1，上传文件
        callables.add(() -> {
            //上传等比缩放至宽度400px的缩略图
            byte[] miniImage = imageUtil.getMiniImage(bytes, 400.0);
            boolean uploadMiniImageResult = ossService.uploadMiniImage(miniImage, objectName);

            //上传原图
            boolean uploadResult;
            try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                uploadResult = ossService.uploadImage(objectName, inputStream, session);
            }

            return uploadResult && uploadMiniImageResult ? new ArrayList<>() : null;
        });

        //线程2，用压缩后的图片进行图像识别(大小限制到3m)
        callables.add(() -> {
            String base64 = imageService.getMiniImageBase64(bytes);
            return imageService.getRecognitionResult(base64);
        });

        List<String> tags = new LinkedList<>();
        List<Future<List<String>>> futures = executorService.invokeAll(callables);
        for (Future<List<String>> item : futures) {
            List<String> list = item.get();
            if (list != null) {
                tags.addAll(list);
            }
        }

        //把数据库的数据补齐
        if (imageService.insertImage(fileName, tags, session)) {
            log.info("图片 {} 数据校对完毕",fileName);
        } else {
            throw new UploadFileException("数据校对失败");
        }

        //更新用户数据
        updateService.updateUserInfo(session);
    }

    public boolean checkUploadFile(String fileName, long size, HttpSession session) throws FileExistsException {

        User userInfo = (User) session.getAttribute("userInfo");

        //检测图片格式
        if (!imageService.checkImage(fileName)) {
            throw new UploadFileException("非图片格式，请重试");
        }

        //检测容量
        long maxSize = Long.parseLong(userInfo.getDepository().getSizeMax());
        if ((size > maxSize)) {
            throw new UploadFileException("当前个人空间容量不足！");
        }

        //检测是否重复
        Image images = imageDataService.getImage(fileName, userInfo.getDepository().getId());
        if (images != null) {
            throw new FileExistsException("存在重复文件");
        }

        return true;
    }
}
