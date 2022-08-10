package com.qingyan.raptojson.raptojson.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.httpclient.HttpClientUtils;
import com.qingyan.raptojson.httpclient.HttpResult;
import com.qingyan.raptojson.raptojson.RapParseException;
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
    public String getRapInterfaceByProjectId(String rapUrl, String rapProjectId) throws RapParseException {
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", rapProjectId);
        HttpResult httpResult = HttpClientUtils.doGet(rapUrl + RapApiEnums.api_queryRAPModel.getApi(), params);

        if (httpResult == null) {
            throw new RapParseException("Rap接口返回异常，请确认rap地址正确，服务正常，以及projectId存在");
        }
        String stringEntity = httpResult.getStringEntity();
        if (stringEntity == null || "".equals(stringEntity)) {
            throw new RapParseException("Rap接口返回结果为空");
        }
        JSONObject jsonObject = JSON.parseObject(stringEntity);
        String modelJson = jsonObject.getString("modelJSON");

        if (modelJson == null || "".equals(modelJson)) {
            throw new RapParseException("查询Rap数据失败，请确认rap地址正确，以及projectId存在");
        }

        return modelJson;
    }

    /**
     * 根据Rap 项目Id和url获取项目接口集合
     *
     * @param rapUrl       Rap2Url
     * @param repositoryId Rap2仓库Id
     * @return 接口集合 json
     * @throws RapParseException Rap2接口转换异常
     */
    public JSONObject getRap2InterfaceByProjectId(String rapUrl, String repositoryId, String token)
            throws RapParseException {

        Map<String, Object> params = new HashMap<>(2);
        params.put("id", repositoryId);
        params.put("token", token);

        HttpResult httpResult = HttpClientUtils.doGet(rapUrl + Rap2ApiEnums.api_repository.getApi(), params);

        return handleRap2Result(httpResult);
    }

    /**
     * 根据Rap 项目Id和url获取项目接口集合
     *
     * @param rapDataUrl Rap2Url
     * @return 接口集合 json
     * @throws RapParseException Rap2接口转换异常
     */
    public JSONObject getRap2InterfaceByProjectId(String rapDataUrl) throws RapParseException {

        HttpResult httpResult = HttpClientUtils.doGet(rapDataUrl);

        return handleRap2Result(httpResult);
    }

    /**
     * 处理Rap2获取接口数据
     *
     * @param httpResult 请求结果
     * @return 接口集合 json
     * @throws RapParseException Rap2接口转换异常
     */
    private JSONObject handleRap2Result(HttpResult httpResult) throws RapParseException {
        if (httpResult == null) {
            throw new RapParseException("Rap2接口返回异常，请确认rap地址正确，服务正常，以及id和token的正确");
        }
        String stringEntity = httpResult.getStringEntity();

        if (stringEntity == null || "".equals(stringEntity)) {
            throw new RapParseException("Rap2接口返回结果为空");
        }
        if (httpResult.getStatus() == 500) {
            throw new RapParseException(httpResult.getStringEntity());
        }

        JSONObject jsonObject = JSON.parseObject(stringEntity);

        if (Boolean.FALSE.equals(jsonObject.getBoolean("isOk"))) {
            throw new RapParseException(jsonObject.getString("errMsg"));
        }

        return jsonObject;
    }

}
