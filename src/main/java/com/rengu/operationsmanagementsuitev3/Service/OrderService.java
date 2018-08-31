package com.rengu.operationsmanagementsuitev3.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Entity.OrderEntity;
import com.rengu.operationsmanagementsuitev3.Utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.jms.Destination;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-30 15:23
 **/

@Slf4j
@Service
public class OrderService {

    private static final String PROCESS_SCAN_TAG = "S105";
    private static final String DISK_SCAN_TAG = "S106";

    private final JmsMessagingTemplate jmsMessagingTemplate;

    @Autowired
    public OrderService(JmsMessagingTemplate jmsMessagingTemplate) {
        this.jmsMessagingTemplate = jmsMessagingTemplate;
    }

    public OrderEntity sendProcessScanOrder(DeviceEntity deviceEntity) throws JsonProcessingException {
        Destination destination = new ActiveMQQueue("QUEUE." + deviceEntity.getHostAddress());
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTag(PROCESS_SCAN_TAG);
        jmsMessagingTemplate.convertAndSend(destination, JsonUtils.toJson(orderEntity));
        return orderEntity;
    }

    public OrderEntity sendDiskScanOrder(DeviceEntity deviceEntity) throws JsonProcessingException {
        Destination destination = new ActiveMQQueue("QUEUE." + deviceEntity.getHostAddress());
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setTag(DISK_SCAN_TAG);
        jmsMessagingTemplate.convertAndSend(destination, JsonUtils.toJson(orderEntity));
        return orderEntity;
    }
}
