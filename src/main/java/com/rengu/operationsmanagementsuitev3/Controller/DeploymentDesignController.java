package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignNodeService;
import com.rengu.operationsmanagementsuitev3.Service.DeploymentDesignService;
import com.rengu.operationsmanagementsuitev3.Service.DeviceService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 实验模板
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 09:44
 **/

@Api(tags = {"3-实验管理","4-实验实例管理"})
@RestController
@RequestMapping(value = "/deploymentdesigns")
public class DeploymentDesignController {

    private final DeploymentDesignService deploymentDesignService;
    private final DeploymentDesignNodeService deploymentDesignNodeService;
    private final DeviceService deviceService;

    @Autowired
    public DeploymentDesignController(DeploymentDesignService deploymentDesignService, DeploymentDesignNodeService deploymentDesignNodeService, DeviceService deviceService) {
        this.deploymentDesignService = deploymentDesignService;
        this.deploymentDesignNodeService = deploymentDesignNodeService;
        this.deviceService = deviceService;
    }

    // 根据Id复制部署设计
    @PostMapping(value = "/{deploymentDesignId}/copy")
    public ResultEntity copyDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.copyDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id建立基线
    @PostMapping(value = "/{deploymentDesignId}/baseline")
    public ResultEntity baselineDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.baselineDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id删除部署设计
    @ApiOperation("根据实验Id删除实验")
    @DeleteMapping(value = "/{deploymentDesignId}")
    public ResultEntity deleteDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.deleteDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id撤销删除部署设计
    @ApiOperation("根据实验Id撤销删除实验")
    @PatchMapping(value = "/{deploymentDesignId}/restore")
    public ResultEntity restoreDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.restoreDeploymentDesignById(deploymentDesignId));
    }

    // 根据id清除部署设计
    @ApiOperation("根据实验Id清除（彻底删除）实验")
    @DeleteMapping(value = "/{deploymentDesignId}/clean")
    public ResultEntity cleanDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.cleanDeploymentDesignById(deploymentDesignId));
    }

    // 根据Id修改部署设计
    @ApiOperation("根据实验Id修改实验配置")
    @PatchMapping(value = "/{deploymentDesignId}")
    public ResultEntity updateDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId,DeploymentDesignEntity deploymentDesignArgs) {
        return ResultUtils.build(deploymentDesignService.updateDeploymentDesignById(deploymentDesignId, deploymentDesignArgs));
    }

    // 根据Id查询部署设计
    @ApiOperation("根据实验Id查询实验")
    @GetMapping(value = "/{deploymentDesignId}")
    public ResultEntity getDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignService.getDeploymentDesignById(deploymentDesignId));
    }

    @ApiOperation("根据应用Id查询实验参数配置列表")
    @GetMapping(value = "/{deploymentDesignId}/params")
    public ResultEntity getComponentParamsByComponent(@PathVariable(value = "deploymentDesignId") String deploymentId){
        return ResultUtils.build(deploymentDesignService.findParamsByDeploymentDesignId(deploymentId));
    }

    // 下发整个部署设计
    @ApiOperation("根据实验Id部署实验")
    @PutMapping(value = "/{deploymentDesignId}/deploy")
    public void deployDeploymentDesignById(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) throws IOException {
        deploymentDesignService.deployDeploymentDesignById(deploymentDesignId);
    }


    // 查询所有部署设计
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getDeploymentDesigns(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(deploymentDesignService.getDeploymentDesigns(pageable));
    }

    // 根据Id建立部署设计节点
//    @ApiOperation("根据实验Id创建实验实例")
//    @PostMapping(value = "/{deploymentDesignId}/deploymentdesignnode")
//    public ResultEntity saveDeploymentDesignNodeByDeploymentDesign(@PathVariable(value = "deploymentDesignId") String deploymentDesignId, DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
//        return ResultUtils.build(deploymentDesignNodeService.saveDeploymentDesignNodeByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId), deploymentDesignNodeEntity));
//    }

    @ApiOperation("根据实验Id生成实验实例")
    @PostMapping(value = "/{deploymentDesignId}/nodelist")
    public ResultEntity saveDeploymentDesignNodeByDeploymentDesign(@PathVariable(value = "deploymentDesignId") String deploymentDesignId,HttpServletRequest request) {
        String device_ip = getRemoteIP(request);




        return ResultUtils.build(deploymentDesignNodeService.createDeploymentDesignNodesByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId),device_ip));
    }



    // 根据Id和设备建立部署设计节点
    @PostMapping(value = "/{deploymentDesignId}/device/{deviceId}/deploymentdesignnode")
    public ResultEntity saveDeploymentDesignNodeByDeploymentDesignAndDevice(@PathVariable(value = "deploymentDesignId") String deploymentDesignId, @PathVariable(value = "deviceId") String deviceId, DeploymentDesignNodeEntity deploymentDesignNodeEntity) {
        return ResultUtils.build(deploymentDesignNodeService.saveDeploymentDesignNodeByDeploymentDesignAndDevice(deploymentDesignService.getDeploymentDesignById(deploymentDesignId), deploymentDesignNodeEntity, deviceService.getDeviceById(deviceId)));
    }

    // 根据Id查询部署设计节点
    @ApiOperation("根据实验Id查询实验实例列表")
    @GetMapping(value = "/{deploymentDesignId}/deploymentdesignnodes")
    public ResultEntity getDeploymentDesignNodesByDeploymentDesign(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignNodeService.getDeploymentDesignNodesByDeploymentDesign(pageable, deploymentDesignService.getDeploymentDesignById(deploymentDesignId)));
    }


    @ApiOperation("根据实验Id下载结果文件")
    @GetMapping(value ="/{deploymentDesignId}/downloadlog")
    public ResultEntity downloadResultFile(@PathVariable(value = "deploymentDesignId") String deploymentDesignId, HttpServletResponse response){
        return ResultUtils.build(deploymentDesignService.downloadResult(deploymentDesignId,response));

    }

    // 根据Id查询部署设计节点
    @GetMapping(value = "/{deploymentDesignId}/devices")
    public ResultEntity getDevicesByDeploymentDesign(@PathVariable(value = "deploymentDesignId") String deploymentDesignId) {
        return ResultUtils.build(deploymentDesignNodeService.getDevicesByDeploymentDesign(deploymentDesignService.getDeploymentDesignById(deploymentDesignId)));
    }

    public static String getRemoteIP(HttpServletRequest request) {
        String ip =null;
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null) {
            //对于通过多个代理的情况，最后IP为客户端真实IP,多个IP按照','分割
            int position = ip.indexOf(",");
            if (position > 0) {
                ip = ip.substring(0, position);
            }
        }
        return ip;
    }
}
