package com.qingyan.raptojson.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author xuzhou
 * @since 2022/8/3
 */
public class FileUtil {

    private FileUtil() {

    }


    /**
     * 写入文件
     *
     * @param content  文件内容
     * @param filePath 文件路径：/test/file.json
     * @return 文件路径
     */
    public static String writeToFile(String content, String filePath) throws Exception {

        File file = new File(filePath);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(touch(file), false), StandardCharsets.UTF_8))) {

            bufferedWriter.write(content);
            bufferedWriter.flush();
            return filePath;

        } catch (IOException e) {
            throw new Exception(e);
        }
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
