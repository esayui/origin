package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:03
 **/

@Repository
public interface ComponentFileRepository extends JpaRepository<ComponentFileEntity, String> {


    boolean existsByFileEntity(FileEntity fileEntity);

    List<ComponentFileEntity> findByParentNodeAndComponentEntity(ComponentFileEntity parentNode, ComponentEntity componentEntity);

    List<ComponentFileEntity> findAllByComponentEntity(ComponentEntity componentEntity);

    boolean existsByNameAndParentNodeAndComponentEntity(String name, ComponentFileEntity parentNode, ComponentEntity componentEntity);

    Optional<ComponentFileEntity> findByNameAndParentNodeAndComponentEntity(String name, ComponentFileEntity parentNode, ComponentEntity componentEntity);

    List<ComponentFileEntity> findByParentNodeAndComponentEntityAndType(ComponentFileEntity parentNode, ComponentEntity componentEntity, int fileType);

    void deleteAllByComponentEntity(ComponentEntity componentEntity);

    List<ComponentFileEntity> findByComponentEntityAndType(ComponentEntity componentEntity, int fileType);

    List<ComponentFileEntity> findAllByIsHistoryAndComponentEntity(boolean b, ComponentEntity componentById);

    List<ComponentFileEntity> findAllByTypeAndComponentEntity(int i, ComponentEntity componentById);

    List<ComponentFileEntity> findAllByName(String deploymentDesignNodeId);
}
