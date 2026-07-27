package com.spark.web.interceptor;

import com.spark.constant.ObjectCacheKey;
import com.spark.bean.system.entity.Session;
import com.spark.bean.base.ResultData;
import com.spark.bean.base.SessionHolder;
import com.spark.enums.ErrorCodeEnum;
import com.spark.config.redis.RedisService;
import com.spark.utils.JsonUtil;
import com.spark.utils.StringUtil;
import com.spark.utils.TraceLogUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * +++/\_/\
 * + ( °w° )=
 * +++)   (  //
 * + (__ __)//
 *
 * @author wangdiankun
 * @since 2024/3/4 22:00
 * 登录验证拦截器
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    @Autowired
    private RedisService redisService;

    /**
     * 重写预处理方法
     * （调用时间：controller方法处理之前，若返回false，则中断执行，注意：不会进入afterCompletion）
     *
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @return true 放行，false 中断执行
     * @throws IOException 抛出异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, @Nullable HttpServletResponse response,  @Nullable Object handler) throws IOException {
        String requestUrl = request.getRequestURI();
        logger.info("loginInterceptor request path={}",requestUrl);
        //检查是否登录
        boolean isLogin = this.checkLogin(request);
        if(!isLogin){
            ResultData<Void> result = new ResultData<>();
            result.setErrorCode(ErrorCodeEnum.NOT_LOGIN);
            if (response != null) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().print(JsonUtil.toString(result));
            }
            return false;
        }
        return true;
    }

    /**
     * 检查是否登录
     * @param request 请求
     * @return true 已登录，false 未登录
     */
    private boolean checkLogin(HttpServletRequest request) {
        String sessionId = request.getHeader("Authorization");
        logger.info("authorization sessionId={}",sessionId);
        if (StringUtil.isBlank(sessionId)) {
            logger.error("sessionId is null");
            return false;
        }
        String sessionIdKey = ObjectCacheKey.LOGIN_SESSION + sessionId;
        String loginUserJson = redisService.getValue(sessionIdKey);
        if (StringUtil.isBlank(loginUserJson)) {
            return false;
        }
        Session session = JsonUtil.toObject(loginUserJson, Session.class);
        SessionHolder.initLocalSession(session);
        // 记录全链路用户id
        TraceLogUtil.cacheTrackUserId(session.getUserId());
        // 延迟缓存时间
        redisService.expire(sessionIdKey, 60*60);
        return true;
    }

    /**
     * 重写处理请求之后方法
     * （调用前提：preHandle返回true，调用时间：Controller方法处理完之后，DispatcherServlet进行视图渲染之前，也就是说在这个方法中可以对ModelAndView进行操作）
     *
     * @param request  请求
     * @param response 响应
     * @param handler 处理器
     * @param modelAndView 模型和视图
     */
    @Override
    public void postHandle(@Nullable HttpServletRequest request,@Nullable HttpServletResponse response, @Nullable Object handler, ModelAndView modelAndView) {
    }

    /**
     * 重写处理之后方法
     * （调用前提：preHandle返回true，调用时间：DispatcherServlet进行视图的渲染之后）
     * 多用于清理资源
     * @param request 请求
     * @param response 响应
     * @param handler 处理器
     * @param ex 异常
     */
    @Override
    public void afterCompletion(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response, @Nullable Object handler, Exception ex) {
        // 一次请求处理完之后就清除 防止积累太多导致内存溢出
        SessionHolder.clearLocalSession();
        TraceLogUtil.removeUserId();
    }
}
