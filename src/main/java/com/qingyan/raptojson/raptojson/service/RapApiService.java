package com.qingyan.raptojson.raptojson.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.httpclient.HttpClientUtils;
import com.qingyan.raptojson.httpclient.HttpResult;
import com.qingyan.raptojson.raptojson.Rap2ApiEnums;
import com.qingyan.raptojson.raptojson.RapApiEnums;

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
     * @param rapUrl       RapUrl
     * @param repositoryId Rap项目Id
     * @return 接口集合 json string
     */
    public String getRap2InterfaceByProjectId(String rapUrl, String repositoryId) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("id", repositoryId);

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "_yapi_uid=11; koa.sid=hWpktmskQTDzZuApMeljL2cYkxouG1SW; koa.sid.sig=Fbhd-20JRmGkyA8N3b-BLH_Clo8; _yapi_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1aWQiOjExLCJpYXQiOjE2NTk2NzkyODcsImV4cCI6MTY2MDI4NDA4N30.G_W0khuORE1UoFCJoTNK7SS-SdLJV9U1KZLCNmJalFM");
        HttpResult httpResult = HttpClientUtils.doGet(rapUrl + Rap2ApiEnums.api_repository.getApi(), params, headers);

        if (httpResult == null) {
            throw new RuntimeException("接口返回结果为空");
        }
        String stringEntity = httpResult.getStringEntity();

        if (stringEntity == null || stringEntity =="") {
            throw new RuntimeException("接口返回结果为空");
        }

        JSONObject jsonObject = JSON.parseObject(stringEntity);
        if (Boolean.FALSE.equals(jsonObject.getBoolean("isOk"))) {
            throw new RuntimeException(jsonObject.getString("errMsg"));
        }
        return stringEntity;
    }

}
