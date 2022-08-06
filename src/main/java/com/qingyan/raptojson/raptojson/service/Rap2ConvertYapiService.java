package com.qingyan.raptojson.raptojson.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.raptojson.RapParseException;
import com.qingyan.raptojson.raptojson.pojo.rap1.ActionList;
import com.qingyan.raptojson.raptojson.pojo.rap1.ModuleList;
import com.qingyan.raptojson.raptojson.pojo.rap1.PageList;
import com.qingyan.raptojson.raptojson.pojo.rap1.RapJsonRootBean;
import com.qingyan.raptojson.raptojson.pojo.rap1.RequestParameterList;
import com.qingyan.raptojson.raptojson.pojo.rap1.ResponseParameterList;
import com.qingyan.raptojson.raptojson.pojo.rap2.Data;
import com.qingyan.raptojson.raptojson.pojo.rap2.Interfaces;
import com.qingyan.raptojson.raptojson.pojo.rap2.Modules;
import com.qingyan.raptojson.raptojson.pojo.rap2.Rap2JsonRootBean;
import com.qingyan.raptojson.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/8/5
 */
@Slf4j
@Service
public class Rap2ConvertYapiService {

    //    @Value("${json.rootPath}")
    private String jsonRootPath = "/apiJson1";


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

        JSONObject swagger = new JSONObject();
        swagger.put("swagger", "2.0");

        JSONObject info = new JSONObject();
        info.put("title", name);
        info.put("description", description);
        swagger.put("info", info);

        JSONArray tags = parseTags(modules);
        swagger.put("tags", tags);

        JSONObject paths = parsePaths(modules);
        swagger.put("paths", paths);

        JSONObject definitions = parseDefinitions(modules);
        swagger.put("definitions", definitions);


        String jsonFile = FileUtil.writeToJsonFile(jsonRootPath, swagger, "swagger_" + data.getString("name"),
                data.getString("name"), "projectToProject", "");

        fileList.add(jsonFile);
        log.info("rap 接口转为 json文件：{}", jsonFile);

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
                String interMethod = inter.getString("method".toLowerCase());
                log.info("Rap2接口转 Swagger json：【接口】模块 {} ，接口 {}，url {}",
                        moduleName,
                        inter.getString("name"),
                        inter.getString("url"));

                JSONObject path = new JSONObject();
                JSONObject method = new JSONObject();

                // TODO：无key数组序列化的时候有问题
                List<String> tags = new ArrayList<>();
                tags.add(moduleName);
                method.put("tags", tags);
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

            if (Objects.equals(prop.getString("scope"), "response")) {
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("name", prop.getString("name"));
            item.put("in", "get".equals(method) ? "query" : "formData");
            item.put("example", "default");
            item.put("description", prop.getString("description"));
            item.put("type", prop.getString("type").toLowerCase());
            item.put("required", prop.getBooleanValue("required"));

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
                        moduleName,
                        inter.getString("name"),
                        inter.getString("url"));

                for (Object p : properties) {
                    JSONObject prop = JSON.parseObject(JSON.toJSONString(p));

                    if (Objects.equals(prop.getString("scope"), "request")) {
                        continue;
                    }

                    Integer suf = prop.getInteger("parentId") == -1 ? module.getInteger("id") : prop.getInteger("parentId");
                    String responseSuf = "Response" + suf;
                    JSONObject response = new JSONObject();
                    if (definitions.get(responseSuf) == null) {

                        response.put("title", "Response" + suf);
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
                            ref.put("$ref", "#/definitions/Response" + prop.getString("id"));
                            break;
                        case "Array":
                            JSONObject items = new JSONObject();
                            items.put("$ref", "#/definitions/Response" + prop.getString("id"));
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
     * Rap 接口转 YApi json
     * rap项目对应 YApi 项目；
     * rap模块对应 YApi 分类；
     * rap接口对应 YApi 接口；
     * （项目-模块-分类-接口） 重组为 (项目-分类（模块作为分类）-接口)
     *
     * @param rap2Json     rap接口对象
     * @param rapProjectId rap项目Id
     * @param projectId    YApi项目Id
     */
    public List<String> rapYApiJson(JSONObject rap2Json, String rapProjectId, String projectId) throws RapParseException {

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
            JSONObject catMap = new JSONObject();
            catMap.put("desc", module.getDescription());
            catMap.put("name", moduleName);
            catMap.put("project_id", projectId);


            log.info("新增接口分类 【{}】", moduleName);

            List<Interfaces> interfaces = module.getInterfaces();
            JSONArray list = new JSONArray();


            for (Interfaces inter : interfaces) {

                log.info("处理分组【{}】 ： 接口 {}", moduleName, inter.getName());

                if (StringUtils.isNotEmpty(inter.getUrl())) {

                    Map<String, Object> yApiInterface = action2YApiInterface(projectId, catid, inter, moduleName);

                    list.add(yApiInterface);
                }
            }

            catMap.put("list", list);

            allJson.add(catMap);
        }


        String jsonFile = FileUtil.writeToJsonFile(jsonRootPath, allJson, "all_" + rapDataName,
                rapDataName, "projectToProject", "");
        fileList.add(jsonFile);
        log.info("rap 接口转为 json文件：{}", jsonFile);

        return fileList;
    }


    /**
     * Rap action 转为 Yapi 接口 json
     *
     * @param inter          Rap action接口
     * @param catid          接口分类Id
     * @param projectId      项目Id
     * @param actionPageName 接口所属页面名称
     * @return Yapi 接口json
     */
    private Map<String, Object> action2YApiInterface(String projectId, String catid, Interfaces inter, String actionPageName) {

        Map<String, Object> yApiInterface = new HashMap<>(16);
        String interfaceId = "";

        String path = getUrl(inter.getUrl());
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
        yApiInterface.put("tag", new ArrayList<>());
        yApiInterface.put("api_opened", false);

        yApiInterface.put("switch_notice", false);

        JSONArray headers = new JSONArray();
        if (!StringUtils.equals(method, "GET")) {
            JSONObject header = new JSONObject();
            header.put("name", "Content-Type");
            header.put("value", "application/json");
            headers.add(header);
        }
        yApiInterface.put("req_headers", headers);


        List<JSONObject> req_query = new ArrayList<>();

        JSONObject req_body_other = null;
        String req_body_type;

//        List<RequestParameterList> requestParameterList = inter.getRequestParameterList();
        List<RequestParameterList> requestParameterList = new ArrayList<>();
        if ("GET".equals(method)) {
            for (RequestParameterList requestParameter : requestParameterList) {

                JSONObject req_queryItem = new JSONObject();
                req_queryItem.put("desc", requestParameter.getName());
                req_queryItem.put("example", JSON.toJSONString(requestParameter.getRemark()).replace("@mock=", ""));
                req_queryItem.put("name", requestParameter.getIdentifier());
                req_queryItem.put("required", "1");
                req_query.add(req_queryItem);
            }

            req_body_type = null;
        } else {
            req_body_other = formatDeepNoMock(JSON.parseArray(JSON.toJSONString(requestParameterList)));

            req_body_type = "json";
        }

//        List<ResponseParameterList> responseParameterList = inter.getResponseParameterList();
        List<ResponseParameterList> responseParameterList = new ArrayList<>();
        Map<String, Object> res_body = formatDeepNoMock(JSON.parseArray(JSON.toJSONString(responseParameterList)));


        yApiInterface.put("req_query", req_query);
        yApiInterface.put("req_params", new ArrayList<>());
        yApiInterface.put("req_body_other", JSON.toJSONString(req_body_other));
        yApiInterface.put("req_body_type", req_body_type);


        yApiInterface.put("markdown", "");
        yApiInterface.put("req_body_form", new ArrayList<>());
        yApiInterface.put("req_body_is_json_schema", true);


        yApiInterface.put("res_body", JSON.toJSONString(res_body));
        yApiInterface.put("res_body_is_json_schema", true);
        yApiInterface.put("res_body_type", "json");

        return yApiInterface;
    }


    private JSONObject formatDeepNoMock(JSONArray key) {
        JSONObject res_body = new JSONObject();
        JSONObject properties = new JSONObject();
        List<String> required = new ArrayList();

        res_body.put("properties", properties);
        res_body.put("required", required);
        res_body.put("title", "empty object");
        res_body.put("type", "object");


        for (Object k : key) {
            JSONObject rp;
            if (k instanceof String) {
                rp = JSON.parseObject((String) k);
            } else {
                rp = JSON.parseObject(JSON.toJSONString(k));
            }

            String[] identifiers = rp.getString("identifier").split("\\|");

            String identifier = identifiers[0];
            required.add(identifier);

            String dataType = rp.getString("dataType");
            if (dataType.matches("array<(.*)>")) {

                if (dataType.contains("object")) {

                    JSONObject prop = new JSONObject();
                    prop.put("items", formatDeepNoMock(rp.getJSONArray("parameterList")));
                    prop.put("type", "array");

                    properties.put(identifier, prop);

                } else {
                    JSONObject prop = new JSONObject();
                    prop.put("items", new JSONObject());
                    prop.put("type", "array");
                    properties.put(identifier, prop);
                }

            } else {

                JSONObject prop = new JSONObject();

                prop.put("description", rp.getString("name"));
                prop.put("type", dataType);

                if (rp.getJSONArray("parameterList").isEmpty()) {
                    properties.put(identifier, prop);
                } else {
                    properties.put(identifier, formatDeepNoMock(rp.getJSONArray("parameterList")));
                }
            }

        }

        res_body.put("properties", properties);
        return res_body;
    }

    /**
     * url需要以 '\' 开头
     *
     * @param url url
     * @return 格式化的url
     */
    private String getUrl(String url) {
        url = url.replace(" ", "")
                .replace("$", "");

        //TODO:  '/app/customer_contract/v2/online/getOnlineEditPage.do?contractId='+id ，将？后面参数处理放入到req_params

        if (url.startsWith("/")) {
            return url;
        } else {
            return "/" + url;
        }
    }

}
