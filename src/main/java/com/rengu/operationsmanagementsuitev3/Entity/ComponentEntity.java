package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *  project版本信息 一个版本对应多个参数配置 对应一个实例源码
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 12:51
 **/

@Data
@Entity
public class ComponentEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();

    private String name;

    private String relativePath;
    private String description;
    private boolean deleted = false;


    @ManyToOne
    private UserEntity userEntity;


    @Override
    public String toString() {
        return "ComponentEntity{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", name='" + name + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", description='" + description + '\'' +
                ", deleted=" + deleted +
                '}';
    }
}
