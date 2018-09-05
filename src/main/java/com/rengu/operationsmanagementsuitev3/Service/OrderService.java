package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;
import com.rengu.operationsmanagementsuitev3.Entity.OrderEntity;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-30 15:23
 **/

@Slf4j
@Service
public class OrderService {

    public static final String PROCESS_SCAN_TAG = "S105";
    public static final String DISK_SCAN_TAG = "S106";
    // 客户端返回报问表示
    public static final String SCAN_RESULT_TAG = "C102";
    public static final String PROCESS_SCAN_RESULT_TAG = "C105";
    public static final String DISK_SCAN_RESULT_TAG = "C106";

    public void sendProcessScanOrderByUDP(DeviceEntity deviceEntity, OrderEntity orderEntity) throws IOException {
        String tag = FormatUtils.getString(orderEntity.getTag(), 4);
        String type = FormatUtils.getString("", 1);
        String uuid = FormatUtils.getString(orderEntity.getId(), 37);
        sandMessageByUDP(deviceEntity.getHostAddress(), tag + type + uuid);
    }

    public void sendDiskScanOrderByUDP(DeviceEntity deviceEntity, OrderEntity orderEntity) throws IOException {
        String tag = FormatUtils.getString(orderEntity.getTag(), 4);
        String type = FormatUtils.getString("", 1);
        String uuid = FormatUtils.getString(orderEntity.getId(), 37);
        sandMessageByUDP(deviceEntity.getHostAddress(), tag + type + uuid);
    }

    private void sandMessageByUDP(String hostAdress, String message) throws IOException {
        DatagramSocket datagramSocket = new DatagramSocket();
        InetAddress inetAddress = InetAddress.getByName(hostAdress);
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, ApplicationConfig.UDP_SEND_PORT);
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), socketAddress);
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }
}
