package com.rengu.operationsmanagementsuitev3.Repository;


import com.rengu.operationsmanagementsuitev3.Entity.DeployNodeErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeployNodeErrorLogRepository extends JpaRepository<DeployNodeErrorLogEntity,String> {


}
