package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
 * @create: 2018-09-03 17:30
 **/

@Data
@Entity
public class DeploymentDesignEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private boolean baseline = false;
    private String name;
    private String description;
    private boolean deleted = false;
    private int testCount;


    public void setTestCount(int testCount) {
        if(!(testCount >0)){
            testCount =1;
        }
        this.testCount = testCount;
    }

    @ManyToOne
    private ComponentEntity   componentEntity;

    @Override
    public String toString() {
        return "DeploymentDesignEntity{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", baseline=" + baseline +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", deleted=" + deleted +
                ", testCount=" + testCount +
                '}';
    }
}
