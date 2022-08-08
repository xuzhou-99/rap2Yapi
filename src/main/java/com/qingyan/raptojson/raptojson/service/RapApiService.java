package com.qingyan.raptojson.raptojson.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.httpclient.HttpClientUtils;
import com.qingyan.raptojson.httpclient.HttpResult;
import com.qingyan.raptojson.raptojson.enums.Rap2ApiEnums;
import com.qingyan.raptojson.raptojson.enums.RapApiEnums;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/8/5
 */
@Slf4j
@Service
public class RapApiService {


    /**
     * 根据Rap 项目Id和url获取项目接口集合
     *
     * @param rapUrl       RapUrl
     * @param rapProjectId Rap项目Id
     * @return 接口集合 json string
     */
    public String getRapInterfaceByProjectId(String rapUrl, String rapProjectId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", rapProjectId);
        HttpResult httpResult = HttpClientUtils.doGet(rapUrl + RapApiEnums.api_queryRAPModel.getApi(), params);

        if (httpResult == null) {
            return null;
        }
        String stringEntity = httpResult.getStringEntity();

        JSONObject jsonObject = JSON.parseObject(stringEntity);
        return jsonObject.getString("modelJSON");
    }

    /**
     * 根据Rap 项目Id和url获取项目接口集合
     *
     * @param rapUrl       Rap2Url
     * @param repositoryId Rap2仓库Id
     * @return 接口集合 json string
     */
    public String getRap2InterfaceByProjectId(String rapUrl, String repositoryId, String token) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", repositoryId);
        params.put("token", token);

        HttpResult httpResult = HttpClientUtils.doGet(rapUrl + Rap2ApiEnums.api_repository.getApi(), params);

        if (httpResult == null) {
            throw new RuntimeException("接口返回结果为空");
        }
        if (httpResult.getStatus() == 500) {
            throw new RuntimeException(httpResult.getStringEntity());
        }
        String stringEntity = httpResult.getStringEntity();

        if (stringEntity == null || stringEntity == "") {
            throw new RuntimeException("接口返回结果为空");
        }

        JSONObject jsonObject = JSON.parseObject(stringEntity);
        if (Boolean.FALSE.equals(jsonObject.getBoolean("isOk"))) {
            throw new RuntimeException(jsonObject.getString("errMsg"));
        }
        return stringEntity;
    }

}
