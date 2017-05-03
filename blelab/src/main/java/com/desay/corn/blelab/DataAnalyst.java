package com.desay.corn.blelab;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by corn on 2016/7/6.
 */
public class DataAnalyst {

    public static class Header
    {
        public String original = "";
        public int type;// 0：为运动，1：为实时运动，2：为睡眠动作 3：为睡眠心率
        public int len;// 包的长度
        public int receiveLen;// 接收长度
        public int num;// 包序号
        public int total;// 包总数
        public String crc;// 包校验
        public String en;// 加密标识
    }

    public static class MonitorData
    {
        public long   time_dev;
        public Header header = new Header();
        public List<Byte> data = new LinkedList<Byte>();

        public void addData(byte[] bytes)
        {
               String end = "";
            if (!header.original.contains("end")){
                if(bytes[bytes.length-1]==0x0A && bytes[bytes.length-2]==0x0D){
                    end = ":end";
                    DesayLog.d("add end ");
                    try{
                        byte[] temp =  new byte[(bytes.length-2)];
                        System.arraycopy(bytes, 0, temp, 0,(bytes.length-2));
                        bytes = new byte[(temp.length)];
                        System.arraycopy(temp, 0,bytes, 0,(temp.length));
                    }catch (Exception e){
                        DesayLog.e("DataAnalyst 45 e = " + e);
                    }
                }
                header.original = header.original + new String(bytes)+end;
                paserDataHeader(header);
            }else{
                if(!(bytes[0]==0xff&&bytes[1]==0xff&&bytes[2]==0xff)){
                    for (byte b : bytes) {
                        data.add(b);
                    }
                }
            }
        }

        public boolean isFull()
        {
            return  (header.len == 0 || data.size() >= header.receiveLen) && header.original.contains("end");
        }

        public long getProgress(){
            if (header.total == 0)
                return 100;
            return Math.round(100.0 * header.num / header.total);
        }
    }

    static private void paserDataHeader(Header header)
    {
        DesayLog.e("header = " + header.original);
        if (!header.original.contains("end")) {
            return ;
        }

        String[] arr = header.original.split(":")[1].split(",");
        if(arr.length !=7 ){//if is a wrong header,maybe be AT+VER:1.0.1
                header.original = "";
            return;
        }
        header.type = Integer.valueOf(arr[0]);
        header.len = Integer.valueOf(arr[1]);
        header.receiveLen = Integer.valueOf(arr[2]);
        header.num = Integer.valueOf(arr[3]);
        header.total = Integer.valueOf(arr[4]);
        header.crc = arr[5];
        header.en = arr[6];
    }

    /**
     * 数据解析，解析上传上来bytes的数据
     */
    public static List<int[]> splitData(List<Byte> bytes, int size) {
        DesayLog.d("bytes.size() =  "+bytes.size());
        int[] bs = null;
        List<int[]> l = new ArrayList<int[]>();

        for (int i = 0; i < bytes.size(); i++) {
            if (i % size == 0) {
                bs = new int[size];
                l.add(bs);
            }
            bs[i % size] = bytes.get(i) & 0xff;
        }
        return l;
    }

    public static class ParserData {
        public int flag;
        public long secondTime;
        public int value;
        public int value1;
    }

    public static ParserData parser(int[] arr, boolean realTime) {
        if (!realTime) {
            boolean b = true;
            for (int i : arr) {
                if (i != 0xff) {
                    b = false;
                    break;
                }
            }
            if (b) return null;
        }

        int flag = 0, s = 0;
        int time = 0, temp = 0, value = 0, usetime = 0;
        flag = (arr[0] >> 6) & 0x03;
        for (s = 0; s < 4; s++) {
            time <<= 16;
            temp = (arr[s++] << 8);
            time |= (temp | arr[s]);
        }
        time &= 0x3fffffff;
        temp = (arr[s++] << 8);
        value = temp | arr[s++];
        if (realTime) {
            temp = (arr[s++] << 8);
            usetime = temp | arr[s++];
        }

        ParserData parserData = new ParserData();
        parserData.flag = flag;
        parserData.secondTime = time;
        parserData.value = value;
        parserData.value1 = usetime;
        return parserData;
    }










}
