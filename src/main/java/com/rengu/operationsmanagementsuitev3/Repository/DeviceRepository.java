package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-23 11:04
 **/

@Repository
public interface DeviceRepository extends JpaRepository<DeviceEntity, String> {



    Page<DeviceEntity> findByDeletedAndComponentEntity(Pageable pageable, boolean deleted, ComponentEntity componentEntity);

    List<DeviceEntity> findByDeletedAndComponentEntity(boolean deleted, ComponentEntity componentEntity);

    List<DeviceEntity> findAllByComponentEntity(ComponentEntity componentEntity);

    long countByDeletedAndComponentEntity(boolean deleted, ComponentEntity componentEntity);

    boolean existsByHostAddressAndDeletedAndComponentEntity(String hostAddress, boolean deleted, ComponentEntity componentEntity);
}
