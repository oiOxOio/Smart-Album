package com.example.smartalbum.controller;

import com.example.smartalbum.domain.User;
import com.example.smartalbum.exception.EmailFormatException;
import com.example.smartalbum.exception.UserExistsException;
import com.example.smartalbum.service.UserService;
import com.example.smartalbum.service.database.UserDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 用户登录
 *
 * @author Administrator
 */
@Controller
public class LoginController {

    @Resource
    private UserService userService;
    @Resource
    private UserDataService userDataService;

    /**
     * 验证用户名
     *
     * @param username 用户名
     */
    @ResponseBody
    @GetMapping("/doesUserExist")
    public String doesUserExist(@RequestParam("username") String username) throws EmailFormatException {
        if ((userService.hasUserName(username) || userService.hasMail(username, false))) {
            return ResponseMsgUtil.success("已存在该用户，可进行登录操作");
        } else {
            return ResponseMsgUtil.failure("不存在该用户，可进行注册操作");
        }
    }

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     */
    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        HttpSession httpSession) {

        //检测用户名和密码是否正确
        if (!userService.checkUser(username, password)) {
            throw new UserExistsException("密码错误，或用户不存在！");
        }
        List<User> list = userDataService.getUserInfoByName(username);

        httpSession.setAttribute("userInfo", list.get(0));
        return "redirect:/userMain";
    }

    /**
     * 退出登录状态、使本次连接的session无效
     *
     * @param session HttpSession
     */
    @RequestMapping("/signOut")
    public String signOut(HttpSession session) {
        session.invalidate();
        return "index";
    }
}
