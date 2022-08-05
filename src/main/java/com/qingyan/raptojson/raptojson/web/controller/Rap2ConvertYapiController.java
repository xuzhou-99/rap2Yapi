package com.qingyan.raptojson.raptojson.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.raptojson.pojo.rap1.RapJsonRootBean;
import com.qingyan.raptojson.raptojson.service.Rap2ConvertYapiService;
import com.qingyan.raptojson.raptojson.service.RapConvertYapiService;
import com.qingyan.raptojson.raptojson.service.RapApiService;
import com.qingyan.raptojson.response.ApiDataResponse;
import com.qingyan.raptojson.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;


/**
 * Rap接口转YApi
 *
 * @author xuzhou
 * @since 2022/8/2
 */
@Slf4j
@Controller
@RequestMapping("/api/v1/rap2")
public class Rap2ConvertYapiController {

    @Value("${url.rap2}")
    private String rap2Url;

    @Resource
    private RapConvertYapiService rapConvertYapiService;

    @Resource
    private Rap2ConvertYapiService rap2ConvertYapiService;

    @Resource
    private RapApiService rapApiService;

    /**
     * Rap 接口转 YApi 导入 json
     *
     * @param repositoryId rap项目Id
     * @return 操作结果
     */
    @RequestMapping("/swagger/json/{repositoryId}")
    @ResponseBody
    public ApiResponse rapSwaggerJson(@PathVariable String repositoryId) {

        try {

            if (repositoryId == "" || repositoryId == null) {
                return ApiDataResponse.ofError("查询数据失败，rap projectId不能为空");
            }

            String rap2JsonStr = rapApiService.getRap2InterfaceByProjectId(rap2Url, repositoryId);

            JSONObject rap2Json = JSON.parseObject(rap2JsonStr);
            List<String> rap2JsonList = rap2ConvertYapiService.rapSwaggerJson(rap2Json);
            return ApiDataResponse.ofSuccess("rap2 项目 " + repositoryId + " 转为Swagger json文件成功", rap2JsonList);

        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap2接口转 json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap2接口转 json 失败：" + eMessage);
        }
    }


    /**
     * Rap 接口转 YApi 导入 json
     *
     * @param repositoryId rap项目Id
     * @param type         处理类型
     * @return 操作结果
     */
    @RequestMapping("/yapi/json/{repositoryId}")
    @ResponseBody
    public ApiResponse rapYApiJson(@PathVariable String repositoryId) {
        try {

            if (repositoryId == "" || repositoryId == null) {
                return ApiDataResponse.ofError("查询数据失败，rap projectId不能为空");
            }

            String rap2JsonStr = rapApiService.getRap2InterfaceByProjectId(rap2Url, repositoryId);

            JSONObject rap2Json = JSON.parseObject(rap2JsonStr);
            List<String> rap2JsonList = rap2ConvertYapiService.rapYApiJson(rap2Json,"","");
            return ApiDataResponse.ofSuccess("rap2 项目 " + repositoryId + " 转为Swagger json文件成功", rap2JsonList);

        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap2接口转 json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap2接口转 json 失败：" + eMessage);
        }
    }

}
