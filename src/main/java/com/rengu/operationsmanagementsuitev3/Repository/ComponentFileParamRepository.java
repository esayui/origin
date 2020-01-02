package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentFileParamEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComponentFileParamRepository extends JpaRepository<ComponentFileParamEntity,String> {


    List<ComponentFileParamEntity> findAllByComponentFileEntity(ComponentFileEntity componentFileEntity);
}
