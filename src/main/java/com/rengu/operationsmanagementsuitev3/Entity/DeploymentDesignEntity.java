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
    private String type;
    private boolean deleted = false;
    private int exampleCount;



    public void setExampleCount(int exampleCount) {
        if(!(exampleCount >0)){
            exampleCount =1;
        }
        this.exampleCount = exampleCount;
    }

    @JsonIgnore
    @ManyToOne
    private ComponentEntity   componentEntity;

    @Override
    public String toString() {
        return "DeploymentDesignEntity{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", baseline=" + baseline +
                ", name='" + name + '\'' +
                ", description='" + type + '\'' +
                ", deleted=" + deleted +
                ", testCount=" + exampleCount +
                '}';
    }
}
