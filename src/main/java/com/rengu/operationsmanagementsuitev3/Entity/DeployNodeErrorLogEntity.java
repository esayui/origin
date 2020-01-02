package com.rengu.operationsmanagementsuitev3.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Data
public class DeployNodeErrorLogEntity {
    @Id
    private String id = UUID.randomUUID().toString();

    @NotBlank
    private String type;

    @NotBlank
    private String content;

    @JsonIgnore
    @ManyToOne
    private DeploymentDesignNodeEntity deploymentDesignNodeEntity;

}
