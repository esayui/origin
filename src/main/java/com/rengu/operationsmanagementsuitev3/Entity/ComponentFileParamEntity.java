package com.rengu.operationsmanagementsuitev3.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class ComponentFileParamEntity implements Serializable {

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


    @JsonIgnore
    @ManyToOne
    private ComponentFileEntity componentFileEntity;

    public void setCreateTime(Date createTime) {
        if (createTime == null) {
            return;
        }
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ComponentFileParamEntity{" +
                "id='" + id + '\'' +
                ", createTime=" + createTime +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", description='" + description + '\'' +
                '}';
    }
}

