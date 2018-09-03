package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileHistoryService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentHistoryService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-03 14:08
 **/

@RestController
@RequestMapping(value = "/componenthistorys")
public class ComponentHistoryController {

    private final ComponentHistoryService componentHistoryService;
    private final ComponentFileHistoryService componentFileHistoryService;

    @Autowired
    public ComponentHistoryController(ComponentHistoryService componentHistoryService, ComponentFileHistoryService componentFileHistoryService) {
        this.componentHistoryService = componentHistoryService;
        this.componentFileHistoryService = componentFileHistoryService;
    }

    // 根据组件历史Id查询组件历史
    @GetMapping(value = "/{componentHistoryId}")
    public ResultEntity getComponentHistoryById(@PathVariable(value = "componentHistoryId") String componentHistoryId) {
        return ResultUtils.build(componentHistoryService.getComponentHistoryById(componentHistoryId));
    }

    // 根据id和父节点查询组件文件
    @GetMapping(value = "/{componentHistoryId}/files")
    public ResultEntity getComponentFileHistorysByParentNodeAndComponentHistory(@PathVariable(value = "componentHistoryId") String componentHistoryId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId) {
        return ResultUtils.build(componentFileHistoryService.getComponentFileHistorysByParentNodeAndComponentHistory(parentNodeId, componentHistoryService.getComponentHistoryById(componentHistoryId)));
    }
}
