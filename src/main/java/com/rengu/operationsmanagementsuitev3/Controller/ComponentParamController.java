package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentParamEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ResultEntity;
import com.rengu.operationsmanagementsuitev3.Service.ComponentParamService;
import com.rengu.operationsmanagementsuitev3.Service.ComponentService;
import com.rengu.operationsmanagementsuitev3.Utils.ResultUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Author: XYmar
 * Date: 2019/9/19 15:49
 */

@RestController
@RequestMapping(value = "/componentparams")
public class ComponentParamController {


    private final ComponentService componentService;
    private final ComponentParamService componentParamService;

    @Autowired
    public ComponentParamController(ComponentService componentService,ComponentParamService componentParamService){
        this.componentParamService = componentParamService;
        this.componentService = componentService;
    }

    // 根据Id删除组件参数配置
    @DeleteMapping(value = "/{componentparamId}")
    public ResultEntity deleteComponentParamById(@PathVariable(value = "componentparamId") String componentparamId) throws IOException {
        return ResultUtils.build(componentParamService.deleteComponentParamById(componentparamId));
    }

    // 根据Id修改组件参数配置
    @PatchMapping(value = "/{componentparamId}")
    public ResultEntity updateComponentParamById(@PathVariable(value = "componentparamId") String componentparamId, ComponentParamEntity componentParamArgs) {
        return ResultUtils.build(componentParamService.updateComponentParamById(componentparamId, componentParamArgs));
    }


    //根据Id给参数配置赋值
    @PatchMapping(value = "/{componentparamId}/setValue")
    public ResultEntity  updateComponentParamValueById(@PathVariable(value = "componentparamId") String componentparamId, ComponentParamEntity componentParamArgs) {
        return ResultUtils.build(componentParamService.updateComponentParamValueById(componentparamId, componentParamArgs));
    }




    // 根据Id查询组件参数配置
    @GetMapping(value = "/{componentparamId}")
    public ResultEntity getComponentParamById(@PathVariable(value = "componentparamId") String componentparamId) {
        return ResultUtils.build(componentParamService.getComponentParamById(componentparamId));
    }




}
