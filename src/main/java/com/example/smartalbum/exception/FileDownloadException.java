package com.example.smartalbum.exception;

import java.io.IOException;

/**
 * 在文件下载时以下操作可能会抛出异常
 * <li>在response.getOutputStream()前调用了在response.getWriter()</li>
 * <li>inputSteam已经调用了close()方法，或inputStream写在了try-with-resources语句中</li>
 * <li>存储空间不足，调用flush()方法无法将缓冲区的东西写出去</li>
 */
public class FileDownloadException extends IOException {

    public FileDownloadException() {
        super();
    }

    public FileDownloadException(String message) {
        super(message);
    }

    public FileDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileDownloadException(Throwable cause) {
        super(cause);
    }
}
