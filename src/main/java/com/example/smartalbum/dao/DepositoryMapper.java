package com.example.smartalbum.dao;

import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.DepositoryExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepositoryMapper {
    long countByExample(DepositoryExample example);

    int deleteByExample(DepositoryExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Depository record);

    int insertSelective(Depository record);

    List<Depository> selectByExample(DepositoryExample example);

    Depository selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") Depository record, @Param("example") DepositoryExample example);

    int updateByExample(@Param("record") Depository record, @Param("example") DepositoryExample example);

    int updateByPrimaryKeySelective(Depository record);

    int updateByPrimaryKey(Depository record);
}