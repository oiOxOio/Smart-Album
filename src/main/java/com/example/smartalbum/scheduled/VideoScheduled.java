package com.example.smartalbum.scheduled;

import com.example.smartalbum.dao.DepositoryMapper;
import com.example.smartalbum.dao.ImageMapper;
import com.example.smartalbum.dao.ImageSetMapper;
import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.ImageSet;
import com.example.smartalbum.domain.ImageSetExample;
import com.example.smartalbum.service.OssService;
import com.example.smartalbum.util.ImageUtil;
import com.example.smartalbum.util.RandomStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Administrator
 * @date 2021/6/5 22:33
 */
@Component
@Slf4j
public class VideoScheduled {

    @Value("${app.ffmpegPath}")
    private String ffmpeg;
    @Value("${app.wonderfulPath}")
    private String wonderfulPath;
    @Value("${ecloud.resources}")
    private String resources;

    @Resource
    private ImageUtil imageUtil;
    @Resource
    private OssService ossService;
    @Resource
    private ImageMapper imageMapper;
    @Resource
    private DepositoryMapper depositoryMapper;
    @Resource
    private ImageSetMapper imageSetMapper;

    private final SecureRandom secureRandom = RandomStringUtil.random;
    /**
     * 当相册里的图片超过这个数后，才生成精彩时刻视频
     */
    private final static int BASE_IMAGE_NUM = 15;
    /**
     * 图片数量，拿多少张图片来合成
     */
    private final static int IMAGE_COUNT = 10;

    /**
     * 初始运行项目24小时后开始执行，上一次执行完毕时间点之后24小时后再执行（每天执行一次）
     */
    @Scheduled(initialDelay = 1000 * 60 * 60 * 24,fixedDelay = 1000 * 60 * 60 * 24)
    public void createVideo() throws IOException {
        log.info("开始生成精彩时刻视频");
        if (!Files.exists(Paths.get(ffmpeg))) {
            log.info("未找到ffmpeg.exe，请检查配置文件，或从官网下载 https://ffmpeg.org/download.html");
            return;
        }

        //汇总，收集数据库中所有相册的id
        List<Integer> imageSetsId = new LinkedList<>();
        ImageSetExample imageSetExample = new ImageSetExample();
        imageSetExample.createCriteria().andIdNotEqualTo(1);
        imageSetMapper.selectByExample(imageSetExample).forEach((imageSet -> imageSetsId.add(imageSet.getId())));

        List<ImageSet> imageSets;
        List<Image> images;

        for (int imageSetId : imageSetsId) {
            //根据相册id查询出相册所有信息，
            imageSets = imageSetMapper.selectWithImage(imageSetId);
            for (ImageSet imageSet : imageSets) {
                images = imageSet.getImages();
                String wonderfulUrl = imageSet.getWonderfulUrl();

                //如果该相册没有精彩时刻视频,且该相册图片数量100张以上 则生成
                boolean isEmpty = wonderfulUrl == null || wonderfulUrl.isEmpty();
                if (isEmpty && images.size() >= BASE_IMAGE_NUM) {
                    log.info("正在为用户仓库 {} 中的相册 {} 生成精彩时刻视频",imageSet.getDepositoryId(), imageSet.getName());

                    List<Image> randomImage = new LinkedList<>();

                    //随机抽10张拿来合成视频
                    int size = images.size();
                    for (int i = 0; i < IMAGE_COUNT; i++) {
                        int index = secureRandom.nextInt(size);
                        randomImage.add(images.get(index));
                    }

                    Depository depository = depositoryMapper.selectByPrimaryKey(imageSet.getDepositoryId());
                    String depositoryName = depository.getName();

                    //构造本地临时文件存放路径
                    String depositoryPath = wonderfulPath + depositoryName + "/" + imageSetId;
                    String imgPath = depositoryPath + "/img/";
                    String videoPath = depositoryPath + "/video/";
                    File file = new File(imgPath);
                    if (!file.exists()) {
                        if (file.mkdirs()) {
                            log.info("创建文件夹 {} 成功", imgPath);
                        }
                    }

                    //将10张随机的照片下载到本地
                    downloadImage(randomImage,imgPath,depositoryName);

                    //合成图片为精彩时刻的视频
                    imageUtil.imagesToVideo(imgPath, videoPath);

                    //上传，返回的url 后续一并放入数据库
                    FileInputStream inputStream = new FileInputStream(videoPath + "/out.mp4");
                    String url = uploadVideo(inputStream,depositoryName,imageSetId);
                    imageSet.setWonderfulUrl(url + "");

                    //插入数据库新数据
                    if (imageSetMapper.updateByPrimaryKeySelective(imageSet) > 0) {
                        log.info("更新数据 wonderfulUrl 成功 精彩时刻视频链接：{}", url);
                    }

                    //上传完后把所有临时文件删除
                    File deleteDir = new File(depositoryPath);
                    if (deleteDir(deleteDir)) {
                        log.info("清除临时数据成功！");
                    }
                }else {
                    log.info("用户仓库 {} 中的相册 {} 不满足生成条件，已结束生成",imageSet.getDepositoryId(), imageSet.getName());
                }
            }
        }
        log.info("生成精彩时刻视频结束");
    }

    /**
     * 类似断点续传
     * 在上一次生成精彩时刻时有可能网络问题导致生成失败
     * 向图片文件夹中补充图片直到10张
     */
    private void downloadImage(List<Image> randomImage,String imgPath,String depositoryName) throws IOException {
        String[] files = new File(imgPath).list();
        if (files != null){
            int count = 10 - files.length;
            for (int i = 0; i < count; i++) {
                String imageName = randomImage.get(i).getName();
                download(imgPath, depositoryName, imageName);
            }
        }else {
            for (Image image : randomImage) {
                String imageName = image.getName();
                download(imgPath, depositoryName, imageName);
            }
        }
    }

    private void download(String imgPath, String depositoryName, String imageName) throws IOException {
        File file = new File(imgPath + "/" + imageName);
        if (file.createNewFile()) {
            log.info("正在准备下载 {} 到本地", file.getPath());
        }
        try (
                InputStream download = ossService.download(depositoryName + "/" + imageName);
                BufferedInputStream in = new BufferedInputStream(download);
                FileOutputStream outputStream = new FileOutputStream(file);
                BufferedOutputStream out = new BufferedOutputStream(outputStream)
        ) {
            byte[] b = new byte[1024];
            int len;
            while ((len = in.read(b)) != -1) {
                out.write(b, 0, len);
            }
        }
    }

    /**
     * 把文件上传到  bucketName/resources/video/depositoryName/imageSetId/out.mp4
     */
    private String uploadVideo(FileInputStream inputStream,String depositoryName,int imageSetId){

        String uploadKey = resources + "/video/" + depositoryName + "/" + imageSetId + "/out.mp4";
        if (ossService.uploadImageB2(uploadKey, inputStream)) {
            log.info("上传视频成功！ objectKey = {}", uploadKey);
            URL url = ossService.createUrlB2(uploadKey);
            return url + "";
        }
        return null;
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     */
    private static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            if (children != null && children.length > 0) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }
}
