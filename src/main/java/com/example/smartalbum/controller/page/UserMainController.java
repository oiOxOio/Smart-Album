package com.example.smartalbum.controller.page;


import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Controller
public class UserMainController {

    @Resource
    private ImageDataService imageDataService;

    @GetMapping("/userMain")
    public String userMain(Model model, HttpSession session) {
        //仓库id
        User user = (User) session.getAttribute("userInfo");

        List<Image> fileList = imageDataService.getSimpleImages(user.getDepository().getId());

        //把用户信息放入model
        putUserinfoIntoTheModel(model, session);

        model.addAttribute("fileList", fileList);
        return "userMain";
    }

    /**
     * 获取用户的信息
     */
    @ResponseBody
    @GetMapping("/getUserInfo")
    private String getUserInfo(HttpSession session) {
        return ResponseMsgUtil.success(getProcessedUserInfo(session));
    }

    /**
     * 处理session中的用户信息，并放入map中
     *
     * @param session 要提取的session
     * @return 用户信息
     */
    public static Map<String, Object> getProcessedUserInfo(HttpSession session) {

        //用户所有信息
        User user = (User) session.getAttribute("userInfo");

        //仓库信息
        long depositorySize = Long.parseLong(user.getDepository().getSize());
        long depositoryMaxSize = Long.parseLong(user.getDepository().getSizeMax());

        //转化 MB 为单位
        double size = depositorySize / 1024.0 / 1024.0;
        double maxSize = depositoryMaxSize / 1024.0 / 1024.0;
        //进度条
        double percentage = 1.0 * depositorySize / depositoryMaxSize * 100;

        Map<String, Object> map = new LinkedHashMap<>(6);
        map.put("username", user.getUsername());
        map.put("mail", user.getMail());
        map.put("depositorySize", String.format("%.2f", size));
        map.put("depositoryMaxSize", maxSize);
        map.put("percentage", String.format("%.2f", percentage));
        map.put("registerDate", user.getRegisterDate());

        return map;
    }

    /**
     * 把session中的用户信息放入model中
     *
     * @param model   要放入的model
     * @param session 当前session
     */
    public static void putUserinfoIntoTheModel(Model model, HttpSession session) {
        Map<String, Object> userinfo = UserMainController.getProcessedUserInfo(session);

        model.addAttribute("username", userinfo.get("username"));
        model.addAttribute("size", userinfo.get("depositorySize"));
        model.addAttribute("maxSize", userinfo.get("depositoryMaxSize"));
        model.addAttribute("percentage", userinfo.get("percentage"));
    }

}
