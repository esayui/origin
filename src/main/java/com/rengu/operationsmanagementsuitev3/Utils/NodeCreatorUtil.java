package com.rengu.operationsmanagementsuitev3.Utils;

import com.rengu.operationsmanagementsuitev3.Entity.ComponentParamEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignNodeEntity;
import com.rengu.operationsmanagementsuitev3.Entity.DeploymentDesignParamEntity;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeCreatorUtil {

    public static InputStream inputStream;


    public static  List<String> aquireDeviceIp() {
        List<String> ips = new ArrayList<>();


        /**
         *   测试ip写死
         */
        ips.add("192.168.31.98");

        /**
         String hostPath = "C:/WINDOWS/system32/drivers/etc/hosts";
         */
        if (inputStream != null) {


            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(inputStream));

                while (br.readLine() != null) {

                    String txt = br.readLine();
                    if(txt.contains("hpc-io")){
                       ips.add(txt.split("hpc-io")[0].trim());
                    }


                }

                return ips;

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                IOUtils.closeQuietly(br);
            }
        }

        return ips;
    }

    public static String HttpRestClient(String url, HttpMethod method, MultiValueMap<String, String> params) throws IOException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10*1000);
        requestFactory.setReadTimeout(10*1000);
        RestTemplate client = new RestTemplate(requestFactory);
        HttpHeaders headers = new HttpHeaders();
        //  请勿轻易改变此提交方式，大部分的情况下，提交方式都是表单提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(params, headers);



        //  执行HTTP请求
        ResponseEntity<String> response = client.exchange(url, method, requestEntity, String.class);
        return response.getBody();
    }




}
