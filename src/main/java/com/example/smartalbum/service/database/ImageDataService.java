package com.example.smartalbum.service.database;

import com.example.smartalbum.dao.ImageMapper;
import com.example.smartalbum.dao.TagMapper;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.ImageExample;
import com.example.smartalbum.domain.TagExample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对image表的操作
 *
 * @author Administrator
 * @date 2021/6/11 11:03
 */
@Service
public class ImageDataService {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private ImageMapper imageMapper;

    /**
     * 获取图片的id
     *
     * @param name         图片名字
     * @param depositoryId 图片所在的仓库id
     * @return id
     */
    public int getImageId(String name, int depositoryId) {
        ImageExample example = new ImageExample();
        example.createCriteria()
                .andNameEqualTo(name)
                .andDepositoryIdEqualTo(depositoryId);
        return imageMapper.selectByExample(example).get(0).getId();
    }

    /**
     * 可选择性的插入image表的数据
     *
     * @param image image实例
     * @return 操作成功的条数
     */
    @Transactional(rollbackFor = Exception.class)
    public int insertSelective(Image image) {
        return imageMapper.insertSelective(image);
    }

    /**
     * 删除数据库中的（image和tag）的数据
     *
     * @param depositoryId 个人仓库的id
     * @param filename     文件名字
     * @return 删除的结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteImage(int depositoryId, String filename) {

        ImageExample example = new ImageExample();
        example.createCriteria().andDepositoryIdEqualTo(depositoryId).andNameEqualTo(filename);

        List<Image> images = imageMapper.selectByExample(example);
        if (images.isEmpty()){
            return false;
        }
        int imageId = images.get(0).getId();

        //先删tag表的数据，然后再删image表的数据
        int deleteTagCount = 0;
        TagExample tagExample;
        for (Image image : images) {
            tagExample = new TagExample();
            tagExample.createCriteria().andImageIdEqualTo(image.getId());
            deleteTagCount += tagMapper.deleteByExample(tagExample);
        }

        return imageMapper.deleteByPrimaryKey(imageId) > 0 && deleteTagCount > 0;
    }

    /**
     * 可选择性的更新image表中的数据
     *
     * @param image   要更新的数据实例
     * @param imageId 要更新的图片的id
     * @return 更改了的行数
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateSelective(Image image, int imageId) {
        image.setId(imageId);
        return imageMapper.updateByPrimaryKeySelective(image);
    }

    /**
     * 获取单张图片的数据
     *
     * @param depositoryId 仓库的id
     * @param imageName    图片名字
     * @return Image的实例
     */
    public Image getImage(String imageName, int depositoryId) {
        ImageExample example = new ImageExample();
        example.createCriteria()
                .andDepositoryIdEqualTo(depositoryId)
                .andNameEqualTo(imageName);
        List<Image> images = imageMapper.selectByExample(example);
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * 多表查询（images、tag）图片的完整信息
     *
     * @param depositoryId 个人仓库的id
     * @param name         图片名字
     * @return Image
     */
    public Image getFullImage(String name, int depositoryId) {
        ImageExample example = new ImageExample();
        example.createCriteria().andDepositoryIdEqualTo(depositoryId).andNameEqualTo(name);
        List<Image> images = imageMapper.selectImagesByExample(example);
        return images.isEmpty() ? null : images.get(0);
    }

    /**
     * 多表查询，获取图片列表,只有图片名字、原图url，缩略图url、创建时间和修改时间,和state_id
     */
    public List<Image> getSimpleImages(int depositoryId) {
        ImageExample example = new ImageExample();
        example.createCriteria().andDepositoryIdEqualTo(depositoryId).andStateIdNotEqualTo(3);
        return imageMapper.selectSimpleImageListByExample(example);
    }

    /**
     * 多表查询，获取图片列表,只有图片名字、原图url，缩略图url、创建时间和修改时间,和state_id
     */
    public List<Image> getSimpleImages(int id, int depositoryId) {
        ImageExample example = new ImageExample();
        example.createCriteria().andIdEqualTo(id).andDepositoryIdEqualTo(depositoryId);
        return imageMapper.selectSimpleImageListByExample(example);
    }

    /**
     * 根据名字进行模糊查询
     *
     * @param name         标签名或者图片名
     * @param depositoryId 当前仓库id
     * @return 图片列表
     */
    public List<Image> querySimpleImagesFuzzy(String name, int depositoryId) {

        ImageExample example = new ImageExample();
        example.createCriteria()
                .andNameLike("%" + name + "%")
                .andDepositoryIdEqualTo(depositoryId);

        return imageMapper.selectSimpleImageListByExample(example);
    }


}
