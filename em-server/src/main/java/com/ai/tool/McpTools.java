package com.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class McpTools {
    @Tool(description ="查询当前系统时间")
    public String getTime(){
        SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd E hh时mm分ss秒S毫秒");
        return df.format(new Date());
    }

    @Tool(description ="根据城市名称查询所属省份")
    public String getCityProvince(@ToolParam(description = "城市或地区的名称")  String cityName){
        Map<String,String> m=new HashMap<>();
        m.put("哈尔滨", "北方的黑龙江省");
        m.put("北京", "袁绍曾经据守的河北省");
        m.put("桂洲", "靠近南方的云南省");
        m.put("抚顺", "历史悠久的辽宁省");
        m.put("幽州", "黑龙江省");
        return  m.getOrDefault(cityName,"未查到,请去百度查");
    }
}
