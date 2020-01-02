package com.rengu.operationsmanagementsuitev3.Thread;

import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Author: XYmar
 * Date: 2019/9/11 11:22
 */


public class TCPSendThread {

    public static void main(String[] args) throws UnsupportedEncodingException {
//        try {
//            sendFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
String filename = "BOSH对接API说明_，20170123-1.docx";
        System.out.println(FormatUtils.getString(filename,256-FormatUtils.filterChinese(filename)*2).getBytes().length);

//        int a = 1*1024*1024*1024;
//        int b = 2>>31;
//
//        System.out.println(a);
//        System.out.println(b);

       // System.out.println(FormatUtils.longToByteArray(321534636l).length);

       // acquireFile();
    }

    public static void send() {

        while (true) {

            byte[] bytes = new byte[8];
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            //C001 控制命令标识
            buf.put(new String("C001").getBytes());
            buf.position(4);
            buf.put(FormatUtils.toLH(1));
            try {
                Socket s = new Socket("192.168.31.156", 6006);
                OutputStream oos = s.getOutputStream();
                oos.write(buf.array());

                s.close();
                oos.close();

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    public static void acquireFile(){
//        File runExe = new File("C:\\Users\\10160\\Downloads\\MemoryAnalyzer-1.9.1.20190826-win32.win32.x86_64.zip");
//        byte[] bytes = new byte[512 * 1024];
//        int number = 1;
//        double sum = 0;
//        if(runExe.length()<bytes.length) {
//            sum = 1;
//        }else{
//            sum = ((double)runExe.length()/(double)bytes.length);
//            if((runExe.length()%bytes.length)>0){
//                sum = sum+1;
//            }
//
//        }
//        System.out.println(new Double(sum).intValue());
//        //System.out.println((runExe.length()%bytes.length));
    }


    public static void sendFile() throws IOException {
        File runExe = new File("E:\\可视化部署文档\\1.txt");
        if (!runExe.exists()) {
            throw new RuntimeException("版本文件丢失");
        }

        FileInputStream fis = null;
        fis = new FileInputStream(runExe);
        Socket s = new Socket("192.168.31.156", ApplicationConfig.CLIENT2_TCP_RECEIVE);
        DataOutputStream oos = new DataOutputStream(s.getOutputStream());
        byte[] bytes = new byte[1000];
        int number = 1;
        double sum = 0;
        if(runExe.length()<bytes.length) {
            sum = 1;
        }else{
            sum = ((double)runExe.length()/(double)bytes.length);
            if((runExe.length()%bytes.length)>0){
                sum = sum+1;
            }

        }
        while (fis.read(bytes) != -1) {


            ByteBuffer byteBuffer = ByteBuffer.allocate(532+bytes.length);
          //4
            byteBuffer.put(FormatUtils.getString("C003",4).getBytes());
            System.out.println(byteBuffer.position());
            //4
            byteBuffer.put(FormatUtils.toLH(number));

            System.out.println("number="+number);
            System.out.println(byteBuffer.position());
            //4
            byteBuffer.put(FormatUtils.toLH(new Double(sum).intValue()));
            System.out.println("totalChrunks=" + new Double(sum).intValue());
            //8
            int chrunkSize = (bytes.length);
            byteBuffer.put(FormatUtils.toLH(chrunkSize));
            long chl = chrunkSize;
            //byteBuffer.putLong(chl);
            System.out.println("chrunkSize="+chrunkSize);
            //8
            byteBuffer.put(FormatUtils.toLH((int)runExe.length()));
            //byteBuffer.put(FormatUtils.longToByteArray(runExe.length()));
            //runExe.length()
            System.out.println("totalSize="+(int)runExe.length());
            //256
            byte[] md5  = FormatUtils.getString("MD5",256).getBytes();
            byteBuffer.put(md5);
    //        System.out.println(byteBuffer.position());
            //256
            byte[] filename  = FormatUtils.getString("123.txt",256).getBytes();
            System.out.println("filename = "+filename.length);
            byteBuffer.put(filename);
  //          System.out.println(byteBuffer.position());
//            pointer = pointer + 256;
//            byteBuffer.position(pointer);
            System.out.println(byteBuffer.position()+"   "+byteBuffer.capacity());
            byteBuffer.put(bytes);
            oos.write(byteBuffer.array(),0,532+chrunkSize);
           // byteBuffer.clear();

            number++;
        }

        s.close();
        oos.close();
    }




}
