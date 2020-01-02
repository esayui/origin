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
 * @create: 2018-08-24 14:27
 **/

@Data
@Entity
public class ComponentFileHistoryEntity implements Serializable,Comparable<ComponentFileHistoryEntity> {

    @Id
    private String id = UUID.randomUUID().toString();
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime = new Date();
    private String name;
    private String extension;
    private boolean isFolder;
    @JsonIgnore
    @ManyToOne
    private FileEntity fileEntity;
    @JsonIgnore
    @ManyToOne
    private ComponentFileHistoryEntity parentNode;
    @JsonIgnore
    @ManyToOne
    private ComponentHistoryEntity componentHistoryEntity;



    public int compareTo(ComponentFileHistoryEntity o) {
        int flag = this.createTime.compareTo(o.createTime);
        if(flag<0){
            flag = 1;
        }else if(flag>0){
            flag = -1;
        }

        return flag;
    }

}
