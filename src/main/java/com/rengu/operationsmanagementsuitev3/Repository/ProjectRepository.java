package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:32
 **/

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    boolean existsByNameAndUserEntity(String name, UserEntity userEntity);

    Page<ProjectEntity> findByUserEntity(Pageable pageable, UserEntity userEntity);
}
