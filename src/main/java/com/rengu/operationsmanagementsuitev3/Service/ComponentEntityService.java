package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Repository.ComponentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-24 14:38
 **/

@Slf4j
@Service
@Transactional
public class ComponentEntityService {

    private final ComponentRepository componentRepository;

    @Autowired
    public ComponentEntityService(ComponentRepository componentRepository) {
        this.componentRepository = componentRepository;
    }
}
