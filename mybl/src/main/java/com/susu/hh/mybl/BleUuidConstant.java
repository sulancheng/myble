package com.susu.hh.mybl;

import java.util.UUID;

/**
 * Created by Administrator on 2016/11/24.
 */
public class BleUuidConstant {
    public static final UUID SERVER_B_UUID_SERVER = UUID.fromString("0000190B-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVER_B_UUID_REQUEST = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");//WRITE
    public static final UUID SERVER_B_UUID_NOTIFY = UUID.fromString("00000004-0000-1000-8000-00805f9b34fb");//NOTIFY
    public static final UUID SERVER_C_UUID_NOTIFY = UUID.fromString("00000006-0000-1000-8000-00805f9b34fb");//NOTIFY
    //ota Attributes
    public static final UUID SERVER_A_UUID_SERVER = UUID.fromString("0000190a-0000-1000-8000-00805f9b34fb");
    public static final UUID SERVER_A_UUID_REQUEST = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");//WRITE
    public static final UUID SERVER_A_UUID_NOTIFY = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");//NOTIFY
    public final static UUID SERVER_A_UUID_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");


    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT);
    public static final UUID SPOTA_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID SPOTA_SERV_STATUS_UUID = UUID.fromString("5f78df94-798c-46f5-990a-b3eb6a065c88");

    public static final int SERVER_A_UUID_REQUEST_INT = 1;// correspond OTA_UUID_REQUEST
    public static final int SERVER_B_UUID_REQUEST_INT = 3;//correspond UUID_REQUEST


    public static final int SNSTTPE_CALL = 0;//来电提醒
    public static final int SNSTTPE_SMS = 1;//短信提醒
    public static final int SNSTTPE_QQ = 2;
    public static final int SNSTTPE_WECHAT = 3;
    public static final int SNSTTPE_FACEBOOK = 4;
    public static final int SNSTTPE_TWITTER = 5;
    public static final int SNSTTPE_WHATSAPP = 6;
    public static final int SNSTTPE_INSTAGRAM = 7;
    public static final int SNSTTPE_EMAIL = 8;
    public static final int SNSTTPE_LINE = 9;
    public static final int SNSTTPE_SKYPE = 10;

    public static final int DATA_STEPS = 0;
    public static final int DATA_HEARTRATE = 3;
    public static final int DATA_SLEEP = 7;


    public static int ENGLISH = 0;
    public static int CHINESE = 1;


    public static int HANDUP_CLOSE = 0;
    public static int HANDUP_AUTO = 1;
    public static int HANDUP_LEFT = 2;
    public static int HANDUP_RIGHT = 3;

    public static final int BAND_UNITS_MILE = 0;
    public static final int BAND_UNITS_KILO = 1;


    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_CONNECTED_FAIL = 3;
    public static final int STATE_AUTO_CONNECTING = 4;

    //    //music control notify
    private static int CURRENT_NOTFY_MUSIC_CMD = 0;
    //    public static final int NT_MUSIC_DEFAULT = 0;
    public static final int NT_MUSIC_PLAY = 1;
    public static final int NT_MUSIC_PAUSE = 2;
    public static final int NT_MUSIC_NEXT = 3;
    public static final int NT_MUSIC_PRE = 4;
    //手环默认未激活状态，发送AT+BOND之后，手环震动，点击手环按键，上报AT+BOND:OK给上层,绑定结束。
    public static final String CMD_BOND = "AT+BOND";
    //实际返回版本根据情况而定 例如 AT+VER:1.0.1
    public static final String CMD_BAND_VERSION = "BT+VER";
    //用户信息
    public static final String CMD_BAND_USER = "AT+USER";
    //查询/设置当前时间
    public static final String CMD_SYN_TIME = "AT+DT";
    //手环闹钟1
    public static final String CMD_SET_CLOCK_ONE = "AT+ALARM";
    //手环闹钟1
    public static final String CMD_SET_CLOCK_TWO = "AT+ALARM2";
    //获取手环电量
    public static final String CMD_BATTERY = "AT+BATT";
    //设置或者获取手环总步数，AT+PACE查询，AT+PACE=01234设置，（格式为5位，不足补零）
    public static final String CMD_STEP = "AT+PACE";
    /**
     * AT+SIT=030,0100,2223,1
     * AT+SIT:cycle,start_time,stop_time,switch
     * ycle:周期，单位分钟
     start_time：开始时间（代表意思，前两位代表时钟,后两位代表分钟，例如0100代表1：00，2223代表22：23）
     stop_time：结束时间（与上同）
     Switch：开关1：开 0:关
     （设定的开始时间到结束时间之间，每间隔cycle分钟，如果步数没达到限度，认为久坐，则震动提醒）
     *
     * **/
    public static final String CMD_SIT_SETTINGS = "AT+SIT";
    /**
     * AT+PUSH=type,phone_num,time_long 来电提醒推送
     * type:类型（号码或者中文名）0：号码 1：中文名
     * phone_num：Type为号码时,phone_num为数据
     * time_long:亮屏时间（秒）
     * **/
    public static final String CMD_PHONE_CALL = "AT+PUSH";

    //APP发送来电的中文字节流准备指令
    public static final String CMD_PUSH_CH = "AT+ZI";
    /**
     * 请求：AT+DATA=num
     * 回复AT+DATA：type,len,fulllen,num,total,crc,en
     * 请求指令中num为包的序号。
     * 返回指令中的type为数据类型，0：为运动，7：睡眠。
     * len为当前包的有效长度，
     * Fulllen为当前包的总长度，
     * num为包序号，
     * total为包总数（有多少个fulllen长度的包），
     * crc为包校验，
     * en为次包是否加密。当校验通过后在重复发
     * **/
    public static final String CMD_REQUEST_DATA = "AT+DATA";


    //获取手环唯一识别码
//    public static final String CMD_GET_SN = "AT+SN";

    //设置手环语言 AT+LAN = 0/1
    //设置手环语言：0:英文
    //1：中文
    public static final String CMD_SET_LAN = "AT+LAN";


    //设置手环运动目标 AT+DEST=10000 五位，不足位数需要补零
    //AT+DEST=00000为关闭该功能
    public static final String CMD_SETSPORT_AIM = "AT+DEST";
    /**
     * APP配置抬手显示 AT+HANDSUP=1
     * 参数：
     * 0：关闭
     * 1：自动
     * 2: 左手
     * 3：右手
     **/
    public static final String CMD_HANDUP = "AT+HANDSUP";



    //专项运动 1为启动，0为退出 AT+RUN =1 /0
    public static final String CMD_START_RUN = "AT+RUN";
    /**
     * AT+TOPACE = 1 打开实时同步步数接口
     * AT+TOPACE = 0 关闭实时同步步数接口
     * **/
    public static final String CMD_SYN_TIMELY_STEPS = "AT+TOPACE";
    /**
     * AT+SYN = 1 通知手环显示同步界面
     * AT+SYN = 0 通知手环关闭同步界面
     * **/
    public static final String CMD_SET_SYN_FLAG = "AT+SYN";
    /**
     * 同步音乐播放状态
     * AT+MUSICPLY = 1 通知显示已经播放的图标
     * AT+MUSICPLY = 0 通知显示停止播放的图标
     * **/
    public static final String CMD_SYN_MUSICPLAY_STATE = "AT+MUSICPLY";
    /**
     * AT+HEART = 1 控制手环测试心率开始
     * AT+HEART = 0 强制控制手环测试心率结束
     * **/
    public static final String CMD_STATIC_HEART = "AT+HEART";
    //找手环
    public static final String CMD_FIND_BAND = "AT+FINDBT";
    /**
     * 设置时区，东八区为0
     * 例如东九区
     * AT+TIMEZONE = （9-1）*3600
     **/
    public static final String CMD_SYN_TIME_ZONE = "AT+TIMEZONE";
    /**
     * 远程拍照
     * AT+CAMERA = 1 通知手环显示拍照界面
     * AT+CAMERA = 0 通知手环结束拍照界面
     * **/
    public static final String CMD_CAMERA_FLAG = "AT+CAMERA";
    //打开或者关闭音乐功能
    public static final String CMD_MUSIC = "AT+MUSIC";
    public static final String BAND_MUSIC_CMD_PLAY = "NT+MUSICON";
    public static final String BAND_MUSIC_CMD_PAUSE = "AT+MUSICPAUSE";
    public static final String BAND_MUSIC_CMD_NEXT = "NT+MUSICNEXT";
    public static final String BAND_MUSIC_CMD_PRE = "NT+MUSICPRE";


    //找手机 AT+FINDPHONE=1/0
    public static final String CMD_FIND_PHONE = "AT+FINDPHONE";

    //设置手环显示单位
    public static final String CMD_SET_DISTANCE_UNITS = "AT+UNITS";

    //    //find phone notify
    public static final String BAND_FIND_PHONE_CMD = "NT+BEEP";

    //    //timely steps
    public static final String TIMELY_STEPS_NOTIF = "NT+TOPACE";
    //    //camera
    public static final String CMD_BAND_PHOTO = "NT+CAMERA";
}
