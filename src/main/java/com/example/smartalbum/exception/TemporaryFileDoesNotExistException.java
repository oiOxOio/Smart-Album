package com.example.smartalbum.exception;

import java.io.IOException;

/**
 * 出现以下情况可能会报错<br>
 * 如multipartFile.getBytes()操作时
 * <li>读取过程中清理了内存</li>
 * <li>读取的过程中删除或者移动临时文件</li>
 * <li>读取后的文件大小与原始文件不同</li>
 *
 * @author Administrator
 */
public class TemporaryFileDoesNotExistException extends IOException {

    public TemporaryFileDoesNotExistException() {
        super();
    }

    public TemporaryFileDoesNotExistException(String message) {
        super(message);
    }

    public TemporaryFileDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemporaryFileDoesNotExistException(Throwable cause) {
        super(cause);
    }
}
