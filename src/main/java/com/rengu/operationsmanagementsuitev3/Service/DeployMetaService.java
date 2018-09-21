package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-05 12:44
 **/

@Slf4j
@Service
public class DeployMetaService {

    public static final Map<String, DeviceEntity> DEPLOYING_DEVICE = new ConcurrentHashMap<>();

    // 部署状态报告信息
    public static final int DEPLOYING_ERROR = 0;
    public static final int DEPLOYING_SUCCEED = 1;
    public static final int DEPLOY_FINISHED = 2;
    public static final int DEPLOYING = 3;

    private final ComponentFileHistoryService componentFileHistoryService;
    private final DeployLogService deployLogService;
    private final DeployLogDetailService deployLogDetailService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public DeployMetaService(ComponentFileHistoryService componentFileHistoryService, DeployLogService deployLogService, DeployLogDetailService deployLogDetailService, SimpMessagingTemplate simpMessagingTemplate) {
        this.componentFileHistoryService = componentFileHistoryService;
        this.deployLogService = deployLogService;
        this.deployLogDetailService = deployLogDetailService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    // 根据部署设计详情创建部署信息
    public List<DeployMetaEntity> createDeployMeta(List<DeploymentDesignDetailEntity> deploymentDesignDetailEntityList) {
        List<DeployMetaEntity> deployMetaEntityList = new ArrayList<>();
        for (DeploymentDesignDetailEntity deploymentDesignDetailEntity : deploymentDesignDetailEntityList) {
            for (ComponentFileHistoryEntity componentFileHistoryEntity : componentFileHistoryService.getComponentFileHistorysByComponentHistory(deploymentDesignDetailEntity.getComponentHistoryEntity())) {
                if (!componentFileHistoryEntity.isFolder()) {
                    DeployMetaEntity deployMetaEntity = new DeployMetaEntity();
                    deployMetaEntity.setDeviceEntity(deploymentDesignDetailEntity.getDeploymentDesignNodeEntity().getDeviceEntity());
                    deployMetaEntity.setComponentHistoryEntity(deploymentDesignDetailEntity.getComponentHistoryEntity());
                    deployMetaEntity.setComponentFileHistoryEntity(componentFileHistoryEntity);
                    deployMetaEntityList.add(deployMetaEntity);
                }
            }
        }
        return deployMetaEntityList;
    }

    // 部署元数据
    public void deployMeta(DeploymentDesignEntity deploymentDesignEntity, DeviceEntity deviceEntity, List<DeployMetaEntity> deployMetaEntityList) throws IOException {
        if (DEPLOYING_DEVICE.containsKey(deviceEntity.getHostAddress())) {
            throw new RuntimeException(ApplicationMessages.DEVICE_IS_DEPOLOYING + deviceEntity.getHostAddress());
        } else {
            DEPLOYING_DEVICE.put(deviceEntity.getHostAddress(), deviceEntity);
        }
        // 初始化Socket、输入输出流
        Socket socket = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            // 建立TCP连接
            socket = new Socket(deviceEntity.getHostAddress(), ApplicationConfig.TCP_DEPLOY_PORT);
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(1000 * 2);
            // 获取输入输出流
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
            // 建立部署日志节点
            DeployLogEntity deployLogEntity = new DeployLogEntity();
            deployLogEntity.setProjectEntity(deviceEntity.getProjectEntity());
            List<DeployLogDetailEntity> deployLogDetailEntityList = new ArrayList<>();
            // 记录文件发送数量、进度、速度等
            int sendFileNum = 0;
            double progress = 0;
            double speed = 0;
            deploylable:
            for (DeployMetaEntity deployMetaEntity : deployMetaEntityList) {
                // 生成部署路径
                String targetPath = FormatUtils.formatPath(deployMetaEntity.getDeviceEntity().getDeployPath() + deployMetaEntity.getComponentHistoryEntity().getRelativePath() + FormatUtils.getComponentFileHistoryRelativePath(deployMetaEntity.getComponentFileHistoryEntity(), ""));
                // 建立日志详情节点
                DeployLogDetailEntity deployLogDetailEntity = new DeployLogDetailEntity();
                deployLogDetailEntity.setHostName(deviceEntity.getHostAddress());
                deployLogDetailEntity.setComponentName(deployMetaEntity.getComponentHistoryEntity().getName());
                deployLogDetailEntity.setComponentVersion(deployMetaEntity.getComponentHistoryEntity().getVersion());
                deployLogDetailEntity.setComponentTag(deployMetaEntity.getComponentHistoryEntity().getTag());
                deployLogDetailEntity.setTargetPath(targetPath);
                deployLogDetailEntity.setDeployLogEntity(deployLogEntity);
                deployLogDetailEntityList.add(deployLogDetailEntity);
                // 1、发送文件开始标志
                outputStream.write("fileRecvStart".getBytes());
                // 2、发送部署路径
                outputStream.write(FormatUtils.getString(targetPath, 255).getBytes());
                // 3、接受路径回复确认
                long startTime = System.currentTimeMillis();
                while (true) {
                    try {
                        if (inputStream.read() == 114) {
                            break;
                        }
                    } catch (IOException exception) {
                        if (System.currentTimeMillis() - startTime >= ApplicationConfig.REPLY_TIME_OUT) {
                            deployLogEntity.setComplete(false);
                            deployLogDetailEntity.setComplete(false);
                            sendFileNum = sendFileNum + 1;
                            progress = (sendFileNum / (double) deployMetaEntityList.size()) * 100;
                            log.info(deployMetaEntity.getComponentHistoryEntity().getName() + "-" + deployMetaEntity.getComponentHistoryEntity().getVersion() + "@" + deviceEntity.getHostAddress() + ":" + targetPath + ",部署失败，接收路径回复超时。当前进度：" + progress + "%");
                            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), speed, progress, DEPLOYING_ERROR, FilenameUtils.getName(targetPath) + "-部署失败"));
                            continue deploylable;
                        }
                    }
                }
                // 4、发送实体文件(判断文件还是文件夹)
                FileInputStream fileInputStream = new FileInputStream(deployMetaEntity.getComponentFileHistoryEntity().getFileEntity().getLocalPath());
                byte[] buffer = new byte[102400];
                int readSize = 0;
                while (-1 != (readSize = fileInputStream.read(buffer))) {
                    long start = System.currentTimeMillis();
                    outputStream.write(buffer);
                    double time = (double) (System.currentTimeMillis() - (start - 1)) / 1000;
                    double size = (double) readSize / 1024;
                    speed = size / time;
                    progress = progress + 0.001;
                    simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), speed, progress, DEPLOYING, FilenameUtils.getName(targetPath) + "-部署中"));
                }
                // 5、发送文件结束标志
//                outputStream.write("fileRecvEnd".getBytes());
//                outputStream.flush();
                // 6、结束标志确认
                startTime = System.currentTimeMillis();
                while (true) {
                    try {
                        if (inputStream.read() == 102) {
                            break;
                        }
                    } catch (IOException exception) {
                        outputStream.write("fileRecvEnd".getBytes());
                        if (System.currentTimeMillis() - startTime >= ApplicationConfig.REPLY_TIME_OUT) {
                            deployLogEntity.setComplete(false);
                            deployLogDetailEntity.setComplete(false);
                            sendFileNum = sendFileNum + 1;
                            progress = (sendFileNum / (double) deployMetaEntityList.size()) * 100;
                            log.info(deployMetaEntity.getComponentHistoryEntity().getName() + "-" + deployMetaEntity.getComponentHistoryEntity().getVersion() + "@" + deviceEntity.getHostAddress() + ":" + targetPath + ",部署失败，接收文件结束标志回复超时。当前进度：" + progress + "%");
                            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), speed, progress, DEPLOYING_ERROR, FilenameUtils.getName(targetPath) + "-部署失败"));
                            continue deploylable;
                        }
                    }
                }
                deployLogDetailEntity.setComplete(true);
                sendFileNum = sendFileNum + 1;
                progress = (sendFileNum / (double) deployMetaEntityList.size()) * 100;
                log.info(deployMetaEntity.getComponentHistoryEntity().getName() + "-" + deployMetaEntity.getComponentHistoryEntity().getVersion() + "@" + deviceEntity.getHostAddress() + ":" + targetPath + ",部署成功，当前进度：" + progress + "%,当前速度：" + speed + "kb/s");
                simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), speed, progress, DEPLOYING_SUCCEED, FilenameUtils.getName(targetPath) + "-部署成功"));
            }
            // 发送部署结束标志
            outputStream.write("DeployEnd".getBytes());
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            socket.close();
            DEPLOYING_DEVICE.remove(deviceEntity.getHostAddress());
            deployLogService.saveDeployLog(deployLogEntity);
            deployLogDetailService.saveDeployLogDetails(deployLogDetailEntityList);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            simpMessagingTemplate.convertAndSend("/deployProgress/" + deploymentDesignEntity.getId(), new DeployProgressEntity(deviceEntity.getHostAddress(), 0, 100, DEPLOY_FINISHED, "部署结束"));
            if (!socket.isClosed()) {
                outputStream.close();
                inputStream.close();
                socket.close();
                DEPLOYING_DEVICE.remove(deviceEntity.getHostAddress());
            }
        }
    }
}
