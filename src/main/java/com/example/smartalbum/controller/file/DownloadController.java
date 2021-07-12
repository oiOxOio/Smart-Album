package com.example.smartalbum.controller.file;

import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.FileDownloadException;
import com.example.smartalbum.service.OssService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 控制文件下载
 *
 * @author Administrator
 */
@Controller
@RequestMapping("/file")
public class DownloadController {

    @Resource
    private OssService ossService;

    /**
     * 文件下载
     *
     * @param filename 文件名
     * @param session  HttpSession
     * @param response HttpServletResponse
     * @throws FileDownloadException 文件下载时异常
     */
    @GetMapping("/download")
    public void downloads(@RequestParam("filename") String filename,
                          HttpSession session,
                          HttpServletResponse response)
            throws FileDownloadException, UnsupportedEncodingException {
        User userInfo = (User) session.getAttribute("userInfo");
        String depositoryName = userInfo.getDepository().getName();
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("multipart/form-data");
        response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(filename, "UTF-8"));

        try (
                InputStream inputStream = ossService.download(depositoryName + "/" + filename);
                OutputStream out = response.getOutputStream()
        ) {
            byte[] buff = new byte[1024];
            int index;
            while ((index = inputStream.read(buff)) != -1) {
                out.write(buff, 0, index);
                out.flush();
            }
        } catch (IOException e) {
            throw new FileDownloadException();
        }
    }
}
