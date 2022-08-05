package com.qingyan.raptojson.raptojson.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.raptojson.RapParseException;
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
            log.info("Rap2接口转 Swagger json：【接口】模块 {} ", module.getString("name"));

            for (Object i : interfaces) {
                JSONObject inter = JSON.parseObject(JSON.toJSONString(i));
                String interMethod = inter.getString("method".toLowerCase());
                log.info("Rap2接口转 Swagger json：【接口】模块 {} ，接口 {}，url {}",
                        module.getString("name"),
                        inter.getString("name"),
                        inter.getString("url"));

                JSONObject path = new JSONObject();
                JSONObject method = new JSONObject();

                method.put("tags", module.getString("name"));
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
            JSONArray interfaces = module.getJSONArray("interfaces");
            log.info("Rap2接口转 Swagger json：【定义参数】模块 {} ", module.getString("name"));

            for (Object i : interfaces) {
                JSONObject inter = JSON.parseObject(JSON.toJSONString(i));
                JSONArray properties = inter.getJSONArray("properties");
                log.info("Rap2接口转 Swagger json：【定义参数】模块 {} ，接口 {}，url {}",
                        module.getString("name"),
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


}
