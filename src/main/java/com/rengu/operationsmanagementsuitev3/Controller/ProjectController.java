package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ProjectService;
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
 * @create: 2018-08-22 17:55
 **/


@RestController
@RequestMapping(value = "/projects")
public class ProjectController {

    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
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
    public ResultEntity cleanProjectById(@PathVariable(value = "projectId") String projectId) {
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

    // 根据Id创建设备
    @PostMapping(value = "/{projectId}/device")
    public ResultEntity saveDeviceByProject(@PathVariable(value = "projectId") String projectId, DeviceEntity deviceEntity) {
        return ResultUtils.build(projectService.saveDeviceByProject(projectId, deviceEntity));
    }

    // 根据Id查询设备
    @GetMapping(value = "/{projectId}/devices")
    public ResultEntity getDevicesByDeletedAndProject(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
        return ResultUtils.build(projectService.getDevicesByDeletedAndProject(pageable, projectId, deleted));
    }

    // 根据Id查询设备数量
    @GetMapping(value = "/{projectId}/devicecounts")
    public ResultEntity countDevicesByDeletedAndProject(@PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
        return ResultUtils.build(projectService.countDevicesByDeletedAndProject(projectId, deleted));
    }

    // 根据Id创建组件
    @PostMapping(value = "/{projectId}/component")
    public ResultEntity saveComponentByProject(@PathVariable(value = "projectId") String projectId, ComponentEntity componentEntity) {
        return ResultUtils.build(projectService.saveComponentByProject(projectId, componentEntity));
    }

    // 根据Id查询组件
    @GetMapping(value = "/{projectId}/components")
    public ResultEntity getComponentsByDeletedAndProject(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
        return ResultUtils.build(projectService.getComponentsByDeletedAndProject(pageable, projectId, deleted));
    }

    // 根据Id查询组件数量
    @GetMapping(value = "/{projectId}/componentcounts")
    public ResultEntity countComponentsByDeletedAndProject(@PathVariable(value = "projectId") String projectId, @RequestParam(value = "deleted") boolean deleted) {
        return ResultUtils.build(projectService.countComponentsByDeletedAndProject(projectId, deleted));
    }
}
