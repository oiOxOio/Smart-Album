package com.example.smartalbum.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 返回数据结果工具
 *
 * @author Administrator
 */
public class ResponseMsgUtil {
    /**
     * 创建一个Gson
     */
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("y-MM-dd hh:mm")
            .disableHtmlEscaping()
            .create();

    public static Gson getGson() {
        return GSON;
    }

    public static <T> String builderResponse(int code, String msg, T data) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("msg", msg);
        map.put("data", data);
        return GSON.toJson(map);
    }

    public static String success(String msg) {
        return builderResponse(1, msg, null);
    }

    public static <T> String success(String msg, T data) {
        return builderResponse(1, msg, data);
    }

    public static <T> String success(T data) {
        return builderResponse(1, "Success", data);
    }

    public static String success() {
        return builderResponse(1, "Success", null);
    }

    public static String failure() {
        return builderResponse(-1, "Failure", null);
    }

    public static String failure(String msg) {
        return builderResponse(-1, msg, null);
    }

    public static <T> String failure(T data) {
        return builderResponse(-1, "Failure", data);
    }

    public static <T> String illegalRequest() {
        return builderResponse(1101, "Illegal request", (T) null);
    }

    public static <T> String exception() {
        return builderResponse(1102, "request exception", (T) null);
    }

    public static <T> String paramsEmpty() {
        return builderResponse(1103, "the input parameter is null", (T) null);
    }
}