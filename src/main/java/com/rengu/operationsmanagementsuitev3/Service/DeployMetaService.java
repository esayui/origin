package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ComponentFileHistoryService componentFileHistoryService;
    private final DeployLogService deployLogService;
    private final DeployLogDetailService deployLogDetailService;

    @Autowired
    public DeployMetaService(ComponentFileHistoryService componentFileHistoryService, DeployLogService deployLogService, DeployLogDetailService deployLogDetailService) {
        this.componentFileHistoryService = componentFileHistoryService;
        this.deployLogService = deployLogService;
        this.deployLogDetailService = deployLogDetailService;
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
    public void deployMeta(DeviceEntity deviceEntity, List<DeployMetaEntity> deployMetaEntityList) throws IOException {
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
            socket.setSoTimeout(1000 * 1);
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
                String targetPath = FormatUtils.formatPath(deployMetaEntity.getDeviceEntity().getDeployPath() + deployMetaEntity.getComponentHistoryEntity().getRelativePath()) + FormatUtils.getComponentFileHistoryRelativePath(deployMetaEntity.getComponentFileHistoryEntity(), "");
                // 建立日志详情节点
                DeployLogDetailEntity deployLogDetailEntity = new DeployLogDetailEntity();
                deployLogDetailEntity.setHostName(deviceEntity.getHostAddress());
                deployLogDetailEntity.setComponentName(deployMetaEntity.getComponentHistoryEntity().getName());
                deployLogDetailEntity.setComponentName(deployMetaEntity.getComponentHistoryEntity().getVersion());
                deployLogDetailEntity.setComponentTag(deployMetaEntity.getComponentHistoryEntity().getTag());
                deployLogDetailEntity.setTargetPath(targetPath);
                deployLogDetailEntity.setDeployLogEntity(deployLogEntity);
                deployLogDetailEntityList.add(deployLogDetailEntity);
                // 1、发送文件开始标志
                outputStream.write("fileRecvStart".getBytes());
                // 2、发送部署路径
                outputStream.write(FormatUtils.getString(deployMetaEntity.getTargetPath(), 255).getBytes());
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
                            continue deploylable;
                        }
                    }
                }
                // 4、发送实体文件(判断文件还是文件夹)
                long start = System.currentTimeMillis();
                IOUtils.copy(new FileInputStream(deployMetaEntity.getComponentFileHistoryEntity().getFileEntity().getLocalPath()), outputStream);
                outputStream.flush();
                double time = (double) (System.currentTimeMillis() - (start - 1)) / 1000;
                double size = (double) deployMetaEntity.getComponentFileHistoryEntity().getFileEntity().getSize() / 1024;
                speed = size / time;
//                // 5、发送文件结束标志
//                outputStream.write("fileRecvEnd".getBytes());
                // 6、结束标志确认
                startTime = System.currentTimeMillis();
                while (true) {
                    try {
                        if (inputStream.read() == 102) {
                            break;
                        }
                    } catch (IOException exception) {
                        if (System.currentTimeMillis() - startTime >= ApplicationConfig.REPLY_TIME_OUT) {
                            deployLogEntity.setComplete(false);
                            deployLogDetailEntity.setComplete(false);
                            sendFileNum = sendFileNum + 1;
                            progress = (sendFileNum / (double) deployMetaEntityList.size()) * 100;
                            log.info(deployMetaEntity.getComponentHistoryEntity().getName() + "-" + deployMetaEntity.getComponentHistoryEntity().getVersion() + "@" + deviceEntity.getHostAddress() + ":" + targetPath + ",部署失败，接收文件结束标志回复超时。当前进度：" + progress + "%");
                            continue deploylable;
                        }
                    }
                }
                deployLogDetailEntity.setComplete(true);
                sendFileNum = sendFileNum + 1;
                progress = (sendFileNum / (double) deployMetaEntityList.size()) * 100;
                log.info(deployMetaEntity.getComponentHistoryEntity().getName() + "-" + deployMetaEntity.getComponentHistoryEntity().getVersion() + "@" + deviceEntity.getHostAddress() + ":" + targetPath + ",部署成功，当前进度：" + progress + "%,当前速度：" + speed + "kb/s");
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
            if (!socket.isClosed()) {
                // 发送部署结束标志
                outputStream.write("DeployEnd".getBytes());
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                socket.close();
                DEPLOYING_DEVICE.remove(deviceEntity.getHostAddress());
            }
        }
    }
}
