package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentHistoryEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.*;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 10:49
 **/

@RestController
@RequestMapping(value = "/deploymentdesignnodes")
public class DeploymentDesignNodeController {

    private final DeploymentDesignNodeService deploymentDesignNodeService;
    private final DeviceService deviceService;
    private final ComponentHistoryService componentHistoryService;
    private final DeploymentDesignDetailService deploymentDesignDetailService;
    private final ComponentService componentService;

    @Autowired
    public DeploymentDesignNodeController(DeploymentDesignNodeService deploymentDesignNodeService, DeviceService deviceService, ComponentHistoryService componentHistoryService, DeploymentDesignDetailService deploymentDesignDetailService, ComponentService componentService) {
        this.deploymentDesignNodeService = deploymentDesignNodeService;
        this.deviceService = deviceService;
        this.componentHistoryService = componentHistoryService;
        this.deploymentDesignDetailService = deploymentDesignDetailService;
        this.componentService = componentService;
    }

    // 根据Id删除部署设计节点
    @DeleteMapping(value = "/{deploymentDesignNodeId}")
    public ResultEntity deleteDeploymentDesignNodeById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignNodeService.deleteDeploymentDesignNodeById(deploymentDesignNodeId));
    }

    // 根据id查询部署设计节点
    @GetMapping(value = "/{deploymentDesignNodeId}")
    public ResultEntity getDeploymentDesignNodeById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId));
    }

    // 根据id部署部署设计节点
    @PutMapping(value = "/{deploymentDesignNodeId}/deploy")
    public void deployDeploymentDesignNodeById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) throws IOException {
        deploymentDesignNodeService.deployDeploymentDesignNodeById(deploymentDesignNodeId);
    }

    // 根据id解绑设备
    @PatchMapping(value = "/{deploymentDesignNodeId}/unbind")
    public ResultEntity unbindDeviceById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignNodeService.unbindDeviceById(deploymentDesignNodeId));
    }

    // 根据Id挂载设备
    @PatchMapping(value = "/{deploymentDesignNodeId}/device/{deviceId}/bind")
    public ResultEntity bindDeviceById(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @PathVariable(value = "deviceId") String deviceId) {
        return ResultUtils.build(deploymentDesignNodeService.bindDeviceById(deploymentDesignNodeId, deviceService.getDeviceById(deviceId)));
    }

    // 根据Id建立部署设计详情
    @PostMapping(value = "/{deploymentDesignNodeId}/deploymentdesigndetailbycomponenthistory")
    public ResultEntity saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistory(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @RequestParam(value = "componentHistoryId") String componentHistoryId) {
        return ResultUtils.build(deploymentDesignDetailService.saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistory(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), componentHistoryService.getComponentHistoryById(componentHistoryId)));
    }

    // 根据Id建立部署设计详情
    @PostMapping(value = "/{deploymentDesignNodeId}/deploymentdesigndetailbycomponent")
    public ResultEntity saveDeploymentDesignDetailByDeploymentDesignNodeAndComponent(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @RequestParam(value = "componentId") String componentId) {
        return ResultUtils.build(deploymentDesignDetailService.saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistory(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), componentHistoryService.getComponentHistoryByComponent(componentService.getComponentById(componentId))));
    }

    // 根据Id建立部署设计详情
    @PostMapping(value = "/{deploymentDesignNodeId}/deploymentdesigndetailbycomponenthistorys")
    public ResultEntity saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistorys(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @RequestParam(value = "componentHistoryIds") String[] componentHistoryIds) {
        List<ComponentHistoryEntity> componentHistoryEntityList = new ArrayList<>();
        for (String componentHistoryId : componentHistoryIds) {
            componentHistoryEntityList.add(componentHistoryService.getComponentHistoryById(componentHistoryId));
        }
        return ResultUtils.build(deploymentDesignDetailService.saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistorys(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), componentHistoryEntityList));
    }

    // 根据Id建立部署设计详情
    @PostMapping(value = "/{deploymentDesignNodeId}/deploymentdesigndetailbycomponents")
    public ResultEntity saveDeploymentDesignDetailByDeploymentDesignNodeAndComponents(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @RequestParam(value = "componentIds") String[] componentIds) {
        List<ComponentHistoryEntity> componentHistoryEntityList = new ArrayList<>();
        for (String componentId : componentIds) {
            componentHistoryEntityList.add(componentHistoryService.getComponentHistoryByComponent(componentService.getComponentById(componentId)));
        }
        return ResultUtils.build(deploymentDesignDetailService.saveDeploymentDesignDetailByDeploymentDesignNodeAndComponentHistorys(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), componentHistoryEntityList));
    }

    // 根据Id查询部署设计详情
    @GetMapping(value = "/{deploymentDesignNodeId}/deploymentdesigndetails")
    public ResultEntity getDeploymentDesignDetailsByDeploymentDesignNode(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId) {
        return ResultUtils.build(deploymentDesignDetailService.getDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId)));
    }

    // 根据部署设计Id及设备Id进行扫描
    @GetMapping(value = "/{deploymentDesignNodeId}/scan")
    public ResultEntity scanDeploymentDesignDetailsByDeploymentDesignAndDevice(@PathVariable(value = "deploymentDesignNodeId") String deploymentDesignNodeId, @RequestParam(value = "extensions", required = false, defaultValue = "") String... extensions) throws InterruptedException, ExecutionException, IOException {
        return ResultUtils.build(deploymentDesignDetailService.scanDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeService.getDeploymentDesignNodeById(deploymentDesignNodeId), extensions));
    }
}
