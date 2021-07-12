package com.example.smartalbum.service.database;

import com.example.smartalbum.dao.DepositoryMapper;
import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.DepositoryExample;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对depository表的操作
 *
 * @author Administrator
 * @date 2021/6/11 16:22
 */
@Service
public class DepositoryDataService {
    @Resource
    private DepositoryMapper depositoryMapper;

    /**
     * 根据仓库名字来获取仓库的Id
     */
    public int getDepositoryId(String name) {
        DepositoryExample example = new DepositoryExample();
        example.createCriteria().andNameEqualTo(name);
        List<Depository> depositories = depositoryMapper.selectByExample(example);
        return depositories.get(0).getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertSelective(Depository depository) {
        return depositoryMapper.insertSelective(depository);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateSelective(Depository depository, String depositoryName) {
        DepositoryExample example = new DepositoryExample();
        example.createCriteria().andNameEqualTo(depositoryName);
        return depositoryMapper.updateByExampleSelective(depository, example);
    }
}
