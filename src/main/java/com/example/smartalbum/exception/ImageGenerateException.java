package com.example.smartalbum.exception;

import java.io.IOException;

/**
 * 上传图片时或者处理图片时(如缩略图等)发生的异常<br>
 * 以下操作可能触发
 * <li>1、ImageIO.read(inputStream)时，如果输入流inputStream为空，或者读取的过程中出错</li>
 * <li>2、使用外部依赖对BufferedImage进行处理时</li>
 * <li>3、使用ImageIO.write()写出缓冲区的数据时，如果任何参数为null，或者在写入OutputStream的过程中发生错误</li>
 */
public class ImageGenerateException extends IOException {
    public ImageGenerateException() {
    }

    public ImageGenerateException(String message) {
        super(message);
    }

    public ImageGenerateException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImageGenerateException(Throwable cause) {
        super(cause);
    }
}
