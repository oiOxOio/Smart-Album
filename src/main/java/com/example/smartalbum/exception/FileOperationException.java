package com.example.smartalbum.exception;

/**
 * 对文件系统进行操作时产生的异常，如删除文件，重命名文件，移动文件，创建文件夹等
 *
 * @author Administrator
 * @date 2021/6/2 14:12
 */
public class FileOperationException extends RuntimeException {
    public FileOperationException() {
    }

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileOperationException(Throwable cause) {
        super(cause);
    }

    public FileOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
