package com.example.smartalbum.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.ecloud.OssUtil;
import com.example.smartalbum.exception.FileOperationException;
import com.example.smartalbum.service.database.ImageDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class OssService {

    @Resource
    private OssUtil ossUtil;
    @Resource
    private OssService ossService;
    @Resource
    private ImageDataService imageDataService;

    @Value("${ecloud.bucketName}")
    private String bucketName;
    @Value("${ecloud.bucketName2}")
    private String bucketName2;

    /**
     * 检测文件是否存在于oss桶中
     *
     * @param filename oss文件名字
     */
    public boolean checkFile(String filename) {
        return ossUtil.checkObject(bucketName, filename);
    }

    /**
     * 获取文件的元数据
     *
     * @param fileName oss文件名字
     */
    public ObjectMetadata getFileMetadata(String fileName) {
        return ossUtil.getObjectMetadata(bucketName, fileName);
    }

    /**
     * 创建桶1资源的外网url,默认永不过期
     *
     * @param name oss文件名字
     */
    public URL createUrl(String name) {
        return this.createUrl(name, new Date(Long.MAX_VALUE));
    }
    public URL createUrlB2(String name) {
        return this.createUrlB2(name, new Date(Long.MAX_VALUE));
    }

    /**
     * 创建桶1资源的外网url
     *
     * @param objectName 对象名字
     * @param date       过期时间
     * @return 外网url链接
     */
    public URL createUrl(String objectName, Date date) {
        return ossUtil.createUrl(bucketName, objectName, date);
    }
    public URL createUrlB2(String objectName, Date date) {
        return ossUtil.createUrl(bucketName2, objectName, date);
    }

    /**
     * 创建桶2资源的外网Url，默认永不过期
     *
     * @param name oss文件名字
     */
    public URL createUrl2(String name) {
        return this.createUrl2(name, new Date(Long.MAX_VALUE));
    }

    /**
     * 创建桶2资源的外网Url
     *
     * @param name oss文件名字
     * @param date 过期时间
     */
    public URL createUrl2(String name , Date date) {
        return ossUtil.createUrl(bucketName2, name, date);
    }

    /**
     * 获取指定文件夹下的文件
     *
     * @param path 路径
     * @return 文件列表
     */
    public List<String> traverseFile(String path) {
        return ossUtil.getObjectList(bucketName, path);
    }
    public List<String> traverseFileB2(String path) {
        return ossUtil.getObjectList(bucketName2, path);
    }

    /**
     * 获取用户文件夹大小
     *
     * @param userRootPath 用户仓库
     */
    public long getDepositorySize(String userRootPath) {
        return ossUtil.getSize(bucketName, userRootPath + "/");
    }

    /**
     * 创建文件夹
     *
     * @param path 路径
     */
    public boolean createFolder(String path) throws IOException {
        return ossUtil.createObject(bucketName, path) && ossUtil.createObject(bucketName2, path);
    }

    /**
     * 删除文件 或 删除文件夹与文件夹下所有文件
     *
     * @param path 路径
     * @param useBucket2 是否同时检测和删除bucket2中的文件
     */
    public boolean deleteFile(String path,boolean useBucket2) {
        log.info("正在删除 {}",path);
        boolean isExist;
        if (useBucket2){
            isExist = ossUtil.checkObject(bucketName, path) && ossUtil.checkObject(bucketName2, path);
            if (!isExist) {
                log.info("文件不存在,或者文件缩略图不存在");
                return false;
            }
            return ossUtil.deleteObject(bucketName, path) && ossUtil.deleteObject(bucketName2, path);
        }else {
            isExist = ossUtil.checkObject(bucketName, path);
            if (!isExist) {
                log.info("文件不存在");
                return false;
            }
            return ossUtil.deleteObject(bucketName, path);
        }
    }

    /**
     * 删除桶2的资源
     * @param path oss路径
     * @return 删除结果
     */
    public boolean deleteFileB2(String path){
        boolean isExist = ossUtil.checkObject(bucketName2, path);
        if (!isExist) {
            log.info("文件不存在");
            return false;
        }
        return ossUtil.deleteObject(bucketName2, path);
    }

    /**
     * 文件重命名
     *
     * @param depositoryId 用户仓库id
     * @param oldName      oss旧文件名字
     * @param newName      oss新文件名字
     */
    public boolean rename(int depositoryId, String oldName, String newName,String depositoryName) {
        if (ossUtil.checkObject(bucketName, oldName) && ossUtil.checkObject(bucketName2, oldName)) {
            if (ossUtil.checkObject(bucketName, newName) || ossUtil.checkObject(bucketName2, newName)) {
                throw new FileOperationException("新名字已存在,请重试");
            }

            //复制文件，复制的过程把文件名字改为新名字
            ossUtil.copyObject(bucketName, oldName, bucketName, newName);
            ossUtil.copyObject(bucketName2, oldName, bucketName2, newName);

            //删除旧文件,只留下新文件
            if (ossUtil.deleteObject(bucketName, oldName) && ossUtil.deleteObject(bucketName2, oldName)) {
                String oldImageName = oldName.replace(depositoryName + "/","");
                String newImageName = newName.replace(depositoryName + "/","");
                //更新数据库中的数据
                int imageId = imageDataService.getImage(oldImageName, depositoryId).getId();
                String newImageUrl = this.createUrl(newName) + "";
                String newMiniImageUrl = this.createUrl2(newName) + "";

                Image image = new Image();
                image.setName(newImageName);
                image.setUrl(newImageUrl);
                image.setUrlMini(newMiniImageUrl);
                Date date = new Date(System.currentTimeMillis());
                image.setUpdateDate(date);

                return imageDataService.updateSelective(image, imageId) > 0;
            } else {
                //删除在oss生成的新文件
                ossUtil.deleteObject(bucketName, newName);
                ossUtil.deleteObject(bucketName2, newName);
                throw new FileOperationException("重命名失败，请重试!");
            }
        } else {
            throw new FileOperationException("需要命名的文件不存在");
        }
    }

    /**
     * 上传图片
     *
     * @param bytes 原图
     * @param name  oss名字
     */
    public boolean uploadImage(byte[] bytes, String name) {
        return ossUtil.simpleUpload(bucketName, bytes, name);
    }

    /**
     * 附带进度条的分片上传
     *
     * @param name    oss名字
     * @param in      文件
     * @param session HttpSession，用来实现进度条回显
     */
    public boolean uploadImage(String name, InputStream in, HttpSession session) {
        return ossUtil.pageUpload(bucketName, name, in, session);
    }

    /**
     * 无进度条的分片上传
     *
     * @param name oss名字
     * @param in   文件输入流
     * @return 上传的结果
     */
    public boolean uploadImage(String name, InputStream in) {
        return ossUtil.pageUpload(bucketName, name, in);
    }

    public boolean uploadImageB2(String name, InputStream in) {
        return ossUtil.pageUpload(bucketName2, name, in);
    }

    /**
     * 上传图片缩略图
     *
     * @param bytes 缩略图
     * @param name  oss名字
     */
    public boolean uploadMiniImage(byte[] bytes, String name) {
        return ossUtil.simpleUpload(bucketName2, bytes, name);
    }


    /**
     * 下载
     *
     * @param name 要下载的oss名字
     * @return 文件流
     */
    public InputStream download(String name) {
        return ossUtil.download(bucketName, name);
    }
}
