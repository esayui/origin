package com.rengu.operationsmanagementsuitev3.Entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.UUID;

/**
 * Author: XYmar
 * Date: 2019/9/25 11:23
 */

@Data
public class DeployNodeRuntimeLog implements Serializable {
    String  id = UUID.randomUUID().toString();
    //initialize;start;terminate
    int cmdcode;

    String deploymentDesignName;
    // 0 success 1 fail
    String ips;






}
