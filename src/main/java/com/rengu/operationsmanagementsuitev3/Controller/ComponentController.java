package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileMetaEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 14:42
 **/

@RestController
@RequestMapping(value = "/components")
public class ComponentController {

    private final ComponentService componentService;

    @Autowired
    public ComponentController(ComponentService componentService) {
        this.componentService = componentService;
    }

    // 根据id复制组件
    @PostMapping(value = "/{componentId}/copy")
    public ResultEntity copyComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.copyComponentById(componentId));
    }

    // 根据Id删除组件
    @DeleteMapping(value = "/{componentId}")
    public ResultEntity deleteComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.deleteComponentById(componentId));
    }

    // 根据Id撤销删除组件
    @PatchMapping(value = "/{componentId}/restore")
    public ResultEntity restoreComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.restoreComponentById(componentId));
    }

    // 根据id清除组件
    @DeleteMapping(value = "/{componentId}/clean")
    public ResultEntity cleanComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.cleanComponentById(componentId));
    }

    // 根据Id修改组件
    @PatchMapping(value = "/{componentId}")
    public ResultEntity updateComponentById(@PathVariable(value = "componentId") String componentId, ComponentEntity componentArgs) {
        return ResultUtils.build(componentService.updateComponentById(componentId, componentArgs));
    }

    // 根据Id查询组件
    @GetMapping(value = "/{componentId}")
    public ResultEntity getComponentById(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentService.getComponentById(componentId));
    }

    // 查询所有组件
    @GetMapping
    @PreAuthorize(value = "hasRole('admin')")
    public ResultEntity getComponents(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResultUtils.build(componentService.getComponents(pageable));
    }

    // 根据id和父节点Id创建文件夹
    @PostMapping(value = "/{componentId}/createfolder")
    public ResultEntity saveComponentFileByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId, ComponentFileEntity componentFileEntity) {
        return ResultUtils.build(componentService.saveComponentFileByParentNodeAndComponent(componentId, parentNodeId, componentFileEntity));
    }

    // 根据id和父节点Id创建文件
    @PostMapping(value = "/{componentId}/uploadfiles")
    public ResultEntity saveComponentFilesByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId, @RequestBody List<FileMetaEntity> fileMetaEntityList) {
        return ResultUtils.build(componentService.saveComponentFilesByParentNodeAndComponent(componentId, parentNodeId, fileMetaEntityList));
    }

    // 根据id和父节点查询组件文件
    @GetMapping(value = "/{componentId}/files")
    public ResultEntity getComponentFilesByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId) {
        return ResultUtils.build(componentService.getComponentFilesByParentNodeAndComponent(componentId, parentNodeId));
    }
}
