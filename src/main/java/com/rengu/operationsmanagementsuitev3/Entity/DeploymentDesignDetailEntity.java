package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 09:07
 **/

@Data
@Entity
public class DeploymentDesignDetailEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @JsonIgnore
    @ManyToOne
    private DeploymentDesignNodeEntity deploymentDesignNodeEntity;
    @JsonIgnore
    @ManyToOne
    private ComponentHistoryEntity componentHistoryEntity;
    @JsonIgnore
    @ManyToOne
    private ComponentEntity componentEntity;
    @JsonIgnore
    @ManyToOne
    private DeploymentDesignEntity deploymentDesignEntity;



}
