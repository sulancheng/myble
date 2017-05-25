package com.susu.hh.mybl;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.susu.hh.mybl.alarmtest.AlarmActivity;

import java.util.ArrayList;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    private BleManager instance;
    private ArrayList<BluetoothDevice> bluetoothDevices = new ArrayList<>();
    private Myadapter madapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private ListView mListView;
    private Button bushu;
    private EditText zhiling;
    private TextView xianshi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initView();
        setLister();
        //initCast();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.mlist);
    }

    private void setLister() {
        mListView.setOnItemClickListener(this);
        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothDevices != null) {
                    bluetoothDevices.clear();
                }
                scanLeDevice(true, 5000);
            }
        });
        findViewById(R.id.dis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instance.disconnect();
            }
        });
        findViewById(R.id.band).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleContrParter.getBleContrpartInstance().bingMybind();
            }
        });

        findViewById(R.id.tiaoz).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TiaozActivity.class));
            }
        });
        findViewById(R.id.remind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AlarmActivity.class));
            }
        });
        bushu = (Button) findViewById(R.id.bushu);
        Button testMORE = (Button) findViewById(R.id.testMORE);
        xianshi = (TextView) findViewById(R.id.xianshi);
        zhiling = (EditText) findViewById(R.id.zhiling);
        bushu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String trim = zhiling.getText().toString().trim();
                BleContrParter.getBleContrpartInstance().zhiling(trim, 1);
            }
        });
        testMORE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BleContrParter.getBleContrpartInstance().zhiling("AT+BOND", 2);
                //blecontror.zhiling("AT+BOND");
            }
        });
    }

    BleContror.OnBLECallBackListener onBLECallBackListener = new BleContror.OnBLECallBackListener() {
        @Override
        public void bindResp(boolean isBind, String respone) {
            MyLog.i("bindRespmainac", isBind + "" + "response = " + respone);
            xianshi.setText(respone);
            if ("AT+BOND:OK".equals(respone)) {
            }
            if (respone.contains("AT+BATT")) {
                final String[] split = respone.split("\\:");
                MyLog.i("bindRespmainac", split.length + "");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bushu.setText(split[1]);
                    }
                });

            }
        }
    };

    private void init() {
        instance = BleManager.getInstanceandName(this, onBLECallBackListener);
        //instance = BleManager.getInstance(this,onBLECallBackListener);//成功回调
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
   /*    BleService service = instance.getService();
        if (null != service) {
            service.setBleContr(new BleContror.OnBLECallBackListener() {
                @Override
                public void bindResp(boolean isBind) {

                }
            });
        }*/
    }

    private void initCast() {
        MyLog.i("onReceive", "注册");
        IntentFilter filter1, filter2, filter3, filter4, filter5, filter6, filter7, filter8;
        filter1 = new IntentFilter("android.bluetooth.a2dp.profile.action.CONNECTION_STATE_CHANGED");
        filter2 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter3 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_CONNECTED);
        filter4 = new IntentFilter(android.bluetooth.BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter5 = new IntentFilter("android.bluetooth.BluetoothAdapter.STATE_OFF");
        filter6 = new IntentFilter("android.bluetooth.BluetoothAdapter.STATE_ON");
        filter7 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter8 = new IntentFilter(BluetoothAdapter.EXTRA_PREVIOUS_STATE);
        // BroadcastReceiver mReceiver;
        MyBroadCast mybroadcast = new MyBroadCast();
        registerReceiver(mybroadcast, filter1);
        registerReceiver(mybroadcast, filter2);
        registerReceiver(mybroadcast, filter3);
        registerReceiver(mybroadcast, filter4);
        registerReceiver(mybroadcast, filter5);
        registerReceiver(mybroadcast, filter6);
        registerReceiver(mybroadcast, filter7);
        registerReceiver(mybroadcast, filter8);
    }

    class MyBroadCast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            MyLog.i("onReceive", action);
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            MyLog.i("onReceivestate", state + "");
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Toast.makeText(MainActivity.this, "蓝牙状态改变广播 !", Toast.LENGTH_SHORT).show();
            // Log.i("autoconnect","调用");
            //autoconnect("D0:7E:A3:AC:04:D4");//解绑酒吧mac清除。 不然 就一直有可以不停的重新绑定

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(MainActivity.this, device.getName() + " 设备已发现！！", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Toast.makeText(MainActivity.this, device.getName() + "已连接", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Toast.makeText(MainActivity.this, device.getName() + "正在断开蓝牙连接。。。", Toast.LENGTH_SHORT).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(MainActivity.this, device.getName() + "蓝牙连接已断开！！！", Toast.LENGTH_SHORT).show();
            } else if (state == (BluetoothAdapter.STATE_OFF)) {
                Toast.makeText(MainActivity.this, "蓝牙已关闭", Toast.LENGTH_SHORT).show();
            } else if (state == (BluetoothAdapter.STATE_ON)) {
                Toast.makeText(MainActivity.this, "蓝牙打开", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean mScanning = false;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            bluetoothDevices.add((BluetoothDevice)msg.obj);
            if (madapter == null) {
                madapter = new Myadapter();
                mListView.setAdapter(madapter);
            } else {
                madapter.notifyDataSetChanged();
            }
        }
    };

    public void openBluetooth() {
        // 先获取一个蓝牙适配器对象
        // 判断设备是否支持蓝牙
        if (mBluetoothAdapter == null) {
             Toast.makeText(this, "当前设备不支持蓝牙", Toast.LENGTH_LONG).show();
            return;
        }
        // 判断蓝牙是否打开
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();  // 直接打开蓝牙
            //Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(intent, 1);
        }
    }//直接打开蓝牙的方法。

    public void scanLeDevice(boolean enable, long SCAN_PERIOD) {
        openBluetooth();
        Runnable mRunable = new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                mScanning = false;
            }
        };
        mHandler.removeCallbacks(mRunable);
        if (enable) {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mHandler.postDelayed(mRunable
                    , SCAN_PERIOD);
            mBluetoothAdapter.startLeScan(mLeScanCallback);//开始扫描的方法（接口）
            mScanning = true;
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            //mBluetoothAdapter.cancelDiscovery();//停止
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            MyLog.i("address  = " + device.getAddress() + ",rssi = " + rssi);//返回扫描到的蓝牙设备
            if (device != null && rssi > -80) {
                if (!bluetoothDevices.contains(device)) {
                    MyLog.i("bluetoothDevices  = " + bluetoothDevices.size() + ",rssi = " + rssi);
                    mHandler.obtainMessage(001,device).sendToTarget();
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        scanLeDevice(false, 0);
        BluetoothDevice bluetoothDevice = bluetoothDevices.get(position);
        MyLog.i("onItemClick", "我点击了" + bluetoothDevice.getAddress() + " name = " + bluetoothDevice.getName());
        MyLog.i("slcbleconnectliuc", "start onclick" );
        BleManager.getBleManagInstance().startNewControl(bluetoothDevice.getName());
        if (BleContrParter.getBleContrpartInstance().getState() == BleContror.BleZt.STATE_CONNECTED) {
            return;
        }
        madapter = null;
        instance.connect(bluetoothDevices.get(position).getAddress());
    }

    class Myadapter extends BaseAdapter {

        @Override
        public int getCount() {
            return bluetoothDevices == null ? null : bluetoothDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return bluetoothDevices == null ? null : bluetoothDevices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BluetoothDevice item = bluetoothDevices.get(position);
            MyLog.i("BluetoothDevice  +++++ " + item.getAddress() + ",rssi = " + item.getName());
            if (convertView == null) {
                convertView = View.inflate(MainActivity.this, R.layout.device_item, null);
            }
            TextView viewById = (TextView) convertView.findViewById(R.id.txt_address);
            TextView viewById1 = (TextView) convertView.findViewById(R.id.txt_name);
            viewById.setText(item.getAddress());
            viewById1.setText(item.getName());
            return convertView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BleManager.getBleManagInstance().release();//注销服务
        //清理缓存蓝牙。
        //BleContror.getInstance().refreshDeviceCache();
    }
}
