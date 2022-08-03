package com.qingyan.raptojson.httpclient;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;


import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.httpclient.config.HttpClientConfig;
import com.qingyan.raptojson.spring.SpringContextUtils;


/**
 * http连接工具
 * 不会对请求的结果做处理，用户可以访问 {@link HttpResult}
 * 通过{@linkplain HttpResult#getStatus()}判断响应码code
 * 通过{@linkplain HttpResult#getStringEntity()}获取响应实体字符串
 *
 * @author xuzhou
 * @version v1.0.0
 */
public class HttpClientUtils {

    /**
     * Http客户端
     */
    public static final CloseableHttpClient httpClient;

    /**
     * 配置类
     */
    private static final HttpClientConfig HTTP_CLIENT_CONFIG;

    /**
     * 编码方式
     */
    private static final String ENCODING = "utf-8";

    /**
     * 日志对象
     */
    private static final Logger log = LoggerFactory.getLogger(HttpClientUtils.class);

    /**
     * 请求配置
     */
    private static final RequestConfig REQUEST_CONFIG;

    /**
     * cookies策略
     */
    private static final CookieStore COOKIE_STORE;

    static {

        // cookies
        COOKIE_STORE = new BasicCookieStore();

        // 配置类
        HTTP_CLIENT_CONFIG = SpringContextUtils.getBean(HttpClientConfig.class);

        // 配置请求参数，请求时常，连接市场，读取数据时长
        REQUEST_CONFIG = RequestConfig.custom()
                .setConnectTimeout(HTTP_CLIENT_CONFIG.getConnectTimeout())
                .setConnectionRequestTimeout(HTTP_CLIENT_CONFIG.getConnectionRequestTimeout())
                .setSocketTimeout(HTTP_CLIENT_CONFIG.getSocketTimeout())
                .build();

        // 配置连接池关联
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(HTTP_CLIENT_CONFIG.getMaxTotal());
        connectionManager.setDefaultMaxPerRoute(HTTP_CLIENT_CONFIG.getMaxPreRoute());

        // 初始化客户端
        httpClient = HttpClients.custom()
                // 连接管理器
                .setConnectionManager(connectionManager)
                // 请求配置参数
                .setDefaultRequestConfig(REQUEST_CONFIG)
                // 携带cookies
                .setDefaultCookieStore(COOKIE_STORE)
                // 重试机制
                .setRetryHandler(new DefaultHttpRequestRetryHandler(HTTP_CLIENT_CONFIG.getRetryCount(), HTTP_CLIENT_CONFIG.isRequestSentRetryEnabled()))
                // 开启后台线程清除过期的连接
                .evictExpiredConnections()
                // 开启后台线程清除闲置的连接
                .evictIdleConnections(HTTP_CLIENT_CONFIG.getConnectionTimeToLive(), TimeUnit.SECONDS)
                .build();
    }

    private HttpClientUtils() {

    }

    /**
     * 获取Cookies
     *
     * @return {@link CookieStore}
     */
    public static CookieStore getCookieStore() {
        return COOKIE_STORE;
    }

    /**
     * GET请求
     * 1.支持不带参数的请求
     * 2.支持参数拼接在URl中的请求
     *
     * @param url 请求地址
     * @return 返回值
     */
    public static HttpResult doGet(String url) {
        return doGet(url, null, null);
    }

    /**
     * 带有参数的GET请求
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 返回值
     */
    public static HttpResult doGet(String url, Map<String, Object> params) {
        return doGet(url, params, null);
    }

    /**
     * Get 请求：指定请求头，请求参数
     *
     * @param url     请求地址
     * @param headers 请求头参数
     * @param params  请求参数
     * @return HttpResult
     */
    public static HttpResult doGet(String url, Map<String, Object> params, Map<String, String> headers) {

        log.info("Http GET 请求URL：{}", url);
        log.info("Http GET 请求参数：{}", JSONObject.toJSONString(params));

        try {
            // 创建访问对象地址
            URIBuilder uriBuilder = new URIBuilder(url);
            if (params != null && !params.isEmpty()) {
                // 构建在URL中的请求参数
                Set<? extends Entry<?, ?>> entrySet = params.entrySet();
                for (Entry<?, ?> entry : entrySet) {
                    uriBuilder.addParameter((String) entry.getKey(), String.valueOf(entry.getValue()));
                }
            }

            HttpGet httpGet = new HttpGet(uriBuilder.build().toString());

            // 封装请求头
            packageHeader(headers, httpGet);

            return execute(httpGet);
        } catch (URISyntaxException e) {
            log.error("Get请求构建URL失败", e);
        }
        return null;
    }

    /**
     * 执行POST请求
     *
     * @param url 请求地址
     * @return 返回值
     */
    public static HttpResult doPost(String url) {
        return doPost(url, null, null);
    }

    /**
     * 执行POST请求：有参数
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 返回值
     */
    public static HttpResult doPost(String url, Map<String, Object> params) {
        return doPost(url, params, null);
    }

    /**
     * 执行POST请求
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求参数
     * @return 返回值
     */
    public static HttpResult doPost(String url, Map<String, Object> params, Map<String, String> headers) {

        log.info("Http POST 请求URL：{}", url);
        log.info("Http POST 请求参数：{}", JSONObject.toJSONString(params));

        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);

        try {
            // 封装请求头
            packageHeader(headers, httpPost);

            // 封装请求参数
            packageParam(params, httpPost);
            return execute(httpPost);

        } catch (UnsupportedEncodingException e) {
            log.error("POST请求参数编码异常", e);
        }

        return null;
    }

    /**
     * http post json数据
     *
     * @param url  请求地址
     * @param json 请求参数
     * @return 返回值
     */
    public static HttpResult doPostJson(String url, String json) {
        return doPostJson(url, json, null);
    }


    /**
     * http post json数据
     *
     * @param url     请求地址
     * @param json    请求参数
     * @param headers 请求头
     * @return 返回值
     */
    public static HttpResult doPostJson(String url, String json, Map<String, String> headers) {
        log.info("Http post json请求URL：{}", url);
        log.info("Http post json请求参数：{}", json);
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString()));
        httpPost.setHeader(new BasicHeader(HttpHeaders.ACCEPT, "text/plain;charset=utf-8"));

        // 封装请求头
        packageHeader(headers, httpPost);
        if (json != null) {
            // 构造一个JSON请求的实体
            StringEntity stringEntity = new StringEntity(json, ContentType.APPLICATION_JSON);
            // 将请求实体设置到httpPost对象中
            httpPost.setEntity(stringEntity);
        }
        return execute(httpPost);
    }

    /**
     * http post stream请求
     *
     * @param url 请求地址
     * @param in  输入流
     * @return 返回数据
     */
    public static HttpResult doPostInputStream(String url, InputStream in) {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString()));
        httpPost.setHeader(new BasicHeader(HttpHeaders.ACCEPT, "text/plain;charset=utf-8"));

        if (in != null) {
            httpPost.setEntity(new InputStreamEntity(in));
        }

        return execute(httpPost);
    }

    /**
     * http post text请求
     *
     * @param url  请求地址
     * @param text 文本内容
     * @return 返回数据
     */
    public static HttpResult doPostWrite(String url, String text) {
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_FORM_URLENCODED.toString()));
        httpPost.setHeader(new BasicHeader(HttpHeaders.ACCEPT, "text/plain;charset=utf-8"));

        if (null != text) {
            StringEntity stringEntity = new StringEntity(text, ContentType.TEXT_PLAIN);
            httpPost.setEntity(stringEntity);
        }
        return execute(httpPost);
    }

    /**
     * 执行HTTP请求
     *
     * @param request {@link HttpRequestBase} 请求
     * @return {@link HttpResult} 请求结果
     */
    public static HttpResult execute(HttpRequestBase request) {
        // 执行http请求
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            // 构建返回实体
            return new HttpResult(response.getStatusLine().getStatusCode(),
                    EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            log.error("http 请求异常", e);
        }

        return null;
    }

    /**
     * 将请求参数处理为 NameValuePair
     *
     * @param params 请求参数Map
     * @return List<NameValuePair>
     */
    public static List<NameValuePair> convertParams2NVPS(Map<String, Object> params) {
        if (!params.isEmpty()) {
            List<NameValuePair> parameters = new ArrayList<>();
            params.forEach((key, value) -> parameters.add(new BasicNameValuePair(key, String.valueOf(value))));
            return parameters;
        }
        return Collections.emptyList();
    }

    /**
     * 封装请求头
     *
     * @param headers    请求头参数列表
     * @param httpMethod 请求方式
     */
    public static void packageHeader(Map<String, String> headers, HttpRequestBase httpMethod) {
        if (null != headers) {
            Set<Entry<String, String>> entrySet = headers.entrySet();
            for (Entry<String, String> entry : entrySet) {
                // 设置请求头到 HttpRequestBase 对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 封装请求参数
     *
     * @param params     请求参数
     * @param httpMethod 请求方式
     * @throws UnsupportedEncodingException 不支持字符编码异常
     */
    private static void packageParam(Map<String, Object> params, HttpEntityEnclosingRequest httpMethod)
            throws UnsupportedEncodingException {

        if (null != params) {
            List<NameValuePair> nameValuePairs = convertParams2NVPS(params);
            httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, ENCODING));
        }
    }
}
