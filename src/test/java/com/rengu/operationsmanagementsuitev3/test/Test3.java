package com.rengu.operationsmanagementsuitev3.test;

import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeviceEntity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Test3 {


    public static void main(String[] args) {

        int nodeCount = 31;

        List<String> ips = new ArrayList<>();
        ips.add("192.168.0.1");
        ips.add("192.168.0.2");
        ips.add("192.168.0.3");
        ips.add("192.168.0.4");
        ips.add("192.168.0.5");
        ips.add("192.168.0.6");
        ips.add("192.168.0.7");
        ips.add("192.168.0.8");
        ips.add("192.168.0.9");
        ips.add("192.168.1.0");
        ips.add("192.168.1.1");
        ips.add("192.168.1.2");
        ips.add("192.168.1.3");
        ips.add("192.168.1.4");
        ips.add("192.168.1.5");





        List<DeviceEntity> devices = new ArrayList<>();

        for(String singleIp:ips){

            DeviceEntity deviceEntity = new DeviceEntity();
            //if(!deviceRepository.existsByHostAddressAndDeleted(singleIp,false)){
                deviceEntity.setDeployPath("");
                deviceEntity.setHostAddress(singleIp);
                deviceEntity.setName("在线设备");
                deviceEntity.setDescription("");
            //    deviceEntity = deviceRepository.save(deviceEntity);
           // }else{
               // deviceEntity = deviceRepository.findByHostAddressAndDeleted(ip,false);
           // }
            devices.add(deviceEntity);

        }

        int minium = nodeCount/ips.size();

        // 实例余数
        int other = nodeCount%ips.size();


        List<DeploymentDesignNodeEntity> nodes = new LinkedList<>();

        //机器顺位
        int r = 0;
        for(int i=0;i<nodeCount;i+=minium){
            DeploymentDesignNodeEntity deploymentDesignNodeEntity = new DeploymentDesignNodeEntity();
            //deploymentDesignNodeEntity.setDeploymentDesignEntity(deploymentDesignEntity);
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


        int num =1;
        for(DeploymentDesignNodeEntity node:nodes){
            System.out.println(num+"--"+node.getId()+"  "+node.getDeviceEntity().getHostAddress());
            num++;
        }

    }

}
