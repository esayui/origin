package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignParamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeploymentDesignParamRepository extends JpaRepository<DeploymentDesignParamEntity,String> {


    List<DeploymentDesignParamEntity> findAllByDeploymentDesignEntityId(String deploymentId);
}
