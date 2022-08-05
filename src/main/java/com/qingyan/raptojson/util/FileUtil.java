package com.qingyan.raptojson.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author xuzhou
 * @since 2022/8/3
 */
public class FileUtil {

    private FileUtil() {

    }

    private static final String file_json_suffix = ".json";

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
    public static String writeToJsonFile(String fileRootPath, JSONObject json, String jsonFileName, String projectName,
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
    public static String writeToJsonFile(String fileRootPath, JSONArray json, String jsonFileName, String projectName,
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
    public static String writeToJsonFile(String fileRootPath, String json, String jsonFileName, String projectName,
                                         String typeName, String moduleName) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(fileRootPath);
        if (projectName != null && !"".equals(projectName)) {
            stringBuilder.append("/").append(projectName);
        }
        if (typeName != null && !"".equals(typeName)) {
            stringBuilder.append("/").append(typeName);
        }
        if (moduleName != null && !"".equals(moduleName)) {
            stringBuilder.append("/").append(moduleName);
        }

        stringBuilder.append("/").append(jsonFileName).append(file_json_suffix);
        String filePath = stringBuilder.toString();

        FileUtil.writeToJsonFile(json, filePath);
        return filePath;
    }

    public static String writeToJsonFile(String content, String filePath) {

        File file = new File(filePath);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(touch(file), false), StandardCharsets.UTF_8))) {

            bufferedWriter.write(content);
            bufferedWriter.flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return filePath;
    }

    /**
     * 创建文件及其父目录，如果这个文件存在，直接返回这个文件<br>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param file 文件对象
     * @return 文件，若路径为null，返回null
     * @throws IOException IO异常
     */
    public static File touch(File file) throws IOException {
        if (null == file) {
            return null;
        }
        if (!file.exists()) {
            mkParentDirs(file);
            try {
                file.createNewFile();
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        return file;
    }

    /**
     * 创建所给文件或目录的父目录
     *
     * @param file 文件或目录
     * @return 父目录
     */
    public static File mkParentDirs(File file) {
        final File parentFile = file.getParentFile();
        if (null != parentFile && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        return parentFile;
    }
}
