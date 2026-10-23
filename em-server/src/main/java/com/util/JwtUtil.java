package com.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.entity.UserInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.databind.type.LogicalType.Map;
@Component
public class JwtUtil {
    private final static String MY_KEY = "这是我的密钥ABC";
    public static String generateJwt(UserInfo user) {
        Map<String,Object> header = new HashMap<>();
        header.put("alg","HS256");
        header.put("typ","JWT");
        return JWT.create()
                .withHeader(header)
                .withClaim("userInfo",JsonUtil.toJsonStr(user))
                .sign(Algorithm.HMAC256(MY_KEY));
    }
    public static Boolean verifyJwt(String jwt){
        JWTVerifier v = JWT.require(Algorithm.HMAC256(MY_KEY)).build();
        try{
            v.verify(jwt);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public static UserInfo parseUserFromJwt(String jwt){
        try{
            JWTVerifier v = JWT.require(Algorithm.HMAC256(MY_KEY)).build();
            DecodedJWT decodedJWT = v.verify(jwt);
            Claim claim = decodedJWT.getClaim("userInfo");
            String json = claim.asString();
            return JsonUtil.toBean(json,UserInfo.class);
        } catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }


}
