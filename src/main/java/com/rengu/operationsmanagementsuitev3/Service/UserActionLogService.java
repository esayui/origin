package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.UserActionLogEntity;
import com.rengu.operationsmanagementsuitev3.Repository.UserActionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-22 17:07
 **/

@Slf4j
@Service
@Transactional
public class UserActionLogService {

    // 操作对象
    public static final int ERROR_OBJECT = 0;
    public static final int USER_OBJECT = 1;
    public static final int PROJECT_OBJECT = 2;

    // 操作类别
    public static final int ERROR_TYPE = 0;
    public static final int CREATE_TYPE = 1;
    public static final int DELETE_TYPE = 2;
    public static final int UPDATE_TYPE = 3;

    @Autowired
    private UserActionLogRepository userActionLogRepository;

    // 保存用户操作日志
    public UserActionLogEntity saveUserActionLog(UserActionLogEntity userActionLogEntity) {
        return userActionLogRepository.save(userActionLogEntity);
    }

    // 根据用户名查询用户操作日志
    public Page<UserActionLogEntity> getUserActionLogsByUsername(Pageable pageable, String username) {
        return userActionLogRepository.findByUsername(pageable, username);
    }

    // 查询全部用户操作日志
    public Page<UserActionLogEntity> getUserActionLogs(Pageable pageable) {
        return userActionLogRepository.findAll(pageable);
    }
}
