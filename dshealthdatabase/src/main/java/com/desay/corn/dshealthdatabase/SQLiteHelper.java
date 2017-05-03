package com.desay.corn.dshealthdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {
	public final String TAG = "database_debug";
	private static final String DB_NAME = "ds_health.db";
	public static final int BaseVersion = 1;

	private static SQLiteHelper Instance = null;
	public SQLiteHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	public SQLiteHelper(Context context) {
		this(context, DB_NAME, null, BaseVersion);
	}

	public static  SQLiteHelper getInstance(Context context) {
		if (Instance == null) {
			Instance = new SQLiteHelper(context);
		}
		return Instance;
	}
	
	
	/**
	 * create sql database
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		String device_table = "CREATE TABLE yc_ble_devices (id INTEGER PRIMARY KEY, "
//				+
//				BluetoothDeviceSQLOperate.KEY_DEVICE_NAME+" TEXT, " +
//				BluetoothDeviceSQLOperate.KEY_DEVICE_ADDRESS+" TEXT," +
//				BluetoothDeviceSQLOperate.KEY_DEVICE_CONNECT_STATE+" integer," +
//				BluetoothDeviceSQLOperate.KEY_DEVICE_VERSIONS+" integer," +
//				BluetoothDeviceSQLOperate.KEY_DEVICE_LOGO_URI+" TEXT," +
//				BluetoothDeviceSQLOperate.KEY_DEVICE_PRIORITY+" integer);"
				;

		String setting_table = "CREATE TABLE yc_ble_settings (id INTEGER PRIMARY KEY, "
//				+
//				SettingSQLOperate.KEY_BLE_ADDRESS + " TEXT," +
//				SettingSQLOperate.KEY_BLE_NAME + " TEXT," +
//				SettingSQLOperate.KEY_MISS_MMS + " integer," +
//				SettingSQLOperate.KEY_MISS_CALL + " integer," +
//				SettingSQLOperate.KEY_ALERT_MUSIC + " TEXT," +
//				SettingSQLOperate.KEY_ALERT_DISTANCE + " integer," +
//				SettingSQLOperate.KEY_BLE_ALERT + " integer," +
//				SettingSQLOperate.KEY_WARN_PATTERN + " integer,"+
//				SettingSQLOperate.KEY_LOST_AND_FIND_LO + " integer,"+
//				SettingSQLOperate.KEY_LOST_AND_FIND_LA + " integer,"+
//				SettingSQLOperate.KEY_SHORTCUT + " integer);"
				;

		
		db.execSQL(device_table);
		db.execSQL(setting_table);
	}

	
	
	/**
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + SportsDataRecorder.MONTH_SPORTS_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SportsDataRecorder.WEEK_SPORTS_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SportsDataRecorder.DAY_SPORTS_TB_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + SportsDataRecorder.ORIGINAL_SPORTS_TB_NAME);
		onCreate(db);
	}

	/**
	 *
	 * @param db
	 * @param oldColumn
	 * @param newColumn
	 * @param typeColumn
	 */
	public void updateColumn(SQLiteDatabase db, String oldColumn,
			String newColumn, String typeColumn) {
		try {
			db.execSQL("ALTER TABLE " + SportsDataRecorder.MONTH_SPORTS_TB_NAME + " CHANGE " + oldColumn + " "
					+ newColumn + " " + typeColumn);
			db.execSQL("ALTER TABLE " + SportsDataRecorder.WEEK_SPORTS_TB_NAME + " CHANGE " + oldColumn + " "
					+ newColumn + " " + typeColumn);
			db.execSQL("ALTER TABLE " + SportsDataRecorder.DAY_SPORTS_TB_NAME + " CHANGE " + oldColumn + " "
					+ newColumn + " " + typeColumn);
			db.execSQL("ALTER TABLE " + SportsDataRecorder.ORIGINAL_SPORTS_TB_NAME + " CHANGE " + oldColumn + " "
					+ newColumn + " " + typeColumn);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
