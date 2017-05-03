package com.desay.corn.blesdk;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.desay.corn.blelab.BLEContentProvider;
import com.desay.corn.blelab.DesayLog;
import com.desay.corn.blelab.DsBluetoothConnector;
import com.desay.corn.blelab.HeartRateData;
import com.desay.corn.blelab.SleepCycle;
import com.desay.corn.blelab.SportsData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by corn on 2016/7/21.
 */
public class BluetoothLoaderService extends Service {
    private boolean isBluetoothAdapterOn = false;
    BroadcastReceiver mBluetoothStateListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                DesayLog.d("action = " + intent.getAction());
            }
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            DesayLog.d("action = " + intent.getAction() + " state = " + state);
            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    if (AutoConnectState && AutoConnectAddress != null) {
                        autoConnectStart();
                    }
                    isBluetoothAdapterOn = true;
                    mOnBTServiceCallBackListener.OnBluetoothAdapterStateCallBack(BLUETOOTH_ADAPTER_ON);
                    break;
                case BluetoothAdapter.STATE_OFF:
                    if (autoConnectStart) {
                        autoConnectStop();
                    }
                    isBluetoothAdapterOn = false;
                    mOnBTServiceCallBackListener.OnBluetoothAdapterStateCallBack(BLUETOOTH_ADAPTER_OFF);
                    break;
            }
        }
    };

    public static final int SERVICE_ON_BIND = 11;
    public static final int SERVICE_ON_DESTROY = 12;
    public static final int BLUETOOTH_ADAPTER_ON = 13;
    public static final int BLUETOOTH_ADAPTER_OFF = 14;

    public interface OnBTServiceCallBackListener {
        void OnBluetoothAdapterStateCallBack(int status);

        void OnServiceStateCallBack(int status);

        void OnConnectCallBack(int event, int status);

        /**
         * event 事件号，映射如下KEY_BAND_VERSION等值,你可以在DsBluetoothConnector查询到这些值
         * value 返回值，例如返回版本号
         * status 返回操作状态，操作成功或者操作失败
         * state 当前指令状态，例如关闭找手机功能，ENABLE = 1； DISABLE = 0;
         * 同时，state在特定的事件中也用于返回int值，例如查询步数，此时返回值state代表返回的步数
         * 再例如设置当前手环语言，state代表着当前手环已经设置的语言
         */
        void OnCallBack(int event, boolean status, String value);

        void OnCallBack(int event, boolean status, int state);

        /**
         * @param event 事件号，映射如下KEY_BAND_VERSION等值
         */
        void OnSleepDataCallBack(int event);

        /**
         * @param event 事件号，映射如下KEY_BAND_VERSION等值
         */
        void OnHeartRateDataCallBack(int event);

        /**
         * @param event 事件号，映射如下KEY_BAND_VERSION等值
         */
        void OnSportsDataCallBack(int event);

        /**
         * @param event 事件号
         */
        void OnCameraShotCallBack(int event);

        /**
         * @param event 事件号
         * @param cmd   音乐指令，你可以在BLEContentProvider里找到这些指令对应的映射
         */
        void OnMusicCtrolCallBack(int event, int cmd);

        /**
         * @param event 事件号
         */
        void OnFindPhoneCallBack(int event);
    }

    OnBTServiceCallBackListener mOnBTServiceCallBackListener = null;

    public void setOnBLECallBackListener(OnBTServiceCallBackListener listener) {
        mOnBTServiceCallBackListener = listener;
    }

    private DsBluetoothConnector mDsBluetoothConnector;

    public class LocalBinder extends Binder {
        public LocalBinder() {
        }

        public BluetoothLoaderService getService() {
            return BluetoothLoaderService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        initialize(this);
        DesayLog.d("onBind  mBluetoothAdapter = " + mBluetoothAdapter);
        if (mBluetoothAdapter != null) {
            mDsBluetoothConnector = DsBluetoothConnector.getInstance(this, mBluetoothAdapter);
        } else {
            mDsBluetoothConnector = DsBluetoothConnector.getInstance(this);
        }
        mDsBluetoothConnector.setOnBLECallBackListener(mOnBLECallBackListener);
        DesayLog.d("mDsBluetoothConnector = " + mDsBluetoothConnector);
        //listen the BluetoothAdapter state
        registerReceiver(mBluetoothStateListener, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DesayLog.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        DesayLog.d("onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        DesayLog.d("onDestroy");
        //close the use bluetooth
        mDsBluetoothConnector.close();
        if (scanState) {
            scanLeDevice(false);
        }
        if (mOnBTServiceCallBackListener != null) {
            mOnBTServiceCallBackListener.OnServiceStateCallBack(SERVICE_ON_DESTROY);
        }
        //end listen
        unregisterReceiver(mBluetoothStateListener);
        super.onDestroy();
    }

    //数据处理
    List<SleepCycle> sleepCycleHandlerList = null;
    List<HeartRateData> heartRateHandlerList = null;
    List<SportsData> sportsHandlerList = null;
    private final int SLEEP_DATA_HANDLE_MSG = 0;
    private final int SPORT_DATA_HANDLE_MSG = 1;
    private final int HEART_RATE_DATA_HANDLE_MSG = 2;
    Handler dataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SLEEP_DATA_HANDLE_MSG:
                    //去看界面需要什么数据，就搭什么样的数据库
                    //插到purifit的数据库吧，其他的不管了
                    if (mOnBTServiceCallBackListener!=null){
                        mOnBTServiceCallBackListener.OnSleepDataCallBack(DsBluetoothConnector.KEY_SYN_DATA_SLEEP);
                    }
                    break;
                case SPORT_DATA_HANDLE_MSG:
                    //去看界面需要什么数据，就搭什么样的数据库
                    if (sportsHandlerList.size() != 0) {
                        for (SportsData sports : sportsHandlerList) {
                            DesayLog.d("start_time = " + sports.startTime + ",steps = " + sports.steps + ",sportsLong = " + sports.sportsLong);
                        }
                    }
                    if (mOnBTServiceCallBackListener!=null){
                        mOnBTServiceCallBackListener.OnSportsDataCallBack(DsBluetoothConnector.KEY_SYN_DATA_SPORTS);
                    }
                    break;
                case HEART_RATE_DATA_HANDLE_MSG:
                    //去看界面需要什么数据，就搭什么样的数据库
                    if (mOnBTServiceCallBackListener!=null){
                        mOnBTServiceCallBackListener.OnHeartRateDataCallBack(DsBluetoothConnector.KEY_SYN_DATA_HEARTRATE);
                    }
                    break;
            }
        }
    };

    private DsBluetoothConnector.OnBLECallBackListener mOnBLECallBackListener = new DsBluetoothConnector.OnBLECallBackListener() {
        @Override
        public void OnConnectCallBack(int event, int status) {
            //这里需要增加一个状态，同步的状态，连接完成后，SDK自动去同步，不需要界面上再去操作了
            DesayLog.d("当前连接状态：" + status);
            int new_status = status;

            if (status == BLEContentProvider.STATE_DISCONNECTED || status == BLEContentProvider.STATE_CONNECTED_FAIL) {
                if (!isOtaDisconnect) {//ota disconnect
                    DesayLog.d("当前连接状态：" + status + ",AutoConnectState = " + AutoConnectState + ",AutoConnectAddress = " + AutoConnectAddress);
                    if (AutoConnectState && AutoConnectAddress != null) {
                        //打开自动连接并且不是OTA造成的断开，并且绑定的地址是正确的,因为OTA调用的是另外一个连接的回调，所以不用担心他下次返回，
                        // 只需要关心第一次断开就OK
                        //AutoConnect enable , AutoConnectAddress not a default null , is not a ota disconnect
                        autoConnectStart();
                        //把正在自动连接的状态丢给上层
                        new_status = BLEContentProvider.STATE_AUTO_CONNECTING;
                    }
                } else {
                    //ota disconnect only single-effective , default ota disconnect state
                    isOtaDisconnect = false;
                }
            }

            //抛到上层去
            if (mOnBTServiceCallBackListener!=null){
                mOnBTServiceCallBackListener.OnConnectCallBack(event, new_status);
            }
        }

        /**
         * @param event
         * @param status  操作状态
         * @param value
         */
        @Override
        public void OnCallBack(int event, boolean status, String value) {
            //更新到内容提供提供器里边
            mOnBTServiceCallBackListener.OnCallBack(event, status, value);
            switch (event) {
                case DsBluetoothConnector.KEY_BAND_VERSION:
                    if (status) {
                        DesayLog.d("当前版本号：" + value);
                    } else { //error 这里其实只因为你查询的格式有问题才会返回error,如出现，是SDK的问题
                        DesayLog.d("当前版本号：" + value);
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
            //抛到上层
            mOnBTServiceCallBackListener.OnCallBack(event, status, state);

            switch (event) {
                case DsBluetoothConnector.KEY_BAND_BOND:
                    if (status) {
                        DesayLog.d("绑定成功");
                    } else {
                        DesayLog.d("绑定失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SYN_TIME:
                    if (status) {
                        DesayLog.d("同步成功");
                    } else {
                        DesayLog.d("同步失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_CLOCK_ONE:
                    if (status) {
                        DesayLog.d("设置闹钟1成功");
                    } else {
                        DesayLog.d("设置闹钟1失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_CLOCK_TWO:
                    if (status) {
                        DesayLog.d("设置闹钟2成功");
                    } else {
                        DesayLog.d("设置闹钟2失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_BATTERY:
                    if (status) {
                        DesayLog.d("当前电池电量" + state + "%");
                    } else {
                        DesayLog.d("查询电量失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_STEPS:
                    if (status) {
                        DesayLog.d("当前步数为 = " + state);
                    } else {
                        DesayLog.d("获取步数失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SIT:
                    if (status) {
                        DesayLog.d("设置久坐参数成功");
                    } else {
                        DesayLog.d("设置久坐参数失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_INCOMING_CALL:
                    if (status) {
                        DesayLog.d("来电提醒操作成功");
                    } else {
                        DesayLog.d("来电提醒操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_LANGUAGE:
                    if (status) {
                        DesayLog.d("手环语言设置成功,当前手环设置语言为：" + (state == 1 ? "中文" : "英文"));
                    } else {
                        DesayLog.d("手环语言设置失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SPORTS_AIM:
                    if (status) {
                        DesayLog.d("设置运动目标成功，设置步数为" + state);
                    } else {
                        DesayLog.d("设置运动目标失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_HAND_UP:
                    if (status) {
                        DesayLog.d("抬手亮屏设置成功，当前设置参数为：" + state);
                    } else {
                        DesayLog.d("抬手亮屏设置失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_TIMELY_STEPS_SYN:
                    if (status) {
                        DesayLog.d("设置实时同步功能操作成功，当前功能已经" + (state == 1 ? "打开" : "关闭"));
                    } else {
                        DesayLog.d("设置实时同步功能操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_SYN_FLAG:
                    if (status) {
                        DesayLog.d("设置同步图标成功，当前图标已经" + (state == 1 ? "显示" : "关闭"));
                    } else {
                        DesayLog.d("设置同步图标失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_SYN_MUSIC:
                    if (status) {
                        DesayLog.d("同步音乐播放状态成功");
                    } else {
                        DesayLog.d("同步音乐播放状态失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_TEST_HEARTRATE:
                    if (status) {
                        DesayLog.d("成功测试心率 当前用户心率 = " + state);
                    } else {
                        DesayLog.d("测试心率失败,请正确佩戴手环");
                    }
                    break;
                case DsBluetoothConnector.KEY_FIND_BAND:
                    if (status) {
                        DesayLog.d("找手环操作成功");
                    } else {
                        DesayLog.d("找手环操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_CAMERA_FLAG:
                    if (status) {
                        DesayLog.d("拍照界面操作成功，当前拍照图标已经在手环界面" + (state == 1 ? "显示" : "关闭"));
                    } else {
                        DesayLog.d("拍照界面操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_MUSIC_FLAG:
                    if (status) {
                        DesayLog.d("音乐功能操作成功，当前音乐播放已经在手环界面" + (state == 1 ? "打开" : "关闭"));
                    } else {
                        DesayLog.d("音乐功能操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_FIND_PHONE_FLAG:
                    if (status) {
                        DesayLog.d("找手机功能操作成功，当前找手机已经在手环界面" + (state == 1 ? "打开" : "关闭"));
                    } else {
                        DesayLog.d("找手机功能操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_UNITS:
                    if (status) {
                        DesayLog.d("设置手环单位操作成功，当前单位" + (state == 1 ? "kilo" : "mile"));
                    } else {
                        DesayLog.d("设置手环单位操作失败");
                    }
                    break;
                case DsBluetoothConnector.KEY_BAND_USER:
                    if (status) {
                        DesayLog.d("用户信息同步成功");
                    } else {
                        DesayLog.d("用户信息同步失败");
                    }
                    break;
            }

        }

        @Override
        public void OnSleepDataCallBack(int event, List<SleepCycle> mSleepCycleList) {
            sleepCycleHandlerList = mSleepCycleList;
            //整理之后存数据库
            dataHandler.sendEmptyMessage(SLEEP_DATA_HANDLE_MSG);
        }

        @Override
        public void OnHeartRateDataCallBack(int event, List<HeartRateData> mHeartRateDataList) {
            //整理之后存数据库
            heartRateHandlerList = mHeartRateDataList;
            dataHandler.sendEmptyMessage(HEART_RATE_DATA_HANDLE_MSG);
        }

        @Override
        public void OnSportsDataCallBack(int event, List<SportsData> mSportsData) {
            //整理之后存数据库
            sportsHandlerList = mSportsData;
            dataHandler.sendEmptyMessage(SPORT_DATA_HANDLE_MSG);
        }

        @Override
        public void OnCameraShotCallBack(int event) {
            //抛到上层
            DesayLog.d("拍照通知到来");
            mOnBTServiceCallBackListener.OnCameraShotCallBack(event);
        }

        @Override
        public void OnMusicCtrolCallBack(int event, int cmd) {
            //直接在这处理掉
            //广播到需要接收的地方去吧，或者直接在这处理掉
            switch (cmd) {
                case BLEContentProvider.NT_MUSIC_PLAY:
                    DesayLog.d("开始播放");
                    break;
                case BLEContentProvider.NT_MUSIC_PAUSE:
                    DesayLog.d("暂停播放");
                    break;
                case BLEContentProvider.NT_MUSIC_NEXT:
                    DesayLog.d("下一曲");
                    break;
                case BLEContentProvider.NT_MUSIC_PRE:
                    DesayLog.d("上一曲");
                    break;
            }
        }

        @Override
        public void OnFindPhoneCallBack(int event) {
            //直接在这处理
            DesayLog.d("手环正在找手机");
            mOnBTServiceCallBackListener.OnFindPhoneCallBack(event);
        }
    };

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    private boolean initialize(Context mContext) {
        // For API level 18 and above, get a reference to BluetoothAdapter
        // through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                DesayLog.e("Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            DesayLog.e("Unable to obtain a BluetoothAdapter.");
            isBluetoothAdapterOn = false;
            return false;
        }
        isBluetoothAdapterOn = mBluetoothAdapter.isEnabled();
        DesayLog.e("isBluetoothAdapterOn = " + isBluetoothAdapterOn);
        return true;
    }


    private boolean scanState = false;//ondestroy

    private void scanLeDevice(boolean enable) {
        if (enable) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);//扫描
            scanState = true;
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            scanState = false;
        }
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
                    DesayLog.d("scan in service address  = " + device.getAddress() + ",rssi = " + rssi);
                    if (device != null) {
                        if (AutoConnectAddress != null) {
                            if (rssi > -80) {//靠近些再连，保证连接的稳定性
                                if (device.getAddress().equals(AutoConnectAddress)) {
                                    DesayLog.d("连接开始 ");
                                    connect(device.getAddress());
                                    DesayLog.d("停止扫描 ");
                                    autoConnectStop();//停止扫描
                                }
                            }
                        }
                    }
                }
            };


    //自动连接，需要设计解绑不再自动连接情况,需要设计手机关闭蓝牙情况
    private boolean AutoConnectState = false;
    private String AutoConnectAddress = null;

    /**
     * @param auto     auto connect bond device or not
     * @param bond_mac bond_device_address
     *                 remember you must call BluetoothLoaderService.disconnect() when you unbind
     *                 device and call BluetoothLoaderService.setAutoConnect() when binding device or start app
     */
    public void setAutoConnect(boolean auto, String bond_mac) {
        if (bond_mac.equals("") || bond_mac == null) {
            return;
        }
        AutoConnectState = auto;
        AutoConnectAddress = bond_mac;
    }

    //自动连接的handler
    private final int AUTO_CONNECT_MSG = 120;
    private Handler autoConnectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //check AutoConnect state and bond address
            //PS:we will reset the auto connect parameter to default
            if (msg.what == AUTO_CONNECT_MSG) {
                if (AutoConnectState && AutoConnectAddress != null) {
                    //无论什么时候自动连接状态改变都会终止自动连接，所以，把他用在解绑的时候
                    //run the scan runable
                    scanLeDevice(!scanState);//scan 5S and rest 5s
                } else {
                    autoConnectStop();
                }
            }
        }
    };

    //just control the scan run
    public boolean autoConnectStart = false;
    Timer mProxyTimer;
    TimerTask mAutoConnectTask;

    private void autoConnectStart() {
        DesayLog.d("autoConnectStart：" + autoConnectStart + ",AutoConnectState = " + AutoConnectState + ",AutoConnectAddress = " + AutoConnectAddress);
        if (AutoConnectState && AutoConnectAddress != null) {
            //必须打开自动连接且绑定了设备，自动连接只会去连接那些已经绑定的设备
            if (!autoConnectStart) {//防止被反复执行
                mProxyTimer = new Timer();
                mAutoConnectTask = new TimerTask() {
                    @Override
                    public void run() {
                        // 需要做的事:发送消息
                        autoConnectHandler.sendEmptyMessage(AUTO_CONNECT_MSG);
                    }
                };
                mProxyTimer.schedule(mAutoConnectTask, 5000, 5000);
                autoConnectStart = true;
            }
        }
    }

    private void autoConnectStop() {
        autoConnectStart = false;
        mProxyTimer.cancel();
        if (scanState) {
            scanLeDevice(false);
        }
        DesayLog.d("mProxyTimer.cancel() ");
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The
     * connection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(String address) {
        DesayLog.d("mDsBluetoothConnector = " + mDsBluetoothConnector);
        if (mDsBluetoothConnector != null) {
            return mDsBluetoothConnector.connect(address);
        }
        return false;
    }


    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     * only used in unbind device
     */
    public void disconnect() {
        AutoConnectState = false;
        AutoConnectAddress = null;
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.disconnect();
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The
     * disconnection result is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     * only used in ota
     */
    private boolean isOtaDisconnect = false;

    public void OTAdisconnect() {
        isOtaDisconnect = true;
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.disconnect();
        }
    }


    /**
     * bind the band
     */
    public void bindMyBand() {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.bindMyBand();
        }
    }


    /**
     * @param user_height user height
     * @param user_weight user weight
     */
    public void synUserInfo(int user_height, int user_weight) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.synUserInfo(user_height, user_weight);
        }
    }

    /**
     * check Band Version
     */
    public void checkBandVersion() {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.checkBandVersion();
        }
    }

    /**
     * @param time 24h sample String "20140520135601"
     */
    public void synBandTime(String time) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.synBandTime(time);
        }
    }

    /**
     * @param witch      witch alarm   the value should be  1/2
     * @param enable     enable or diable the value should be  1/0
     * @param cycle_time should be String sample "1111110" means 周日周一周二周三周四周五有闹钟提醒 周六没有 0 for open  1 for close
     * @param alarm_time should be String sample "0800"  for 08:00 in 24h
     **/
    public void setBandAlarm(int witch, int enable, String cycle_time, String alarm_time) {//Set the alarm
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setBandAlarm(witch, enable, cycle_time, alarm_time);
        }
    }

    /**
     * check band battry,you can get the result by call BLEContentProvider.getBandPower();
     */
    public void checkBandBattry() {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.checkBandBattry();
        }
    }


    /**
     * @param steps if steps <= 0 will be get the band total steps
     *              else  will be set the band total steps
     *              sample :
     *              getBandTotalSteps(0);
     *              return the band current steps,you can get the result by call BLEContentProvider.getBandCurrentSteps();
     *              getBandTotalSteps(1000);
     *              set the band current steps as 1000.
     *              the steps set to band must be more than the band steps already had.
     *              the steps set to band must Less than the maximum 99999
     */
    public void getBandTotalSteps(int steps) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.getBandTotalSteps(steps);
        }
    }

    /**
     * @param _long      how long we sit the band will remind will be 30/60/90 (min)
     * @param start_time the remind start time in a day sample "0800" means start 08:00
     * @param end_time   the remind end time in a day sample "2300" means end 23:00
     * @param _enable    enable/disable the Sedentary function , must be 1/0
     */
    public void setSedentary(int _long, String start_time, String end_time, int _enable) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setSedentary(_long, start_time, end_time, _enable);
        }
    }


    private boolean PHONE_CALL_STATE = false;
    private String phone_call_cmd = "";

    /**
     * @param type    type 0/1 to en/ch
     * @param content
     * @param msgType message type:
     *                0-phone call，1-SMS，3-QQ，4-wechat，5-fackbook, 6-twitter,7-whatsapp,8-email,9-line,
     *                you should get message type from @BLEContentProvider
     *                remember! this method must be used before setPhoneCallState();
     *                如果当前已经在内部通知状态，则不再接受其他的来电指令，不然逻辑会混乱
     */
    public void massageRemind(int type, String content, int msgType) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.massageRemind(type, content, msgType);
        }
    }

    /**
     * @param isPhoneCallNow phone call state
     *                       remember! this method must be used and must be used after synCallerInfo();
     */
    public void setPhoneCallState(boolean isPhoneCallNow) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setPhoneCallState(isPhoneCallNow);
        }
    }

//    public void getBandSerialNumber(){
//        addCMD(BLEContentProvider.CMD_GET_SN,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
//    }


    /**
     * @param language 1/0 - cn/en
     *                 you should get language from @BLEContentProvider
     */
    public void setBandLanguage(int language) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setBandLanguage(language);
        }
    }


    /**
     * @param aim aim for sports,  the aim set to band must Less than the maximum 99999 and should be n*1000
     */
    public void setBandSportsAim(int aim) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setBandSportsAim(aim);
        }
    }

    /**
     * @param param the hand up param,
     *              <p>
     *              * 参数：
     *              0：关闭
     *              1：自动
     *              2: 左手
     *              3：右手
     *              you should get param from @BLEContentProvider
     *              public static int HANDUP_CLOSE = 0;
     *              public static int HANDUP_AUTO = 1;
     *              public static int HANDUP_LEFT = 2;
     *              public static int HANDUP_RIGHT = 3;
     */
    public void setBandHandUpParam(int param) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setBandHandUpParam(param);
        }
    }


    /**
     * @param enable enable Timely steps syn
     *               with true , you can get the timely steps from band.
     *               you can get the result by call BLEContentProvider.getTimelyStepsEnableState();
     */
    public void enableSynTimelySteps(boolean enable) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.enableSynTimelySteps(enable);
        }
    }


    /**
     * @param show show the syn icon or close the icon show
     *             witch this method you can control the band syn show or not
     */
    public void showBandSynIcon(boolean show) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.showBandSynIcon(show);
        }
    }


    /**
     * @param play Synchronize the phone music play state .
     *             the band would not know the play state if it is change by
     *             operating at phone,so need to syn the state to band.
     */
    public void synPhoneMusicPlayState(boolean play) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.synPhoneMusicPlayState(play);
        }
    }


    /**
     * control band start check user HeartRate
     * you can get the result by call BLEContentProvider.getBandHeartRateCheckState();
     */
    public void startCheckHeartRate() {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.startCheckHeartRate();
        }
    }


    /**
     * You can get the result by call BLEContentProvider.getFindBandState();
     * But you must reset the state after getFindBandState().
     * Call BLEContentProvider.setFindBandState(false) after you get the result.
     */
    public void findMyBand(boolean start) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.findMyBand(start);
        }
    }


    /**
     * @param show show/close the band snapshot View
     */
    public void showBandPhotoView(boolean show) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.showBandPhotoView(show);
        }
    }

    /**
     * @param enable enable/disable band music function
     */
    public void enableMusicFunction(boolean enable) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.enableMusicFunction(enable);
        }
    }


    /**
     * @param enable enable/disable band find phone function
     */
    public void enableFindPhoneFunction(boolean enable) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.enableFindPhoneFunction(enable);
        }
    }


    /**
     * @param units you should get units from @BLEContentProvider
     *              public static final int BAND_UNITS_MILE = 0;
     *              public static final int BAND_UNITS_KILO = 1;
     */
    public void setBandUnits(int units) {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.setBandUnits(units);
        }
    }

    /**
     * syn data
     */
    public void synData() {
        if (mDsBluetoothConnector != null) {
            mDsBluetoothConnector.synData();
        }
    }


}
