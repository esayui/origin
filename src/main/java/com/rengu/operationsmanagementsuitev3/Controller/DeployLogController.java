package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeployLogDetailService;
import com.rengu.operationsmanagementsuitev3.Service.DeployLogService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-06 18:24
 **/

@RestController
@RequestMapping(value = "/deploylogs")
public class DeployLogController {

    private final DeployLogService deployLogService;
    private final DeployLogDetailService deployLogDetailService;

    @Autowired
    public DeployLogController(DeployLogService deployLogService, DeployLogDetailService deployLogDetailService) {
        this.deployLogService = deployLogService;
        this.deployLogDetailService = deployLogDetailService;
    }

    // 根据Id删除部署日志
    @DeleteMapping(value = "/{deployLogId}")
    public ResultEntity deleteDeployLogById(@PathVariable(value = "deployLogId") String deployLogId) {
        return ResultUtils.build(deployLogService.deleteDeployLogById(deployLogId));
    }

    // 根据id查询部署日志
    @GetMapping(value = "/{deployLogId}")
    public ResultEntity getDeployLogById(@PathVariable(value = "deployLogId") String deployLogId) {
        return ResultUtils.build(deployLogService.getDeployLogById(deployLogId));
    }

    // 根据id查询部署日志详情
    @GetMapping(value = "/{deployLogId}/deploylogdetails")
    public ResultEntity getDeployLogDetailsByDeployLog(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "deployLogId") String deployLogId) {
        return ResultUtils.build(deployLogDetailService.getDeployLogDetailsByDeployLog(pageable, deployLogService.getDeployLogById(deployLogId)));
    }

    // 根据全部部署设计详情
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getDeployLogs(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(deployLogService.getDeployLogs(pageable));
    }
}
