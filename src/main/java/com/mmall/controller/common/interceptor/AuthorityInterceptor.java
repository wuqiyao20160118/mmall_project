package com.mmall.controller.common.interceptor;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * @author wqy
 * @version 1.0
 * @date 2020/9/29 22:53
 */
@Slf4j
public class AuthorityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        // 请求中Controller中的方法名
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        // 解析HandlerMethod
        String methodName = handlerMethod.getMethod().getName();
        String className = handlerMethod.getBean().getClass().getSimpleName();
        // 解析参数，即得到具体的参数key以及value
        StringBuilder requestParamBuffer = new StringBuilder();
        Map<String, String[]> parameterMap = httpServletRequest.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String mapKey = entry.getKey();
            String[] strs = entry.getValue();
            String mapValue = Arrays.toString(strs);

            requestParamBuffer.append(mapKey).append("=").append(mapValue);
        }

        // 直接转给controller进行登录操作，避免登陆操作死循环
        if (StringUtils.equals(className, "UserManageController") && StringUtils.equals(methodName, "login")) {
            // 如果是拦截到登录请求，不打印参数
            log.info("权限拦截器拦截到请求, className: {}, methodName: {}", className, methodName);
            return true;
        }

        log.info("权限拦截器拦截到请求, className: {}, methodName: {}, param: {}",
                className, methodName, requestParamBuffer.toString());

        User user = null;
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isNotEmpty(loginToken)) {
            String userJsonStr = RedisShardedUtil.get(loginToken);
            user = JsonUtil.string2Obj(userJsonStr, User.class);
        }

        if (user == null || (user.getRole() != Const.Role.ROLE_ADMIN)) {
            // 返回false，即拦截器执行了拦截
            // 但我们依然需要返回Json信息
            httpServletResponse.reset();  // 这里必须要添加reset()，否则报异常getWriter() has already been called for response
            // 防止乱码，并指定ContentType
            // 原先是在SpringMVC返回给客户端之前配置的messageConverter中执行的配置
            httpServletResponse.setCharacterEncoding("utf-8");
            httpServletResponse.setContentType("application/json;charset=UTF-8");

            PrintWriter out = httpServletResponse.getWriter();

            if (user == null) {
                // 富文本上传方法返回值为约定的map类型json
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "用户未登录，请登录");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("用户未登录")));
                }
            } else {
                if (StringUtils.equals(className, "ProductManageController") && StringUtils.equals(methodName, "richtextImgUpload")) {
                    Map resultMap = Maps.newHashMap();
                    resultMap.put("success", false);
                    resultMap.put("msg", "无权限操作，需要管理员权限");
                    out.print(JsonUtil.obj2String(resultMap));
                } else {
                    out.print(JsonUtil.obj2String(ServerResponse.createByErrorMessage("用户无操作权限")));
                }
            }
            out.flush();
            out.close();

            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }
}
