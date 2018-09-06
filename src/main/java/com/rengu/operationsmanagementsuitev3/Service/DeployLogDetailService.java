package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.DeployLogDetailEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeployLogEntity;
import com.rengu.operationsmanagementsuitev3.Repository.DeployLogDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-06 18:21
 **/

@Slf4j
@Service
@Transactional
public class DeployLogDetailService {

    private final DeployLogDetailRepository deployLogDetailRepository;

    @Autowired
    public DeployLogDetailService(DeployLogDetailRepository deployLogDetailRepository) {
        this.deployLogDetailRepository = deployLogDetailRepository;
    }

    public List<DeployLogDetailEntity> saveDeployLogDetails(List<DeployLogDetailEntity> deployLogDetailEntityList) {
        return deployLogDetailRepository.saveAll(deployLogDetailEntityList);
    }

    public Page<DeployLogDetailEntity> getDeployLogDetailsByDeployLog(Pageable pageable, DeployLogEntity deployLogEntity) {
        return deployLogDetailRepository.findAllByDeployLogEntity(pageable, deployLogEntity);
    }
}
