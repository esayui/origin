package com.rengu.operationsmanagementsuitev3.Controller;

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
}
