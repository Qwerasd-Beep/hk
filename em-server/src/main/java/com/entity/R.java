package com.entity;

import com.enums.RCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class R {
    private int code;
    private String msg;
    private Object data;

    public static R OK(){
        return R.builder()
                .code(RCodeEnum.OK.getCode())
                .msg(RCodeEnum.OK.getMsg())
                .build();
    }
    public static  R OK(int code, String msg) {
        return R.builder()
                .code(code)
                .msg(msg)
                .build();
    }

    public static    R  OK(Object data) {
        return R.builder()
                .code(RCodeEnum.OK.getCode())
                .msg(RCodeEnum.OK.getMsg())
                .data(data)
                .build();
    }

    public static R OK(RCodeEnum codeEnum) {
        return R.builder()
                .code(codeEnum.getCode())
                .msg(codeEnum.getMsg())
                .build();
    }

    public static R ERROR() {
        return R.builder()
                .code(RCodeEnum.ERROR.getCode())
                .msg(RCodeEnum.ERROR.getMsg())
                .build();
    }

    public static R ERROR(String msg) {
        return R.builder()
                .code(RCodeEnum.ERROR.getCode())
                .msg(msg)
                .build();
    }

    public static R  ERROR(RCodeEnum codeEnum) {
        return R.builder()
                .code(codeEnum.getCode())
                .msg(codeEnum.getMsg())
                .build();
    }

}
