package com.rengu.operationsmanagementsuitev3.ActiveMQ;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Destination;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 09:45
 **/

@Slf4j
@Service
public class ActiveMQMessageProducer {

    private final JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    public ActiveMQMessageProducer(JmsMessagingTemplate jmsMessagingTemplate) {
        this.jmsMessagingTemplate = jmsMessagingTemplate;
    }

    public void sendMessage(Destination destination, Object message) {
        jmsMessagingTemplate.convertAndSend(destination, message);
    }
}
