package com.rengu.operationsmanagementsuitev3.Thread;

import com.rengu.operationsmanagementsuitev3.Entity.HeartbeatEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeviceService;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 17:17
 **/

@Slf4j
@Component
public class UDPReceiveThread {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public UDPReceiveThread(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Async
    public void UDPMessageReceiver() throws IOException {
        log.info("启动客户端UDP报文监听线程，监听端口：" + ApplicationConfig.UDP_RECEIVE_PORT);
        DatagramSocket datagramSocket = new DatagramSocket(ApplicationConfig.UDP_RECEIVE_PORT);
        DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
        while (true) {
            datagramSocket.receive(datagramPacket);
            // 解析心跳报文信息
            byte bytes[] = datagramPacket.getData();
            int pointer = 0;
            String codeType = new String(bytes, pointer, 4).trim();
            pointer = pointer + 4;
            String cpuTag = new String(bytes, pointer, 64).trim();
            pointer = pointer + 64;
            long cpuClock = Long.parseLong(new String(bytes, pointer, 6).trim());
            pointer = pointer + 6;
            int cpuUtilization = Integer.parseInt(new String(bytes, pointer, 4).trim());
            pointer = pointer + 4;
            int ramTotalSize = Integer.parseInt(new String(bytes, pointer, 6).trim());
            pointer = pointer + 6;
            int freeRAMSize = Integer.parseInt(new String(bytes, pointer, 6).trim());
            pointer = pointer + 6;
            double upLoadSpeed = Double.parseDouble(new String(bytes, pointer, 8).trim());
            pointer = pointer + 8;
            double downLoadSpeed = Double.parseDouble(new String(bytes, pointer, 8).trim());
            HeartbeatEntity heartbeatEntity = new HeartbeatEntity();
            heartbeatEntity.setHostAddress(datagramPacket.getAddress().getHostAddress());
            heartbeatEntity.setCpuTag(cpuTag);
            heartbeatEntity.setCpuClock(cpuClock);
            heartbeatEntity.setCpuUtilization(cpuUtilization);
            heartbeatEntity.setRamTotalSize(ramTotalSize);
            heartbeatEntity.setRamFreeSize(freeRAMSize);
            heartbeatEntity.setUpLoadSpeed(upLoadSpeed);
            heartbeatEntity.setDownLoadSpeed(downLoadSpeed);
            simpMessagingTemplate.convertAndSend("/deviceInfo/" + heartbeatEntity.getHostAddress(), JsonUtils.toJson(heartbeatEntity));
            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(heartbeatEntity.getHostAddress())) {
                log.info(heartbeatEntity.getHostAddress() + "----->建立服务器连接。");
            }
            DeviceService.ONLINE_HOST_ADRESS.put(heartbeatEntity.getHostAddress(), heartbeatEntity);
            simpMessagingTemplate.convertAndSend("/onlineDevice", JsonUtils.toJson(DeviceService.ONLINE_HOST_ADRESS));
        }
    }
}
