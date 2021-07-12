package com.example.smartalbum.exception.hander;

import com.chinamobile.cmss.sdk.ECloudServerException;
import com.example.smartalbum.util.ResponseMsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileExistsException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

/**
 * 处理自定义异常以外的其它异常的异常处理器
 *
 * @author Administrator
 */
@Slf4j
@RestControllerAdvice
public class OtherExceptionHandler {

    /**
     * 404
     */
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public String notFound(NoHandlerFoundException e) {
        log.error("页面丢失或资源找不到,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(404, e.getMessage(), e.getMessage());
    }

    /**
     * 500
     */
//    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
//    @ExceptionHandler(Exception.class)
//    public String serverError(Exception e) {
//        log.error("服务器内部错误,错误信息：" + e.getMessage(), e);
//        return ResponseMsgUtil.builderResponse(500, "服务器内部错误", e.getMessage());
//    }

    /**
     * IO异常
     */
//    @ExceptionHandler(IOException.class)
//    public String ioException(IOException e){
//        log.error("IO异常,错误信息：" + e.getMessage(), e);
//        return ResponseMsgUtil.builderResponse(1001,"服务器内部错误", e.getMessage());
//    }

    /**
     * 运行时异常
     */
//    @ExceptionHandler(RuntimeException.class)
//    public String runtimeException(RuntimeException e){
//        log.error("运行时异常异常,错误信息：" + e.getMessage(), e);
//        return ResponseMsgUtil.builderResponse(1002,"服务器内部错误", e.getMessage());
//    }

    /**
     * sql异常
     */
    @ExceptionHandler(SQLException.class)
    public String sqlException(SQLException e) {
        log.error("SQL异常,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1003, "获取数据失败", e.getMessage());
    }

    /**
     * 缺少Servlet请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String paramException(MissingServletRequestParameterException e) {
        log.error("缺少参数,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1004, "缺少参数:" + e.getParameterName(), e.getMessage());
    }


    /**
     * 上传的文件大小超出限制
     */
    @ExceptionHandler(MultipartException.class)
    public String fileSizeLimit(MultipartException e) {
        log.error("上传的文件大小超出限制,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1005, "超过文件大小限制,最大10MB", e.getMessage());
    }

    /**
     * 下载前对文件名进行编码的过程中报错
     */
    @ExceptionHandler(UnsupportedEncodingException.class)
    public String unsupportedEncodingException(UnsupportedEncodingException e) {
        log.error("下载前对文件名进行编码的过程中报错,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1006, "下载文件失败,请重试！", e.getMessage());
    }

    /**
     * 上传时文件已存在
     */
    @ExceptionHandler(FileExistsException.class)
    public String fileExistedException(FileExistsException e) {
        log.error("上传时文件已存在,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1007, "文件或文件夹已存在,请重试！", e.getMessage());
    }

    /**
     * 参数类型不匹配
     */
    @ExceptionHandler(TypeMismatchException.class)
    public String requestTypeMismatch(TypeMismatchException e) {
        log.error("参数类型不匹配,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1008, "值 " + e.getValue() + " 的参数类型不匹配,参数类型应该为" + e.getRequiredType(), e.getMessage());
    }

    /**
     * 请求方法不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String requestMethod(HttpRequestMethodNotSupportedException e) {
        log.error("请求方式有误,错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1009, "请求方式有误:" + e.getMethod(), e.getMessage());
    }

    /**
     * 移动云的异常
     */
    @ExceptionHandler(ECloudServerException.class)
    public String eCloudServerException(ECloudServerException e) {
        log.error("图片识别,错误信息：" + e.getMessage(), e);
        return e.getMessage();
    }

    /**
     * 邮件发送服务异常
     */
    @ExceptionHandler(MailException.class)
    public String mailException(MailException e) {
        log.error("邮件服务异常，错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1011, e.getMessage(), null);
    }

    /**
     * 线程执行异常
     */
    @ExceptionHandler(ExecutionException.class)
    public String interruptedException(ExecutionException e) {
        log.error("线程异常，错误信息：" + e.getMessage(), e);
        return ResponseMsgUtil.builderResponse(1012, e.getMessage(), null);
    }
}
