package com.qingyan.raptojson.httpclient;

/**
 * http请求返回对象
 *
 * @author xuzhou
 * @version 1.0.0
 */
public class HttpResult {

    /**
     * 状态码
     */
    private Integer status;
    /**
     * 返回数据
     */
    private String stringEntity;

    public HttpResult() {
    }

    /**
     * http请求返回对象
     *
     * @param status       返回状态
     * @param stringEntity 返回数据
     */
    public HttpResult(Integer status, String stringEntity) {
        this.status = status;
        this.stringEntity = stringEntity;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStringEntity() {
        return stringEntity;
    }

    public void setStringEntity(String stringEntity) {
        this.stringEntity = stringEntity;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "status=" + status +
                ", stringEntity='" + stringEntity + '\'' +
                '}';
    }
}