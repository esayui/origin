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
        log.info("OMS服务器-启动客户端UDP报文监听线程，监听端口：" + ApplicationConfig.UDP_RECEIVE_PORT);
        DatagramSocket datagramSocket = new DatagramSocket(ApplicationConfig.UDP_RECEIVE_PORT);
        DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
        while (true) {
            datagramSocket.receive(datagramPacket);
            // 解析心跳报文信息
            byte[] bytes = datagramPacket.getData();
            int pointer = 0;
            String cpuTag = "";
            long cpuClock = 0;
            int cpuUtilization = 0;
            int ramTotalSize = 0;
            int freeRAMSize = 0;
            double upLoadSpeed = 0.0;
            double downLoadSpeed = 0.0;
            int OSType = 0;
            String OSName = "";


            String codeType = new String(bytes, pointer, 4).trim();
            switch(codeType){
                case "cmd":{
                    try {
                        pointer = pointer+4;

                    } catch (Exception e) {
                        log.info("命令解析异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                }

                case "heart":{
                    try {



                        pointer = pointer + 4;
                        OSType = bytes[pointer];
                        pointer = pointer + 1;
                        OSName = new String(bytes, pointer, 16).trim();
                        pointer = pointer + 16;
                        cpuTag = new String(bytes, pointer, 64).trim();
                        pointer = pointer + 64;
                        cpuClock = Long.parseLong(new String(bytes, pointer, 6).trim());
                        pointer = pointer + 6;
                        cpuUtilization = Integer.parseInt(new String(bytes, pointer, 4).trim());
                        pointer = pointer + 4;
                        ramTotalSize = Integer.parseInt(new String(bytes, pointer, 6).trim());
                        pointer = pointer + 6;
                        freeRAMSize = Integer.parseInt(new String(bytes, pointer, 6).trim());
                        pointer = pointer + 6;
                        upLoadSpeed = Double.parseDouble(new String(bytes, pointer, 8).trim());
                        pointer = pointer + 8;
                        downLoadSpeed = Double.parseDouble(new String(bytes, pointer, 8).trim());
                    } catch (Exception e) {
                        log.info("心跳格式解析异常:" + e.getMessage());
                        e.printStackTrace();
                    }
                    HeartbeatEntity heartbeatEntity = new HeartbeatEntity();
                    heartbeatEntity.setHostAddress(datagramPacket.getAddress().getHostAddress());
                    heartbeatEntity.setCpuTag(cpuTag);
                    heartbeatEntity.setCpuClock(cpuClock);
                    heartbeatEntity.setCpuUtilization(cpuUtilization);
                    heartbeatEntity.setRamTotalSize(ramTotalSize);
                    heartbeatEntity.setRamFreeSize(freeRAMSize);
                    heartbeatEntity.setUpLoadSpeed(upLoadSpeed);
                    heartbeatEntity.setDownLoadSpeed(downLoadSpeed);
                    heartbeatEntity.setOSType(OSType);
                    heartbeatEntity.setOSName(OSName);
                    simpMessagingTemplate.convertAndSend("/deviceInfo/" + heartbeatEntity.getHostAddress(), JsonUtils.toJson(heartbeatEntity));
                    if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(heartbeatEntity.getHostAddress())) {
                        log.info(heartbeatEntity.getHostAddress() + "----->建立服务器连接。");
                    }
                    DeviceService.ONLINE_HOST_ADRESS.put(heartbeatEntity.getHostAddress(), heartbeatEntity);
                    simpMessagingTemplate.convertAndSend("/onlineDevice", JsonUtils.toJson(DeviceService.ONLINE_HOST_ADRESS));
                    break;
                }
            }

        }
    }
}
