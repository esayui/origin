package com.rengu.operationsmanagementsuitev3.Interceptor;

import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Author: XYmar
 * Date: 2019/9/26 14:07
 */
@Component
public class TimeInterceptor  implements HandlerInterceptor {
    //controller 调用之前被调用
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object handler) throws Exception {
        System.out.println("preHandle");

        System.out.println(((HandlerMethod)handler).getBean().getClass().getName());



        System.out.println(((HandlerMethod)handler).getMethod().getName());
        httpServletRequest.setAttribute("startTime",System.currentTimeMillis());
        return true;
    }

    //controller 调用之后被调用，如果有异常则不调用
    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

        System.out.println("postHandle");

        long startTime = (long) httpServletRequest.getAttribute("startTime");
        System.out.println("时间拦截器耗时:"+(System.currentTimeMillis() -startTime));
    }

    //controller 调用之后被调用，有没有异常都会被调用,Exception 参数里放着异常信息
    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        System.out.println("afterCompletion");
        long startTime = (long) httpServletRequest.getAttribute("startTime");
        System.out.println("时间拦截器耗时:"+(System.currentTimeMillis() -startTime));
    }
}

