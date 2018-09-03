package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileHistoryEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:29
 **/

@Repository
public interface ComponentFileHistoryRepository extends JpaRepository<ComponentFileHistoryEntity, String> {

    List<ComponentFileHistoryEntity> findAllByParentNodeAndComponentHistoryEntity(ComponentFileHistoryEntity parentNode, ComponentHistoryEntity componentHistoryEntity);
}
