package com.constant;

public final class Const {
    public static final String LOGIN_URI = "/login";

    private Const() {}
    // redis的key的命名规范： 项目名:模块名:功能名:唯一业务参数(比如用户id)
    public static final String REDIS_JWT_PREFIX = "em:user:logintoken:";

    // jwt过期时间7天
    public static final Long EXPIRE_TIME = 7 * 24 * 60 * 60L;

    // jwt过期时间30分钟
    public static final Long DEFAULT_EXPIRE_TIME = 30 * 60L;

    // 分页时每页显示5条数据
    public static final int PAGE_SIZE = 5;

    // token的名称,客户端发请求的时候,携带token的请求头的名称
    public static final String HK_TOKEN = "hk_token";


}
