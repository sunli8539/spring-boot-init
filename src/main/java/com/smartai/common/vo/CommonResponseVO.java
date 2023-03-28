package com.smartai.common.vo;

import com.smartai.common.constants.CommonConstant;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class CommonResponseVO<T> implements Serializable {
    private static final long serialVersionUID = -355013070372364263L;

    private String code;

    private String message;

    private T data;

    public CommonResponseVO() {

    }

    public CommonResponseVO(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponseVO<T> buildSuccess() {
        return new CommonResponseVO<>(CommonConstant.SUCCESS_CODE, CommonConstant.SUCCESS_MSG, null);
    }

    public static <T> CommonResponseVO<T> buildSuccess(T data) {
        return new CommonResponseVO<>(CommonConstant.SUCCESS_CODE, CommonConstant.SUCCESS_MSG, data);
    }

    public static <T> CommonResponseVO<T> buildError() {
        return new CommonResponseVO<>(CommonConstant.FAILURE_CODE, CommonConstant.FAILURE_MSG, null);
    }

    public static <T> CommonResponseVO<T> buildError(String msg) {
        return new CommonResponseVO<>(CommonConstant.FAILURE_CODE, msg, null);

    }

}
