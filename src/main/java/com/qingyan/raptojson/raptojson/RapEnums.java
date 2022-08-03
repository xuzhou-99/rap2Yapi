package com.qingyan.raptojson.raptojson;

/**
 * YApi 接口
 * {url：https://hellosean1025.github.io/yapi/openapi.html}
 *
 * @author xuzhou
 * @since 2022/8/3
 */
public enum RapEnums {

    /**
     * 获取项目接口
     */
    api_queryRAPModel("获取项目接口", "/api/queryRAPModel.do", "GET"),

    ;

    /**
     * 接口名称
     */
    private final String name;
    /**
     * 接口api
     */
    private final String api;
    /**
     * 请求方式
     */
    private final String method;

    RapEnums(String name, String api, String method) {
        this.name = name;
        this.api = api;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public String getApi() {
        return api;
    }

    public String getMethod() {
        return method;
    }
}
