package com.qingyan.raptojson.raptojson.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.raptojson.RapParseException;
import com.qingyan.raptojson.raptojson.RapUtil;
import com.qingyan.raptojson.raptojson.enums.JsonConvertTypeEnum;
import com.qingyan.raptojson.raptojson.pojo.rap2.Data;
import com.qingyan.raptojson.raptojson.pojo.rap2.Interfaces;
import com.qingyan.raptojson.raptojson.pojo.rap2.Modules;
import com.qingyan.raptojson.raptojson.pojo.rap2.Properties;
import com.qingyan.raptojson.raptojson.pojo.rap2.Rap2JsonRootBean;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/8/5
 */
@Slf4j
@Service
public class Rap2ConvertYapiService {

    /**
     * 参数根节点Id
     */
    private static final int PARAM_ROOT_ID = -1;

    @Value("${json.rootPath}")
    private String jsonRootPath;

    @Resource
    private SaveJsonService saveJsonService;


    /**
     * Rap2接口转为Swagger json
     *
     * @param rap2Json Rap2接口
     * @return 转换后文件地址
     * @throws RapParseException Rap接口转换异常
     */
    public List<String> rapSwaggerJson(JSONObject rap2Json) throws RapParseException {
        List<String> fileList = new ArrayList<>();

        if (rap2Json == null) {
            log.error("原始json不能为空");
            throw new RapParseException("Rap2 原始json为空");
        }

        JSONObject data = rap2Json.getJSONObject("data");

        JSONArray modules = data.getJSONArray("modules");
        String name = data.getString("name");
        String description = data.getString("description");
        log.info("Rap2接口转 Swagger json：开始处理 {} 项目", name);

        // 构建Swagger json
        JSONObject swagger = new JSONObject();
        swagger.put("swagger", "2.0");

        JSONObject info = new JSONObject();
        info.put("title", name);
        info.put("description", description);
        swagger.put("info", info);

        // 分类
        JSONArray tags = parseTags(modules);
        swagger.put("tags", tags);

        // 接口
        JSONObject paths = parsePaths(modules);
        swagger.put("paths", paths);

        // 请求、响应参数
        JSONObject definitions = parseDefinitions(modules);
        swagger.put("definitions", definitions);

        // 存放构建的json 文件
        String jsonFile = saveJsonService.writeToJsonFile(jsonRootPath, swagger, "swagger_" + name,
                name, JsonConvertTypeEnum.PROJECT_TO_PROJECT.getTypeName(), "");
        fileList.add(jsonFile);

        return fileList;
    }

    /**
     * 解析tag
     *
     * @param modules 模块
     * @return tag集合
     */
    private JSONArray parseTags(JSONArray modules) {
        JSONArray tags = new JSONArray();
        for (Object m : modules) {
            JSONObject module = JSON.parseObject(JSON.toJSONString(m));
            JSONObject tag = new JSONObject();
            tag.put("name", module.getString("name"));
            tag.put("description", module.getString("description"));
            tags.add(tag);
            log.info("Rap2接口转 Swagger json：模块 {} ", module.getString("name"));
        }
        return tags;
    }

    /**
     * 解析接口
     *
     * @param modules 模块
     * @return 接口集合
     */
    private JSONObject parsePaths(JSONArray modules) {
        JSONObject paths = new JSONObject();

        for (Object m : modules) {
            JSONObject module = JSON.parseObject(JSON.toJSONString(m));
            JSONArray interfaces = module.getJSONArray("interfaces");
            String moduleName = module.getString("name");
            log.info("Rap2接口转 Swagger json：【接口】模块 {} ", moduleName);

            for (Object i : interfaces) {
                JSONObject inter = JSON.parseObject(JSON.toJSONString(i));
                String interMethod = inter.getString("method").toLowerCase();
                log.info("Rap2接口转 Swagger json：【接口】模块 {} ，接口 {}，url {}",
                        moduleName, inter.getString("name"), inter.getString("url"));

                JSONObject path = new JSONObject();
                JSONObject method = new JSONObject();

                // JSON内容循环会因为已有对象进行引用对应的地址，可以手动在序列化的时候关闭
                // 关闭FastJson循环引用 SerializerFeature.DisableCircularReferenceDetect
                JSONArray tagArray = new JSONArray();
                tagArray.add(moduleName);
                log.info("tagArray：{}", JSON.toJSONString(tagArray));
                method.put("tags", tagArray);

                method.put("summary", inter.getString("name"));
                if ("post".equals(interMethod)) {
                    method.put("consumes", Collections.singletonList("multipart/form-data"));
                }
                method.put("description", inter.getString("description"));
                method.put("parameters", parseParameters(inter.getJSONArray("properties"), interMethod));
                method.put("deprecated", false);

                JSONObject responses = new JSONObject();
                JSONObject responsesOk = new JSONObject();
                JSONObject schema = new JSONObject();
                schema.put("$ref", "#/definitions/Response" + inter.getString("id"));
                responsesOk.put("description", "ok");
                responsesOk.put("schema", schema);

                responses.put("200", responsesOk);
                method.put("responses", responses);

                path.put(interMethod, method);
                paths.put(inter.getString("url"), path);
            }
        }
        return paths;
    }

    /**
     * 解析请求参数
     *
     * @param properties 属性
     * @param method     请求方式
     * @return 请求参数集合
     */
    private JSONArray parseParameters(JSONArray properties, String method) {
        JSONArray list = new JSONArray();
        for (Object p : properties) {
            JSONObject prop = JSON.parseObject(JSON.toJSONString(p));

            if ("response".equals(prop.getString("scope"))) {
                continue;
            }

            JSONObject item = new JSONObject();
            item.put("name", prop.getString("name"));
            item.put("in", "get".equals(method) ? "query" : "formData");
            // 初始化值 value可以作为 example
            item.put("example", prop.getString("value"));
            item.put("description", prop.getString("description"));
            item.put("type", prop.getString("type").toLowerCase());
            item.put("required", Boolean.TRUE.equals(prop.getBooleanValue("required")) ? "1" : "0");

            list.add(item);
        }
        return list;
    }

    /**
     * 解析定义
     *
     * @param modules 模块
     * @return 定义参数
     */
    private JSONObject parseDefinitions(JSONArray modules) {

        JSONObject definitions = new JSONObject();

        for (Object m : modules) {
            JSONObject module = JSON.parseObject(JSON.toJSONString(m));
            String moduleName = module.getString("name");
            JSONArray interfaces = module.getJSONArray("interfaces");
            log.info("Rap2接口转 Swagger json：【定义参数】模块 {} ", moduleName);

            for (Object i : interfaces) {
                JSONObject inter = JSON.parseObject(JSON.toJSONString(i));
                JSONArray properties = inter.getJSONArray("properties");
                log.info("Rap2接口转 Swagger json：【定义参数】模块 {} ，接口 {}，url {}",
                        moduleName, inter.getString("name"), inter.getString("url"));

                for (Object p : properties) {
                    JSONObject prop = JSON.parseObject(JSON.toJSONString(p));

                    if ("request".equals(prop.getString("scope"))) {
                        continue;
                    }

                    Integer suf = prop.getInteger("parentId") == -1 ? inter.getInteger("id") : prop.getInteger("parentId");
                    String responseSuf = "Response" + suf;
                    JSONObject response = new JSONObject();
                    if (definitions.get(responseSuf) == null) {

                        response.put("title", responseSuf);
                        response.put("type", "object");
                        response.put("properties", new JSONObject());

                        definitions.put(responseSuf, response);
                    } else {
                        response = JSON.parseObject(JSON.toJSONString(definitions.get(responseSuf)));
                    }

                    JSONObject object = response.getJSONObject("properties");
                    JSONObject ref = new JSONObject();
                    switch (prop.getString("type")) {
                        case "Object":
                            ref.put("$ref", "#/definitions/Response" + suf);
                            break;
                        case "Array":
                            JSONObject items = new JSONObject();
                            items.put("$ref", "#/definitions/Response" + suf);
                            ref.put("type", "array");
                            ref.put("items", items);
                            break;
                        default:
                            ref.put("type", prop.getString("type").toLowerCase());
                            ref.put("description", prop.getString("description"));
                            ref.put("default", prop.getString("value"));
                            break;
                    }
                    object.put(prop.getString("name"), ref);
                    response.put("properties", object);
                    definitions.put(responseSuf, response);

                }
            }
        }
        return definitions;
    }


    /**
     * Rap2 接口转 YApi json
     * rap项目对应 YApi 项目；
     * rap模块对应 YApi 分类；
     * rap接口对应 YApi 接口；
     *
     * @param rap2Json  rap接口对象
     * @param projectId YApi项目Id
     */
    public List<String> rapYApiJson(JSONObject rap2Json, String projectId) throws RapParseException {

        List<String> fileList = new ArrayList<>();

        Rap2JsonRootBean rap2JsonRootBean = JSONObject.toJavaObject(rap2Json, Rap2JsonRootBean.class);

        if (rap2JsonRootBean == null) {
            log.error("原始json不能为空");
            throw new RapParseException("Rap2 原始json为空");
        }

        Data data = rap2JsonRootBean.getData();
        List<Modules> modules = data.getModules();
        String rapDataName = data.getName();
        String description = data.getDescription();
        log.info("Rap2接口转 YApi json：开始处理 {} 项目", rapDataName);

        JSONArray allJson = new JSONArray();

        for (Modules module : modules) {

            String moduleName = module.getName();
            if (StringUtils.equals(moduleName, "")) {
                moduleName = rapDataName;
            }

            log.info("Rap2接口转 YApi json：模块 {} ", module.getName());

            String catid = "";
            String moduleDescription = module.getDescription();
            JSONObject catMap = new JSONObject();
            catMap.put("project_id", projectId);
            catMap.put("name", moduleName);
            if (StringUtils.isNotEmpty(moduleDescription)) {
                catMap.put("desc", moduleDescription);
            } else {
                catMap.put("desc", description);
            }

            log.info("新增接口分类 【{}】", moduleName);

            List<Interfaces> interfaces = module.getInterfaces();
            JSONArray list = new JSONArray();

            for (Interfaces inter : interfaces) {

                log.info("处理分组【{}】 ： 接口 {}，Url {}", moduleName, inter.getName(), inter.getUrl());

                if (StringUtils.isNotEmpty(inter.getUrl())) {

                    Map<String, Object> yApiInterface = action2YApiInterface(projectId, catid, inter);
                    list.add(yApiInterface);
                }
            }

            catMap.put("list", list);

            allJson.add(catMap);
        }

        String jsonFile = saveJsonService.writeToJsonFile(jsonRootPath, allJson, "all_" + rapDataName,
                rapDataName, JsonConvertTypeEnum.PROJECT_TO_PROJECT.getTypeName(), "");
        fileList.add(jsonFile);

        return fileList;
    }


    /**
     * Rap action 转为 Yapi 接口 json
     *
     * @param inter     Rap action接口
     * @param catid     接口分类Id
     * @param projectId 项目Id
     * @return Yapi 接口json
     */
    private Map<String, Object> action2YApiInterface(String projectId, String catid, Interfaces inter) {

        Map<String, Object> yApiInterface = new HashMap<>(16);
        String interfaceId = "";

        String path = RapUtil.getUrl(inter.getUrl());
        String method = inter.getMethod().toUpperCase();
        String title = inter.getName();

        yApiInterface.put("id", interfaceId);
        yApiInterface.put("token", null);
        yApiInterface.put("title", title);
        yApiInterface.put("catid", catid);
        yApiInterface.put("path", path);
        yApiInterface.put("status", "done");
        yApiInterface.put("desc", inter.getDescription());
        yApiInterface.put("method", method);
        yApiInterface.put("project_id", projectId);
        yApiInterface.put("tag", Collections.emptyList());
        yApiInterface.put("api_opened", false);

        yApiInterface.put("switch_notice", false);

        JSONArray headers = new JSONArray();
        if (!"GET".equals(method)) {
            JSONObject header = new JSONObject();
            header.put("name", "Content-Type");
            header.put("value", "multipart/form-data");
            headers.add(header);
        }

        yApiInterface.put("req_headers", headers);

        List<JSONObject> req_query = new ArrayList<>();

        JSONObject req_body_other = null;
        String req_body_type = null;

        List<Properties> propertieReq = inter.getProperties().stream()
                .filter(o -> "request".equals(o.getScope()))
                .collect(Collectors.toList());
        if ("GET".equals(method)) {
            for (Properties requestParameter : propertieReq) {
                JSONObject reqQueryItem = new JSONObject();
                reqQueryItem.put("desc", requestParameter.getDescription());
                reqQueryItem.put("example", requestParameter.getValue());
                reqQueryItem.put("name", requestParameter.getName());
                String required = "0";
                if (StringUtils.isNotEmpty(requestParameter.getRequired())) {
                    if (Boolean.getBoolean(requestParameter.getRequired())) {
                        required = "1";
                    }
                }
                reqQueryItem.put("required", required);
                req_query.add(reqQueryItem);
            }
        } else {
            req_body_other = formatDeepProperty(PARAM_ROOT_ID, propertieReq);
            req_body_type = "json";
        }

        List<Properties> propertiesRes = inter.getProperties().stream()
                .filter(o -> "response".equals(o.getScope()))
                .collect(Collectors.toList());
        Map<String, Object> res_body = formatDeepProperty(PARAM_ROOT_ID, propertiesRes);

        yApiInterface.put("req_query", req_query);
        yApiInterface.put("req_params", Collections.emptyList());
        yApiInterface.put("req_body_other", JSON.toJSONString(req_body_other));
        yApiInterface.put("req_body_type", req_body_type);

        yApiInterface.put("markdown", "");
        yApiInterface.put("req_body_form", Collections.emptyList());
        yApiInterface.put("req_body_is_json_schema", true);

        yApiInterface.put("res_body", JSON.toJSONString(res_body));
        yApiInterface.put("res_body_is_json_schema", true);
        yApiInterface.put("res_body_type", "json");

        return yApiInterface;
    }

    /**
     * 循环构建请求响应参数
     *
     * @param parentId     父级参数Id
     * @param propertyList 参数集合
     * @return 参数对象
     */
    private JSONObject formatDeepProperty(Integer parentId, List<Properties> propertyList) {
        JSONObject res_body = new JSONObject();
        JSONObject properties = new JSONObject();
        List<String> required = new ArrayList<>();

        res_body.put("title", "empty object");
        res_body.put("type", "object");
        res_body.put("properties", properties);
        res_body.put("required", required);

        for (Properties property : propertyList) {
            if (property.getParentId() == parentId) {
                String identifier = property.getName();
                String requiredStr = property.getRequired();
                if (requiredStr != null && Boolean.getBoolean(requiredStr)) {
                    required.add(property.getName());
                }

                String dataType = property.getType();
                JSONObject prop = new JSONObject();
                switch (dataType) {
                    case "Array":
                        prop.put("description", property.getDescription());
                        prop.put("example", property.getValue());
                        prop.put("items", formatDeepProperty(property.getId(), propertyList));
                        prop.put("type", "array");
                        properties.put(identifier, prop);
                        break;
                    case "Object":
                        properties.put(identifier, formatDeepProperty(property.getId(), propertyList));
                        break;
                    case "String":
                    case "Number":
                    case "Boolean":
                        prop.put("description", property.getDescription());
                        prop.put("example", property.getValue());
                        prop.put("type", dataType);
                        properties.put(identifier, prop);
                        break;
                    default:
                        prop.put("description", property.getDescription());
                        prop.put("example", property.getValue());
                        prop.put("type", "String");
                        properties.put(identifier, prop);
                        break;
                }
            }
        }

        res_body.put("properties", properties);
        return res_body;
    }


}
