package com.qingyan.raptojson.raptojson.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.qingyan.raptojson.raptojson.pojo.rap1.RapJsonRootBean;
import com.qingyan.raptojson.raptojson.service.RapConvertYapiService;
import com.qingyan.raptojson.raptojson.service.RapApiService;
import com.qingyan.raptojson.response.ApiDataResponse;
import com.qingyan.raptojson.response.ApiResponse;


/**
 * Rap接口转YApi
 *
 * @author xuzhou
 * @since 2022/8/2
 */
@Controller
@RequestMapping("/api/v1/rap")
public class RapConvertYapiController {

    @Value("${url.rap}")
    private String rapUrl;

    @Resource
    private RapConvertYapiService rapConvertYapiService;

    @Resource
    private RapApiService rapApiService;

    /**
     * RAP接口
     **/
    @RequestMapping("/yapi/{rapProjectId}/{projectId}")
    @ResponseBody
    public ApiResponse rap2YApi(@PathVariable String rapProjectId, @PathVariable String projectId) {

        if (rapProjectId == "" || rapProjectId == null) {
            return ApiDataResponse.ofError("查询数据失败，rap projectId不能为空");
        }
        if (projectId == "" || projectId == null) {
            return ApiDataResponse.ofError("查询数据失败，YApi projectId不能为空");
        }

        String modelJson = rapApiService.getRapInterfaceByProjectId(rapUrl, rapProjectId);

        if (modelJson == null) {
            return ApiDataResponse.ofError("查询数据失败，请确认rap地址正确，以及projectId存在");
        }

        RapJsonRootBean rapJsonRootBean = JSON.parseObject(modelJson, RapJsonRootBean.class);
        rapConvertYapiService.rap2JsonGroupByModule(rapJsonRootBean, rapProjectId, projectId, false);
        return ApiDataResponse.ofSuccess(rapJsonRootBean);
    }

    /**
     * Rap 接口转 YApi 导入 json
     *
     * @param rapProjectId rap项目Id
     * @param type         处理类型
     * @return 操作结果
     */
    @RequestMapping("/json/{type}/{rapProjectId}")
    @ResponseBody
    public ApiResponse rapJson(@PathVariable String rapProjectId, @PathVariable(required = false) String type) {

        if (rapProjectId == "" || rapProjectId == null) {
            return ApiDataResponse.ofError("查询数据失败，rap projectId不能为空");
        }

        String modelJson = rapApiService.getRapInterfaceByProjectId(rapUrl, rapProjectId);
        if (modelJson == null) {
            return ApiDataResponse.ofError("查询数据失败，请确认rap地址正确，以及projectId存在");
        }

        RapJsonRootBean rapJsonRootBean = JSON.parseObject(modelJson, RapJsonRootBean.class);
        if (type == null) {
            type = "module";
        }
        List<String> rap2JsonList = rapConvertYapiService.rap2Json(rapJsonRootBean, rapProjectId, "", type, false);
        return ApiDataResponse.ofSuccess("rap 项目 " + rapProjectId + " 转为json文件成功", rap2JsonList);
    }

    /**
     * Rap 接口转 YApi 导入 json（一个json）
     *
     * @param rapProjectId rap项目Id
     * @param type         处理类型
     * @return 操作结果
     */
    @RequestMapping("/json/toOne/{type}/{rapProjectId}")
    @ResponseBody
    public ApiResponse rapJsonToOne(@PathVariable String rapProjectId, @PathVariable(required = false) String type) {

        if (rapProjectId == "" || rapProjectId == null) {
            return ApiDataResponse.ofError("查询数据失败，rap projectId不能为空");
        }

        String modelJson = rapApiService.getRapInterfaceByProjectId(rapUrl, rapProjectId);
        if (modelJson == null) {
            return ApiDataResponse.ofError("查询数据失败，请确认rap地址正确，以及projectId存在");
        }

        RapJsonRootBean rapJsonRootBean = JSON.parseObject(modelJson, RapJsonRootBean.class);
        if (type == null) {
            type = "module";
        }
        List<String> rap2JsonList = rapConvertYapiService.rap2Json(rapJsonRootBean, rapProjectId, "", type, true);
        return ApiDataResponse.ofSuccess("rap 项目 " + rapProjectId + " 转为json文件成功", rap2JsonList);
    }
}
