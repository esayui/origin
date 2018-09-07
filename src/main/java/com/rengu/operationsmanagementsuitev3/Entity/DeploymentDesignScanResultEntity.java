package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-07 10:10
 **/

@Data
public class DeploymentDesignScanResultEntity {

    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private DeploymentDesignDetailEntity deploymentDesignDetailEntity;
    private List<ScanResultEntity> correctFiles;
    private List<ScanResultEntity> modifyedFiles;
    private List<ScanResultEntity> unknownFiles;
    private List<ScanResultEntity> missingFiles;
}
