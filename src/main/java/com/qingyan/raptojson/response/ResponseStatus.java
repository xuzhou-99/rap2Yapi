package com.qingyan.raptojson.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ResponseStatus
 *
 * @author xuzhou
 * @version v1.0.0
 * @create 2021/5/11 13:59
 */
@Getter
@AllArgsConstructor
public enum ResponseStatus {
    /**
     * Status
     */
    SUCCESS(true, 200, "success"),

    FAIL(false, 500, "fail"),

    UNKNOWN_ERROR(false, 500, "unknown error");


    private final Boolean success;

    private final Integer code;

    private final String message;
}
