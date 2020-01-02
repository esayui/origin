package com.rengu.operationsmanagementsuitev3.Controller;


import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileParamEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentParamEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileParamService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentFileService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentParamService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Api(tags = "2-2-1-应用版本参数管理")
@RestController
@RequestMapping(value = "/componentfileparams")
public class ComponentFileParamController {
    private final ComponentFileService componentFileService;
    private final ComponentFileParamService componentFileParamService;

    @Autowired
    public ComponentFileParamController(ComponentFileService componentFileService,ComponentFileParamService componentFileParamService){
        this.componentFileService = componentFileService;
        this.componentFileParamService = componentFileParamService;
    }

    // 根据Id删除组件参数配置
    @ApiOperation("根据应用版本文件Id添加参数配置")
    @PostMapping(value = "/{componentFileId}")
    public ResultEntity addComponentFileParamById(@PathVariable(value = "componentFileId") String componentFileId,ComponentFileParamEntity componentFileParamEntity) throws IOException {
        return ResultUtils.build(componentFileParamService.saveComponentFileParamByComponentFile(componentFileService.getComponentFileById(componentFileId),componentFileParamEntity));
    }


    // 根据Id删除组件参数配置
    @ApiOperation("根据应用版本文件参数Id删除指定参数配置")
    @DeleteMapping(value = "/{componentFileParamId}")
    public ResultEntity deleteComponentFileParamById(@PathVariable(value = "componentFileParamId") String componentFileParamId) throws IOException {
        return ResultUtils.build(componentFileParamService.deleteComponentFileParamById(componentFileParamId));
    }

    // 根据Id修改组件参数配置
    @ApiOperation("根据应用版本文件参数Id修改指定参数配置")
    @PatchMapping(value = "/{componentFileParamId}")
    public ResultEntity updateComponentFileParamById(@PathVariable(value = "componentFileParamId") String componentFileParamId,@RequestBody ComponentFileParamEntity componentFileParamArgs) {
        return ResultUtils.build(componentFileParamService.updateComponentFileParamById(componentFileParamId, componentFileParamArgs));
    }

    //根据Id给参数配置赋值
    @ApiOperation("根据应用版本参数Id给指定参数配置赋值")
    @PatchMapping(value = "/{componentFileParamId}/setValue")
    public ResultEntity  updateComponentFileParamValueById(@PathVariable(value = "componentFileParamId") String componentFileParamId,@RequestBody ComponentFileParamEntity componentFileParamArgs) {
        return ResultUtils.build(componentFileParamService.updateComponentFileParamValueById(componentFileParamId, componentFileParamArgs));
    }


    // 根据Id查询组件参数配置
    @ApiOperation("根据应用版本参数Id查看指定参数配置")
    @GetMapping(value = "/{componentFileParamId}")
    public ResultEntity getComponentFileParamById(@PathVariable(value = "componentFileParamId") String componentFileParamId) {
        return ResultUtils.build(componentFileParamService.getComponentFileParamById(componentFileParamId));
    }

}
