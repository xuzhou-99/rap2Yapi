package com.qingyan.raptojson.raptojson.enums;

/**
 * Rap项目转YApi项目处理方式
 *
 * @author xuzhou
 * @since 2022/8/8
 */
public enum JsonConvertTypeEnum {

    /**
     * Rap项目转为项目，模块作为接口分类
     */
    PROJECT_TO_PROJECT("Rap项目转为项目，模块作为接口分类", "projectToProject"),

    /**
     * Rap模块转为项目，分页作为接口分类
     */
    MODULE_TO_PROJECT("Rap模块转为项目，分页作为接口分类", "moduleToProject"),

    ;

    /**
     * 接口名称
     */
    private final String type;
    /**
     * 接口api
     */
    private final String typeName;

    JsonConvertTypeEnum(String type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public String getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }
}
