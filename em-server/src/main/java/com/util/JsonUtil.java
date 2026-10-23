package com.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    /**
     * 把对象转化为Json字符串，注意Date类型会转成数值类型的时间戳格式
     */
    public static String toJsonStr(Object o){
        try{
            return new ObjectMapper().writeValueAsString(o);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 把json格式的字符串转成对象
     */
    public static <T> T toBean(String jsonStr, Class<T> c){
        try{
            return new ObjectMapper().readValue(jsonStr,c);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
