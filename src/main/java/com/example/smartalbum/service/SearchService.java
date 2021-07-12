package com.example.smartalbum.service;

import com.example.smartalbum.dao.ImageSetMapper;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.Tag;
import com.example.smartalbum.service.database.ImageDataService;
import com.example.smartalbum.service.database.TagDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 搜索服务中心，所有从数据库查询的操作，都进过此类来查询需要的信息
 *
 * @author Administrator
 */
@Service
public class SearchService {

    @Resource
    private ImageSetMapper imageSetMapper;
    @Resource
    private ImageDataService imageDataService;
    @Resource
    private TagDataService tagDataService;

    /**
     * 搜索功能
     *
     * @param tagNameOrImgName 标签名或者图片名
     * @param depositoryId     当前仓库id
     * @return 图片列表
     */
    public List<Image> searchImage(String tagNameOrImgName, int depositoryId) {

        List<Image> images = imageDataService.querySimpleImagesFuzzy(tagNameOrImgName, depositoryId);

        List<Tag> tags = tagDataService.queryTagFuzzy(tagNameOrImgName);

        Set<Integer> imageIdSet = new LinkedHashSet<>();
        for (Tag tag : tags) {
            imageIdSet.add(tag.getImageId());
        }

        for (int imageId : imageIdSet) {
            images.addAll(imageDataService.getSimpleImages(imageId, depositoryId));
        }

        //去重
        return images.stream().distinct().collect(Collectors.toList());
    }

}
