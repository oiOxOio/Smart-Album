package com.example.smartalbum.service;

import com.example.smartalbum.dao.ImageMapper;
import com.example.smartalbum.dao.TagMapper;
import com.example.smartalbum.domain.Image;
import com.example.smartalbum.domain.ImageExample;
import com.example.smartalbum.domain.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * @author Administrator
 */
@Service
@Slf4j
public class ImageSetService {

    @Resource
    private TagMapper tagMapper;
    @Resource
    private ImageMapper imageMapper;

    private static DocumentBuilder db = null;

    public final static String PEOPLE_SET_NAME = "人物集";
    public final static String CAR_SET_NAME = "交通工具";
    public final static String SCENERY_SET_NAME = "风景录";
    public final static String BUILDING_SET_NAME = "建筑";
    public final static String ANIMAL_SET_NAME = "动物";

    /**
     * 如果当前图片的标签符合标签库，则计数，如果计数大于这个数，则筛选出这张照片
     */
    private final static int BASE_NUMBER = 3;

    private static final Map<String, String> TAG_MAP = new HashMap<>(4);

     static {
        TAG_MAP.put("people", PEOPLE_SET_NAME);
        TAG_MAP.put("car", CAR_SET_NAME);
        TAG_MAP.put("building", BUILDING_SET_NAME);
        TAG_MAP.put("scenery", SCENERY_SET_NAME);
        TAG_MAP.put("animals", ANIMAL_SET_NAME);
    }

    public Map<String, Integer> matchPhone(int depositoryId , HttpSession session) {
        Map<String, Integer> map = new HashMap<>(4);
        //查出当前用户下的所有图片
        ImageExample imageExample = new ImageExample();
        imageExample.createCriteria().andDepositoryIdEqualTo(depositoryId);
        List<Image> images = imageMapper.selectByExample(imageExample);

        List<Integer> allImageId = new ArrayList<>();
        images.forEach((image) -> allImageId.add(image.getId()));

        Set<Integer> people = new HashSet<>();
        Set<Integer> building = new HashSet<>();
        Set<Integer> scenery = new HashSet<>();
        Set<Integer> car = new HashSet<>();
        Set<Integer> animal = new HashSet<>();
        session.setAttribute(PEOPLE_SET_NAME, people);
        session.setAttribute(CAR_SET_NAME, car);
        session.setAttribute(SCENERY_SET_NAME, scenery);
        session.setAttribute(BUILDING_SET_NAME, building);
        session.setAttribute(ANIMAL_SET_NAME, animal);
        List<Tag> tags;
        for (Integer image : allImageId) {
            tags = tagMapper.selectByImageId(image);
            String s = toIdentify(tags);
            switch (s) {
                case PEOPLE_SET_NAME:
                    map.merge(PEOPLE_SET_NAME, 1, Integer::sum);
                    people.add(image);
                    break;
                case CAR_SET_NAME:
                    map.merge(CAR_SET_NAME, 1, Integer::sum);
                    car.add(image);
                    break;
                case SCENERY_SET_NAME:
                    map.merge(SCENERY_SET_NAME, 1, Integer::sum);
                    scenery.add(image);
                    break;
                case BUILDING_SET_NAME:
                    map.merge(BUILDING_SET_NAME, 1, Integer::sum);
                    building.add(image);
                    break;
                case ANIMAL_SET_NAME:
                    map.merge(ANIMAL_SET_NAME, 1, Integer::sum);
                    animal.add(image);
                    break;
                default:
                    log.info("{}", s);
                    break;
            }
        }
        return map;
    }

    public void load() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public String toIdentify(List<Tag> tag) {
        load();
        NodeList nodelist;
        Document document;
        try {
            document = db.parse("data.xml");
        } catch (SAXException | IOException e) {
            return "转化失败";
        }

        try {
            for (String s : TAG_MAP.keySet()) {
                nodelist = document.getElementsByTagName(s);
                if (filter(tag, nodelist)) {
                    return TAG_MAP.get(s);
                }
            }
        } catch (Exception e) {
            return "无法识别";
        }

        return "筛选结束";
    }

    public boolean filter(List<Tag> tag, NodeList nodelist) {
        String temp;
        for (int i = 0; i < nodelist.getLength(); i++) {
            temp = nodelist.item(i).getTextContent();
            for (Tag value : tag) {
                if (value.getName().contains(temp)) {
                    if (i >= BASE_NUMBER) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
