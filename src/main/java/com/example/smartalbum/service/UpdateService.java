package com.example.smartalbum.service;

import com.example.smartalbum.domain.Depository;
import com.example.smartalbum.domain.User;
import com.example.smartalbum.service.database.DepositoryDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 数据更新服务
 *
 * @author Administrator
 */
@Service
public class UpdateService {
    @Resource
    private OssService ossService;
    @Resource
    private DepositoryDataService depositoryDataService;

    /**
     * 更新数据库数据（depository的size）和 session中的数据
     *
     * @param session HttpSession
     */
    public void updateUserInfo(HttpSession session) {
        User userInfo = (User) session.getAttribute("userInfo");

        String depositoryName = userInfo.getDepository().getName();

        //云端size
        long size = ossService.getDepositorySize(depositoryName);

        Depository depository = new Depository();
        depository.setId(userInfo.getDepository().getId());
        depository.setName(userInfo.getDepository().getName());
        depository.setSize(size + "");
        depository.setSizeMax(userInfo.getDepository().getSizeMax());

        //更新数据库中的size
        depositoryDataService.updateSelective(depository, depositoryName);

        //更新session中的userInfo
        userInfo.setDepository(depository);
        session.setAttribute("userInfo", userInfo);
    }
}
