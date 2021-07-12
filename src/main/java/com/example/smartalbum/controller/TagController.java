package com.example.smartalbum.controller;

import com.example.smartalbum.domain.Tag;
import com.example.smartalbum.service.database.TagDataService;
import com.example.smartalbum.util.ResponseMsgUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Administrator
 * @date 2021/06/28 23:48
 */
@RestController
public class TagController {

    @Resource
    private TagDataService tagDataService;

    /**
     * 删除标签
     * @param tagId 标签的id
     * @return json串，成功码1，失败码-1
     */
    @PostMapping("/deleteTag")
    public String deleteTag(@RequestParam("tagId") int tagId){
        if (tagDataService.deleteTag(tagId)) {
            return ResponseMsgUtil.success();
        }else {
            return ResponseMsgUtil.failure();
        }
    }

    /**
     * 插入自定义标签
     * @param imageId 图片id
     * @param tagName 标签名字
     * @return json串，成功码1,成功后把id等数据添加到dom树中，失败码-1
     */
    @PostMapping("/addTag")
    public String addTag(@RequestParam("imageId") int imageId,
                         @RequestParam("tagName") String tagName){
        Tag tag = new Tag();
        tag.setImageId(imageId);
        tag.setName(tagName);
        if (tagDataService.insertSelective(tag)) {
            tag = tagDataService.select(tag);
            return ResponseMsgUtil.success(tag);
        }
        return ResponseMsgUtil.failure();
    }

}
