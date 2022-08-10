package com.qingyan.raptojson.raptojson.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.qingyan.raptojson.raptojson.pojo.rap1.RapJsonRootBean;
import com.qingyan.raptojson.raptojson.service.RapApiService;
import com.qingyan.raptojson.raptojson.service.RapConvertYapiService;
import com.qingyan.raptojson.response.ApiDataResponse;
import com.qingyan.raptojson.response.ApiResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


/**
 * Rap接口转YApi
 *
 * @author xuzhou
 * @since 2022/8/2
 */
@Api
@Slf4j
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
     * Rap 接口转 YApi 导入 json
     *
     * @param rapProjectId rap项目Id
     * @param type         处理类型：module 模块作为分类；page:页面作为接口分类
     * @return 操作结果
     */
    @GetMapping("/json/{type}/{rapProjectId}")
    @ResponseBody
    @ApiOperation("Rap 接口转 YApi 导入 json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "处理类型：module：模块作为分类，page:页面作为接口分类；默认为module",
                    dataType = "string", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "rapProjectId", value = "rap项目Id",
                    dataType = "string", dataTypeClass = String.class, required = true),
    })
    public ApiResponse rapJson(@PathVariable String rapProjectId, @PathVariable String type) {

        try {
            String modelJson = rapApiService.getRapInterfaceByProjectId(rapUrl, rapProjectId);

            RapJsonRootBean rapJsonRootBean = JSON.parseObject(modelJson, RapJsonRootBean.class);
            List<String> rap2JsonList = rapConvertYapiService.rap2Json(rapJsonRootBean, rapProjectId, "", type, false);
            return ApiDataResponse.ofSuccess("rap 项目 " + rapProjectId + " 转为json文件成功", rap2JsonList);
        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap接口转 YApi json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap接口转 YApi json 失败：" + eMessage);
        }
    }

    /**
     * Rap 接口转 YApi 导入 json（一个json）
     *
     * @param rapProjectId rap项目Id
     * @param type         处理类型：module 模块作为分类；page:页面作为接口分类
     * @return 操作结果
     */
    @GetMapping("/json/toOne/{type}/{rapProjectId}")
    @ResponseBody
    @ApiOperation("Rap 接口转 YApi 导入 json（一个json)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "处理类型：module：模块作为分类，page:页面作为接口分类；默认为module",
                    dataType = "string", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "rapProjectId", value = "rap项目Id",
                    dataType = "string", dataTypeClass = String.class, required = true),
    })
    public ApiResponse rapJsonToOne(@PathVariable String rapProjectId, @PathVariable String type) {

        try {
            String modelJson = rapApiService.getRapInterfaceByProjectId(rapUrl, rapProjectId);

            RapJsonRootBean rapJsonRootBean = JSON.parseObject(modelJson, RapJsonRootBean.class);

            List<String> rap2JsonList = rapConvertYapiService.rap2Json(rapJsonRootBean, rapProjectId, "", type, true);
            return ApiDataResponse.ofSuccess("rap 项目 " + rapProjectId + " 转为json文件成功", rap2JsonList);
        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap接口转 YApi json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap接口转 YApi json 失败：" + eMessage);
        }
    }
}
