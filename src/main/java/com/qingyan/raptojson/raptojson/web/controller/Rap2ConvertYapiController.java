package com.qingyan.raptojson.raptojson.web.controller;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.raptojson.service.Rap2ConvertYapiService;
import com.qingyan.raptojson.raptojson.service.RapApiService;
import com.qingyan.raptojson.response.ApiDataResponse;
import com.qingyan.raptojson.response.ApiResponse;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
    private Rap2ConvertYapiService rap2ConvertYapiService;

    @Resource
    private RapApiService rapApiService;

    /**
     * Rap2接口转YApi：生成Swagger导入json
     *
     * @param dataUrl Rap2仓库数据URL：http://IP:PORT/repository/get?id=id&token=token
     * @return 操作结果
     */
    @GetMapping("/json/swagger")
    @ResponseBody
    @ApiOperation("Rap2接口转YApi：生成Swagger导入json")
    public ApiResponse rapSwaggerJsonByDataUrl(@ApiParam(name = "dataUrl", value = "Rap2仓库数据URL", required = true) String dataUrl) {

        if (StringUtils.isEmpty(dataUrl)) {
            return ApiDataResponse.ofError("查询数据失败，Rap2 dataUrl 不能为空");
        }

        try {

            JSONObject rap2Json = rapApiService.getRap2InterfaceByProjectId(dataUrl);
            List<String> rap2JsonList = rap2ConvertYapiService.rapSwaggerJson(rap2Json);
            return ApiDataResponse.ofSuccess("Rap2项目 转为Swagger json文件成功", rap2JsonList);

        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap2接口转 Swagger json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap2接口转 Swagger json 失败：" + eMessage);
        }
    }

    /**
     * Rap2接口转YApi：生成Swagger导入json
     *
     * @param repositoryId rap项目Id
     * @param token        接口token
     * @return 操作结果
     */
    @GetMapping("/json/swagger/{repositoryId}/{token}")
    @ResponseBody
    @ApiOperation("Rap2接口转YApi：生成Swagger导入json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repositoryId", value = "Rap2仓库Id",
                    dataType = "string", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "token", value = "接口token",
                    dataType = "string", dataTypeClass = String.class, required = true)
    })
    public ApiResponse rapSwaggerJson(@PathVariable String repositoryId, @PathVariable String token) {

        try {

            JSONObject rap2Json = rapApiService.getRap2InterfaceByProjectId(rap2Url, repositoryId, token);
            List<String> rap2JsonList = rap2ConvertYapiService.rapSwaggerJson(rap2Json);
            return ApiDataResponse.ofSuccess("Rap2项目 " + repositoryId + " 转为Swagger json文件成功", rap2JsonList);

        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap2接口转 Swagger json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap2接口转 Swagger json 失败：" + eMessage);
        }
    }

    /**
     * Rap2接口转YApi：生成YApi导入json
     *
     * @param dataUrl Rap2仓库数据URL：http://IP:PORT/repository/get?id=id&token=token
     * @return 操作结果
     */
    @GetMapping("/json/yapi")
    @ResponseBody
    @ApiOperation("Rap2接口转YApi：生成YApi导入json")
    public ApiResponse rapYApiJsonByDataUrl(@ApiParam(name = "dataUrl", value = "Rap2仓库数据URL", required = true) String dataUrl) {

        if (StringUtils.isEmpty(dataUrl)) {
            return ApiDataResponse.ofError("查询数据失败，Rap2 dataUrl 不能为空");
        }

        try {

            JSONObject rap2Json = rapApiService.getRap2InterfaceByProjectId(dataUrl);
            List<String> rap2JsonList = rap2ConvertYapiService.rapYApiJson(rap2Json, "");
            return ApiDataResponse.ofSuccess("Rap2项目 转为 YApi json文件成功", rap2JsonList);

        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap2接口转 YApi json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap2接口转 YApi json 失败：" + eMessage);
        }
    }

    /**
     * Rap2接口转YApi：生成YApi导入json
     *
     * @param repositoryId rap项目Id
     * @param token        接口token
     * @return 操作结果
     */
    @GetMapping("/json/yapi/{repositoryId}/{token}")
    @ResponseBody
    @ApiOperation("Rap2接口转YApi：生成YApi导入json")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repositoryId", value = "Rap2仓库Id",
                    dataType = "string", dataTypeClass = String.class, required = true),
            @ApiImplicitParam(name = "token", value = "接口token",
                    dataType = "string", dataTypeClass = String.class, required = true)
    })
    public ApiResponse rapYApiJson(@PathVariable String repositoryId, @PathVariable String token) {

        try {

            JSONObject rap2Json = rapApiService.getRap2InterfaceByProjectId(rap2Url, repositoryId, token);
            List<String> rap2JsonList = rap2ConvertYapiService.rapYApiJson(rap2Json, "");
            return ApiDataResponse.ofSuccess("Rap2项目 " + repositoryId + " 转为YApi json文件成功", rap2JsonList);

        } catch (Exception e) {
            String eMessage = e.getMessage();
            log.error("Rap2接口转 YApi json 失败：{}", eMessage);
            return ApiResponse.ofFail("Rap2接口转 YApi json 失败：" + eMessage);
        }
    }

}
