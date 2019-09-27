package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:04
 **/

@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, String> {


    Page<ComponentEntity> findByDeletedAndUserEntity(Pageable pageable, boolean deleted,  UserEntity userEntity);

    List<ComponentEntity> findByDeletedAndUserEntity(boolean deleted,  UserEntity userEntity);

    List<ComponentEntity> findAllByUserEntity( UserEntity userEntity);

    long countByDeletedAndUserEntity(boolean deleted,  UserEntity userEntity);


    boolean existsByNameAndDeletedAndUserEntity(String name, boolean deleted, UserEntity userEntity);
}
