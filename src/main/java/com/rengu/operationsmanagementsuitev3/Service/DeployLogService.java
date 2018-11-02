package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.DeployLogEntity;
import com.rengu.operationsmanagementsuitev3.Entity.ProjectEntity;
import com.rengu.operationsmanagementsuitev3.Repository.DeployLogRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @program: operations-management-suite-v3
 * @author: hanch
 * @create: 2018-09-06 18:19
 **/

@Slf4j
@Service
@Transactional
public class DeployLogService {

    private final DeployLogRepository deployLogRepository;
    private final DeployLogDetailService deployLogDetailService;

    @Autowired
    public DeployLogService(DeployLogRepository deployLogRepository, DeployLogDetailService deployLogDetailService) {
        this.deployLogRepository = deployLogRepository;
        this.deployLogDetailService = deployLogDetailService;
    }

    @CacheEvict(value = "DeployLog_Cache", allEntries = true)
    public DeployLogEntity saveDeployLog(DeployLogEntity deployLogEntity) {
        return deployLogRepository.save(deployLogEntity);
    }

    @CacheEvict(value = "DeployLog_Cache", allEntries = true)
    public DeployLogEntity deleteDeployLogById(String deployLogId) {
        DeployLogEntity deployLogEntity = getDeployLogById(deployLogId);
        deployLogDetailService.deleteDeployLogDetailsByDeployLog(deployLogEntity);
        deployLogRepository.delete(deployLogEntity);
        return deployLogEntity;
    }

    @CacheEvict(value = "DeployLog_Cache", allEntries = true)
    public List<DeployLogEntity> deleteDeployLogByProject(ProjectEntity projectEntity) {
        List<DeployLogEntity> deployLogEntityList = getDeployLogsByProject(projectEntity);
        for (DeployLogEntity deployLogEntity : deployLogEntityList) {
            deleteDeployLogById(deployLogEntity.getId());
        }
        return deployLogEntityList;
    }

    public boolean hasDeployLogById(String deployLogId) {
        if (StringUtils.isEmpty(deployLogId)) {
            return false;
        }
        return deployLogRepository.existsById(deployLogId);
    }

    @Cacheable(value = "DeployLog_Cache", key = "#deployLogId")
    public DeployLogEntity getDeployLogById(String deployLogId) {
        if (!hasDeployLogById(deployLogId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOY_LOG_ID_NOT_FOUND + deployLogId);
        }
        return deployLogRepository.findById(deployLogId).get();
    }

    public Page<DeployLogEntity> getDeployLogsByProject(Pageable pageable, ProjectEntity projectEntity) {
        return deployLogRepository.findAllByProjectEntity(pageable, projectEntity);
    }

    public List<DeployLogEntity> getDeployLogsByProject(ProjectEntity projectEntity) {
        return deployLogRepository.findAllByProjectEntity(projectEntity);
    }

    public Page<DeployLogEntity> getDeployLogs(Pageable pageable) {
        return deployLogRepository.findAll(pageable);
    }
}
