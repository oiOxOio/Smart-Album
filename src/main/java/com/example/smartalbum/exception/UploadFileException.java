package com.example.smartalbum.exception;

/**
 * 上传文件时报错<br>
 * 发生以下异常时可能会抛出此异常<br>
 * 1、上传文件时，上传的文件是空的，从而导致inputStream.available()获取大小时报错<br>
 * 2、上传的文件格式不对<br>
 * 3、容量不足<br>
 * 4、分片上传时默认采用多线程并发上传，使用到了future.get()获得计算结果时<br>
 * <li>如果中途不明原因取消了计算</li>
 * <li>如果计算过程中引发其它异常</li>
 * <li>如果当前线程在等待时被中断</li>
 *
 * @author Administrator
 */
public class UploadFileException extends RuntimeException {
    public UploadFileException() {
    }

    public UploadFileException(String message) {
        super(message);
    }

    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public UploadFileException(Throwable cause) {
        super(cause);
    }

    public UploadFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
