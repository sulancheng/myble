package com.desay.corn.blesdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import com.desay.corn.blelab.BLEContentProvider;
import com.desay.corn.blelab.DesayLog;
import com.desay.corn.blelab.DsBluetoothConnector;

public class BTControlInterface {
	private static final String TAG = "wxf_BLEDolenService";
	
	private static BTControlInterface Instance = null;
	private static Handler mHandler;
	private static BluetoothLoaderService mLeService = null;
	private static BluetoothLoaderService.OnBTServiceCallBackListener mOnBTServiceCallBackListener = null;

	//know that this mConnection is the BluetoothLeService , not the divice services
	private static ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
		 	mLeService = ((BluetoothLoaderService.LocalBinder) service)
					.getService();
			if(mOnBTServiceCallBackListener!=null){
				mLeService.setOnBLECallBackListener(mOnBTServiceCallBackListener);
			}
			mOnBTServiceCallBackListener.OnServiceStateCallBack(BluetoothLoaderService.SERVICE_ON_BIND);
			DesayLog.d("BluetoothLoaderService connect finish");
		}
	};

	private BTControlInterface(Context context) {
		Intent service = new Intent(context, BluetoothLoaderService.class);
		try {
			context.bindService(service, mConnection, Context.BIND_AUTO_CREATE);
			DesayLog.d("BTControlInterface  bindService");
		} catch (Exception e) {
			// TODO: handle exception
			DesayLog.d("BTControlInterface e" + e);
		}
	}

	public static BTControlInterface getInstance(Context context) {
		if (Instance == null) {
			Instance = new BTControlInterface(context);
			DesayLog.d("BTControlInterface  getInstance");
		}
		return Instance;
	}

	public void setOnBLECallBackListener(BluetoothLoaderService.OnBTServiceCallBackListener listener) {
		mOnBTServiceCallBackListener = listener;
		if(mLeService!=null){
			mLeService.setOnBLECallBackListener(listener);
		}
	}

	/**
	 * @param auto  auto connect bond device or not
	 * @param bond_mac bond_device_address
	 *                 remember you must call BluetoothLoaderService.disconnect() when you unbind
	 *                 device and call BluetoothLoaderService.setAutoConnect() when binding device or start app
	 */
	public void setAutoConnect(boolean auto,String bond_mac){
		DesayLog.d( "mLeService = "+ mLeService);
		if(mLeService!=null){
			mLeService.setAutoConnect(auto,bond_mac);
		}
	}

	public boolean connect(String address) {
		DesayLog.d( "mLeService = "+ mLeService);
		if(mLeService!=null){
			if(mLeService.autoConnectStart){
				mOnBTServiceCallBackListener.OnConnectCallBack(DsBluetoothConnector.KEY_CONNECT_STATUS,BLEContentProvider.STATE_AUTO_CONNECTING);
				return true;
			}
			return mLeService.connect(address);
		}
		return false;
	}

	public void disconnect() {
		DesayLog.d( "mLeService = "+ mLeService);
		if(mLeService!=null){
			 mLeService.disconnect();
		}
	}


	/**
	 * bind the band
	 */
	public void bindMyBand(){
		if(mLeService!=null){
			mLeService.bindMyBand();
		}
	}


	/**
	 * @param user_height user height
	 * @param user_weight user weight
	 */
	public void synUserInfo(int user_height,int user_weight){
		if(mLeService!=null){
			mLeService.synUserInfo(user_height,user_weight);
		}
	}

	/**
	 *check Band Version
	 */
	public void checkBandVersion(){
		if(mLeService!=null){
			mLeService.checkBandVersion();
		}
	}

	/**
	 * @param time 24h sample String "20140520135601"
	 */
	public void synBandTime(String time){
		if(mLeService!=null){
			mLeService.synBandTime(time);
		}
	}

	/**
	 * @param witch       witch alarm   the value should be  1/2
	 * @param enable      enable or diable the value should be  1/0
	 * @param cycle_time  should be String sample "1111110" means 周日周一周二周三周四周五有闹钟提醒 周六没有 0 for open  1 for close
	 * @param alarm_time  should be String sample "0800"  for 08:00 in 24h
	 * **/
	public void setBandAlarm(int witch,int enable,String cycle_time,String alarm_time){//Set the alarm
		if(mLeService!=null){
			mLeService.setBandAlarm(witch,enable,cycle_time,alarm_time);
		}
	}

	/**
	 *check band battry,you can get the result by call BLEContentProvider.getBandPower();
	 */
	public void checkBandBattry(){
		if(mLeService!=null){
			mLeService.checkBandBattry();
		}
	}


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
	public void getBandTotalSteps(int steps){
		if(mLeService!=null){
			mLeService.getBandTotalSteps(steps);
		}
	}

	/**
	 * @param _long    how long we sit the band will remind will be 30/60/90 (min)
	 * @param start_time the remind start time in a day sample "0800" means start 08:00
	 * @param end_time the remind end time in a day sample "2300" means end 23:00
	 * @param _enable enable/disable the Sedentary function , must be 1/0
	 */
	public void setSedentary(int _long,String start_time,String end_time,int _enable){
		if(mLeService!=null){
			mLeService.setSedentary(_long,start_time,end_time,_enable);
		}
	}



	private boolean PHONE_CALL_STATE = false;
	private String phone_call_cmd = "";
	/**
	 * @param type type 0/1 to en/ch
	 * @param content
	 * @param msgType message type:
	 *                              0-phone call，1-SMS，3-QQ，4-wechat，5-fackbook, 6-twitter,7-whatsapp,8-email,9-line,
	 *                              you should get message type from @BLEContentProvider
	 *                remember! this method must be used before setPhoneCallState();
	 *                如果当前已经在内部通知状态，则不再接受其他的来电指令，不然逻辑会混乱
	 */
	public void massageRemind(int type,String content,int msgType){
		if(mLeService!=null){
			mLeService.massageRemind(type,content,msgType);
		}
	}

	/**
	 * @param isPhoneCallNow phone call state
	 *        remember! this method must be used and must be used after synCallerInfo();
	 */
	public void setPhoneCallState(boolean isPhoneCallNow){
		if(mLeService!=null){
			mLeService.setPhoneCallState(isPhoneCallNow);
		}
	}

//    public void getBandSerialNumber(){
//        addCMD(BLEContentProvider.CMD_GET_SN,BLEContentProvider.SERVER_A_UUID_REQUEST_INT);
//    }


	/**
	 * @param language 1/0 - cn/en
	 *                 you should get language from @BLEContentProvider
	 */
	public void setBandLanguage(int language){
		if(mLeService!=null){
			mLeService.setBandLanguage(language);
		}
	}


	/**
	 * @param aim aim for sports,  the aim set to band must Less than the maximum 99999 and should be n*1000
	 */
	public void setBandSportsAim(int aim){
		if(mLeService!=null){
			mLeService.setBandSportsAim(aim);
		}
	}

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
	public void setBandHandUpParam(int param){
		if(mLeService!=null){
			mLeService.setBandHandUpParam(param);
		}
	}


	/**
	 * @param enable
	 *        enable Timely steps syn
	 *        with true , you can get the timely steps from band.
	 *        you can get the result by call BLEContentProvider.getTimelyStepsEnableState();
	 */
	public void enableSynTimelySteps(boolean enable){
		if(mLeService!=null){
			mLeService.enableSynTimelySteps(enable);
		}
	}


	/**
	 * @param show    show the syn icon or close the icon show
	 *                witch this method you can control the band syn show or not
	 */
	public void showBandSynIcon(boolean show){
		if(mLeService!=null){
			mLeService.showBandSynIcon(show);
		}
	}


	/**
	 * @param play  Synchronize the phone music play state .
	 *              the band would not know the play state if it is change by
	 *              operating at phone,so need to syn the state to band.
	 *
	 *
	 */
	public void synPhoneMusicPlayState(boolean play){
		if(mLeService!=null){
			mLeService.synPhoneMusicPlayState(play);
		}
	}


	/**
	 *
	 * control band start check user HeartRate
	 * you can get the result by call BLEContentProvider.getBandHeartRateCheckState();
	 */
	public void startCheckHeartRate(){
		if(mLeService!=null){
			mLeService.startCheckHeartRate();
		}
	}


	/**
	 * You can get the result by call BLEContentProvider.getFindBandState();
	 * But you must reset the state after getFindBandState().
	 * Call BLEContentProvider.setFindBandState(false) after you get the result.
	 */
	public void findMyBand(boolean start){
		if(mLeService!=null){
			mLeService.findMyBand(start);
		}
	}


	/**
	 * @param show show/close the band snapshot View
	 *
	 */
	public void showBandPhotoView(boolean show){
		if(mLeService!=null){
			mLeService.showBandPhotoView(show);
		}
	}

	/**
	 *
	 * @param enable    enable/disable band music function
	 */
	public void enableMusicFunction(boolean enable){
		if(mLeService!=null){
			mLeService.enableMusicFunction(enable);
		}
	}


	/**
	 *
	 * @param enable    enable/disable band find phone function
	 */
	public void enableFindPhoneFunction(boolean enable){
		if(mLeService!=null){
			mLeService.enableFindPhoneFunction(enable);
		}
	}


	/**
	 *
	 * @param units     you should get units from @BLEContentProvider
	 *                   public static final int BAND_UNITS_MILE = 0;
	 *                   public static final int BAND_UNITS_KILO = 1;
	 */
	public void setBandUnits(int units){
		if(mLeService!=null){
			mLeService.setBandUnits(units);
		}
	}

	/**
	 *syn data
	 */
	public void synData(){
		if(mLeService!=null){
			mLeService.synData();
		}
	}



}
