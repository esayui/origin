package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/9/19 12:50
 */

@Entity
@Data
public class ComponentParamEntity implements Serializable {

    @Id
    private String id = UUID.randomUUID().toString();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime = new Date();
    @NotBlank(message = "参数名不能为空")
    private String name;
    @NotNull(message = "参数类型不能为空")
    private int type;

    private String value = "";

    private String description;

    //同一批参数设置 批次甄别
    private String pid;


    @ManyToOne
    private ComponentEntity componentEntity;

    public void setCreateTime(Date createTime) {
        if (createTime == null) {
            return;
        }
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ComponentParamEntity{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", pid='" + pid + '\'' +
                '}';
    }
}
