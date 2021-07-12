package com.example.smartalbum.ecloud;

import com.chinamobile.cmss.sdk.ECloudServerException;
import com.chinamobile.cmss.sdk.IECloudClient;
import com.chinamobile.cmss.sdk.request.EngineImageCarDetectPostRequest;
import com.chinamobile.cmss.sdk.request.EngineImageClassifyDetectPostRequest;
import com.chinamobile.cmss.sdk.request.EngineImagePersonDetectPostRequest;
import com.chinamobile.cmss.sdk.response.EngineImageCarDetectResponse;
import com.chinamobile.cmss.sdk.response.EngineImageClassifyDetectResponse;
import com.chinamobile.cmss.sdk.response.EngineImagePersonDetectResponse;
import com.chinamobile.cmss.sdk.response.bean.EngineCar;
import com.chinamobile.cmss.sdk.response.bean.EngineClassify;
import com.chinamobile.cmss.sdk.response.bean.EnginePerson;
import com.chinamobile.cmss.sdk.response.bean.EnginePersonAttrs;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 图像识别
 *
 * @author Administrator
 */
@Service
@Slf4j
public class ImageRecognition {

    @Resource
    private IECloudClient client;
    @Resource
    private Gson gson;

    private static final String RESPONSE_OK = "OK";
    private static final String EXCLUDE_RESPONSE_CODE = "10009999";

    /**
     * 通用图像识别
     *
     * @param base64 Base64编码的图片
     * @return 识别结果
     */
    public List<String> classifyDetect(String base64) {
        EngineImageClassifyDetectPostRequest request = new EngineImageClassifyDetectPostRequest();
        //图片 base64 ，注意不要包含 {data:image/jpeg;base64,}
        request.setImage(base64);
        request.setUserId("");

        EngineImageClassifyDetectResponse response;
        try {
            response = client.call(request);
            log.info("通用物品识别结果：" + gson.toJson(response.getBody()));
        } catch (IOException | IllegalAccessException | ECloudServerException e) {
            if (!e.getMessage().contains(EXCLUDE_RESPONSE_CODE)) {
                throw new ECloudServerException(e.getMessage());
            }
            return null;
        }
        if (RESPONSE_OK.equals(response.getState())) {
            //识别结果
            List<EngineClassify> body = response.getBody();
            //把结果都添加到list
            List<String> list = new LinkedList<>();
            body.forEach((classify) -> {
                String[] split = classify.getClasses().split(",");
                list.addAll(Arrays.asList(split));
            });
            return list;
        }

        return null;
    }

    /**
     * 人体检测与人体属性识别
     *
     * @param base64 Base64编码的图片
     * @return 识别结果
     */
    public List<String> personDetect(String base64) {
        EngineImagePersonDetectPostRequest request = new EngineImagePersonDetectPostRequest();
        //图片 base64 ，注意不要包含 {data:image/jpeg;base64,}
        request.setImage(base64);
        request.setUserId("");

        //获得响应结果
        EngineImagePersonDetectResponse response;
        try {
            response = client.call(request);
            log.info("人体识别结果：" + gson.toJson(response.getBody()));
        } catch (IOException | IllegalAccessException | ECloudServerException e) {
            if (!e.getMessage().contains(EXCLUDE_RESPONSE_CODE)) {
                throw new ECloudServerException(e.getMessage());
            }
            return null;
        }

        //处理响应后的数据
        List<String> tags = null;
        if (RESPONSE_OK.equals(response.getState())) {
            tags = new LinkedList<>();
            tags.add("单人");

            List<EnginePerson> body = response.getBody();

            //行人属性
            List<EnginePersonAttrs> attrs = new LinkedList<>();
            body.forEach((data) -> attrs.add(data.getAttribute()));

            EnginePersonAttrs temp;
            if (attrs.size() > 1) {
                tags.add("多人");
                // temp = new EnginePersonAttrs();
                // TODO: 2021/5/12 如果是多人，数据取交集合并(未完成)
            }
            temp = attrs.get(0);

            addTags(tags, temp);
        }
        return tags;
    }

    /**
     * 筛选数据，把部分数据相似度高于50%的添加进标签列表里
     *
     * @param tags  被添加的标签列表
     * @param attrs 等待筛选的数据
     */
    private static void addTags(List<String> tags, EnginePersonAttrs attrs) {
        BigDecimal baseLimit = new BigDecimal("0.5");
        //年龄描述
        if (attrs.getAge().getScore().compareTo(baseLimit) > 0  && !attrs.getAge().getDesc().contains("未识别")) {
            tags.add(attrs.getAge().getDesc());
        }
        //性别描述
        if (attrs.getGenderCode().getScore().compareTo(baseLimit) > 0 && !attrs.getGenderCode().getDesc().contains("未识别")) {
            tags.add(attrs.getGenderCode().getDesc());
        }
        //头发描述
        if (attrs.getHairStyle().getScore().compareTo(baseLimit) > 0) {
            tags.add(attrs.getHairStyle().getDesc());
        }
        //上衣颜色+裤子颜色
        String coatColor = attrs.getCoatColor().getDesc();
        String pantsColor = attrs.getTrousersColor().getDesc();
        String color = (coatColor.contains("未识别") ? "" : coatColor) + (pantsColor.contains("未识别") ? "" : pantsColor);
        tags.add(color);
    }

    /**
     * 车辆检测与车辆属性识别
     *
     * @param base64 Base64编码的图片
     * @return 识别结果
     */
    public List<String> carDetect(String base64) {
        EngineImageCarDetectPostRequest request = new EngineImageCarDetectPostRequest();
        //图片 base64 ，注意不要包含 {data:image/jpeg;base64,}
        request.setImage(base64);
        request.setUserId("");

        //获得响应结果
        EngineImageCarDetectResponse response;
        try {
            response = client.call(request);
            log.info("车辆识别结果：" + gson.toJson(response.getBody()));
        } catch (IOException | IllegalAccessException | ECloudServerException e) {
            if (!e.getMessage().contains(EXCLUDE_RESPONSE_CODE)) {
                throw new ECloudServerException(e.getMessage());
            }
            return null;
        }

        //处理响应后数据
        List<String> tags = null;
        if (RESPONSE_OK.equals(response.getState())) {
            tags = new LinkedList<>();
            //车辆识别
            EngineCar body = response.getBody();
            //颜色
            tags.add(body.getVehicleColor().getDesc());
            //品牌
            tags.add(body.getVehicleBrand().getDesc());
        }

        return tags;
    }
}

