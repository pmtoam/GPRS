package net.pmtoam.gprsm.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private final static int DATABASE_VERSION = 1;
	private final static String DATABASE_NAME = "gprsm.db";
	public final static String TABLE_TBTRAFFIC = "tbTraffic";
	public final static String TABLE_TBCUSTIMEPER = "tbCusTimePer";
	public final static String TABLE_TBUSER = "tbUser";
	public final static String TABLE_TBUSERCONFIG = "tbUserConfig";
	private final static String GUID = "04623F6C-493A-46B2-8FE4-F186DF13D706";
	public SQLiteDatabase sdb;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		sdb = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//
		String sql1 = "CREATE TABLE IF NOT EXISTS " + TABLE_TBTRAFFIC //
				+ "(" + "ID INTEGER PRIMARY KEY, " //
				+ "traffic INTEGER, "//
				+ "ym INTEGER, "//
				+ "day INTEGER, "//
				+ "hms INTEGER)";//
		db.execSQL(sql1);
		//
		String sql2 = "CREATE INDEX yearmonth ON " + TABLE_TBTRAFFIC + " (ym)";
		db.execSQL(sql2);
		String sql3 = "CREATE INDEX dayIndex ON " + TABLE_TBTRAFFIC + " (day)";
		db.execSQL(sql3);
		String sql4 = "CREATE INDEX hhmmss ON " + TABLE_TBTRAFFIC + " (hms)";
		db.execSQL(sql4);
		//
		String sql5 = "CREATE TABLE IF NOT EXISTS " + TABLE_TBUSER //
				+ "(IdentifyId TEXT)";//
		db.execSQL(sql5);
		//
		String sql6 = "CREATE TABLE IF NOT EXISTS " + TABLE_TBCUSTIMEPER //
				+ "(" + "ID INTEGER PRIMARY KEY, " //
				+ "UserId TEXT, "//
				+ "StartDate INTEGER, "//
				+ "EndDate INTEGER)";//
		db.execSQL(sql6);
		//
		String sql7 = "CREATE TABLE IF NOT EXISTS " + TABLE_TBUSERCONFIG //
				+ "(" + "UserId TEXT, " //
				+ "TotalTraffic INTEGER, "//
				+ "WarnVal INTEGER)";//
		db.execSQL(sql7);
		//
		String sql8 = "INSERT INTO " + TABLE_TBUSER + "(IdentifyId) VALUES('" + GUID + "')";
		db.execSQL(sql8);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public void insertDataToTable(long temp, int year, int month, int day,
			int hour, int minute) {

		StringBuilder sb = new StringBuilder();
		sb.append(year);

		if (month < 10)
			sb.append(0).append(month);
		else
			sb.append(month);

		int ym = Integer.parseInt(sb.toString());
		int hms = hour * 60 + minute;

		String sql = "INSERT INTO " + TABLE_TBTRAFFIC + "(traffic,ym,day,hms) VALUES(" + temp + "," + ym + "," + day + "," + hms + ")";
		sdb.execSQL(sql);
	}

	public void insertCustomTimePer(int startTime, int endTime) {
		String sql = "INSERT INTO " + TABLE_TBCUSTIMEPER + "(UserId,StartDate,EndDate) VALUES('" + GUID + "'," + startTime + "," + endTime + ");";
		sdb.execSQL(sql);
	}

	public Cursor selectCustomTimePer() {
		String sql = "SELECT ID,StartDate,EndDate FROM " + TABLE_TBCUSTIMEPER + " WHERE UserId='" + GUID + "'";
		return sdb.rawQuery(sql, null);
	}

	public void deleteItemCustomTime(int userId) {
		String sql = "DELETE FROM " + TABLE_TBCUSTIMEPER + " WHERE ID = " + userId;
		sdb.execSQL(sql);
	}

	public long queryCustomTimePerTraffic(int ym, int startTime, int endTime)
			throws Exception {
		String sql = "SELECT traffic FROM " + TABLE_TBTRAFFIC + " WHERE ym=" + ym + " AND hms between " + startTime + " and " + endTime;

		long traffic = 0;
		Cursor cursor = null;
		try {
			cursor = sdb.rawQuery(sql, null);
			if (null != cursor) {
				if (cursor.moveToFirst()) {
					do {
						traffic += cursor.getLong(cursor.getColumnIndex("traffic"));
					} while (cursor.moveToNext());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor)
				cursor.close();
		}

		return traffic;
	}

	public void deleteAllTableData(String tableName) {
		String sql = "DELETE FROM " + tableName;
		sdb.execSQL(sql);
	}
}
