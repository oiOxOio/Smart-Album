package com.example.smartalbum.exception.hander;

import com.example.smartalbum.exception.*;
import com.example.smartalbum.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 处理自定义异常的异常处理器
 *
 * @author Administrator
 */
@Slf4j
@RestControllerAdvice
public class MyExceptionHandler {

    /**
     * 上传文件时把multipartFile转为byte[]时报错
     */
    @ExceptionHandler(TemporaryFileDoesNotExistException.class)
    public String temporaryFileDoesNotExistException(TemporaryFileDoesNotExistException e) {
        log.error("上传文件时把multipartFile转为byte[]时报错,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3001, "上传文件失败,请重试！", e.getMessage());
    }

    /**
     * 下载文件
     */
    @ExceptionHandler(FileDownloadException.class)
    public String fileDownloadFailedException(FileDownloadException e) {
        log.info("下载文件报错,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3002, "下载文件失败,请重试！", e.getMessage());
    }

    /**
     * 邮箱格式错误
     */
    @ExceptionHandler(EmailFormatException.class)
    public String emailFormatException(EmailFormatException e) {
        log.info("邮箱格式错误,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3003, "邮箱格式错误,请重新输入！", e.getMessage());
    }

    /**
     * 注册的时候，用户已存在
     */
    @ExceptionHandler(UserExistsException.class)
    public String userExistsException(UserExistsException e) {
        log.info("验证用户信息时,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3004, "验证失败,请重试！", e.getMessage());
    }

    /**
     * 上传文件时的异常
     */
    @ExceptionHandler(UploadFileException.class)
    public String uploadFileException(UploadFileException e) {
        log.error("上传文件失败,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3005, "上传文件失败！", e.getMessage());
    }

    /**
     * 图片生成时发生的异常
     */
    @ExceptionHandler(ImageGenerateException.class)
    public String imageGenerateException(ImageGenerateException e) {
        log.error("处理图片失败,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3006, "处理图片失败！", e.getMessage());
    }

    /**
     * 对文件进行操作时产生的异常
     */
    @ExceptionHandler(FileOperationException.class)
    public String fileOperationFailedException(FileOperationException e) {
        log.error("文件操作失败,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(3007, e.getMessage(), null);
    }

    @ExceptionHandler(AlbumOperationException.class)
    public String albumOperationException(AlbumOperationException e) {
        log.info(e.getMessage());
        return ResponseMsgUtil.builderResponse(3008, "操作失败！", e.getMessage());
    }
}
