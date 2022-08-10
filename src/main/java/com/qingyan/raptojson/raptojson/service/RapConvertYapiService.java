package com.qingyan.raptojson.raptojson.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.raptojson.RapUtil;
import com.qingyan.raptojson.raptojson.enums.JsonConvertTypeEnum;
import com.qingyan.raptojson.raptojson.pojo.rap1.ActionList;
import com.qingyan.raptojson.raptojson.pojo.rap1.ModuleList;
import com.qingyan.raptojson.raptojson.pojo.rap1.PageList;
import com.qingyan.raptojson.raptojson.pojo.rap1.RapJsonRootBean;
import com.qingyan.raptojson.raptojson.pojo.rap1.RequestParameterList;
import com.qingyan.raptojson.raptojson.pojo.rap1.ResponseParameterList;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/8/2
 */
@Slf4j
@Service
public class RapConvertYapiService {


    @Value("${json.rootPath}")
    private String jsonRootPath;

    @Resource
    private RapSaveJsonService rapSaveJsonService;


    /**
     * Rap 接口转Yapi json
     * 项目-模块-分类-接口 -> 项目-分类（模块作为分类）-接口
     *
     * @param rapJsonRootBean rap接口对象
     * @param rapProjectId    rap项目Id
     * @param projectId       YApi项目Id
     * @param type            接口转换措施
     * @param oneJson         是否是一个json
     */
    public List<String> rap2Json(RapJsonRootBean rapJsonRootBean, String rapProjectId, String projectId,
                                 String type, Boolean oneJson) {
        List<String> fileList;
        switch (type) {
            case "page":
                fileList = rap2JsonGroupByPage(rapJsonRootBean, rapProjectId, projectId);
                break;
            case "module":
            default:
                fileList = rap2JsonGroupByModule(rapJsonRootBean, rapProjectId, projectId, oneJson);
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
     * @param rapJsonRootBean rap接口对象
     * @param rapProjectId    rap项目Id
     * @param projectId       YApi项目Id
     * @param oneJson         是否是一个json
     */
    public List<String> rap2JsonGroupByModule(RapJsonRootBean rapJsonRootBean, String rapProjectId,
                                              String projectId, Boolean oneJson) {

        List<String> fileList = new ArrayList<>();

        String rapDataName = rapJsonRootBean.getName();

        log.info("处理Rap 项目 ：【{}】", rapDataName);

        JSONArray allJson = new JSONArray();
        List<ModuleList> moduleList = rapJsonRootBean.getModuleList();

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
                    pageItemName = moduleName;
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

                String jsonFile = rapSaveJsonService.writeToJsonFile(jsonRootPath, json,
                        rapDataName + "_" + moduleName,
                        rapDataName, JsonConvertTypeEnum.PROJECT_TO_PROJECT.getTypeName(), "");
                fileList.add(jsonFile);
            } else {
                allJson.add(catMap);
            }
        }

        if (Boolean.TRUE.equals(oneJson)) {
            String jsonFile = rapSaveJsonService.writeToJsonFile(jsonRootPath, allJson, "all_" + rapDataName,
                    rapDataName, JsonConvertTypeEnum.PROJECT_TO_PROJECT.getTypeName(), "");
            fileList.add(jsonFile);
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
     * @param rapJsonRootBean rap接口对象
     * @param rapProjectId    rap项目Id
     * @param projectId       YApi项目Id
     */
    public List<String> rap2JsonGroupByPage(RapJsonRootBean rapJsonRootBean, String rapProjectId, String projectId) {

        List<String> fileList = new ArrayList<>();
        String rapDataName = rapJsonRootBean.getName();

        log.info("处理Rap 项目 ：【{}】", rapDataName);
        List<ModuleList> moduleList = rapJsonRootBean.getModuleList();
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

            String jsonFile = rapSaveJsonService.writeToJsonFile(jsonRootPath, interCatList, moduleName, rapDataName,
                    JsonConvertTypeEnum.MODULE_TO_PROJECT.getTypeName(), moduleName);
            fileList.add(jsonFile);
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
    private Map<String, Object> action2YApiInterface(String projectId, String catid, ActionList action,
                                                     String actionPageName) {

        Map<String, Object> yApiInterface = new HashMap<>(16);
        String interfaceId = "";

        String path = RapUtil.getUrl(action.getRequestUrl());
        String method = RapUtil.convertMethod(action.getRequestType());
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
        yApiInterface.put("tag", Collections.emptyList());
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
        String req_body_type = null;

        List<RequestParameterList> requestParameterList = action.getRequestParameterList();

        if ("GET".equals(method)) {
            for (RequestParameterList requestParameter : requestParameterList) {

                String name = requestParameter.getName();
                String identifier = requestParameter.getIdentifier();
                String remark = requestParameter.getRemark()
                        .replace("@mock=", "")
                        .replace("$order", "");
                String fullName = RapUtil.buildDesAndRemark(name, remark);

                JSONObject req_queryItem = new JSONObject();
                req_queryItem.put("desc", fullName);
                // req_queryItem.put("example", remark);
                req_queryItem.put("name", identifier);
                req_queryItem.put("required", "1");
                req_query.add(req_queryItem);
            }

        } else {
            req_body_other = formatDeepNoMock(JSON.parseArray(JSON.toJSONString(requestParameterList)));
            req_body_type = "json";
        }

        List<ResponseParameterList> responseParameterList = action.getResponseParameterList();
        Map<String, Object> res_body = formatDeepNoMock(JSON.parseArray(JSON.toJSONString(responseParameterList)));

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
     * 递归处理参数
     *
     * @param parameterList 参数集合
     * @return 参数json
     */
    private JSONObject formatDeepNoMock(JSONArray parameterList) {
        JSONObject res_body = new JSONObject();
        JSONObject properties = new JSONObject();
        List<String> required = new ArrayList<>();

        res_body.put("properties", properties);
        res_body.put("required", required);
        res_body.put("title", "empty object");
        res_body.put("type", "object");

        for (Object k : parameterList) {
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
            String remark = rp.getString("remark")
                    .replace("@mock=", "")
                    .replace("$order", "");
            String name = rp.getString("name");
            String fullName = RapUtil.buildDesAndRemark(name, remark);

            if (dataType.matches("array<(.*)>")) {

                if (dataType.contains("object")) {

                    JSONObject prop = new JSONObject();
                    prop.put("description", fullName);
                    prop.put("items", formatDeepNoMock(rp.getJSONArray("parameterList")));
                    prop.put("type", "array");
                    properties.put(identifier, prop);
                } else {
                    JSONObject prop = new JSONObject();
                    prop.put("description", fullName);
                    prop.put("items", new JSONObject());
                    prop.put("type", "array");
                    properties.put(identifier, prop);
                }

            } else {

                if (rp.getJSONArray("parameterList").isEmpty()) {
                    JSONObject prop = new JSONObject();
                    prop.put("description", fullName);
                    prop.put("type", dataType);
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
        //TODO：处理成带mock的json
        JSONObject res_body = new JSONObject();
        JSONObject properties = new JSONObject();
        List<String> required = new ArrayList<>();

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

                if ("object".equals(type)) {
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

                List<Object> arr = !"".equals(mock) && mock.contains("$order") ?
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


}
