package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentHistoryService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentParamService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 14:42
 **/

@RestController
@RequestMapping(value = "/components")
public class ComponentController {

    private final ComponentService componentService;
    private final ComponentFileService componentFileService;
    private final ComponentHistoryService componentHistoryService;
    private final ComponentParamService componentParamService;

    @Autowired
    public ComponentController(ComponentService componentService, ComponentFileService componentFileService, ComponentHistoryService componentHistoryService,ComponentParamService componentParamService) {
        this.componentService = componentService;
        this.componentFileService = componentFileService;
        this.componentHistoryService = componentHistoryService;
        this.componentParamService = componentParamService;
    }




    // 根据id复制组件
    @PostMapping(value = "/{componentId}/copy")
    public ResultEntity copyComponentById(@PathVariable(value = "componentId") String componentId) {
        ComponentEntity componentArgs = componentService.getComponentById(componentId);
        ComponentEntity componentEntity = componentService.copyComponentById(componentArgs);
        componentFileService.copyComponentFileByComponent(componentArgs, componentEntity);
        return ResultUtils.build(componentEntity);
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
    public ResultEntity cleanComponentById(@PathVariable(value = "componentId") String componentId) throws IOException {
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

    //根据Id创建组件参数配置
    @PostMapping(value = "/{componentId}/params")
    public ResultEntity saveComponentParamsByComponent(@PathVariable(value = "componentId") String componentId,@RequestBody ComponentParamEntity[] componentParamEntities){
        return ResultUtils.build(componentParamService.saveComponentParamsByComponent(componentService.getComponentById(componentId),componentParamEntities));
    }





    // 根据Id查询组件历史
    @GetMapping(value = "/{componentId}/history")
    public ResultEntity getComponentHistorysByComponent(@PageableDefault(sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable, @PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentHistoryService.getComponentHistorysByComponent(pageable, componentService.getComponentById(componentId)));
    }

    // 根据Id查询组件历史
    @GetMapping(value = "/{componentId}/historys")
    public ResultEntity getComponentHistorysByComponent(@PathVariable(value = "componentId") String componentId) {
        return ResultUtils.build(componentHistoryService.getComponentHistorysByComponent(componentService.getComponentById(componentId)));
    }

    // 根据id和父节点Id创建文件夹
    @PostMapping(value = "/{componentId}/createfolder")
    public ResultEntity saveComponentFileByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId, ComponentFileEntity componentFileEntity) {
        return ResultUtils.build(componentFileService.saveComponentFileByParentNodeAndComponent(componentService.getComponentById(componentId), parentNodeId, componentFileEntity));
    }

    // 根据id和父节点Id创建文件
    @PostMapping(value = "/{componentId}/uploadfiles")
    public ResultEntity saveComponentFilesByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId, @RequestBody List<FileMetaEntity> fileMetaEntityList) {
        return ResultUtils.build(componentFileService.saveComponentFilesByParentNodeAndComponent(componentService.getComponentById(componentId), parentNodeId, fileMetaEntityList));
    }

    // 根据id和父节点查询组件文件
    @GetMapping(value = "/{componentId}/files")
    public ResultEntity getComponentFilesByParentNodeAndComponent(@PathVariable(value = "componentId") String componentId, @RequestHeader(value = "parentNodeId", required = false, defaultValue = "") String parentNodeId) {
        return ResultUtils.build(componentFileService.getComponentFilesByParentNodeAndComponent(parentNodeId, componentService.getComponentById(componentId)));
    }

    // 根据id导出组件文件
    @GetMapping(value = "/{componentId}/export")
    public void exportComponentFileByComponent(@PathVariable(value = "componentId") String componentId, HttpServletResponse httpServletResponse) throws IOException {
        File exportFile = componentFileService.exportComponentFileByComponent(componentService.getComponentById(componentId));
        String mimeType = URLConnection.guessContentTypeFromName(exportFile.getName()) == null ? "application/octet-stream" : URLConnection.guessContentTypeFromName(exportFile.getName());
        httpServletResponse.setContentType(mimeType);
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + new String(exportFile.getName().getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
        httpServletResponse.setContentLengthLong(exportFile.length());
        // 文件流输出
        IOUtils.copy(new FileInputStream(exportFile), httpServletResponse.getOutputStream());
        httpServletResponse.flushBuffer();
    }
}
