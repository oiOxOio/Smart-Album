package com.example.smartalbum.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.Tag;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.ecloud.ImageRecognition;
import com.example.smartalbum.exception.ImageGenerateException;
import com.example.smartalbum.exception.UploadFileException;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.service.database.TagDataService;
import com.example.smartalbum.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 图片服务，提供了
 * 图片格式检测、缩略图、数据库操作、图像识别、图片上传等功能
 *
 * @author Administrator
 */
@Slf4j
@Service
public class ImageService {

    @Resource
    private OssService ossService;
    @Resource
    private UpdateService updateService;
    @Resource
    private ImageRecognition imageRecognition;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private TagDataService tagDataService;
    @Resource
    private ImageUtil imageUtil;

    /**
     * 创建固定数线程量的可重用线程池
     */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(30);

    /**
     * 检测是否为图片格式
     *
     * @param fileName 文件名字
     * @return 检测结果
     */
    public boolean checkImage(String fileName) {
        return imageUtil.checkImage(fileName);
    }

    /**
     * 生成图片的缩略图并进行base64编码
     *
     * @param bytes 原图byte数组
     * @return 缩略图的Base64
     */
    public String getMiniImageBase64(byte[] bytes) throws ImageGenerateException {
        //循环压缩图片到小于3m
        byte[] bytes1 = imageUtil.compressUnderSize(bytes, 300000);
        //byte转为base64
        return Base64.getEncoder().encodeToString(bytes1);
    }

    /**
     * 插入图片数据到数据库
     *
     * @param imageName 图片名字
     * @param tags      对应的标签
     * @param session   用来更新仓库大小
     * @return 插入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertImage(String imageName, List<String> tags, HttpSession session) {
        User userInfo = (User) session.getAttribute("userInfo");
        Depository depository = userInfo.getDepository();
        int depositoryId = depository.getId();

        //插入部分数据
        Image image = new Image();
        image.setDepositoryId(depositoryId);
        image.setName(imageName);
        if (imageDataService.insertSelective(image) <= 0) {
            throw new UploadFileException("数据处理失败!");
        }

        //用oss生成url，和元数据
        String objectName = depository.getName() + "/" + imageName;
        image.setUrl(ossService.createUrl(objectName) + "");
        image.setUrlMini(ossService.createUrl2(objectName) + "");

        ObjectMetadata metadata = ossService.getFileMetadata(objectName);
        image.setSize(String.valueOf(metadata.getContentLength()));
        image.setCreateDate(metadata.getLastModified());
        image.setUpdateDate(metadata.getLastModified());

        //获取刚插入图片的id
        int imageId = imageDataService.getImageId(imageName, depositoryId);
        //补全其它数据
        int insertImageResult = imageDataService.updateSelective(image, imageId);

        //更新用户数据
        updateService.updateUserInfo(session);

        //把图像识别的结果插入tag表
        int insertTagCount = 0;
        Tag tag;
        for (String name : tags) {
            tag = new Tag();
            tag.setName(name);
            tag.setImageId(imageId);
            if (tagDataService.insertSelective(tag)) {
                insertTagCount++;
            }
        }
        log.info("向image表中插入了 {} 条数据，向tag表插入了 {} 条数据", insertImageResult, insertTagCount);
        return insertImageResult > 0 && insertTagCount > 0;
    }

    /**
     * 异步图像识别（通用识别、人体检测、汽车检测）
     *
     * @param base64 Base64编码的图片
     * @return 识别的结果
     */
    public List<String> getRecognitionResult(String base64) {

        //标签
        List<String> tags = new LinkedList<>();

        //线程集合
        List<Callable<List<String>>> callables = new ArrayList<>();

        //通用图像识别、人体检测与属性识别、车辆检测与属性识别
        callables.add(() -> imageRecognition.classifyDetect(base64));
        callables.add(() -> imageRecognition.personDetect(base64));
        callables.add(() -> imageRecognition.carDetect(base64));

        //异步计算结果列表
        List<Future<List<String>>> futures;
        try {
            //异步执行所有线程
            futures = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        futures.forEach((future) -> {
            try {
                //没有结果则阻塞当前线程，直到有为止
                List<String> tag = future.get();
                if (tag != null) {
                    tags.addAll(tag);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

        return tags;
    }

}
