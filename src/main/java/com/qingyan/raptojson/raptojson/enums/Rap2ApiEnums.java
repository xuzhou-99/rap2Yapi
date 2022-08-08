package com.qingyan.raptojson.raptojson.enums;

/**
 * YApi 接口
 * {url：https://hellosean1025.github.io/yapi/openapi.html}
 *
 * @author xuzhou
 * @since 2022/8/3
 */
public enum Rap2ApiEnums {

    /**
     * 获取仓库
     * 参数：?id=:repositoryId
     */
    api_repository("获取项目接口", "/repository/get", "GET"),

    /**
     * 获取接口
     * 参数：?id=:interfaceId
     */
    api_interface("获取项目接口", "/interface/get", "GET"),

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

    Rap2ApiEnums(String name, String api, String method) {
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
