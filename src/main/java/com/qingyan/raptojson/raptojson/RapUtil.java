package com.qingyan.raptojson.raptojson;

import org.apache.commons.lang3.StringUtils;

/**
 * Rap 接口转换工具类
 *
 * @author xuzhou
 * @since 2022/8/8
 */
public class RapUtil {

    private RapUtil() {
    }

    /**
     * url需要以 '/' 开头
     *
     * @param url url
     * @return 格式化的url
     */
    public static String getUrl(String url) {
        url = url.replace(" ", "")
                .replace("$", "");

        if (url.startsWith("/")) {
            return url;
        } else {
            return "/" + url;
        }
    }

    /**
     * Rap 接口请求方式转 YApi：参数名称（名称 + 备注）
     *
     * @param des    参数名称
     * @param remark 参数备注
     * @return 参数名称
     */
    public static String buildDesAndRemark(String des, String remark) {
        String fullName = des;
        if (StringUtils.isNotEmpty(remark)) {
            fullName = fullName + " (" + remark + ")";
        }
        return fullName;
    }

    /**
     * Rap 接口请求方式转 YApi 接口请求方式
     *
     * @param type Rap 接口请求方式
     * @return YApi 接口请求方式
     */
    public static String convertMethod(String type) {
        if (type == null) {
            return "GET";
        }
        switch (type) {
            case "2":
                return "POST";
            case "3":
                return "PUT";
            case "4":
                return "DELETE";
            default:
                return "GET";
        }
    }

}
