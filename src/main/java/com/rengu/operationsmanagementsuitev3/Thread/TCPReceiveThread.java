package com.rengu.operationsmanagementsuitev3.Thread;

import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 17:11
 **/

@Slf4j
@Component
public class TCPReceiveThread {

    // TCP报文接受进程
    @Async
    public void TCPMessageReceiver() throws IOException {
        log.info("启动客户端TCP报文监听线程，监听端口：" + ApplicationConfig.TCP_RECEIVE_PORT);
        ServerSocket serverSocket = new ServerSocket(ApplicationConfig.TCP_RECEIVE_PORT);
        while (true) {
            socketHandler(serverSocket.accept());
        }
    }

    @Async
    public void socketHandler(Socket socket) throws IOException {
        try {
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            bytesHandler(byteArrayOutputStream.toByteArray());
        } finally {
            socket.shutdownOutput();
            socket.close();
        }
    }

    private void bytesHandler(byte[] bytes) {

    }
}
