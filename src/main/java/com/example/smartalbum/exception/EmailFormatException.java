package com.example.smartalbum.exception;

/**
 * 邮箱格式错误
 */
public class EmailFormatException extends RuntimeException {
    public EmailFormatException() {
    }

    public EmailFormatException(String message) {
        super(message);
    }

    public EmailFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailFormatException(Throwable cause) {
        super(cause);
    }

    public EmailFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
