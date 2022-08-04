package com.qingyan.raptojson.raptojson.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.httpclient.HttpClientUtils;
import com.qingyan.raptojson.httpclient.HttpResult;
import com.qingyan.raptojson.raptojson.pojo.ActionList;
import com.qingyan.raptojson.raptojson.pojo.JsonRootBean;
import com.qingyan.raptojson.raptojson.pojo.ModuleList;
import com.qingyan.raptojson.raptojson.pojo.PageList;
import com.qingyan.raptojson.raptojson.pojo.RequestParameterList;
import com.qingyan.raptojson.raptojson.pojo.ResponseParameterList;
import com.qingyan.raptojson.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/8/2
 */
@Slf4j
@Service
public class Rap2YapiService {

    private String url = "http://1.116.215.27:3000";


    //    @Value("${json.rootPath}")
    private String jsonRootPath = "/apiJson1";


    public String getRapInterfaceByProjectId(String rapUrl, String rap_project_id) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("projectId", rap_project_id);
        HttpResult httpResult = HttpClientUtils.doGet(rapUrl + "/api/queryRAPModel.do", params);

        String stringEntity = httpResult.getStringEntity();

        JSONObject jsonObject = JSON.parseObject(stringEntity);
        return jsonObject.getString("modelJSON");
    }


    /**
     * Rap 接口转Yapi json
     * 项目-模块-分类-接口 -> 项目-分类（模块作为分类）-接口
     *
     * @param jsonRootBean rap接口对象
     * @param rapProjectId rap项目Id
     * @param projectId    YApi项目Id
     * @param type         接口转换措施
     * @param oneJson      是否是一个json
     */
    public List<String> rap2Json(JsonRootBean jsonRootBean, String rapProjectId, String projectId, String type, Boolean oneJson) {
        List<String> fileList = new ArrayList<>();
        switch (type) {
            case "page":
                fileList = rap2JsonGroupByPage(jsonRootBean, rapProjectId, projectId);
                break;
            case "module":
            default:
                fileList = rap2JsonGroupByModule(jsonRootBean, rapProjectId, projectId, oneJson);
                break;
        }
        return fileList;
    }

    /**
     * Rap 接口转 YApi json
     * rap项目对应 YApi 项目；
     * rap模块对应 YApi 分类；
     * rap接口对应 YApi 接口；
     * （项目-模块-分类-接口） 重组为 (项目-分类（模块作为分类）-接口)
     *
     * @param jsonRootBean rap接口对象
     * @param rapProjectId rap项目Id
     * @param projectId    YApi项目Id
     * @param oneJson      是否是一个json
     */
    public List<String> rap2JsonGroupByModule(JsonRootBean jsonRootBean, String rapProjectId, String projectId, Boolean oneJson) {

        List<String> fileList = new ArrayList<>();

        String rapDataName = jsonRootBean.getName();

        log.info("处理Rap 项目 ：【{}】", rapDataName);

        JSONArray allJson = new JSONArray();
        List<ModuleList> moduleList = jsonRootBean.getModuleList();

        for (ModuleList module : moduleList) {

            String moduleName = module.getName();
            if (StringUtils.equalsAny(moduleName, "", "某模块（点击编辑后双击修改）")) {
                moduleName = rapDataName;
            }

            log.info("处理Rap moduleList 模块：【{}】", moduleName);

            String catid = "";
            JSONObject catMap = new JSONObject();
            catMap.put("desc", module.getIntroduction());
            catMap.put("name", moduleName);
            catMap.put("project_id", projectId);


            log.info("新增接口分类 【{}】", moduleName);

            List<PageList> pageList = module.getPageList();
            JSONArray list = new JSONArray();

            for (PageList pageItem : pageList) {

                String pageItemName = pageItem.getName();
                if (StringUtils.equalsAny(pageItemName, "", "某页面")) {
                    pageItemName = "";
                }
                log.info("处理Rap pageList 分组：【{}】", pageItemName);

                List<ActionList> actionList = pageItem.getActionList();

                for (ActionList action : actionList) {

                    log.info("处理分组【{}】 ： 接口 {}", pageItemName, action.getName());

                    if (StringUtils.isNotEmpty(action.getRequestUrl())) {

                        Map<String, Object> yApiInterface = action2YApiInterface(projectId, catid, action, pageItemName);

                        list.add(yApiInterface);
                    }
                }
            }
            catMap.put("list", list);

            if (Boolean.FALSE.equals(oneJson)) {
                JSONArray json = new JSONArray();
                json.add(catMap);

                String jsonFile = writeToJsonFile(json, rapDataName + "_" + catMap.getString("name"),
                        rapDataName, "projectToProject", "");
                fileList.add(jsonFile);
                log.info("rap 接口转为 json文件：{}", jsonFile);
            } else {
                allJson.add(catMap);
            }
        }

        if (Boolean.TRUE.equals(oneJson)) {
            String jsonFile = writeToJsonFile(allJson, "all_" + rapDataName,
                    rapDataName, "projectToProject", "");
            fileList.add(jsonFile);
            log.info("rap 接口转为 json文件：{}", jsonFile);
        }
        return fileList;
    }


    /**
     * Rap 接口转 YApi json
     * rap模块对应 YApi 项目；
     * rap分类对应 YApi 分类；
     * rap接口对应 YApi 接口；
     * （项目-模块-分类-接口） 重组为 (项目（模块作为项目）-分类-接口)
     *
     * @param jsonRootBean rap接口对象
     * @param rapProjectId rap项目Id
     * @param projectId    YApi项目Id
     */
    public List<String> rap2JsonGroupByPage(JsonRootBean jsonRootBean, String rapProjectId, String projectId) {

        List<String> fileList = new ArrayList<>();
        String rapDataName = jsonRootBean.getName();

        log.info("处理Rap 项目 ：【{}】", rapDataName);
        List<ModuleList> moduleList = jsonRootBean.getModuleList();
        for (ModuleList module : moduleList) {

            String moduleName = module.getName();
            if (StringUtils.equalsAny(moduleName, "", "某模块（点击编辑后双击修改）")) {
                moduleName = rapDataName;
            }

            log.info("处理Rap moduleList 模块：【{}】", moduleName);

            // 接口集合-包含多个分类
            JSONArray interCatList = new JSONArray();

            List<PageList> pageList = module.getPageList();


            for (PageList pageItem : pageList) {

                String pageItemName = pageItem.getName();
                if (StringUtils.equalsAny(pageItemName, "", "某页面")) {
                    pageItemName = moduleName;
                }
                log.info("处理Rap pageList 分组：【{}】", pageItemName);
                log.info("新增接口分类 【{}】", pageItemName);


                String catid = "";
                JSONObject catMap = new JSONObject();
                catMap.put("desc", pageItem.getIntroduction());
                catMap.put("name", pageItemName);
                catMap.put("project_id", projectId);

                List<ActionList> actionList = pageItem.getActionList();

                JSONArray list = new JSONArray();
                for (ActionList action : actionList) {

                    log.info("处理分组 【{}】 ： 接口 {}", pageItemName, action.getName());

                    if (StringUtils.isEmpty(action.getRequestUrl())) {
                        continue;
                    }

                    Map<String, Object> yApiInterface = action2YApiInterface(projectId, catid, action, null);

                    list.add(yApiInterface);
                }

                catMap.put("list", list);

                interCatList.add(catMap);
            }

            String jsonFile = writeToJsonFile(interCatList, moduleName, rapDataName, "moduleToProject", moduleName);
            fileList.add(jsonFile);
            log.info("rap 接口转为 json文件：{}", jsonFile);
        }
        return fileList;
    }

    /**
     * Rap action 转为 Yapi 接口 json
     *
     * @param action         Rap action接口
     * @param catid          接口分类Id
     * @param projectId      项目Id
     * @param actionPageName 接口所属页面名称
     * @return Yapi 接口json
     */
    private Map<String, Object> action2YApiInterface(String projectId, String catid, ActionList action, String actionPageName) {

        Map<String, Object> yApiInterface = new HashMap<>(16);
        String interfaceId = "";

        String path = getUrl(action.getRequestUrl());
        String method = convertMethod(action.getRequestType());
        String title = action.getName();
        if (actionPageName != null && !"".equals(actionPageName)) {
            title = (actionPageName + "—" + title).trim();
        }

        yApiInterface.put("id", interfaceId);
        yApiInterface.put("token", null);
        yApiInterface.put("title", title);
        yApiInterface.put("catid", catid);
        yApiInterface.put("path", path);
        yApiInterface.put("status", "done");
        yApiInterface.put("desc", action.getDescription());
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

        List<RequestParameterList> requestParameterList = action.getRequestParameterList();
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

        List<ResponseParameterList> responseParameterList = action.getResponseParameterList();
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
     * TODO：带 mock json处理
     *
     * @param key
     * @return
     */
    private JSONObject formatDeepWithMock(JSONArray key) {
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


            String identifier = "";
            String decorate = "";
            String[] identifiers = rp.getString("identifier").split("\\|");

            identifier = identifiers[0];
            required.add(identifier);
            if (identifiers.length > 1) {
                decorate = identifiers[1];
            }

            if (rp.getString("dataType").contains("array")) {
                String type = rp.getString("dataType");

                if (type == "object") {
                    String len = decorate;
                    if (len == "") {
                        len = "1";
                    }

                    JSONObject prop = new JSONObject();
                    prop.put("items", formatDeepWithMock(rp.getJSONArray("parameterList")));
                    prop.put("maxItems", len.split("-")[0]);
                    prop.put("minItems", len.split("-").length > 1 ? len.split("-")[1] : len.split("-")[0]);
                    prop.put("type", "array");

                    properties.put(identifier, prop);

                } else {

                    JSONArray arr = new JSONArray();

                    if (rp.getString("remark") != "") {
                        String mocksr = rp.getString("remark").replace("@mock=", "");
                        if (mocksr.contains("$order")) {
                            arr = new JSONArray(Arrays.asList(mocksr.split("$order")[1].split(", ")));
                        }
                    }


                    JSONObject prop = new JSONObject();

                    Map<String, Object> items = new HashMap<>();
                    items.put("mock", type == "number" ? "@integer(1, 999999)" : ("@" + type));


                    prop.put("items", items);
                    prop.put("maxItems", !arr.isEmpty() ? arr.size() : 1);
                    prop.put("minItems", !arr.isEmpty() ? arr.size() : 1);
                    prop.put("type", "array");

                    properties.put(identifier, prop);
                }

            } else {

                String remark = rp.getString("remark");
                String mock = !StringUtils.equals(remark, "") ? remark.replace("@mock=", "") : " ";

                List<Object> arr = mock != "" && mock.contains("$order") ?
                        Arrays.asList(mock.split("$order")[1].replace("[()'\"]", " ").split(", ")) : new ArrayList<>();
                String len = decorate != null && decorate.indexOf('+') < 0 && decorate.indexOf('.') < 0 ? decorate : "0";
                String rule;
                if (!StringUtils.equals(rp.getString("dataType"), "number")) {
                    rule = mock;
                } else {
                    if (decorate != null && decorate.indexOf('+') > -1) {
                        rule = "@increment(${decorate.replace('+', '')})";
                    } else if (decorate != null && decorate.indexOf('.') > -1) {
                        rule = "@float(1, 999999, 1, 10)";
                    } else {
                        rule = !"".equals(mock) ? mock : "@integer";
                    }
                }


                JSONObject prop = new JSONObject();

                String maxItems = "";
                String minItems = "";
                if (!"".equals(len)) {
                    String[] split = len.split("-");
                    if (split.length > 1 && !StringUtils.equals(split[1], "")) {
                        minItems = split[1];
                    } else {
                        maxItems = minItems = split[0];
                    }
                }


                prop.put("description", rp.getString("name"));
                prop.put("maxItems", maxItems);
                prop.put("minItems", minItems);
                prop.put("type", rp.getString("dataType"));

                prop.put("mock", arr.isEmpty() ? mock : "");

                JSONObject defaultM = new JSONObject();
                defaultM.put("mock", "");
                if (StringUtils.isNotEmpty(rule)) {
                    JSONObject defaultMc = new JSONObject();
                    defaultMc.put("mock", rule);
                    defaultM.put("mock", defaultMc);
                }
                prop.put("default", defaultM);


                properties.put(identifier, prop);


                if (!arr.isEmpty()) {
                    prop.remove("mock");
                    prop.remove("description");
                    prop.put("enum", arr);
                }
                if (rp.getJSONArray("parameterList").isEmpty()) {
                    properties.put(identifier, prop);
                } else {
                    properties.put(identifier, formatDeepWithMock(rp.getJSONArray("parameterList")));
                }
            }

        }

        res_body.put("properties", properties);
        return res_body;
    }

    /**
     * Rap 接口请求方式转 YApi 接口请求方式
     *
     * @param type Rap 接口请求方式
     * @return YApi 接口请求方式
     */
    private String convertMethod(String type) {
        if (type == null) {
            return "GET";
        }
        switch (type) {
            case "1":
                return "GET";
            case "2":
                return "POST";
            case "3":
                return "PUT";
            case "4":
                return "DELETE";
            default:
                return "GET";
        }
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

        if (url.startsWith("/")) {
            return url;
        } else {
            return "/" + url;
        }
    }

    /**
     * 将json写入到文件中
     *
     * @param json         json
     * @param jsonFileName 文件名称
     * @param projectName  项目名称
     * @param typeName     处理方式
     * @param moduleName   模块名称
     * @return 文件路径
     */
    private String writeToJsonFile(JSONArray json, String jsonFileName, String projectName,
                                   String typeName, String moduleName) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(jsonRootPath);
        if (projectName != null && !"".equals(projectName)) {
            stringBuilder.append("/").append(projectName);
        }
        if (typeName != null && !"".equals(typeName)) {
            stringBuilder.append("/").append(typeName);
        }
        if (moduleName != null && !"".equals(moduleName)) {
            stringBuilder.append("/").append(moduleName);
        }

        stringBuilder.append("/").append(jsonFileName).append(".json");
        String filePath = stringBuilder.toString();

        FileUtil.writeToJsonFile(JSON.toJSONString(json), filePath);
        return filePath;
    }


}
