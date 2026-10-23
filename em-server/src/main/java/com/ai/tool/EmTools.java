package com.ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmTools {
    @Tool(description = "根据企业id查询企业信息")
    public Map<String, Object> queryForeignEnterprises(
            @ToolParam(required = true,  description =  "企业的id,或企业的编号")
            String id
    )	 {
        Map m=Map.of("1","恒大","2","男足","3","小米","4","碧桂园");
        return Map.of("状态码", 200 ,"data", m.get(id));
    }
}




