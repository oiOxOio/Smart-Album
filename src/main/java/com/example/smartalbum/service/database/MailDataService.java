package com.example.smartalbum.service.database;

import com.example.smartalbum.dao.MailMapper;
import com.example.smartalbum.domain.Mail;
import com.example.smartalbum.domain.MailExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 对mail表的操作
 *
 * @author Administrator
 * @date 2021/6/11 16:38
 */
@Service
public class MailDataService {
    @Resource
    private MailMapper mailMapper;

    /**
     * 查询
     *
     * @param name 名字
     * @param code 验证码
     * @return 符合条件的mail实例的列表
     */
    public List<Mail> queryMails(String name, String code) {
        MailExample example = new MailExample();

        example.createCriteria().andMailNameEqualTo(name).andMailCodeEqualTo(code);

        return mailMapper.selectByExample(example);
    }

    /**
     * 删除数据
     *
     * @param name 名字
     * @return 操作成功的条数
     */
    public int delete(String name) {
        MailExample example = new MailExample();
        example.createCriteria().andMailNameEqualTo(name);
        return mailMapper.deleteByExample(example);
    }

    /**
     * 选择性插入数据
     *
     * @param mail Mail的实例
     * @return 操作成功的条数
     */
    public int insertSelective(Mail mail) {
        return mailMapper.insertSelective(mail);
    }
}
