package com.example.smartalbum.service.database;

import com.example.smartalbum.dao.TagMapper;
import com.example.smartalbum.domain.Tag;
import com.example.smartalbum.domain.TagExample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对tag表进行操作
 *
 * @author Administrator
 * @date 2021/6/11 15:00
 */
@Service
public class TagDataService {

    @Resource
    private TagMapper tagMapper;

    /**
     * 可选择性插入自定义图片标签
     * @param tag Tag实体
     * @return 插入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertSelective(Tag tag) {
        return tagMapper.insertSelective(tag) > 0;
    }

    /**
     * 模糊查询tag表
     *
     * @param name    模糊查询的名字
     * @return 所有符合条件的tag列表
     */
    public List<Tag> queryTagFuzzy(String name) {
        TagExample tagExample = new TagExample();
        tagExample.createCriteria().andNameLike("%" + name + "%");
        return tagMapper.selectByExample(tagExample);
    }

    /**
     * 删除标签
     * @param tagId 标签id
     * @return 删除结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTag(int tagId){
        return tagMapper.deleteByPrimaryKey(tagId) > 0;
    }

    public Tag select(Tag tag){
        TagExample tagExample = new TagExample();
        tagExample.createCriteria()
                .andNameEqualTo(tag.getName())
                .andImageIdEqualTo(tag.getImageId());
        return tagMapper.selectByExample(tagExample).get(0);
    }

}
