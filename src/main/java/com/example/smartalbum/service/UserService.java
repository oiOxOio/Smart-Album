package com.example.smartalbum.service;

import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.domain.UserExample;
import com.example.smartalbum.exception.EmailFormatException;
import com.example.smartalbum.exception.UserExistsException;
import com.example.smartalbum.service.database.DepositoryDataService;
import com.example.smartalbum.service.database.UserDataService;
import com.example.smartalbum.util.RandomStringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 验证账号，账号注册，获取用户信息
 *
 * @author Administrator
 */
@Service
public class UserService {

    @Resource
    private UserService userService;
    @Resource
    private MailService mailService;
    @Resource
    private OssService ossService;
    @Resource
    private UserDataService userDataService;
    @Resource
    private DepositoryDataService depositoryDataService;
    @Resource
    private RandomStringUtil randomStringUtil;

    @Value("${app.maxDepositorySize}")
    private String maxDepositorySize;

    /**
     * 判断是否存在同名账号
     *
     * @param username 用户名
     * @return 检测结果
     */
    public boolean hasUserName(String username) {
        UserExample example = new UserExample();
        example.createCriteria().andUsernameEqualTo(username);
        return userDataService.queryByExample(example).size() > 0;
    }

    /**
     * 从数据库检测是否存在这个邮箱
     *
     * @param mail        邮箱
     * @param checkFormat 是否检测邮箱合法性
     * @return 邮箱是否存在
     */
    public boolean hasMail(String mail, boolean checkFormat) {
        UserExample example = new UserExample();
        example.createCriteria().andMailEqualTo(mail);
        if (checkFormat) {
            //检测邮箱合法性
            if (mailService.checkMailFormat(mail)) {
                return userDataService.queryByExample(example).size() > 0;
            } else {
                throw new EmailFormatException("邮件格式不合法");
            }
        } else {
            return userDataService.queryByExample(example).size() > 0;
        }
    }

    /**
     * 验证账号
     *
     * @param username 用户名或者邮箱
     * @param password 密码
     * @return 验证结果
     */
    public boolean checkUser(String username, String password) {
        UserExample example = new UserExample();
        //检测邮箱格式
        if (mailService.checkMailFormat(username)) {
            example.createCriteria().andMailEqualTo(username).andPasswordEqualTo(password);
        } else {
            example.createCriteria().andUsernameEqualTo(username).andPasswordEqualTo(password);
        }
        return userDataService.queryByExample(example).size() > 0;
    }

    /**
     * 注册,注册的时候生成随机字符串，把这个字符串当做用户的根目录
     *
     * @param username 用户名
     * @param password 密码
     * @param mail     邮箱
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean register(String username, String password, String mail) throws IOException, EmailFormatException {
        if (userService.hasUserName(username)) {
            throw new UserExistsException("用户名已存在，请重新注册");
        }
        if (userService.hasMail(mail, false)) {
            throw new UserExistsException("邮箱已被绑定，请重新注册");
        }
        String depositoryName = randomStringUtil.createRandomString();

        Depository depository = new Depository();
        depository.setName(depositoryName);
        depository.setSize("0");
        depository.setSizeMax(maxDepositorySize);

        //用户仓库（用户根目录）
        if (depositoryDataService.insertSelective(depository) > 0) {
            if (ossService.checkFile(depositoryName + "/")) {
                throw new RuntimeException("初始化用户个人空间失败，请重新注册");
            }

            //获取刚创建的仓库id
            int depositoryId = depositoryDataService.getDepositoryId(depositoryName);

            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setMail(mail);
            user.setDepositoryId(depositoryId);
            user.setDepository(depository);

            userDataService.insertSelective(user);

            //创建个人文件夹,创建成功后向数据库插入数据
            return ossService.createFolder(depositoryName + "/");
        }
        return false;
    }
}
