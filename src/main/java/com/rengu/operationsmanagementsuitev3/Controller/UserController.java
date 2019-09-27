package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Entity.UserEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Service.ProjectService;
import com.rengu.operationsmanagementsuitev3.Service.UserActionLogService;
import com.rengu.operationsmanagementsuitev3.Service.UserService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:13
 **/


@RestController
@RequestMapping(value = "/users")
public class UserController {

    private final UserService userService;
    private final ComponentService componentService;
    private final UserActionLogService userActionLogService;

    @Autowired
    public UserController(UserService userService, ComponentService componentService, UserActionLogService userActionLogService) {
        this.userService = userService;
        this.componentService = componentService;
        this.userActionLogService = userActionLogService;
    }

    // 保存普通用户
    @PostMapping(value = "/user")
    public ResultEntity saveDefaultUser(UserEntity userEntity) {
        return ResultUtils.build(userService.saveDefaultUser(userEntity));
    }

    //保存管理员用户
    @PostMapping(value = "/admin")
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity saveAdminUser(UserEntity userEntity) {
        return ResultUtils.build(userService.saveAdminUser(userEntity));
    }

    // 删除用户
    @DeleteMapping(value = "/{userId}")
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity deleteUserById(@PathVariable(value = "userId") String userId) {
        return ResultUtils.build(userService.deleteUserById(userId));
    }

    // 根据Id修改密码
    @PatchMapping(value = "/{userId}/password")
    public ResultEntity updatePasswordById(@PathVariable(value = "userId") String userId, @RequestParam(value = "password") String password) {
        return ResultUtils.build(userService.updateUserPasswordById(userId, password));
    }

    // 根据Id升级用户
    @PatchMapping(value = "/{userId}/upgrade")
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity userUpgradeById(@PathVariable(value = "userId") String userId) {
        return ResultUtils.build(userService.userUpgradeById(userId));
    }

    // 根据Id降级用户
    @PatchMapping(value = "/{userId}/degrade")
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity userDegradeById(@PathVariable(value = "userId") String userId) {
        return ResultUtils.build(userService.userDegradeById(userId));
    }

    // 根据id查询用户
    @GetMapping(value = "/{userId}")
    public ResultEntity getUserById(@PathVariable(value = "userId") String userId) {
        return ResultUtils.build(userService.getUserById(userId));
    }

    // 查询所有用户
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getUsers(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(userService.getUsers(pageable));
    }

    // 根据Id查询用户操作日志
    @GetMapping(value = "/{userId}/useractionlogs")
    public ResultEntity getUserActionLogsByUsername(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "userId") String userId) {
        return ResultUtils.build(userActionLogService.getUserActionLogsByUsername(pageable, userService.getUserById(userId).getUsername()));
    }

    // 根据Id创建组件
    @PostMapping(value = "/{userId}/component")
    public ResultEntity saveComponentByUser(@PathVariable(value = "userId") String userId, ComponentEntity componentEntity) {
        return ResultUtils.build(componentService.saveComponentByUser(userService.getUserById(userId), componentEntity));
    }

    // 根据Id查询组件
    @GetMapping(value = "/{userId}/component")
    public ResultEntity getComponentsByDeletedAndUser(@PathVariable(value = "userId") String userId, @RequestParam(value = "deleted") boolean deleted) {
        return ResultUtils.build(componentService.getComponentsByDeletedAndUser(deleted, userService.getUserById(userId)));
    }

    // 根据Id查询组件
    @GetMapping(value = "/{userId}/components")
    public ResultEntity getComponentsByDeletedAndUser(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "userId") String userId, @RequestParam(value = "deleted") boolean deleted) {
        return ResultUtils.build(componentService.getComponentsByDeletedAndUser(pageable, deleted, userService.getUserById(userId)));
    }




//    // 根据id创建工程
//    @PostMapping(value = "/{userId}/project")
//    public ResultEntity saveProjectByUser(@PathVariable(value = "userId") String userId, ProjectEntity projectEntity) {
//        return ResultUtils.build(projectService.saveProjectByUser(projectEntity, userService.getUserById(userId)));
//    }
//
//    // 根据用户id查询工程
//    @GetMapping(value = "/{userId}/projects")
//    public ResultEntity getProjectsByDeletedAndUser(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "userId") String userId, @RequestParam(value = "deleted") boolean deleted) {
//        return ResultUtils.build(projectService.getProjectsByDeletedAndUser(pageable, deleted, userService.getUserById(userId)));
//    }
}
