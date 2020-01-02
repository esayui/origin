package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/9/29 13:09
 */
@Data
@Entity
public class DeploymentDesignParamEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private int type ;
    private String description;
    private String value;

    @JsonIgnore
    @ManyToOne
    private DeploymentDesignEntity deploymentDesignEntity;


    @Override
    public String toString() {
        return "DeploymentDesignParamEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
