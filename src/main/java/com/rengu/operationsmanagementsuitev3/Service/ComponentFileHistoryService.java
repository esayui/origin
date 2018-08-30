package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-29 14:20
 **/

@Slf4j
@Service
@Transactional
public class ComponentFileHistoryService {

    private final ComponentFileHistoryRepository componentFileHistoryRepository;

    @Autowired
    public ComponentFileHistoryService(ComponentFileHistoryRepository componentFileHistoryRepository) {
        this.componentFileHistoryRepository = componentFileHistoryRepository;
    }
}
