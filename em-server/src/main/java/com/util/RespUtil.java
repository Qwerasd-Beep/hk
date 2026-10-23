package com.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
@Component
public class RespUtil {
    public static void write(HttpServletResponse response,String s){
        response.setContentType("text/html;charset=utf-8");
        try{
            PrintWriter out = response.getWriter();
            out.print(s);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
