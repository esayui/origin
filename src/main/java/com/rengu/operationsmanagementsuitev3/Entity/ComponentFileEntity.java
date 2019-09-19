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
 *   实例源码、脚本文件、仿真结果文件
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 11:41
 **/

@Data
@Entity
public class ComponentFileEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String name;
    private int type;//实验脚本文件类型 0：exe源码 1：脚本文件 2：结果文件
//    private String extension;
//    private boolean isFolder;
    @ManyToOne
    private FileEntity fileEntity;


    @ManyToOne
    private ComponentFileEntity parentNode; //


    @ManyToOne
    private ComponentEntity componentEntity;


}
