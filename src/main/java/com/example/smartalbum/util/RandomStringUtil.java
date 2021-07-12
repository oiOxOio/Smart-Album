package com.example.smartalbum.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 生成随机数
 */
@Component
public class RandomStringUtil {

    public final static SecureRandom random = new SecureRandom();

    /**
     * 采用UUID生成随机唯一字符串
     *
     * @return 生成的字符串
     */
    public String createRandomString() {
        //生成65~90或者97~122的数字，对应ASCII的A到z的值
        int n = random.nextBoolean() ? random.nextInt(25) + 65 : random.nextInt(25) + 97;
        char pre = (char) n;
        //清除“-”
        String suf = UUID.randomUUID().toString().replace("-", "");
        return pre + "" + suf;
    }

    /**
     * 创建邮箱验证码
     *
     * @return 生成的字符串
     */
    public String createRandomMailCode() {
        StringBuilder str = new StringBuilder();
        while (str.length() < 5) {
            str.append(random.nextInt(2) == 0 ? random.nextInt(10) : Character.toString((char) (random.nextInt(25) + 65)));
        }
        return str.toString();
    }

}
