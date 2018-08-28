package com.rengu.operationsmanagementsuitev3.Task;

import com.rengu.operationsmanagementsuitev3.Utils.IPUtils;
import com.rengu.operationsmanagementsuitev3.Utils.ServerCastUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-27 15:15
 **/

@Slf4j
@Component
public class ServerTask {

    // 周期发送心跳
    @Scheduled(fixedRate = 1000)
    public void serverCastTask() throws IOException {
        Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaceEnumeration.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
            if (!networkInterface.isLoopback() && !networkInterface.isVirtual()) {
                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    if (interfaceAddress.getAddress() != null && interfaceAddress.getBroadcast() != null) {
                        InetAddress inetAddress = interfaceAddress.getAddress();
                        if (!inetAddress.isLoopbackAddress() && IPUtils.isIPv4Address(inetAddress.getHostAddress())) {
                            ServerCastUtils.sendMessage(interfaceAddress);
                        }
                    }
                }
            }
        }
    }
}
