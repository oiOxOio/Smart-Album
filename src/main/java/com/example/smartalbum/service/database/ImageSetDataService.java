package com.example.smartalbum.service.database;

import com.example.smartalbum.dao.ImageMapper;
import com.example.smartalbum.dao.ImageSetMapper;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.ImageExample;
import com.example.smartalbum.domain.ImageSet;
import com.example.smartalbum.domain.ImageSetExample;
import com.example.smartalbum.exception.AlbumOperationException;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @date 2021/6/12 11:12
 */
@Service
public class ImageSetDataService {

    @Resource
    ImageMapper imageMapper;
    @Resource
    ImageSetMapper imageSetMapper;

    /**
     * 根据主键来删除相册
     *
     * @param imageSetId 相册id
     * @return 操作成功的条数
     */
    public int deleteImageSet(int imageSetId, int depositoryId) {
        ImageSetExample imageSetExample = new ImageSetExample();
        imageSetExample.createCriteria()
                .andIdEqualTo(imageSetId)
                .andDepositoryIdEqualTo(depositoryId);
        return imageSetMapper.deleteByExample(imageSetExample);
    }

    /**
     * 删除相册后,修改对应的image_set_id,根据相册的id来修改
     *
     * @param imageSetId   相册id
     * @param depositoryId 仓库id
     * @return 操作成功的条数
     */
    public int updateImageSetId(int imageSetId, int depositoryId) {
        ImageExample imageExample = new ImageExample();
        imageExample.createCriteria()
                .andImageSetIdEqualTo(imageSetId)
                .andDepositoryIdEqualTo(depositoryId);
        Image image = new Image();
        image.setImageSetId(1);
        return imageMapper.updateByExampleSelective(image, imageExample);
    }

    /**
     * 可选择性的新增相册
     *
     * @param imageSet     imageSet的实例
     * @param depositoryId 仓库id
     * @return 操作成功的条数
     */
    public int insertSelective(ImageSet imageSet, int depositoryId) {
        imageSet.setDepositoryId(depositoryId);
        return imageSetMapper.insertSelective(imageSet);
    }

    /**
     * 修改相册信息，默认修改不了id，depositoryId，createTime等数据
     *
     * @param imageSet     imageSet的实例
     * @param depositoryId 仓库id
     * @return 操作成功的条数
     */
    public int updateImageSet(ImageSet imageSet, int depositoryId) {
        int imageSetId = imageSet.getId();
        imageSet.setId(null);
        imageSet.setDepositoryId(null);
        imageSet.setCreateTime(null);
        imageSet.setUpdateTime(new Date(System.currentTimeMillis()));
        String backgroundUrl = imageSet.getBackgroundUrl();
        imageSet.setBackgroundUrl(backgroundUrl == null ? "" : backgroundUrl);
        ImageSetExample imageSetExample = new ImageSetExample();
        imageSetExample.createCriteria()
                .andDepositoryIdEqualTo(depositoryId)
                .andIdEqualTo(imageSetId);
        return imageSetMapper.updateByExampleSelective(imageSet, imageSetExample);
    }

    /**
     * 在当前用户下的某个相册添加照片
     *
     * @param imagesId     要添加的图片的id集合
     * @param imageSetId   相册id
     * @param depositoryId 仓库id
     * @return 操作成功的条数
     */
    public int addImage(Set<Integer> imagesId, int imageSetId, int depositoryId) {
        int count = 0;
        Image img = new Image();
        img.setImageSetId(imageSetId);
        ImageExample imageExample = new ImageExample();
        for (int imageId : imagesId) {
            imageExample.createCriteria()
                    .andIdEqualTo(imageId)
                    .andDepositoryIdEqualTo(depositoryId);
            count += imageMapper.updateByExampleSelective(img, imageExample);
            imageExample.clear();
        }
        return count;
    }

    /**
     * 在当前相册下移除图片
     *
     * @param imagesId     要添加的图片的id集合
     * @param depositoryId 仓库id
     * @param imageSetId   相册id
     * @return 操作成功的条数
     */
    public int removeImageForImageSet(Set<Integer> imagesId, int depositoryId, int imageSetId) {
        int count = 0;
        Image image = new Image();
        image.setImageSetId(1);
        ImageExample imageExample = new ImageExample();
        for (int imageId : imagesId) {
            imageExample.clear();
            imageExample.createCriteria()
                    .andIdEqualTo(imageId)
                    .andDepositoryIdEqualTo(depositoryId)
                    .andImageSetIdEqualTo(imageSetId);
            count += imageMapper.updateByExampleSelective(image, imageExample);
        }
        return count;
    }

    /**
     * 查询指定仓库中所有相册
     *
     * @param depositoryId depositoryId
     */
    public List<ImageSet> queryImageSet(int depositoryId) {
        ImageSetExample imageSetExample = new ImageSetExample();
        imageSetExample.createCriteria()
                .andDepositoryIdEqualTo(depositoryId)
                .andNameIsNotNull();
        return imageSetMapper.selectByExample(imageSetExample);
    }

    /**
     * 根据相册名字获取id
     *
     * @param name         相册名字
     * @param depositoryId 仓库id
     * @return imageSetId
     */
    public int getImageSetId(String name, int depositoryId) {
        ImageSetExample example = new ImageSetExample();
        example.createCriteria()
                .andNameEqualTo(name)
                .andDepositoryIdEqualTo(depositoryId);

        List<ImageSet> imageSets = imageSetMapper.selectByExample(example);
        if (imageSets.isEmpty()) {
            throw new AlbumOperationException("获取相册数据失败，请重试！");
        }
        return imageSets.get(0).getId();
    }

    /**
     * 根据名字和仓库id获取相册简要信息
     *
     * @param name         相册名字
     * @param depositoryId 仓库id
     */
    public List<ImageSet> getImageSet(String name, int depositoryId) {
        ImageSetExample example = new ImageSetExample();
        example.createCriteria()
                .andNameEqualTo(name)
                .andDepositoryIdEqualTo(depositoryId);
        return imageSetMapper.selectByExample(example);
    }

    public List<ImageSet> getImageSets(int depositoryId){
        ImageSetExample example = new ImageSetExample();
        example.createCriteria().andDepositoryIdEqualTo(depositoryId);
        return imageSetMapper.selectByExample(example);
    }

    /**
     * 带image的image_set查询
     *
     * @return 多表查询结果, 相册的完整信息
     */
    public ImageSet getImageSet(int imageSetId) {
        List<ImageSet> imageSets = imageSetMapper.selectWithImage(imageSetId);
        if (imageSets.isEmpty()) {
            return null;
        }
        return imageSets.get(0);
    }

    /**
     * 获取相册外的图片
     * @return 图片列表
     */
    public List<Image> getImageOutsideImageSet(int imageSetId){

        ImageExample imageExample = new ImageExample();
        imageExample.createCriteria().andImageSetIdNotEqualTo(imageSetId);
        return imageMapper.selectSimpleImageListByExample(imageExample);
    }

    /**
     * 获取相册外的图片-分页
     * @return 分页信息
     */
    public PageInfo<Image> getImageOutsideImageSet(int imageSetId, int pageNum){

        Page<Object> objects = PageHelper.startPage(pageNum, 12);
        List<Image> imageUrls = getImageOutsideImageSet(imageSetId);
        objects.close();
        return new PageInfo<>(imageUrls,5);
    }
}
