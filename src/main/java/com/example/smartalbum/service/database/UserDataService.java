package com.example.smartalbum.service.database;

import com.example.smartalbum.dao.UserMapper;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.domain.UserExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对User表的操作
 *
 * @author Administrator
 * @date 2021/6/11 16:20
 */
@Service
public class UserDataService {

    @Resource
    private UserMapper userMapper;

    /**
     * 从数据库里获取用户的信息
     *
     * @param username 用户名
     * @return 用户信息列表
     */
    public List<User> getUserInfoByName(String username) {
        return userMapper.selectAllByUserName(username);
    }

    /**
     * 可选择性的插入一条数据
     *
     * @param user user的实例
     * @return 操作成功的条数
     */
    public int insertSelective(User user) {
        return userMapper.insertSelective(user);
    }

    public List<User> queryByExample(UserExample example) {
        return userMapper.selectByExample(example);
    }
}
