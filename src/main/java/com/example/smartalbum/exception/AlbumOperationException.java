package com.example.smartalbum.exception;

/**
 * 对相册进行操作时发生的异常
 *
 * @author Administrator
 * @date 2021/6/12 13:36
 */
public class AlbumOperationException extends RuntimeException {
    public AlbumOperationException() {
    }

    public AlbumOperationException(String message) {
        super(message);
    }

    public AlbumOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AlbumOperationException(Throwable cause) {
        super(cause);
    }

    public AlbumOperationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
