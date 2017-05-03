package com.desay.corn.blesdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.desay.corn.blelab.BLEContentProvider;
import com.desay.corn.blelab.DesayLog;
import com.desay.corn.blelab.DsBluetoothConnector;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity{

    private Button san_bt,write_bt,next_cmd_bt,pre_cmd_bt,dis_bt;
    private TextView conncet_state_text,content_text,current_cmd,notify_text;
    private EditText ble_address_edit;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning= false;
    private DsBluetoothConnector mDsBluetoothConnector;
    private int ConnectState =  BLEContentProvider.STATE_DISCONNECTED;
    private String ConnectAddress = "";
    private BTControlInterface mBTControlInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,"手机不支持BLE4.0", Toast.LENGTH_SHORT).show();
            finish();
        }
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,"手机不支持BLE4.0", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        setContentView(R.layout.activity_main);
        conncet_state_text = (TextView) findViewById(R.id.conncet_state_text);
        notify_text = (TextView) findViewById(R.id.notify_text);
        current_cmd = (TextView) findViewById(R.id.current_cmd);
        current_cmd.setText("绑定手环，点击触摸屏绑定");
        content_text = (TextView) findViewById(R.id.content_text);
        ble_address_edit = (EditText) findViewById(R.id.ble_address_edit);
        san_bt = (Button) findViewById(R.id.san_bt);
        findViewById(R.id.san_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanning) {
                    scanLeDevice(false);
                }else{
                    scanLeDevice(true);
                }
            }
        });
        findViewById(R.id.dis_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   mBTControlInterface.disconnect();
            }
        });
        next_cmd_bt = (Button) findViewById(R.id.next_cmd_bt);
        next_cmd_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmd_int++;
                textSwitch();
            }
        });
        pre_cmd_bt = (Button) findViewById(R.id.pre_cmd_bt);
        pre_cmd_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmd_int--;
                if(cmd_int<0){
                    cmd_int = 31;
                }
                textSwitch();
            }
        });

        write_bt = (Button) findViewById(R.id.write_bt);
        findViewById(R.id.write_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                cmdTest();
            }
        });
        mBTControlInterface = BTControlInterface.getInstance(this);
        mBTControlInterface.setOnBLECallBackListener(mOnBTServiceCallBackListener);


    }

    private BluetoothLoaderService.OnBTServiceCallBackListener mOnBTServiceCallBackListener = new BluetoothLoaderService.OnBTServiceCallBackListener(){

        @Override
        public void OnBluetoothAdapterStateCallBack(int status) {
            //phone bluetooth state，on or off
            switch(status){
                case BluetoothLoaderService.BLUETOOTH_ADAPTER_ON:
                    //bluetooth on
                    //接到手机蓝牙打开通知
                    break;
                case BluetoothLoaderService.BLUETOOTH_ADAPTER_OFF:
                    //bluetooth off
                   //接到蓝牙关闭通知
                    break;
            }
        }
        @Override
        public void OnServiceStateCallBack(int status) {
            switch(status){
                case BluetoothLoaderService.SERVICE_ON_BIND:
                    DesayLog.d("mBTControlInterface = " + mBTControlInterface + "setAutoConnect start");
                    mBTControlInterface.setAutoConnect(true,"C1:A7:C9:ED:C8:88");
                    break;
                case BluetoothLoaderService.SERVICE_ON_DESTROY:
                    DesayLog.d("SERVICE_ON_DESTROY " );
                    break;
            }
        }

        @Override
        public void OnConnectCallBack(int event, int status) {
            ConnectState = status;
            connectHandler.sendEmptyMessage(0);
        }
        /**
         * @param event
         * @param status  操作状态
         * @param value
         */
        @Override
        public void OnCallBack(int event, boolean status, String value) {
            switch (event){
                case DsBluetoothConnector.KEY_BAND_VERSION:
                    if(status){
                        content_text.setText("当前版本号："+value);
                    }else{ //error 这里其实只因为你查询的格式有问题才会返回error,如出现，是SDK的问题
                        content_text.setText("当前版本号："+value);
                    }
                    break;
            }
        }

        /**
         * @param event  事件ID
         * @param status 操作状态
         * @param state 功能状态
         *               此回调当你只关注操作是否成功时，不必在意state功能状态
         *              例如你只是在同步当前手机时间到手环，那只需要判断操作状态status就OK了
         *              但是例如你要打开实时步数同步功能，那么你必须关注操作状态是否成功status的同时
         *              关注当前步数同步功能的状态state是否是打开的还是关闭的
         *              state映射如下两个值
         *                  public final int FUNCTION_ENABLE = 1;//功能当前状态，开启
         *                   public final int FUNCTION_DISABLE = 0;//功能当前状态，关闭
         *              或者state映射为int型，例如步数、电量
         */
        @Override
        public void OnCallBack(int event, final boolean status, int state) {
            switch(event){
                case DsBluetoothConnector.KEY_SYN_DATA:
                    if(status){
                        content_text.setText("数据同步完成");
                    }else{
                        content_text.setText("数据同步失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_BOND:
                    if(status){
                        content_text.setText("绑定成功");
                    }else{
                        content_text.setText("绑定失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SYN_TIME:
                    if(status){
                        content_text.setText("同步成功");
                    }else{
                        content_text.setText("同步失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_CLOCK_ONE:
                    if(status){
                        content_text.setText("设置闹钟1成功");
                    }else{
                        content_text.setText("设置闹钟1失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_CLOCK_TWO:
                    if(status){
                        content_text.setText("设置闹钟2成功");
                    }else{
                        content_text.setText("设置闹钟2失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_BATTERY:
                    if(status){
                        content_text.setText("当前电池电量"+state+"%");
                    }else{
                        content_text.setText("查询电量失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_STEPS:
                    if(status){
                        content_text.setText("当前步数为 = " + state);
                    }else{
                        content_text.setText("获取步数失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SIT:
                    if(status){
                        content_text.setText("设置久坐参数成功");
                    }else{
                        content_text.setText("设置久坐参数失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_INCOMING_CALL:
                    if(status){
                        content_text.setText("来电提醒操作成功");
                    }else{
                        content_text.setText("来电提醒操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_LANGUAGE:
                    if(status){
                        content_text.setText("手环语言设置成功,当前手环设置语言为："+(state==1?"中文":"英文"));
                    }else{
                        content_text.setText("手环语言设置失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SPORTS_AIM:
                    if(status){
                        content_text.setText("设置运动目标成功，设置步数为"+state);
                    }else{
                        content_text.setText("设置运动目标失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_HAND_UP:
                    if(status){
                        content_text.setText("抬手亮屏设置成功，当前设置参数为："+state);
                    }else{
                        content_text.setText("抬手亮屏设置失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_TIMELY_STEPS_SYN:
                    if(status){
                        content_text.setText("设置实时同步功能操作成功，当前功能已经"+(state==1?"打开":"关闭"));
                    }else{
                        content_text.setText("设置实时同步功能操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_SYN_FLAG:
                    if(status){
                        content_text.setText("设置同步图标成功，当前图标已经"+(state==1?"显示":"关闭"));
                    }else{
                        content_text.setText("设置同步图标失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SYN_MUSIC:
                    if(status){
                        content_text.setText("同步音乐播放状态成功");
                    }else{
                        content_text.setText("同步音乐播放状态失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_TEST_HEARTRATE:
                    if(status){
                        content_text.setText("成功测试心率 当前用户心率 = " + state);
                    }else{
                        content_text.setText("测试心率失败,请正确佩戴手环");
                    }
                    break;
                case DsBluetoothConnector.KEY_FIND_BAND:
                    if(status){
                        content_text.setText("找手环操作成功");
                    }else{
                        content_text.setText("找手环操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_CAMERA_FLAG:
                    if(status){
                        content_text.setText("拍照界面操作成功，当前拍照图标已经在手环界面"+(state==1?"显示":"关闭"));
                    }else{
                        content_text.setText("拍照界面操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_MUSIC_FLAG:
                    if(status){
                        content_text.setText("音乐功能操作成功，当前音乐播放已经在手环界面"+(state==1?"打开":"关闭"));
                    }else{
                        content_text.setText("音乐功能操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_FIND_PHONE_FLAG:
                    if(status){
                        content_text.setText("找手机功能操作成功，当前找手机已经在手环界面"+(state==1?"打开":"关闭"));
                    }else{
                        content_text.setText("找手机功能操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_UNITS:
                    if(status){
                        content_text.setText("设置手环单位操作成功，当前单位"+(state==1?"kilo":"mile"));
                    }else{
                        content_text.setText("设置手环单位操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_USER:
                    if(status){
                        content_text.setText("用户信息同步成功");
                    }else{
                        content_text.setText("用户信息同步失败");
                    }
                    break;
            }

        }

        @Override
        public void OnSleepDataCallBack(int event) {
            DesayLog.d("OnSleepDataCallBack = " + event);
        }

        @Override
        public void OnHeartRateDataCallBack(int event) {
            DesayLog.d("OnHeartRateDataCallBack = " + event);
        }

        @Override
        public void OnSportsDataCallBack(int event) {
            DesayLog.d("OnSportsDataCallBack = " + event);
        }

        @Override
        public void OnCameraShotCallBack(int event) {
            notify_text.setText("拍照通知到来");
        }

        @Override
        public void OnMusicCtrolCallBack(int event, int cmd) {
            switch(cmd){
                case BLEContentProvider.NT_MUSIC_PLAY:
                    notify_text.setText("开始播放");
                    break;
                case BLEContentProvider.NT_MUSIC_PAUSE:
                    notify_text.setText("暂停播放");
                    break;
                case BLEContentProvider.NT_MUSIC_NEXT:
                    notify_text.setText("下一曲");
                    break;
                case BLEContentProvider.NT_MUSIC_PRE:
                    notify_text.setText("上一曲");
                    break;
            }
        }

        @Override
        public void OnFindPhoneCallBack(int event) {
            notify_text.setText("手环正在找手机");
        }
    };



    int cmd_int = 0 ;
    private void cmdTest(){
//          startActivity(new Intent(this,TestActivity.class));
        if(ConnectState!=BLEContentProvider.STATE_CONNECTED){
            content_text.setText("请先连接手环再测试指令");
            return;
        }

        switch(cmd_int%32){
            case 0:
                //申请绑定
                mBTControlInterface.bindMyBand();
                break;
            case 1:
                //查询手环版本号
                mBTControlInterface.checkBandVersion();
                break;
            case 2:
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                String str = formatter.format(curDate);
                mBTControlInterface.synBandTime(str);
                break;
            case 3:
                /**
                 * @param witch       witch alarm   the value should be  1/2
                 * @param enable      enable or diable the value should be  1/0
                 * @param cycle_time  should be String sample "1111110" means 周日周一周二周三周四周五有闹钟提醒 周六没有 0 for open  1 for close
                 * @param alarm_time  should be String sample "0800"  for 08:00 in 24h
                 * **/
                //如下设置代表，设置第一个闹钟（只有两个闹钟1,2）打开，
                // 闹钟循环周期为每周-周日周一周二周三周四周五有闹钟提醒 周六没有，闹钟时间为早上八点08:00
                mBTControlInterface.setBandAlarm(1,1,"1111110","0800");
                break;
            case 4:
                /**
                 * @param witch       witch alarm   the value should be  1/2
                 * @param enable      enable or diable the value should be  1/0
                 * @param cycle_time  should be String sample "1111110" means 周日周一周二周三周四周五有闹钟提醒 周六没有 0 for open  1 for close
                 * @param alarm_time  should be String sample "0800"  for 08:00 in 24h
                 * **/
                //如下设置代表，设置第二个闹钟（只有两个闹钟1,2）打开，
                // 闹钟循环周期为每周-周日周一周二周三周四周五有闹钟提醒 周六没有，闹钟时间为早上八点09:00
                mBTControlInterface.setBandAlarm(2,1,"1111110","0900");
                break;
            case 5:
                mBTControlInterface.checkBandBattry();
                break;
            case 6:
                /**
                 *
                 * @param steps    if steps <= 0 will be get the band total steps
                 *                 else  will be set the band total steps
                 *                 sample :
                 *                 getBandTotalSteps(0);
                 *                 return the band current steps,you can get the result by call BLEContentProvider.getBandCurrentSteps();
                 *                 getBandTotalSteps(1000);
                 *                 set the band current steps as 1000.
                 *                 the steps set to band must be more than the band steps already had.
                 *                 the steps set to band must Less than the maximum 99999
                 *
                 */
                //当参数填0时，此方法为获取当前手环步数，
                mBTControlInterface.getBandTotalSteps(0);
                break;
            case 7:
                //当参数填其他数值（大于0小于99999），此方法为设置当前手环步数，注意，需要的设置步数必须比当前手环已有步数要多才会生效
                //如下为设置手环当前步数为10000步
                mBTControlInterface.getBandTotalSteps(10000);
                break;
            case 8:
                /**
                 * @param _long    how long we sit the band will remind will be 30/60/90 (min)
                 * @param start_time the remind start time in a day sample "0800" means start 08:00
                 * @param end_time the remind end time in a day sample "2300" means end 23:00
                 * @param _enable enable/disable the Sedentary function , must be 1/0
                 */
                //如下例，久坐提醒打开，在8点到23点之间，久坐30分钟手环震动以提醒用户
                mBTControlInterface.setSedentary(30,"0800","2300",1);
                break;
            case 9:
                /**
                 * @param type type 0/1 to en/ch  英文、中文字符
                 * @param content 中文或者英文，中文长度最大为4个中文，英文最大为12个，超过则会被剪切成最大长度
                 * @param msgType message type:
                 *                              0-phone call，1-SMS，3-QQ，4-wechat，5-fackbook, 6-twitter,7-whatsapp,8-email,9-line,
                 *                              you should get message type from @BLEContentProvider
                 *                remember! this method must be used before setPhoneCallState();
                 */
                //synCallerInfo 同步来电信息，setPhoneCallState，设置当前来电状态，该功能必须两个指令结合来使用
                mBTControlInterface.massageRemind(0,"abc",BLEContentProvider.SNSTTPE_CALL);
                mBTControlInterface.setPhoneCallState(true);
                break;
            case 10:
                //关闭手环来电提醒
                mBTControlInterface.setPhoneCallState(false);
                break;
            case 11:
                /**
                 * @param language 1/0 - cn/en
                 *                 you should get language from @BLEContentProvider
                 */
                //设置手环的语言 0:英文 1：中文
                mBTControlInterface.setBandLanguage(1);
                break;
            case 12:
                /**
                 * @param aim aim for sports,  the aim set to band must Less than the maximum 99999 and should be n*1000
                 */
                //设置手环步数目标，达到该目标之后，手环会振动提醒运动达标，注意，数值应该是1000的大于0整数倍且小于99999
                mBTControlInterface.setBandSportsAim(0);
                break;
            case 13:
                /**
                 * @param param the hand up param,
                 *
                 * * 参数：
                 * 0：关闭
                 * 1：自动
                 * 2: 左手
                 * 3：右手
                 * you should get param from @BLEContentProvider
                 *public static int HANDUP_CLOSE = 0;
                 *public static int HANDUP_AUTO = 1;
                 *public static int HANDUP_LEFT = 2;
                 *public static int HANDUP_RIGHT = 3;
                 */
                mBTControlInterface.setBandHandUpParam(BLEContentProvider.HANDUP_AUTO);
                break;
            case 14:
                /**
                 * @param enable
                 *        enable Timely steps syn
                 *        with true , you can get the timely steps from band.
                 *        you can get the result by call BLEContentProvider.getTimelyStepsEnableState();
                 */
                //打开步数实时通知接口，打开之后，步数改变会实时的通知到手机端
                //之后你可以调用getBandTotalSteps（）方法来获取实时的步数
                mBTControlInterface.enableSynTimelySteps(true);
                mBTControlInterface.getBandTotalSteps(0);
                break;
            case 15:
                //关闭步数实时通知接口
                mBTControlInterface.enableSynTimelySteps(false);
                break;
            case 16:
                //通知手环显示正在同步的图标
                //这个功能用于同步数据，同步数据开始时通知手环显示正在同步的图标，结束时通知手环关闭同步图标
                mBTControlInterface.showBandSynIcon(true);
                break;
            case 17:
                //通知手环关闭同步图标
                mBTControlInterface.showBandSynIcon(false);
                break;
            case 18:
                /**
                 * @param play  Synchronize the phone music play state .
                 *              the band would not know the play state if it is change by
                 *              operating at phone,so need to syn the state to band.
                 *
                 *
                 */
                //同步手机当前的播放状态，如果打开了音乐控制功能，手环可以通知手机播放音乐
                //此时如果用户自己手动去改变手机的音乐播放状态，手环本身不知道，就会造成他的音乐播放图标显示错误，
                // 所以打开了音乐控制功能则必须实时的调用该接口同步当前手机音乐播放状态
                mBTControlInterface.synPhoneMusicPlayState(false);
                break;
            case 19:
                //控制手环主动开始测试用户心率，手环本身每15分钟会自动检测一次用户心率，该接口用户立刻测试用户心率
                //测试用户心率是一个过程量，需要一定的时间才会返回测试结果，且返回的测试结果有可能是"error",指未能测试到用户心率结果
                mBTControlInterface.startCheckHeartRate();
                break;
            case 20:
                //手机找手环，开启后，手环会间歇振动，直到控制手环停止 call:DsBluetoothConnector.findMyBand(false)或者断开连接
                mBTControlInterface.findMyBand(true);
                break;
            case 21:
                //手机找手环，关闭
                mBTControlInterface.findMyBand(false);
                break;
            case 22:
                //拍照功能，控制手环进入拍照界面。通知手环进行拍照界面显示，
                // 显示之后，用户点击触摸按键，则会将拍照指令发回手机端
                mBTControlInterface.showBandPhotoView(true);
                break;
            case 23:
                //拍照功能，控制手环退出拍照界面。
                mBTControlInterface.showBandPhotoView(false);
                break;
            case 24:
                //打开或者关闭手环控制手机播放音乐功能
                //打开之后，手环端会显示音乐操作入口，关闭则不显示
                mBTControlInterface.enableMusicFunction(true);
                break;
            case 25:
                //关闭显示
                mBTControlInterface.enableMusicFunction(false);
                break;
            case 26:
                //打开或者关闭手环找手机功能
                //打开之后，手环端会显示找手机操作入口，关闭则不显示
                mBTControlInterface.enableFindPhoneFunction(true);
                break;
            case 27:
                //关闭显示
                mBTControlInterface.enableFindPhoneFunction(false);
                break;
            case 28:
                /**
                 *
                 * @param units     you should get units from @BLEContentProvider
                 *                   public static final int BAND_UNITS_MILE = 0;  英里
                 *                   public static final int BAND_UNITS_KILO = 1;  公里
                 */
                //设置手环端距离显示单位，公里或者英里
                mBTControlInterface.setBandUnits(BLEContentProvider.BAND_UNITS_KILO);
                break;
            case 29:
                //同步当前手环所有数据
                //会返回睡眠数据，计步数据，心率数据
                //同步过程中数据传输频繁，对手环的任何操作都会暂时会搁置不执行，
                mBTControlInterface.synData();
                break;
            case 30:
                //同步用户身高体重用于计算卡路里
                mBTControlInterface.synUserInfo(180,60);
                break;
            case 31:
                //提醒
                mBTControlInterface.massageRemind(0,"abc",BLEContentProvider.SNSTTPE_SMS);
                break;
        }
    }

    private void textSwitch(){
        switch(cmd_int%32){
            case 0:
                //申请绑定
                current_cmd.setText("绑定手环，点击触摸屏绑定");
                break;
            case 1:
                //查询手环版本号
                current_cmd.setText("查询手环版本号");
                break;
            case 2:
                current_cmd.setText("同步手环时间");
                break;
            case 3:
                current_cmd.setText("设置手环闹钟1");
                break;
            case 4:
                current_cmd.setText("设置手环闹钟2");
                break;
            case 5:
                current_cmd.setText("查询手环电量");
                break;
            case 6:
                current_cmd.setText("查询手环当前步数");
                break;
            case 7:
                current_cmd.setText("设置手环步数为10000步");
                break;
            case 8:
                current_cmd.setText("设置手环久坐提醒");
                break;
            case 9:
                current_cmd.setText("开始来电提醒，来电名“小锋”");
                break;
            case 10:
                current_cmd.setText("停止来电提醒");
                break;
            case 11:
                current_cmd.setText("设置手环语言");
                break;
            case 12:
                current_cmd.setText("设置手环运动目标为5000");
                break;
            case 13:
                current_cmd.setText("设置抬手亮屏参数");
                break;
            case 14:
                current_cmd.setText("打开实时步数通知");
                break;
            case 15:
                current_cmd.setText("关闭实时步数通知");
                break;
            case 16:
                current_cmd.setText("通知手环显示正在数据同步的图标");
                break;
            case 17:
                current_cmd.setText("通知手环关闭正在数据同步的图标");

                break;
            case 18:
                current_cmd.setText("同步手机音乐播放状态到手环");
                break;
            case 19:
                current_cmd.setText("控制手环测试心率");
                break;
            case 20:
                current_cmd.setText("找手环开始");

                break;
            case 21:
                current_cmd.setText("找手环结束");
                break;
            case 22:
                current_cmd.setText("控制手环显示拍照界面");
                break;
            case 23:
                current_cmd.setText("控制手环关闭拍照界面");
                break;
            case 24:
                current_cmd.setText("打开音乐功能，查看手环显示音乐控制图标入口");
                break;
            case 25:
                current_cmd.setText("打开音乐功能，查看手环不显示音乐控制图标入口");
                break;
            case 26:
                current_cmd.setText("打开找手机功能，查看手环显示找手机图标入口");
                break;
            case 27:
                current_cmd.setText("打开找手机功能，查看手环不显示找手机图标入口");
                break;
            case 28:
                current_cmd.setText("设置手环距离单位");
                break;
            case 29:
                current_cmd.setText("同步数据");
                break;
            case 30:
                current_cmd.setText("同步用户身高体重");
                break;
            case 31:
                current_cmd.setText("短信提醒");
                break;
        }
    }

    Handler connectHandler =new Handler() {
        Bundle bundle;
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 0:
                    DesayLog.d("ConnectState = " + ConnectState);
                    switch (ConnectState){
                        case BLEContentProvider.STATE_CONNECTED:
                            conncet_state_text.setText("已连接");
                            break;
                        case BLEContentProvider.STATE_CONNECTING:
                            conncet_state_text.setText("连接中");
                            break;
                        case BLEContentProvider.STATE_DISCONNECTED:
                            conncet_state_text.setText("连接已断开");
                            break;
                        case BLEContentProvider.STATE_CONNECTED_FAIL:
                            conncet_state_text.setText("连接失败，请靠近设备");
                            break;
                        case BLEContentProvider.STATE_AUTO_CONNECTING:
                            conncet_state_text.setText("连接已断开，正在为您重连");
                            break;
                    }

                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDsBluetoothConnector!=null){
            mDsBluetoothConnector.close();
        }
        this.stopService(new Intent(this, BluetoothLoaderService.class));
    }
    private Handler mHandler = new Handler();
    private static final long SCAN_PERIOD = 100000;
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                   mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描的方法（接口）
            san_bt.setText("扫描中");
        } else {
            mScanning = false;
            //mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.cancelDiscovery();//停止
            san_bt.setText("开始扫描");
        }
    }
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    DesayLog.d( "address  = "+device.getAddress() + ",rssi = " + rssi);//返回扫描到的蓝牙设备
                    try {
                        if(device!=null && rssi > -80){//进行过滤
//                                    if(device.getName().toLowerCase().contains("521")
//                                       || device.getName().toLowerCase().contains("103")
//                                            || device.getName().toLowerCase().contains("528")
//                                            || device.getName().toLowerCase().contains("502")
//                                            || device.getName().toLowerCase().contains("010")
//                                            ){
//                                        StringBuilder stringBuilder = null;
//                                        if (scanRecord != null && scanRecord.length > 0) {
//                                            stringBuilder = new StringBuilder(scanRecord.length);
//                                            for (byte byteChar : scanRecord)
//                                                stringBuilder.append(String.format("%02X ", byteChar));
//                                        }
//                                    DesayLog.d( "MAC地址 "+device.getAddress()+"，设备名 "+device.getName()
//                                            +"  广播内容  " + stringBuilder);
//                                        try {
//                                            Integer.parseInt(parseBroasdcastRecordForMode(scanRecord));
//                                        }catch (Exception e){
//                                            DesayLog.d( "MAC地址 "+device.getAddress()+"，设备名 "+device.getName()+"  e  " + e);
//                                        }
//                                    }

                            if(ble_address_edit!=null){
                                //ConnectAddress = ":"+ble_address_edit.getText().toString().trim();
                                ConnectAddress = ":"+ble_address_edit.getText().toString().trim().toLowerCase();//mine
                            }
                            if(!ConnectAddress.equals("")){
                                String address = device.getAddress().toLowerCase();
                                //address = address.substring(address.length()-4,address.length());
                                DesayLog.d(address);
                                if(address.contains(ConnectAddress)){
                                    if(mDsBluetoothConnector==null){
                                        mDsBluetoothConnector = DsBluetoothConnector.getInstance(MainActivity.this);
                                    }
//                                            mDsBluetoothConnector.connect(device.getAddress());
                                    if(mBTControlInterface==null){
                                        mBTControlInterface = BTControlInterface.getInstance(MainActivity.this);
                                    }
                                    DesayLog.d( "mBTControlInterface = "+mBTControlInterface);
                                    mBTControlInterface.connect(device.getAddress());//连接
                                    scanLeDevice(false);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                }
            };

    private String parseBroasdcastRecordForMode(byte[] records) {
        int pos = 0;
        String result = "";
        try{
            for (; pos + 1 < records.length; ) {
                int length = records[pos];
                int flag = records[pos + 1];
                if (flag == 0x0A) {
                    result = String.valueOf(records[pos + 2]);
                    break;
                }
                pos += length + 1;
            }
        }catch (Exception e){
            DesayLog.d("parseBroasdcastRecordForMode + e = " + e);
        }

        DesayLog.d("parseBroasdcastRecordForMode + result = " + result);
        return result;
    }


}
