package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.DeployLogDetailEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeployLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-06 18:18
 **/

@Repository
public interface DeployLogDetailRepository extends JpaRepository<DeployLogDetailEntity, String> {

    Page<DeployLogDetailEntity> findAllByDeployLogEntity(Pageable pageable, DeployLogEntity deployLogEntity);

    List<DeployLogDetailEntity> findAllByDeployLogEntity(DeployLogEntity deployLogEntity);
}
