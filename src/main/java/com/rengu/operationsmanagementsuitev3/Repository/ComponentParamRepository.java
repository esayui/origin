package com.rengu.operationsmanagementsuitev3.Repository;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ComponentParamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentParamRepository extends JpaRepository<ComponentParamEntity,String> {


    Optional<ComponentParamEntity> findAllByComponentEntity(ComponentEntity componentEntity);
}
