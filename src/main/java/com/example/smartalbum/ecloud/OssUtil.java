package com.example.smartalbum.ecloud;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.transfer.model.UploadResult;
import com.example.smartalbum.exception.UploadFileException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 封装对oss操作
 *
 * @author Administrator
 */
@Slf4j
@Component
public class OssUtil {

    @Resource
    AmazonS3Client client;

    /**
     * 检测对象是否存在
     *
     * @param bucketName 对象桶名字
     * @param objectName 对象名字
     * @return 对象是否存在
     */
    public boolean checkObject(String bucketName, String objectName) {
        return client.doesObjectExist(bucketName, objectName);
    }

    /**
     * 获取对象的元数据
     *
     * @param bucketName 对象桶名字
     * @param objectName 对象名字
     * @return 对象的元数据
     */
    public ObjectMetadata getObjectMetadata(String bucketName, String objectName) {
        return client.getObjectMetadata(bucketName, objectName);
    }

    /**
     * 生成文件的共享外链，默认Long.MAX_VALUE过期
     *
     * @param bucketName 对象桶名字
     * @param objectName 对象名字
     * @return 外网URL
     */
    public URL createUrl(String bucketName, String objectName,Date date) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectName);
        //设置过期时间
        request.setExpiration(date);
        return client.generatePresignedUrl(request);
    }

    /**
     * 获取指定文件夹下的文件名列表
     *
     * @param bucketName 对象桶名字
     * @param path       路径
     * @return 文件名List
     */
    public List<String> getObjectList(String bucketName, String path) {
        List<String> fileList = new ArrayList<>();
        //获取用户文件夹下所有文件
        ObjectListing objectList = client.listObjects(bucketName, path);
        for (S3ObjectSummary summary : objectList.getObjectSummaries()) {
            fileList.add(summary.getKey());
        }

        return fileList;
    }

    /**
     * 获取对象的大小
     *
     * @param bucketName 桶名字
     * @param objectName 对象名
     * @return 对象大小，单位byte
     */
    public long getSize(String bucketName, String objectName) {
        ObjectListing objectLis = client.listObjects(bucketName, objectName);

        long size = 0L;
        for (S3ObjectSummary summary : objectLis.getObjectSummaries()) {
            size += summary.getSize();
        }
        System.out.println("正在获取文件夹大小 " + size);
        return size;
    }

    /**
     * 删除对象
     *
     * @param bucketName 桶名字
     * @param objectName 对象名字
     * @return 是否删除成功
     */
    public boolean deleteObject(String bucketName, String objectName) {
        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
        //true是简单模式：只返回删除失败的结果
        //false是详细模式:包括了成功的与失败的结果，默认模式
        List<DeleteObjectsRequest.KeyVersion> list = new ArrayList<>();

        //这里添加多个可实现一次性删除多个对象
        list.add(new DeleteObjectsRequest.KeyVersion(objectName));

        request.setKeys(list);
        DeleteObjectsResult result = client.deleteObjects(request);

        //成功删除的对象
        List<DeleteObjectsResult.DeletedObject> deletedObjects = result.getDeletedObjects();
        deletedObjects.forEach((item) -> System.out.printf("删除了oss中的 %s\n", item));
        return deletedObjects.size() > 0;
    }

    /**
     * 创建文件夹(即创建一个'/'结尾的object)
     *
     * @param bucketName 桶名字
     * @param objectName 对象名
     * @return 是否成功创建
     */
    public boolean createObject(String bucketName, String objectName) throws IOException {
        try (ByteArrayInputStream input = new ByteArrayInputStream(new byte[0])) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            client.putObject(bucketName, objectName, input, metadata);
        }
        //查询有没有这个object，判断是否成功创建
        return client.doesObjectExist(bucketName, objectName);
    }

    /**
     * 拷贝对象，可以用这个实现重命名等功能
     *
     * @param fromBucket 起始对象桶名字
     * @param oldName    起始文件
     * @param toBucket   要拷贝到的对象桶的名字
     * @param newName    对象新名字
     */
    public void copyObject(String fromBucket, String oldName, String toBucket, String newName) {
        client.copyObject(fromBucket, oldName, toBucket, newName);
    }

    /**
     * 下载文件
     *
     * @param bucketName 对象桶名字
     * @param objectName 对象名字
     * @return 文件字节输入流
     */
    public InputStream download(String bucketName, String objectName) {
        //返回的结果中包含了元数据与一个输入流
        S3Object s3Object = client.getObject(bucketName, objectName);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        return inputStream.getDelegateStream();
    }

    /**
     * 简单上传文件,文件小的时候可以用这种方式
     *
     * @param bucketName 对象桶名字
     * @param bytes      要上传的文件的byte数组
     * @param name       上传后对象名字
     * @return 是否上传成功
     */
    public boolean simpleUpload(String bucketName, byte[] bytes, String name) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(bytes.length);
        client.putObject(bucketName, name, new ByteArrayInputStream(bytes), metadata);
        //检测这个文件是否存在,返回结果
        return client.doesObjectExist(bucketName, name);
    }

    public boolean pageUpload(String bucketName, String name, InputStream inputStream) {
        return this.pageUpload(bucketName,name,inputStream,null);
    }
    /**
     * 上传文件
     *
     * @param bucketName  对象桶名字
     * @param name        对象的名字
     * @param inputStream 上传的文件流
     * @param session     用来实现进度条回显
     * @return 是否上传成功
     */
    public boolean pageUpload(String bucketName, String name, InputStream inputStream, HttpSession session) {

        //设置对象大小，如果不设置，由于数据会全部缓存在内存中，可能会将内存耗尽。
        ObjectMetadata meta = new ObjectMetadata();
        long size;

        try {
            size = inputStream.available();
        } catch (IOException e) {
            throw new UploadFileException("获取文件大小失败，请重试", e);
        }
        meta.setContentLength(size);
        double stage = size / 8192.0;
        ExecutorService executorService = Executors.newFixedThreadPool(stage > 50 ? 50 : (int) stage);
//        TransferManager tm = new TransferManager(client, executorService);
        TransferManager tm = TransferManagerBuilder.standard().withS3Client(client)
                .withShutDownThreadPools(true)
                .withExecutorFactory(() -> executorService)
                .build();
        PutObjectRequest request = new PutObjectRequest(bucketName, name, inputStream, meta);

        Upload upload = tm.upload(request);

        if (session != null){
            upload.addProgressListener(new com.amazonaws.event.ProgressListener() {
                double i = 0;
                double j = 0;

                @Override
                public void progressChanged(com.amazonaws.event.ProgressEvent progressEvent) {
                    long transferred = progressEvent.getBytesTransferred();
                    double k = (stage + j);
                    if (transferred > 0) {
                        double percent = (++i) / k;
                        session.setAttribute("uploadPercent", percent * 100);
                    }
                    if (i / k >= 1.0) {
                        j++;
                    }
                }
            });
            session.setAttribute("uploadPercent", 0);
        }

        try {
            UploadResult uploadResult = upload.waitForUploadResult();
            log.info(uploadResult.getKey() + "上传成功");
        } catch (InterruptedException e) {
            throw new UploadFileException("等待上传结果时线程被中断！");
        }

        return true;
    }


}
