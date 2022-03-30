package com.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.log.Log;

import java.util.Map;

/**
 * @author yuanmengfan
 * @date 2021/10/19 2:50 下午
 * @description  http 与 https 连接工具
 */
public class HttpConnectionUtil {

    private static final Log log = Log.get(HttpConnectionUtil.class);

    /**
     * 不带请求头与请求参数的GET请求
     * @param url 请求地址
     * @return
     */
    public static String doGet(String url){
        return getHttpRequest(HttpUtil.createGet(url));
    }

    /**
     * 带请求参数的GET请求
     * @param url 请求地址
     * @param params 请求参数
     * @return
     */
    public static String doGet(String url, Map<String,Object> params){
        return getHttpRequest(HttpUtil.createGet(url).form(params));
    }

    /**
     * 带请求头与请求参数的GET请求
     * @param url 请求地址
     * @param headers 请求头
     * @param params 请求参数
     * @return
     */
    public static String doGet(String url, Map<String,String> headers, Map<String,Object> params){
        return getHttpRequest(HttpUtil.createGet(url).addHeaders(headers).form(params));
    }

    /**
     * 不带请求头与请求参数的POST请求
     * @param url 请求地址
     * @return
     */
    public static String doPost(String url){
        return getHttpRequest(HttpUtil.createPost(url));
    }

    /**
     * 带请求参数的POST请求
     * @param url 请求地址
     * @param params 请求参数
     * @return
     */
    public static String doPost(String url, Map<String,Object> params){
        return getHttpRequest(HttpUtil.createPost(url).form(params));
    }

    /**
     * 带请求头与请求参数的GET请求
     * @param url 请求地址
     * @param headers 请求头
     * @param params 请求参数
     * @return
     */
    public static String doPost(String url, Map<String,String> headers, Map<String,Object> params){
        return getHttpRequest(HttpUtil.createPost( url).addHeaders(headers).form(params));
    }

    private static String getHttpRequest(HttpRequest request){
        log.info("请求URL： "+ request.getUrl());
        log.info("请求头： "+ request.headers());
        log.info("请求参数： "+ request.form());
        long start = System.currentTimeMillis();
        String body = request.execute().body();
        long end = System.currentTimeMillis();
        log.info("耗时： "+ (end - start) +"毫秒" );
        log.info("请求结果： "+ body );
        return body;
    }
}
