package com.example.smartalbum.controller;

import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.SearchService;
import com.example.smartalbum.util.ResponseMsgUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 搜索当前用户下的图片（模糊查询）
 *
 * @author Administrator
 */
@Controller
public class SearchController {

    @Resource
    private SearchService searchService;

    /**
     * 搜索框功能，根据标签名查询，或图片名字查询
     *
     * @param tagNameOrImgName 搜索框的内容
     * @return JSON
     */
    @ResponseBody
    @GetMapping("/searchImage")
    public String searchImage(@RequestParam(value = "tagNameOrImgName") String tagNameOrImgName,
                              HttpSession session) {

        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();
        List<Image> result = searchService.searchImage(tagNameOrImgName, depositoryId);

        return ResponseMsgUtil.success(result);
    }

}
