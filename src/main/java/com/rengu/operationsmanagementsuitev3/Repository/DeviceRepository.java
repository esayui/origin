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
    

    boolean existsByHostAddressAndDeleted(String hostAddress, boolean deleted);

    Page<DeviceEntity> findByDeleted(Pageable pageable, boolean deleted);

    List<DeviceEntity> findByDeleted(boolean deleted);

    long countByDeleted(boolean deleted);

    DeviceEntity findByHostAddressAndDeleted(String ip, boolean b);
}
