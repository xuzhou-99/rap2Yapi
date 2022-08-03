package com.qingyan.raptojson.httpclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * HttpClientProperties
 *
 * @author xuzhou
 * @version v1.0.0
 * @since 2021/7/22 16:35
 */
@Component
@ConfigurationProperties(prefix = "http.client")
public class HttpClientConfig {
    /**
     * 返回从连接管理器请求连接时使用的超时时间（以毫秒为单位）。
     * 默认值： -1，为无限超时。
     */
    private int connectionRequestTimeout = 5000;

    /**
     * 连接超时：连接一个url的连接等待时间
     * 确定建立连接之前的超时时间（以毫秒为单位）。
     * 默认值： -1，为无限超时。
     */
    private int connectTimeout = 5000;

    /**
     * 读取数据超时：连上url，获取response的返回等待时间
     * 以毫秒为单位定义套接字超时，它是等待数据的超时，或者换句话说，两个连续数据包之间的最长不活动时间。
     * 默认值： -1，为无限超时。
     */
    private int socketTimeout = 5000;

    /**
     * 客户端总并行链接最大数
     */
    private int maxTotal = 50;

    /**
     * 客户端每个路由最高链接最大数
     */
    private int maxPreRoute = 4;

    /**
     * 连接存活时长：秒
     */
    private long connectionTimeToLive = 60;

    /**
     * 重试尝试最大次数
     * 默认为3
     */
    private int retryCount = 3;

    /**
     * 非幂等请求是否可以重试
     * 默认不开启
     */
    private boolean requestSentRetryEnabled = false;

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxPreRoute() {
        return maxPreRoute;
    }

    public void setMaxPreRoute(int maxPreRoute) {
        this.maxPreRoute = maxPreRoute;
    }

    public long getConnectionTimeToLive() {
        return connectionTimeToLive;
    }

    public void setConnectionTimeToLive(long connectionTimeToLive) {
        this.connectionTimeToLive = connectionTimeToLive;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isRequestSentRetryEnabled() {
        return requestSentRetryEnabled;
    }

    public void setRequestSentRetryEnabled(boolean requestSentRetryEnabled) {
        this.requestSentRetryEnabled = requestSentRetryEnabled;
    }
}
