package com.rengu.operationsmanagementsuitev3.Aspect;

import com.rengu.operationsmanagementsuitev3.Controller.ProjectController;
import com.rengu.operationsmanagementsuitev3.Controller.UserController;
import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Service.UserActionLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:15
 **/


@Slf4j
@Aspect
@Component
public class LogAspect {

    private final UserActionLogService userActionLogService;

    @Autowired
    public LogAspect(UserActionLogService userActionLogService) {
        this.userActionLogService = userActionLogService;
    }

    @Pointcut(value = "execution(public * com.rengu.operationsmanagementsuitev3.Controller..*(..))")
    private void requestPonitCut() {
    }

    @Before(value = "requestPonitCut()")
    public void doBefore(JoinPoint joinPoint) {
        // 获取Http请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        // 记录文件日志
        LogEntity logEntity = new LogEntity();
        logEntity.setUsername(httpServletRequest.getUserPrincipal() == null ? "未知用户" : httpServletRequest.getUserPrincipal().getName());
        logEntity.setHostAddress(httpServletRequest.getRemoteAddr());
        logEntity.setRquestMethod(httpServletRequest.getMethod());
        logEntity.setUrl(httpServletRequest.getRequestURI());
        logEntity.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        logEntity.setCallMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info(logEntity.toString());
    }

    @AfterReturning(pointcut = "requestPonitCut()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, ResultEntity result) {
        // 获取Http请求对象
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = servletRequestAttributes.getRequest();
        if (httpServletRequest.getUserPrincipal() != null) {
            String username = httpServletRequest.getUserPrincipal().getName();
            int object = UserActionLogService.ERROR_OBJECT;
            int type = UserActionLogService.ERROR_TYPE;
            String description = "";
            // 用户接口
            if (joinPoint.getTarget().getClass().equals(UserController.class)) {
                switch (joinPoint.getSignature().getName()) {
                    case "saveAdminUser": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，创建管理员用户：" + userEntity.getUsername();
                        break;
                    }
                    case "deleteUserById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.DELETE_TYPE;
                        description = "用户：" + username + "，根据Id删除用户：" + userEntity.getUsername();
                        break;
                    }
                    case "userUpgradeById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，升级用户：" + userEntity.getUsername() + "为管理员";
                        break;
                    }
                    case "userDegradeById": {
                        UserEntity userEntity = (UserEntity) result.getData();
                        object = UserActionLogService.USER_OBJECT;
                        type = UserActionLogService.UPDATE_TYPE;
                        description = "用户：" + username + "，降级管理员：" + userEntity.getUsername() + "为普通用户";
                        break;
                    }
                    case "saveProjectById": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.CREATE_TYPE;
                        description = "用户：" + username + "，创建工程：" + projectEntity.getName();
                        break;
                    }
                    default:
                }
            }
            // 工程接口
            if (joinPoint.getTarget().getClass().equals(ProjectController.class)) {
                switch (joinPoint.getSignature().getName()) {
                    case "deleteProjectById": {
                        ProjectEntity projectEntity = (ProjectEntity) result.getData();
                        object = UserActionLogService.PROJECT_OBJECT;
                        type = UserActionLogService.DELETE_TYPE;
                        description = "用户：" + username + "，删除工程：" + projectEntity.getName();
                        break;
                    }
                    default:
                }
            }
            if (object != UserActionLogService.ERROR_OBJECT || type != UserActionLogService.ERROR_TYPE || !StringUtils.isEmpty(description)) {
                UserActionLogEntity userActionLogEntity = new UserActionLogEntity();
                userActionLogEntity.setUsername(username);
                userActionLogEntity.setObject(object);
                userActionLogEntity.setType(type);
                userActionLogEntity.setDescription(description);
                userActionLogService.saveUserActionLog(userActionLogEntity);
            }
        }
    }

    @AfterThrowing(pointcut = "requestPonitCut()", throwing = "exception")
    public void doAfterThrowing(JoinPoint joinPoint, RuntimeException exception) {
    }

    @After(value = "requestPonitCut()")
    public void doAfter(JoinPoint joinPoint) {
    }
}
