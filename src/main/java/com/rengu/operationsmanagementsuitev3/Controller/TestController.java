package com.rengu.operationsmanagementsuitev3.Controller;

import com.rengu.operationsmanagementsuitev3.ActiveMQ.ActiveMQMessageProducer;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 09:52
 **/

@RestController
@RequestMapping(value = "/tests")
public class TestController {

    @Autowired
    private ActiveMQMessageProducer activeMQMessageProducer;

    @GetMapping(value = "/avtiveMQ")
    public void sendMessage() {
        Destination destination = new ActiveMQQueue("QUEUE.HEARTBEAT");
        for (int i = 0; i <= 10; i++) {
            activeMQMessageProducer.sendMessage(destination, "第" + i + "条信息：" + "Hello form OSX");
        }
    }
}
