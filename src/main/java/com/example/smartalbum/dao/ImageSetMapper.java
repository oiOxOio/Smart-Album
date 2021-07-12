package com.example.smartalbum.dao;

import com.example.smartalbum.domain.ImageSet;
import com.example.smartalbum.domain.ImageSetExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImageSetMapper {
    long countByExample(ImageSetExample example);

    int deleteByExample(ImageSetExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ImageSet record);

    int insertSelective(ImageSet record);

    List<ImageSet> selectByExample(ImageSetExample example);

    ImageSet selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ImageSet record, @Param("example") ImageSetExample example);

    int updateByExample(@Param("record") ImageSet record, @Param("example") ImageSetExample example);

    int updateByPrimaryKeySelective(ImageSet record);

    int updateByPrimaryKey(ImageSet record);

    List<ImageSet> selectWithImage(Integer id);
}