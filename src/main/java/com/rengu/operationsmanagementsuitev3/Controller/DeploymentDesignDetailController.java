package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignDetailService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 13:13
 **/

@RestController
@RequestMapping(value = "/deploymentdesigndetails")
public class DeploymentDesignDetailController {

    private final DeploymentDesignDetailService deploymentDesignDetailService;

    @Autowired
    public DeploymentDesignDetailController(DeploymentDesignDetailService deploymentDesignDetailService) {
        this.deploymentDesignDetailService = deploymentDesignDetailService;
    }

    @DeleteMapping(value = "/{deploymentDesignDetailId}")
    public ResultEntity deleteDeploymentDesignDetailById(@PathVariable(value = "deploymentDesignDetailId") String deploymentDesignDetailId) {
        return ResultUtils.build(deploymentDesignDetailService.deleteDeploymentDesignDetailById(deploymentDesignDetailId));
    }

    // 根据部署设计Id及设备Id进行扫描
    @GetMapping(value = "/{deploymentDesignDetailId}/scan")
    public ResultEntity scanDeploymentDesignDetailsByDeploymentDesignAndDevice(@PathVariable(value = "deploymentDesignDetailId") String deploymentDesignDetailId, @RequestParam(value = "extensions", required = false, defaultValue = "") String... extensions) throws InterruptedException, ExecutionException, IOException {
        return ResultUtils.build(deploymentDesignDetailService.scanDeploymentDesignDetailsById(deploymentDesignDetailId, extensions, ""));
    }
}
