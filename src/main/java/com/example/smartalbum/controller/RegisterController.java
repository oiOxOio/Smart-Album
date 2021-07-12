package com.example.smartalbum.controller;

import com.example.smartalbum.exception.EmailFormatException;
import com.example.smartalbum.service.MailService;
import com.example.smartalbum.service.UserService;
import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 用户注册
 *
 * @author Administrator
 */
@Controller
public class RegisterController {

    @Resource
    private UserService userService;
    @Resource
    private MailService mailService;
    @Resource
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮箱验证码
     *
     * @param mail 收件邮箱
     */
    @ResponseBody
    @GetMapping("/sendcode")
    public String sendEmailCode(@RequestParam("mail") String mail) {

        // 生成并保存验证码
        String emailCode = mailService.getRandomMailCode();
        mailService.addEmailCode(mail, emailCode);
        // 向邮箱发送验证码
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setSubject("欢迎注册智能云相册！");
        simpleMailMessage.setText("您的注册验证码是：" + emailCode);
        simpleMailMessage.setTo(mail);
        simpleMailMessage.setFrom(from);
        javaMailSender.send(simpleMailMessage);

        return ResponseMsgUtil.success("发送验证码成功！");
    }

    /**
     * 注册
     *
     * @param username 用户名
     * @param password 密码
     * @param mail     电子邮箱
     * @param code     邮箱验证码
     */
    @PostMapping("/register")
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("mail") String mail,
                           @RequestParam("code") String code,
                           HttpServletRequest request,
                           HttpServletResponse response) throws IOException, ServletException {

        if (!mailService.checkMailFormat(mail)) {
            throw new EmailFormatException("邮件格式不合法");
        }
        //检测验证码后，再把验证码删了
        if (mailService.hasEmailCode(mail, code) && mailService.delEmailCode(mail)) {
            //注册成功后进行登录操作
            if (userService.register(username, password, mail)) {
                request.getRequestDispatcher("/login").forward(request, response);
            }
        }
        return "index";
    }
}
