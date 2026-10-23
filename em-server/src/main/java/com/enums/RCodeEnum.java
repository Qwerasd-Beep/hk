package com.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum RCodeEnum {
    OK(20000,"成功"),
    ERROR(-1,"失败"),
    TOKEN_IS_EMPTY(90100,"请求Token为空"),
    TOKEN_IS_ERROR(90200,"请求Token错误"),
    TOKEN_IS_EXPIRED(90300,"请求哦Token失效"),
    TOKEN_IS_NONE_MATCH(90400, "请求Token不匹配"),
    DATA_ACCESS_EXCEPTION(50000,"数据库操作失败"),
    ACCESS_DENIED(40100, "权限不足");

    private int code;
    private String msg;

}
class T{
    public static void main(String[] args) {
        RCodeEnum [] values = RCodeEnum.values();
        for(RCodeEnum value : values) {
            System.out.println(value.getCode()+"_"+value.getMsg());
        }
    }
}
