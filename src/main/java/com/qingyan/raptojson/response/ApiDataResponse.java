package com.qingyan.raptojson.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * ApiDataResponse
 *
 * @author xuzhou
 * @version v1.0.0
 * @create 2021/5/11 14:12
 */
@Getter
@ToString
@AllArgsConstructor
public class ApiDataResponse<T> extends ApiResponse implements Serializable {


    private static final long serialVersionUID = -178852446366766961L;

    private final T data;

    /**
     * 通用返回结果封装
     *
     * @param success 是否成功
     * @param code    响应码
     * @param message 消息
     * @param data    数据
     */
    private ApiDataResponse(final Boolean success, final Integer code, final String message, T data) {
        super(success, code, message);
        this.data = data;
    }

    /**
     * 通用返回结果封装
     *
     * @param success 是否成功
     * @param code    响应码
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 封装结果
     */
    public static <T> ApiDataResponse<T> of(final Boolean success, final Integer code, final String message, T data) {
        return new ApiDataResponse<>(success, code, message, data);
    }

    /**
     * 通用返回结果封装-操作成功
     *
     * @param data 数据
     * @param <T>  数据类型
     * @return 封装结果
     */
    public static <T> ApiDataResponse<T> ofSuccess(T data) {
        return of(ResponseStatus.SUCCESS.getSuccess(), ResponseStatus.SUCCESS.getCode(), ResponseStatus.SUCCESS.getMessage(), data);
    }

    /**
     * 通用返回结果封装-操作成功
     *
     * @param message 消息
     * @param data    数据
     * @param <T>     数据类型
     * @return 封装结果
     */
    public static <T> ApiDataResponse<T> ofSuccess(final String message, T data) {
        return of(ResponseStatus.SUCCESS.getSuccess(), ResponseStatus.SUCCESS.getCode(), message, data);
    }

    /**
     * 通用返回结果封装-操作失败
     *
     * @param <T> 数据类型
     * @return 封装结果
     */
    public static <T> ApiDataResponse<T> ofError() {
        return of(ResponseStatus.FAIL.getSuccess(), ResponseStatus.FAIL.getCode(), ResponseStatus.FAIL.getMessage(), null);
    }

    /**
     * 通用返回结果封装-操作失败
     *
     * @param message 消息
     * @param <T>     数据类型
     * @return 封装结果
     */
    public static <T> ApiDataResponse<T> ofError(final String message) {
        return of(ResponseStatus.FAIL.getSuccess(), ResponseStatus.FAIL.getCode(), message, null);
    }

    /**
     * 通用返回结果封装-操作失败
     *
     * @param code    响应码
     * @param message 消息
     * @param <T>     数据类型
     * @return 封装结果
     */
    public static <T> ApiDataResponse<T> ofError(final Integer code, final String message) {
        return of(ResponseStatus.FAIL.getSuccess(), code, message, null);
    }

    /**
     * 通用返回结果封装-操作异常
     *
     * @param data 数据
     * @param <E>  数据类型
     * @return 封装结果
     */
    public static <E extends Exception> ApiDataResponse<Object> ofError(final E e, final Object data) {
        return of(ResponseStatus.UNKNOWN_ERROR.getSuccess(), ResponseStatus.UNKNOWN_ERROR.getCode(), e.getMessage(), data);
    }
}
