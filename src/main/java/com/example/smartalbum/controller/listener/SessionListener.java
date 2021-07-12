package com.example.smartalbum.controller.listener;

import com.example.smartalbum.dao.ImageMapper;
import com.example.smartalbum.dao.ImageSetMapper;
import com.example.smartalbum.domain.*;
import com.example.smartalbum.service.ImageSetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class SessionListener implements HttpSessionListener {

    @Resource
    private ImageSetService imageSetService;
    @Resource
    private ImageSetMapper imageSetMapper;
    @Resource
    private ImageMapper imageMapper;

    private NodeList node = null;

    Map<String, Object> imageSetMsg = new HashMap<>();


    public String loadNodeList(String imageSetName) {
        try {
            Document document = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse("data.xml");
            node = document.getElementsByTagName(imageSetName);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("读取data.xml失败",e);
            e.printStackTrace();
        }
        return node.item(0).getTextContent();
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        imageSetMsg.put(ImageSetService.PEOPLE_SET_NAME, "person");
        imageSetMsg.put(ImageSetService.CAR_SET_NAME, "vehicle");
        imageSetMsg.put(ImageSetService.SCENERY_SET_NAME, "landscape");
        imageSetMsg.put(ImageSetService.BUILDING_SET_NAME, "architecture");
        imageSetMsg.put(ImageSetService.ANIMAL_SET_NAME, "animal");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {

        if (se.getSession() == null) {
            return;
        }
        HttpSession session = se.getSession();
        User user = (User) session.getAttribute("userInfo");
        int depositoryId = user.getDepository().getId();

        Map<String, Integer> result = imageSetService.matchPhone(depositoryId, session);

        //销毁session
        HttpSessionListener.super.sessionDestroyed(se);

        Set<String> resultTypes = result.keySet();

        Set<Integer> resultSet;
        List<ImageSet> imageSets;
        List<ImageSet> imgSet1;

        ImageSetExample imageSetExample = new ImageSetExample();
        ImageSetExample iE1 = new ImageSetExample();

        ImageSet imageSet;

        for (String resultType : resultTypes) {
            if (result.get(resultType) >= 10) {
                log.info("{}", session.getAttribute(resultType));

                imageSetExample.createCriteria()
                        .andNameEqualTo(resultType)
                        .andDepositoryIdEqualTo(depositoryId);
                imageSets = imageSetMapper.selectByExample(imageSetExample);
                imageSetExample.clear();

                //如果存在该相册 就 不生成新的
                resultSet = (Set<Integer>) session.getAttribute(resultType);

                if (imageSets.isEmpty()) {

                    String[] imgMsg = loadNodeList((String) imageSetMsg.get(resultType)).split("\n");
                    imageSet = new ImageSet();
                    imageSet.setSummary(imgMsg[1].trim());
                    imageSet.setDetail(imgMsg[2].trim());
                    imageSet.setBackgroundUrl(imgMsg[3].trim());
                    imageSet.setWonderfulUrl(null);
                    imageSet.setDepositoryId(depositoryId);
                    imageSet.setName(resultType);

                    //这些相册名要换 先判断（我这里默认用key）
                    if (imageSetMapper.insertSelective(imageSet) > 0) {

                        iE1.createCriteria().andNameEqualTo(resultType);
                        imgSet1 = imageSetMapper.selectByExample(iE1);
                        iE1.clear();

                        update(depositoryId, imgSet1, resultSet);
                    }
                } else {
                    //对需要改的相册进行修改
                    update(depositoryId, imageSets, resultSet);
                }
            }
        }

    }

    public void update(int depositoryId, List<ImageSet> imageSets, Set<Integer> resultSet) {
        Integer id = imageSets.get(0).getId();
        for (Integer imageId : resultSet) {
            ImageExample imageExample = new ImageExample();
            ImageExample.Criteria criteria1 = imageExample.createCriteria();
            criteria1.andDepositoryIdEqualTo(depositoryId);
            criteria1.andIdEqualTo(imageId);
            Image image = new Image();
            image.setImageSetId(id);
            imageMapper.updateByExampleSelective(image, imageExample);
        }
    }
}
