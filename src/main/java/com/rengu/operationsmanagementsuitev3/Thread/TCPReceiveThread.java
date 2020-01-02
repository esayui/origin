package com.rengu.operationsmanagementsuitev3.Thread;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.DeployNodeErrorLogRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignNodeRepository;
import com.rengu.operationsmanagementsuitev3.Service.FileService;
import com.rengu.operationsmanagementsuitev3.Service.OrderService;
import com.rengu.operationsmanagementsuitev3.Service.ScanHandlerService;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 17:11
 **/

@Slf4j
@Component
public class TCPReceiveThread {

    @Autowired
    protected FileService fileService;
    private static TCPReceiveThread tcpReceiveThread;
    @Autowired
    protected DeployNodeErrorLogRepository deployNodeErrorLogRepository;

    @Autowired
    protected DeploymentDesignNodeRepository deploymentDesignNodeRepository;

    @PostConstruct //通过@PostConstruct实现初始化bean之前进行的操作
    public void init() {
        tcpReceiveThread = this;
        tcpReceiveThread.fileService = this.fileService;
        tcpReceiveThread.deployNodeErrorLogRepository = this.deployNodeErrorLogRepository;
        tcpReceiveThread.deploymentDesignNodeRepository = this.deploymentDesignNodeRepository;
        // 初使化时将已静态化的testService实例化
    }


    // TCP报文接受进程
    @Async
    public void TCPMessageReceiver() {
        try {
            log.info("OMS服务器-启动客户端TCP报文监听线程，监听端口：" + ApplicationConfig.TCP_RECEIVE_PORT);
            ServerSocket serverSocket = new ServerSocket(ApplicationConfig.TCP_RECEIVE_PORT);
            while (true) {
                socketHandler(serverSocket.accept());
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void socketHandler(Socket socket) throws IOException {
        try {
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(inputStream, byteArrayOutputStream);
            bytesHandler(byteArrayOutputStream.toByteArray());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            socket.shutdownOutput();
            socket.close();
        }
    }

    private void bytesHandler(byte[] bytes) {
        int pointer = 0;

        // System.out.println("tcp类型："+new String(bytes,0,8));

        String messageType = new String(bytes, 0, 4).trim();
        System.out.println("tcp类型：" + messageType);
        pointer = pointer + 4;
        // 进程扫描信息
        if (messageType.equals(OrderService.PROCESS_SCAN_RESULT_TAG)) {
            String id = new String(bytes, pointer, 37).trim();
            pointer = pointer + 37;
            List<ProcessScanResultEntity> processScanResultEntityList = new ArrayList<>();
            while (pointer + 5 + 128 + 8 + 8 < bytes.length) {
                try {
                    String pid = new String(bytes, pointer, 5).trim();
                    pointer = pointer + 5;
                    String name = new String(bytes, pointer, 128).trim();
                    pointer = pointer + 128;
                    String priority = new String(bytes, pointer, 8).trim();
                    pointer = pointer + 8;
                    double ramUsedSize = Double.parseDouble(new String(bytes, pointer, 8).trim()) / 1024;
                    pointer = pointer + 8;
                    ProcessScanResultEntity processScanResultEntity = new ProcessScanResultEntity();
                    processScanResultEntity.setPid(pid);
                    processScanResultEntity.setName(name);
                    processScanResultEntity.setRamUsedSize(ramUsedSize);
                    processScanResultEntityList.add(processScanResultEntity);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            ScanHandlerService.PROCESS_SCAN_RESULT.put(id, processScanResultEntityList);
        }
        // 扫描磁盘结果解析
        if (messageType.equals(OrderService.DISK_SCAN_RESULT_TAG)) {
            String id = new String(bytes, pointer, 37).trim();
            pointer = pointer + 37;
            List<DiskScanResultEntity> diskScanResultEntityList = new ArrayList<>();
            while (pointer + 32 + 12 + 12 <= bytes.length) {
                try {
                    String name = new String(bytes, pointer, 32).trim().replace("\\", "/");
                    pointer = pointer + 32;
                    double size = Double.parseDouble(new String(bytes, pointer, 12).trim());
                    pointer = pointer + 12;
                    double usedSize = Double.parseDouble(new String(bytes, pointer, 12).trim());
                    pointer = pointer + 12;
                    DiskScanResultEntity diskScanResultEntity = new DiskScanResultEntity();
                    diskScanResultEntity.setName(name);
                    diskScanResultEntity.setSize(size);
                    diskScanResultEntity.setUsedSize(usedSize);
                    diskScanResultEntityList.add(diskScanResultEntity);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            ScanHandlerService.DISK_SCAN_RESULT.put(id, diskScanResultEntityList);
        }
        // 组件设备扫描信息
        if (messageType.equals(OrderService.DEPLOY_DESIGN_SCAN_RESULT_TAG)) {
            String id = new String(bytes, pointer, 36).trim();
            pointer = pointer + 36;
            String deploymentDesignNodeId = new String(bytes, pointer, 36).trim();
            pointer = pointer + 36;
            String deploymentDesignDetailId = new String(bytes, pointer, 36).trim();
            pointer = pointer + 36;
            List<DeploymentDesignScanResultDetailEntity> deploymentDesignScanResultDetailEntityList = new ArrayList<>();
            while (pointer + 256 + 34 <= bytes.length) {
                String targetPath = new String(bytes, pointer, 256).trim();
                pointer = pointer + 256;
                String md5 = new String(bytes, pointer, 34).trim();
                pointer = pointer + 34;
                DeploymentDesignScanResultDetailEntity deploymentDesignScanResultDetailEntity = new DeploymentDesignScanResultDetailEntity();
                deploymentDesignScanResultDetailEntity.setName(FilenameUtils.getName(targetPath));
                deploymentDesignScanResultDetailEntity.setTargetPath(FormatUtils.formatPath(targetPath));
                deploymentDesignScanResultDetailEntity.setMd5(md5);
                deploymentDesignScanResultDetailEntityList.add(deploymentDesignScanResultDetailEntity);
            }
            ScanHandlerService.DEPLOY_DESIGN_SCAN_RESULT.put(id, deploymentDesignScanResultDetailEntityList);
        }

        //文件分包接收
        if (messageType.equals(OrderService.DEPLOY_DESIGN_NODE_RESULT_TAG)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            //设为小端
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);


            ChunkEntity chunkEntity = new ChunkEntity();
            //int chunkNumber = Integer.parseInt(new String(bytes,pointer,4).trim());
            int chunkNumber = byteBuffer.getInt(pointer);
            chunkEntity.setChunkNumber(chunkNumber);
            pointer = pointer + 4;
            // int totalChunks = Integer.parseInt(new String(bytes,pointer,4).trim());
            int totalChunks = byteBuffer.getInt(pointer);
            chunkEntity.setTotalChunks(totalChunks);
            pointer = pointer + 4;
            //long chunkSize = Long.parseLong(new String(bytes,pointer,8).trim());
            long chunkSize = byteBuffer.getLong(pointer);
            chunkEntity.setChunkSize(chunkSize);
            pointer = pointer + 8;
            //long totalSize = Long.parseLong(new String(bytes,pointer,8).trim());
            long totalSize = byteBuffer.getLong(pointer);
            chunkEntity.setTotalSize(totalSize);
            pointer = pointer + 8;
            //String md5 = new String(bytes,pointer,256);
            byte[] byte1 = new byte[256];
            byteBuffer.position(pointer);
            byteBuffer.get(byte1);
            String md5 = FormatUtils.byteToStr(byte1);
            chunkEntity.setIdentifier(md5);
            pointer = pointer + 256;
            //String filename = new String(bytes,pointer,256);
            byte[] byte2 = new byte[256];
            byteBuffer.position(pointer);
            byteBuffer.get(byte2);
            String filename = FormatUtils.byteToStr(byte2);

            chunkEntity.setFilename(filename);
            pointer = pointer + 256;


            System.out.println(chunkEntity);

            byte[] b1 = new byte[byteBuffer.limit() - pointer];
            System.out.println(b1.length);

            byteBuffer.position(pointer);
            byteBuffer.get(b1);

            File chunk = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + md5 + File.separator + chunkNumber + ".tmp");
            if (!chunk.getParentFile().exists()) {
                chunk.getParentFile().mkdirs();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(chunk);
                fos.write(b1, 0, b1.length);
                fos.flush();

            } catch (Exception e) {
                throw new RuntimeException("文件分包接收失败");
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (fileService.hasChunk(chunkEntity)) {
                boolean merageSignal = true;
                File chunkTemp = null;
                for (int i = 1; i <= totalChunks; i++) {
                    chunkTemp = new File(ApplicationConfig.CHUNKS_SAVE_PATH + File.separator + md5 + File.separator + i + ".tmp");
                    merageSignal = chunkTemp.exists() == false ? false : merageSignal;
                }
                chunkTemp = null;
                if (merageSignal == true) {
                    try {
                        fileService.mergeChunks(chunkEntity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }


        }


        //C2状态
        if (messageType.equals(OrderService.DEPLOY_DESIGN_NODE_STATE_TAG)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            String state_type = new String(bytes, pointer, 4).trim();
            pointer = pointer + 4;
            //String node_id = new String(bytes,pointer,256);
            byte[] byte1 = new byte[256];
            byteBuffer.position(pointer);
            byteBuffer.get(byte1);
            String node_id = FormatUtils.byteToStr(byte1);
            pointer = pointer + 256;

            int error_length = byteBuffer.getInt(pointer);

            pointer = pointer + 4;
            String error_content = "";
            if (state_type.equals(OrderService.DEPLOY_DESIGN_NODE_STATE_ERROR)) {


                String error_type = new String(bytes, pointer, 4).trim();
                pointer = pointer + 4;

                byte[] b1 = new byte[error_length - 4];
                byteBuffer.position(pointer);
                byteBuffer.get(b1);

                error_content = FormatUtils.byteToStr(b1);

                DeployNodeErrorLogEntity deployNodeErrorLogEntity = new DeployNodeErrorLogEntity();
                deployNodeErrorLogEntity.setType(error_type);
                deployNodeErrorLogEntity.setContent(error_content);
                DeploymentDesignNodeEntity nodeEntity = new DeploymentDesignNodeEntity();
                nodeEntity.setId(node_id);
                deployNodeErrorLogEntity.setDeploymentDesignNodeEntity(nodeEntity);

                deployNodeErrorLogRepository.save(deployNodeErrorLogEntity);
            }


            if (state_type.equals(OrderService.DEPLOY_DESIGN_NODE_STATE_TEST)) {
                //转发给C2

                DeploymentDesignNodeEntity nodeEntity = deploymentDesignNodeRepository.findById(node_id).get();
                Socket sp = null;
                DataOutputStream dos = null;
                try {
                    sp = new Socket(nodeEntity.getDeviceEntity().getHostAddress(), ApplicationConfig.CLIENT2_TCP_RECEIVE);
                    dos = new DataOutputStream(sp.getOutputStream());
                    dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                IOUtils.closeQuietly(sp);
                IOUtils.closeQuietly(dos);


            } else {

                //转发给C1
                {
                    DeploymentDesignNodeEntity nodeEntity = deploymentDesignNodeRepository.findById(node_id).get();
                    Socket sp = null;
                    DataOutputStream dos = null;
                    try {
                        sp = new Socket(ApplicationConfig.CLIENT_ADDRESS, ApplicationConfig.CLIENT1_TCP_RECEIVE);
                        dos = new DataOutputStream(sp.getOutputStream());
                        dos.write(byteBuffer.array(), 0, byteBuffer.limit());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    IOUtils.closeQuietly(sp);
                    IOUtils.closeQuietly(dos);


                }
            }


        }

        //C2异常信息
        if (messageType.equals(OrderService.DEPLOY_DESIGN_NODE_ERROR_TAG)) {
            ChunkEntity chunkEntity = new ChunkEntity();


        }
    }
}
