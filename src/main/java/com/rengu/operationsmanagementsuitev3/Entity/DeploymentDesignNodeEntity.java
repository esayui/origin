package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-03 18:06
 **/

@Data
@Entity
public class DeploymentDesignNodeEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    @JsonIgnore
    @ManyToOne
    private DeviceEntity deviceEntity;
    private String params;

    // 0 未初始化  1 已初始化  2 运行中 3 运行结束 4 异常
    private int state = 0;
    @JsonIgnore
    @ManyToOne
    private DeploymentDesignEntity deploymentDesignEntity;




    @Override
    public String toString() {
        return "DeploymentDesignNodeEntity{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", deviceEntity=" + deviceEntity +
                ", params="+params+
                '}';
    }
}
