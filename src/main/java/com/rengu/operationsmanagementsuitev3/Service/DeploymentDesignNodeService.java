package com.rengu.operationsmanagementsuitev3.Service;

import com.alibaba.fastjson.JSON;
import com.rengu.operationsmanagementsuitev3.Entity.*;
import com.rengu.operationsmanagementsuitev3.Repository.ComponentFileRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignNodeRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeploymentDesignParamRepository;
import com.rengu.operationsmanagementsuitev3.Repository.DeviceRepository;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationMessages;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import com.rengu.operationsmanagementsuitev3.Utils.NodeCreatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 10:01
 **/

@Slf4j
@Service
@Transactional
public class DeploymentDesignNodeService {

    private final DeploymentDesignNodeRepository deploymentDesignNodeRepository;
    private final DeploymentDesignParamRepository deploymentDesignParamRepository;
//    private final DeployMetaService deployMetaService;
    private final DeploymentDesignDetailService deploymentDesignDetailService;
    private final ComponentFileRepository componentFileRepository;
    private final DeviceRepository deviceRepository;

    @Autowired
    public DeploymentDesignNodeService(DeploymentDesignParamRepository deploymentDesignParamRepository,DeploymentDesignNodeRepository deploymentDesignNodeRepository,  DeploymentDesignDetailService deploymentDesignDetailService, DeviceRepository deviceRepository, ComponentFileRepository componentFileRepository) {
        this.deploymentDesignNodeRepository = deploymentDesignNodeRepository;
//        this.deployMetaService = deployMetaService;
        this.deploymentDesignDetailService = deploymentDesignDetailService;
        this.deviceRepository = deviceRepository;
        this.componentFileRepository = componentFileRepository;
        this.deploymentDesignParamRepository = deploymentDesignParamRepository;
    }

    // 根据部署设计保存部署节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public DeploymentDesignNodeEntity saveDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity, DeploymentDesignNodeEntity deploymentDesignNodeEntity) {

        //TODO device应在此由外部获得
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();


        deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
        deploymentDesignNodeEntity.setDeviceEntity(deviceRepository.save(deviceEntity));


        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id和设备建立部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public DeploymentDesignNodeEntity saveDeploymentDesignNodeByDeploymentDesignAndDevice(DeploymentDesignEntity deploymentDesignEntity, DeploymentDesignNodeEntity deploymentDesignNodeEntity, DeviceEntity deviceEntity) {
        if (hasDeploymentDesignNodeByDeviceAndDeploymentDesign(deviceEntity, deploymentDesignNodeEntity.getDeploymentDesignEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_EXISTED + deviceEntity.getHostAddress());
        }
        deploymentDesignNodeEntity.setDeviceEntity(deviceEntity);
        deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据部署设计复制部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public void copyDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity sourceDeploymentDesign, DeploymentDesignEntity targetDeploymentDesign) {
        for (DeploymentDesignNodeEntity deploymentDesignNodeArgs : getDeploymentDesignNodesByDeploymentDesign(sourceDeploymentDesign)) {
            DeploymentDesignNodeEntity deploymentDesignNodeEntity = new DeploymentDesignNodeEntity();
            BeanUtils.copyProperties(deploymentDesignNodeArgs, deploymentDesignNodeEntity, "id", "createTime", "deploymentDesignEntity");
            deploymentDesignNodeEntity.setDeploymentDesignEntity(targetDeploymentDesign);
            deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
            deploymentDesignDetailService.copyDeploymentDesignDetailsByDeploymentDesignNode(deploymentDesignNodeArgs, deploymentDesignNodeEntity);
        }
    }

    // 根据Id删除部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public DeploymentDesignNodeEntity deleteDeploymentDesignNodeById(String deploymentDesignNodeId) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        deploymentDesignDetailService.deleteDeploymentDesignDetailByDeploymentDesignNode(deploymentDesignNodeEntity);
        deploymentDesignNodeRepository.delete(deploymentDesignNodeEntity);
        return deploymentDesignNodeEntity;
    }

    // 根据Id删除部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public List<DeploymentDesignNodeEntity> deleteDeploymentDesignNodeByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        List<DeploymentDesignNodeEntity> deploymentDesignNodeEntityList = getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity);
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeEntityList) {
            deleteDeploymentDesignNodeById(deploymentDesignNodeEntity.getId());
        }
        return deploymentDesignNodeEntityList;
    }

    // 根据Id删除部署设计节点
    @CacheEvict(value = "DeploymentDesignNode_Cache", allEntries = true)
    public List<DeploymentDesignNodeEntity> deleteDeploymentDesignNodeByDevice(DeviceEntity deviceEntity) {
        List<DeploymentDesignNodeEntity> deploymentDesignNodeEntityList = getDeploymentDesignNodesByDevice(deviceEntity);
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : deploymentDesignNodeEntityList) {
            deploymentDesignNodeEntity.setDeviceEntity(null);
            deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
        }
        return deploymentDesignNodeEntityList;
    }

    // 根据Id绑定设备
    @CacheEvict(value = {"DeploymentDesignNode_Cache", "DeploymentDesignDetail_Cache"}, allEntries = true)
    public DeploymentDesignNodeEntity bindDeviceById(String deploymentDesignNodeId, DeviceEntity deviceEntity) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        if (hasDeploymentDesignNodeByDeviceAndDeploymentDesign(deviceEntity, deploymentDesignNodeEntity.getDeploymentDesignEntity())) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_EXISTED + deviceEntity.getHostAddress());
        }
        deploymentDesignNodeEntity.setDeviceEntity(deviceEntity);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id解绑设备
    @CacheEvict(value = {"DeploymentDesignNode_Cache", "DeploymentDesignDetail_Cache"}, allEntries = true)
    public DeploymentDesignNodeEntity unbindDeviceById(String deploymentDesignNodeId) {
        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        deploymentDesignNodeEntity.setDeviceEntity(null);
        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);
    }

    // 根据Id判断部署设计节点是否存在
    public boolean hasDeploymentDesignNodeById(String deploymentDesignNodeId) {
        if (StringUtils.isEmpty(deploymentDesignNodeId)) {
            return false;
        }
        return deploymentDesignNodeRepository.existsById(deploymentDesignNodeId);
    }

    // 根据设备和部署设计查询实存已存在该部署节点
    public boolean hasDeploymentDesignNodeByDeviceAndDeploymentDesign(DeviceEntity deviceEntity, DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.existsByDeviceEntityAndDeploymentDesignEntity(deviceEntity, deploymentDesignEntity);
    }

    // 根据id查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignNodeId")
    public DeploymentDesignNodeEntity getDeploymentDesignNodeById(String deploymentDesignNodeId) {
        if (!hasDeploymentDesignNodeById(deploymentDesignNodeId)) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_ID_NOT_FOUND + deploymentDesignNodeId);
        }
        return deploymentDesignNodeRepository.findById(deploymentDesignNodeId).get();
    }

    // 根据部署设计查询部署设计节点
    public Page<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDeploymentDesign(Pageable pageable, DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.findAllByDeploymentDesignEntity(pageable, deploymentDesignEntity);
    }

    // 根据部署设计查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deploymentDesignEntity.getId()")
    public List<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        return deploymentDesignNodeRepository.findAllByDeploymentDesignEntity(deploymentDesignEntity);
    }

    // 根据部署设计查询部署设计节点
    @Cacheable(value = "DeploymentDesignNode_Cache", key = "#deviceEntity.getId()")
    public List<DeploymentDesignNodeEntity> getDeploymentDesignNodesByDevice(DeviceEntity deviceEntity) {
        return deploymentDesignNodeRepository.findAllByDeviceEntity(deviceEntity);
    }

    // 根据部署设计查询设备
    public List<DeviceEntity> getDevicesByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity) {
        List<DeviceEntity> deviceEntityList = new ArrayList<>();
        for (DeploymentDesignNodeEntity deploymentDesignNodeEntity : getDeploymentDesignNodesByDeploymentDesign(deploymentDesignEntity)) {
            if (deploymentDesignNodeEntity.getDeviceEntity() != null) {
                deviceEntityList.add(deploymentDesignNodeEntity.getDeviceEntity());
            }
        }
        return deviceEntityList;
    }

    // 根据部署设计节点部署
    @Async
    public void deployDeploymentDesignNodeById(String deploymentDesignNodeId) throws IOException {

        DeploymentDesignNodeEntity deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);
        if (deploymentDesignNodeEntity.getDeviceEntity() == null) {
            throw new RuntimeException(ApplicationMessages.DEPLOYMENT_DESIGN_NODE_DEVICE_ARGS_NOT_FOUND);
        }
        DeviceEntity deviceEntity = deploymentDesignNodeEntity.getDeviceEntity();
//        if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(deviceEntity.getHostAddress())) {
//            throw new RuntimeException(ApplicationMessages.DEVICE_NOT_ONLINE + deviceEntity.getHostAddress());
//        }

        String componentId = deploymentDesignNodeEntity.getDeploymentDesignEntity().getComponentEntity().getId();
        ComponentEntity componentEntity = new ComponentEntity();
        componentEntity.setId(componentId);
        ComponentFileEntity componentFileEntity = componentFileRepository.findAllByIsHistoryAndComponentEntity(false,componentEntity).get(0);

        /*
         * 发送文件
         * */

        if(sendFile(componentFileEntity.getFileEntity(),deviceEntity.getHostAddress(),ApplicationConfig.CLIENT2_TCP_RECEIVE,deploymentDesignNodeEntity.getId()) == false){
            return;
        }
        /*
         * 发送参数
         * */
        {
//            List<DeploymentDesignParamEntity> params = new ArrayList<>();
//            params = deploymentDesignParamRepository.findAllByDeploymentDesignEntityId(deploymentDesignNodeEntity.getDeploymentDesignEntity().getId());
            String jsonString = deploymentDesignNodeEntity.getParams();


            Socket sp = new Socket(deviceEntity.getHostAddress(), ApplicationConfig.CLIENT2_TCP_RECEIVE);

            ByteBuffer byteBuffer = ByteBuffer.allocate(268+jsonString.getBytes().length);
            byteBuffer.put(FormatUtils.getString("C002",4).getBytes());
            byteBuffer.put(FormatUtils.toLH(jsonString.getBytes().length));
            System.out.println("dataSize:"+jsonString.getBytes().length);
            //1:UTF-8, 2:GBK
            byteBuffer.put(FormatUtils.toLH(1));

            byte[] nodeIdbyte = FormatUtils.getString(deploymentDesignNodeEntity.getId(),256).getBytes();

            byteBuffer.put(nodeIdbyte);


            byteBuffer.put(jsonString.getBytes());

            DataOutputStream dos = new DataOutputStream(sp.getOutputStream());
            dos.write(byteBuffer.array(),0,byteBuffer.limit());

            IOUtils.closeQuietly(sp);
            IOUtils.closeQuietly(dos);

        }



    }


    public List<DeploymentDesignNodeEntity> createDeploymentDesignNodesByDeploymentDesign(DeploymentDesignEntity deploymentDesignEntity,String ip) {

        if(deploymentDesignEntity==null){
            throw  new RuntimeException("未找到该实验");
        }
//        String url = "localhost";
//        //post请求
//        HttpMethod method =HttpMethod.GET;
//        // 封装参数，千万不要替换为Map与HashMap，否则参数无法传递
//        MultiValueMap<String, String> params= new LinkedMultiValueMap<String, String>();
//        params.put("ip","");
//
//
//
//        NodeCreatorUtil.HttpRestClient(url,method,params);
//
//
        int waitTime = 0;

        List<String> ips = NodeCreatorUtil.aquireDeviceIp();
        while(ips == null||ips.size()==0){
            if(waitTime>10000){
                throw  new RuntimeException("pbs通信超时，生成实例失败，请重试！");
            }

            waitTime += 300;
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        List<DeviceEntity> devices = new ArrayList<>();

        for(String singleIp:ips){

            DeviceEntity deviceEntity = new DeviceEntity();
            if(!deviceRepository.existsByHostAddressAndDeleted(singleIp,false)){
                deviceEntity.setDeployPath("");
                deviceEntity.setHostAddress(singleIp);
                deviceEntity.setName("在线设备");
                deviceEntity.setDescription("");
                deviceEntity = deviceRepository.save(deviceEntity);
            }else{
                deviceEntity = deviceRepository.findByHostAddressAndDeleted(singleIp,false);
            }
            devices.add(deviceEntity);

        }



        int nodeCount = deploymentDesignEntity.getExampleCount();
        nodeCount=nodeCount>0?nodeCount:1;

        //先清空所有实例
        deploymentDesignNodeRepository.deleteAllByDeploymentDesignEntity(deploymentDesignEntity);

        List<DeploymentDesignNodeEntity> nodes = new ArrayList<>();

        // 每个机器最少分配实例数
        int minium = nodeCount/ips.size();

        // 实例余数
        int other = nodeCount%ips.size();


        //机器顺位
        int r = 0;
        for(int i=0;i<nodeCount;i+=minium){
            DeploymentDesignNodeEntity deploymentDesignNodeEntity = new DeploymentDesignNodeEntity();
            deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
            //deploymentDesignNodeEntity.setDeviceEntity(deviceEntity);

            int jlimit = (i+minium)<nodeCount?(i+minium):nodeCount;
            for(int j = i;j<jlimit;j++){

                r=r>=(devices.size()-1)?(devices.size()-1):r;
                deploymentDesignNodeEntity.setDeviceEntity(devices.get(r));
                nodes.add(deploymentDesignNodeEntity);
            }

            if(other>0){
                deploymentDesignNodeEntity.setDeviceEntity(devices.get(r));
                nodes.add(deploymentDesignNodeEntity);
                other--;
            }

            if(nodes.size()>=nodeCount){
                break;
            }

            r++;
        }


        return deploymentDesignNodeRepository.saveAll(nodes);
    }

    public DeploymentDesignNodeEntity setDeploymentDesignNodeValueById(String deploymentDesignNodeId, String params) {
        DeploymentDesignNodeEntity  deploymentDesignNodeEntity = getDeploymentDesignNodeById(deploymentDesignNodeId);

        deploymentDesignNodeEntity.setParams(params);

        return deploymentDesignNodeRepository.save(deploymentDesignNodeEntity);

    }



    public boolean sendFile(FileEntity fileEntity,String ip,int port,String nodeId){
        FileInputStream fis = null;
        Socket s = null;
        DataOutputStream  oos= null;
        try{

            File runExe = new File(fileEntity.getLocalPath());
            if (!runExe.exists()) {
                throw new RuntimeException("版本文件丢失");
            }


            fis = new FileInputStream(runExe);
             s = new Socket(ip, port);
             oos = new DataOutputStream(s.getOutputStream());
            byte[] bytes = new byte[1000];
            int number = 1;
            double sum = 0;
            if (runExe.length() < bytes.length) {
                sum = 1;
            } else {
                sum = ((double) runExe.length() / (double) bytes.length);
                if ((runExe.length() % bytes.length) > 0) {
                    sum = sum + 1;
                }

            }

            while (fis.read(bytes) != -1) {

                ByteBuffer byteBuffer = ByteBuffer.allocate(788+bytes.length);
                //4
                byteBuffer.put(FormatUtils.getString("C003",4).getBytes());
                // System.out.println(byteBuffer.position());
                //4
                byteBuffer.put(FormatUtils.toLH(number));

//                System.out.println("number="+number);
//                System.out.println(byteBuffer.position());
                //4
                byteBuffer.put(FormatUtils.toLH(new Double(sum).intValue()));
                //System.out.println("totalChrunks=" + new Double(sum).intValue());
                //8
                int chrunkSize = (bytes.length);
                byteBuffer.put(FormatUtils.toLH(chrunkSize));
                //long chl = chrunkSize;
                //byteBuffer.putLong(chl);
                System.out.println("chrunkSize="+chrunkSize);
                //8
                byteBuffer.put(FormatUtils.toLH((int)runExe.length()));
                //byteBuffer.put(FormatUtils.longToByteArray(runExe.length()));
                //runExe.length()
                System.out.println("totalSize="+(int)runExe.length());
                //256
                byte[] md5  = FormatUtils.getString(fileEntity.getMD5(),256).getBytes();
                byteBuffer.put(md5);
                //256
                byte[] filename  = FormatUtils.getString(runExe.getName(),256-FormatUtils.filterChinese(runExe.getName())*2).getBytes();
                System.out.println("filename = "+filename.length);
                byteBuffer.put(filename);
                byte[] nodeIdbyte = FormatUtils.getString(nodeId,256).getBytes();

                byteBuffer.put(nodeIdbyte);

                System.out.println(byteBuffer.position()+"   "+byteBuffer.capacity());
                byteBuffer.put(bytes);
                oos.write(byteBuffer.array(),0,788+chrunkSize);
                // byteBuffer.clear();

                number++;
            }

            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }finally {
            IOUtils.closeQuietly(s);
            IOUtils.closeQuietly(oos);
            IOUtils.closeQuietly(fis);
        }

    }

    public DeploymentDesignNodeEntity downloadNodeResult(String deploymentDesignNodeId, HttpServletResponse response){
            List<ComponentFileEntity> logFiles = null;

            logFiles = componentFileRepository.findAllByName(deploymentDesignNodeId);

            if(logFiles!=null&&logFiles.size()>0){

                //下载单个文件
                if(logFiles.size() ==1) {
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    OutputStream os = null;
                    try{
                        File file = new File(logFiles.get(0).getFileEntity().getLocalPath());
                        if(file.exists()){
                            response.setHeader("Content-Type","application/octet-stream");
                            response.setHeader("Content-Disposition","attachment;filename="+new String(file.getName().getBytes("gbk"),"ISO8859-1"));
                            fis = new FileInputStream(file);
                            bis = new BufferedInputStream(fis);
                            os = response.getOutputStream();
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while((len = bis.read(buffer))!=-1){
                                os.write(buffer);
                            }

                        }
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        IOUtils.closeQuietly(fis);
                        IOUtils.closeQuietly(bis);
                        IOUtils.closeQuietly(os);

                    }

                // 下载多个文件
                }else{
                    Collections.sort(logFiles, new Comparator<ComponentFileEntity>() {
                        @Override
                        public int compare(ComponentFileEntity o1, ComponentFileEntity o2) {
                            if(o1.getCreateTime().after(o2.getCreateTime())){
                                return -1;
                            }

                            return 0;
                        }
                    });
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    OutputStream os = null;
                    try{
                        File file = new File(logFiles.get(0).getFileEntity().getLocalPath());
                        if(file.exists()){
                            response.setHeader("Content-Type","application/octet-stream");
                            response.setHeader("Content-Disposition","attachment;filename="+new String(file.getName().getBytes("gbk"),"ISO8859-1"));
                            fis = new FileInputStream(file);
                            bis = new BufferedInputStream(fis);
                            os = response.getOutputStream();
                            byte[] buffer = new byte[1024];
                            int len = 0;
                            while((len = bis.read(buffer))!=-1){
                                os.write(buffer);
                            }

                        }
                    }catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        IOUtils.closeQuietly(fis);
                        IOUtils.closeQuietly(bis);
                        IOUtils.closeQuietly(os);

                    }



                }


            }


        return getDeploymentDesignNodeById(deploymentDesignNodeId);
    }



}
