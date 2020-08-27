package com.mmall.service;


import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {

    // 用户登陆接口
    ServerResponse<User> login(String username, String password);
    // 用户注册接口
    ServerResponse<String> register(User user);
    // 用户校验接口
    ServerResponse<String> checkValid(String str, String type);
    // 忘记密码接口
    ServerResponse<String> selectQuestion(String username);
    // 密保问题校验接口
    ServerResponse<String> checkAnswer(String username, String question, String answer);
    // 忘记密码后的重置密码接口
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);
    // 登录状态下的重置密码接口
    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);
    // 更新用户信息接口
    ServerResponse<User> updateInformation(User user);
    // 获取用户信息接口
    ServerResponse<User> getInformation(Integer userId);
}
