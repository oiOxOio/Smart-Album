package com.example.smartalbum.service;

import com.example.smartalbum.domain.Mail;
import com.example.smartalbum.service.database.MailDataService;
import com.example.smartalbum.util.RandomStringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 邮箱服务
 *
 * @author Administrator
 */
@Service
public class MailService {
    @Resource
    private MailDataService mailDataService;
    @Resource
    private RandomStringUtil randomStringUtil;

    /**
     * 生成验证码
     *
     * @return 验证码
     */
    public String getRandomMailCode() {
        return randomStringUtil.createRandomMailCode();
    }

    /**
     * 检测邮箱合法性
     *
     * @return 邮箱格式是否合法
     */
    public boolean checkMailFormat(String mail) {
        //检测邮箱合法性
        String regEx1 = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(mail);
        return m.matches();
    }

    /**
     * 添加验证码到数据库
     *
     * @param mailname  邮箱名字
     * @param emailCode 邮箱验证码
     */
    @Transactional(rollbackFor = Exception.class)
    public void addEmailCode(String mail, String emailCode) {
        Mail email = new Mail();
        email.setMailName(mail);
        email.setMailCode(emailCode);
        mailDataService.insertSelective(email);
    }

    /**
     * 检测验证码是否正确
     *
     * @param mail 电子邮箱
     * @param code 邮箱验证码
     * @return 检测结果
     */
    public boolean hasEmailCode(String mail, String code) {
        //转化为大写
        code = code.toUpperCase(Locale.ROOT);

        return mailDataService.queryMails(mail, code).size() > 0;
    }

    /**
     * 删除邮箱验证码
     *
     * @param mail 邮箱
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean delEmailCode(String mail) {
        return mailDataService.delete(mail) > 0;
    }
}
