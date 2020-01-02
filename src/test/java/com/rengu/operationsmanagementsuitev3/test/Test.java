package com.rengu.operationsmanagementsuitev3.test;

import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import javassist.bytecode.ByteArray;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: XYmar
 * Date: 2019/9/23 17:36
 */
public class Test {



    public static void main(String[] args) {
            List list = new ArrayList();
            for(int i = 0;i<20;i++){
                list.add("任务"+(i+1));
            }
        most(list);
            int m = 1;

            long st = System.currentTimeMillis();
            for(Object j:list){
                if(j instanceof String ){
                    System.out.println("["+m+"]" + Thread.currentThread().getName() + "----" + j);
                }
                m++;
            }
            long ed =System .currentTimeMillis();
             System.out.println("共耗时：" + (ed - st)+"ms");


    }


    public static String byte2SplitString(byte a) {
        // 字节a转成2进制数值字符串
        String s1 = String.format("%8s", Integer.toBinaryString(a & 0xFF)).replace(' ', '0').trim();
        System.out.println(s1);

        //不满8位如000010 补齐为00000010
        while (s1.length() < 8) {
            s1 = "0" + s1;
        }

        //每两位转10进制数值int输出
        for (int i = 0; i < 8; i += 1) {
            String rstr = s1.substring(i, i + 2);
            BigInteger bi = new BigInteger(rstr, 2);
            System.out.println(Integer.parseInt(bi.toString()));
        }

        return null;
    }


    public static String timeC2String(String time) {
        String str = "11111100011000010010001100000000000001110010000111100001011";
        System.out.println(str.length());
        time = "568307976666418955";

        String a = Long.toBinaryString(Long.parseLong(time));
        System.out.println(a);
        while (a.length() < 64) {
            a = "0" + a;
        }
        int point = 0;
        String HH = a.substring(point, point + 8);
        //System.out.println(HH);
        BigInteger HB = new BigInteger(HH, 2);
        System.out.println("时：" + Integer.parseInt(HB.toString()));
        point += 8;
        String mm = a.substring(point, point + 8);
        //System.out.println(mm);
        BigInteger mB = new BigInteger(mm, 2);
        System.out.println("分：" + Integer.parseInt(mB.toString()));
        point += 8;
        String ss = a.substring(point, point + 16);
        //System.out.println(ss);
        BigInteger sB = new BigInteger(ss, 2);
        System.out.println("秒：" + Integer.parseInt(sB.toString()));
        point += 16;
        String dd = a.substring(point, point + 8);
        //System.out.println(dd);
        BigInteger dB = new BigInteger(dd, 2);
        System.out.println("日：" + Integer.parseInt(dB.toString()));
        point += 8;
        String MM = a.substring(point, point + 8);
        //System.out.println(MM);
        BigInteger MB = new BigInteger(MM, 2);
        System.out.println("月：" + Integer.parseInt(MB.toString()));
        point += 8;
        String yyyy = a.substring(point, point + 16);
        //System.out.println(yyyy);
        BigInteger yB = new BigInteger(yyyy, 2);
        System.out.println("年：" + Integer.parseInt(yB.toString()));


        return "";
    }


    public native static long timeTest();

    public static byte[] getMacBytes(String mac) {

        long result6 = 568307976666418955l;
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(result6));
        System.out.println(dateStr);

        return null;
    }

    public static void sendUDP() throws IOException {
        String str = "abc、";

        System.out.println(str.substring(0, str.length() - 1));
    }


    public static  void most(List<String> list) {
        long st = System.currentTimeMillis();
        //执行任务 无限大 浪费资源
        //ExecutorService pool = Executors.newCachedThreadPool(); 
        //默认5个进程 空闲下来工作线程就会捡取等待队列里的其他工作进行执行。
        ExecutorService pool = Executors.newFixedThreadPool(5);

        for ( int i = 0;i < list.size(); i++) {
            final String str = list.get(i);
            final int att = i;

            Runnable run = new Runnable() {
                public void run() {
                    //模拟耗时操作
                    System.out.println("["+att+"]" + Thread.currentThread().getName() + "----" + str);

//                    try {
//                        Thread.sleep(1000);
//                    } catch (Exception e) {
//                    }
                }
            };
            //将线程放入池中进行执行  
            pool.execute(run);

        }
        //关闭执行服务对象


        pool.shutdown();



        boolean fa = true;
        while(fa) {
            if(pool.isTerminated()) {
                long ed = System.currentTimeMillis();
                System.out.println("共耗时：" + (ed - st)+"ms");
                fa = false;
            }
        }

    }




}
