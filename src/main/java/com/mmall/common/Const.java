package com.mmall.common;

// Const类的作用
// 减少硬编码，一些常用的信息可以封装入常量类中，不用每次创建一个变量进行复制，便于维护
// 当需要维护时，只需要修改常量类中的数据，所有引用都会改变，不需要一个个去查找、修改
public class Const {

    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    // 内部接口类对用户进行分组
    // 当一组常量数量较少时，在类中使用内部接口对常量进行分组
    // 较多时可以使用枚举类enum
    public interface Role {
        int ROLE_CUSTOMER = 0; // 普通用户
        int ROLE_ADMIN = 1;  // 管理员
    }
}
