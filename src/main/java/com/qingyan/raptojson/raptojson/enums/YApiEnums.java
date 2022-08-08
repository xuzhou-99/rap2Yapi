package com.qingyan.raptojson.raptojson.enums;

/**
 * YApi 接口
 * {url：https://hellosean1025.github.io/yapi/openapi.html}
 *
 * @author xuzhou
 * @since 2022/8/3
 */
public enum YApiEnums {

    /**
     * 新增接口分类
     */
    api_addCat("新增接口分类", "/api/interface/add_cat", "POST"),

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

    YApiEnums(String name, String api, String method) {
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
