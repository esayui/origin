package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Service.*;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:55
 **/


@RestController
@RequestMapping(value = "/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final DeviceService deviceService;
    private final ComponentService componentService;
    private final DeploymentDesignService deploymentDesignService;
    private final DeployLogService deployLogService;
    private final UserService userService;

    @Autowired
    public ProjectController(ProjectService projectService, DeviceService deviceService, ComponentService componentService, DeploymentDesignService deploymentDesignService, DeployLogService deployLogService, UserService userService) {
        this.projectService = projectService;
        this.deviceService = deviceService;
        this.componentService = componentService;
        this.deploymentDesignService = deploymentDesignService;
        this.deployLogService = deployLogService;
        this.userService = userService;
    }



    // 根据Id删除工程
    @DeleteMapping(value = "/{projectId}")
    public ResultEntity deleteProjectById(@PathVariable(value = "projectId") String projectId) {
        return ResultUtils.build(projectService.deleteProjectById(projectId));
    }

    // 根据Id还原工程
    @PatchMapping(value = "/{projectId}/restore")
    public ResultEntity restoreProjectById(@PathVariable(value = "projectId") String projectId) {
        return ResultUtils.build(projectService.restoreProjectById(projectId));
    }

    // 根据Id彻底删除工程
    @DeleteMapping(value = "/{projectId}/clean")
    public ResultEntity cleanProjectById(@PathVariable(value = "projectId") String projectId) throws IOException {
        return ResultUtils.build(projectService.cleanProjectById(projectId));
    }

    // 根据Id修改工程
    @PatchMapping(value = "/{projectId}")
    public ResultEntity updateProjectById(@PathVariable(value = "projectId") String projectId, ProjectEntity projectArgs) {
        return ResultUtils.build(projectService.updateProjectById(projectId, projectArgs));
    }

    // 根据Id查询工程
    @GetMapping(value = "/{projectId}")
    public ResultEntity getProjectById(@PathVariable(value = "projectId") String projectId) {
        return ResultUtils.build(projectService.getProjectById(projectId));
    }

    // 查询所有工程
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getProjects(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(projectService.getProjects(pageable));
    }

    // 添加设置星标状态
    @PatchMapping(value = "/{projectId}/star")
    public ResultEntity starProjectById(@PathVariable(value = "projectId") String projectId, @RequestParam(value = "hasStar") boolean hasStar) {
        return ResultUtils.build(projectService.starProjectById(projectId, hasStar));
    }



//    // 根据Id创建组件
//    @PostMapping(value = "/{projectId}/component")
//    public ResultEntity saveComponentByProject(@PathVariable(value = "projectId") String projectId, ComponentEntity componentEntity) {
//        return ResultUtils.build(componentService.saveComponentByProject(projectService.getProjectById(projectId), componentEntity));
//    }
//
//    // 根据Id查询组件
//    @GetMapping(value = "/{projectId}/component")
//    public ResultEntity getComponentsByDeletedAndProject(@PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
//        return ResultUtils.build(componentService.getComponentsByDeletedAndProject(deleted, projectService.getProjectById(projectId)));
//    }
//
//    // 根据Id查询组件
//    @GetMapping(value = "/{projectId}/components")
//    public ResultEntity getComponentsByDeletedAndProject(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
//        return ResultUtils.build(componentService.getComponentsByDeletedAndProject(pageable, deleted, projectService.getProjectById(projectId)));
//    }
//
//    // 根据Id查询组件数量
//    @GetMapping(value = "/{projectId}/componentcounts")
//    public ResultEntity countComponentsByDeletedAndProject(@PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
//        return ResultUtils.build(componentService.countComponentsByDeletedAndProject(deleted, projectService.getProjectById(projectId)));
//    }



    // 移交工程管理用户
    @PatchMapping(value = "/{projectId}/users/{userId}/transfer")
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity transferProjectByUser(@PathVariable(value = "projectId") String projectId, @PathVariable(value = "userId") String userId) {
        return ResultUtils.build(projectService.transferProjectByUser(projectId, userService.getUserById(userId)));
    }
}
