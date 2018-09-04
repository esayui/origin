package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignDetailService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
