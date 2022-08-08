package com.qingyan.raptojson.raptojson.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingyan.raptojson.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * @author xuzhou
 * @since 2022/8/8
 */
@Slf4j
@Service
public class RapSaveJsonService {

    /**
     * json 文件后缀名
     */
    private static final String FILE_SUFFIX_JSON = ".json";

    @Value("${json.isShare}")
    private Boolean isShare;

    @Value("${json.sharePath}")
    private String sharePath;

    /**
     * 将json写入到文件中
     *
     * @param fileRootPath 文件根路径
     * @param json         json
     * @param jsonFileName 文件名称
     * @param projectName  项目名称
     * @param typeName     处理方式
     * @param moduleName   模块名称
     * @return 文件路径
     */
    public String writeToJsonFile(String fileRootPath, JSONObject json, String jsonFileName, String projectName,
                                  String typeName, String moduleName) {
        return writeToJsonFile(fileRootPath, JSON.toJSONString(json), jsonFileName, projectName, typeName, moduleName);
    }


    /**
     * 将json写入到文件中
     *
     * @param fileRootPath 文件根路径
     * @param json         json
     * @param jsonFileName 文件名称
     * @param projectName  项目名称
     * @param typeName     处理方式
     * @param moduleName   模块名称
     * @return 文件路径
     */
    public String writeToJsonFile(String fileRootPath, JSONArray json, String jsonFileName, String projectName,
                                  String typeName, String moduleName) {
        return writeToJsonFile(fileRootPath, JSON.toJSONString(json), jsonFileName, projectName, typeName, moduleName);
    }

    /**
     * 将json写入到文件中
     *
     * @param fileRootPath 文件根路径
     * @param json         json
     * @param jsonFileName 文件名称
     * @param projectName  项目名称
     * @param typeName     处理方式
     * @param moduleName   模块名称
     * @return 文件路径
     */
    public String writeToJsonFile(String fileRootPath, String json, String jsonFileName, String projectName,
                                  String typeName, String moduleName) {

        StringBuilder stringBuilder = new StringBuilder();

        // 文件根路径
        stringBuilder.append(fileRootPath);
        // 项目名称
        if (projectName != null && !"".equals(projectName)) {
            stringBuilder.append("/").append(projectName);
        }
        // 项目处理类型
        if (typeName != null && !"".equals(typeName)) {
            stringBuilder.append("/").append(typeName);
        }
        // 模块名称
        if (moduleName != null && !"".equals(moduleName)) {
            stringBuilder.append("/").append(moduleName);
        }
        // 文件名称
        stringBuilder.append("/").append(jsonFileName).append(FILE_SUFFIX_JSON);
        String filePath = stringBuilder.toString();

        try {
            FileUtil.writeToFile(json, filePath);
        } catch (Exception e) {
            log.error("写入文件 {} 失败！", filePath, e);
        }
        // 共享文件夹
        // TODO：修改为多种方式，minio
        if (Boolean.TRUE.equals(isShare) && sharePath != null && !"".equals(sharePath)) {
            return sharePath + filePath;
        }
        return filePath;
    }
}
