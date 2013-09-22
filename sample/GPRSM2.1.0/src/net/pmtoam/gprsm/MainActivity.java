package net.pmtoam.gprsm;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.pmtoam.gprsm.model.CusTimePerEntity;
import net.pmtoam.gprsm.sqlite.DBHelper;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class MainActivity extends Activity implements OnClickListener {
	
	private int hour1;								  // 开始时间的时
	private int minute1;							  // 开始时间的分
	private int hour2;								  // 结束时间的时
	private int minute2;							  // 结束时间的分
	private List<CusTimePerEntity> cusTimePers;       // 用户列表集合
	private ListView lv;                              // 控件ListView
	private ArrayAdapter<String> adapter;             // 适配器适配数据
	private List<String> startAndEndTimeList;         // 用户定制的时间段列表集合
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		findViewById(R.id.btn_bottom1).setOnClickListener(this);
		findViewById(R.id.btn_bottom2).setOnClickListener(this);
		findViewById(R.id.btn_bottom3).setOnClickListener(this);
		findViewById(R.id.btn_bottom4).setOnClickListener(this);

		startService(new Intent("net.pmtoam.gprsm.CORE_SERVICE"));

		showUserSubscriptionList();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_bottom1:
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			// 添加时间段View
			View v_add = inflater.inflate(R.layout.set_time_layout, null);

			final TextView tvStart = (TextView) v_add.findViewById(R.id.textView1);
			final TextView tvEnd = (TextView) v_add.findViewById(R.id.textView2);

			final TimePicker timePicker1 = (TimePicker) v_add.findViewById(R.id.timePicker1);
			final TimePicker timePicker2 = (TimePicker) v_add.findViewById(R.id.timePicker2);
			timePicker1.setIs24HourView(true);
			timePicker2.setIs24HourView(true);

			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			int currentH = calendar.get(Calendar.HOUR_OF_DAY);
			int currentM = calendar.get(Calendar.MINUTE);

			hour1 = hour2 = currentH;
			minute1 = minute2 = currentM;

			StringBuilder sbH = new StringBuilder();
			if (hour1 < 10)
				sbH.append(0).append(hour1);
			else
				sbH.append(hour1);

			StringBuilder sbM = new StringBuilder();
			if (minute1 < 10)
				sbM.append(0).append(minute1);
			else
				sbM.append(minute1);

			tvStart.setText(getResources().getString(R.string.time_start) + "-" + sbH.toString() + ":" + sbM.toString());
			tvEnd.setText(getResources().getString(R.string.time_end) + "-" + sbH.toString() + ":" + sbM.toString());

			timePicker1.setOnTimeChangedListener(new OnTimeChangedListener() {

				public void onTimeChanged(TimePicker view, int hourOfDay,
						int minute) {

					hour1 = hourOfDay;
					minute1 = minute;

					StringBuilder sbH = new StringBuilder();
					if (hourOfDay < 10)
						sbH.append(0).append(hourOfDay);
					else
						sbH.append(hourOfDay);

					StringBuilder sbM = new StringBuilder();
					if (minute < 10)
						sbM.append(0).append(minute);
					else
						sbM.append(minute);

					tvStart.setText(getResources().getString(R.string.time_start) + "-" + sbH.toString() + ":" + sbM.toString());
				}
			});
			timePicker2.setOnTimeChangedListener(new OnTimeChangedListener() {

				public void onTimeChanged(TimePicker view, int hourOfDay,
						int minute) {

					hour2 = hourOfDay;
					minute2 = minute;

					StringBuilder sbH = new StringBuilder();
					if (hourOfDay < 10)
						sbH.append(0).append(hourOfDay);
					else
						sbH.append(hourOfDay);

					StringBuilder sbM = new StringBuilder();
					if (minute < 10)
						sbM.append(0).append(minute);
					else
						sbM.append(minute);

					tvEnd.setText(getResources().getString(R.string.time_end) + "-" + sbH.toString() + ":" + sbM.toString());
				}
			});

			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_input_add)
					.setTitle(R.string.add)
					.setView(v_add)
					.setPositiveButton(R.string.add,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									
									timePicker1.clearFocus();
									timePicker2.clearFocus();
									int startTime = hour1 * 60 + minute1;
									int endTime = hour2 * 60 + minute2;

									if (startTime >= endTime) {
										new AlertDialog.Builder(
												MainActivity.this)
												.setMessage(R.string.select_time_error)
												.setPositiveButton(R.string.ok, null).show();
									} else {
										addCustomTimePer(startTime, endTime);
									}
								}
							}).setNegativeButton(R.string.cancel, null).show();
			break;
		case R.id.btn_bottom2:
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.clear)
					.setMessage(R.string.clear_all_data)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									
									delAllDataInTable(DBHelper.TABLE_TBTRAFFIC);
									
									new AlertDialog.Builder(MainActivity.this)
											.setMessage(R.string.clear_success)
											.setPositiveButton(R.string.ok, null).show();
								}
							}).setNegativeButton(R.string.cancel, null).show();
			break;
		case R.id.btn_bottom3:
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setTitle(R.string.reset)
					.setMessage(R.string.del_all)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									
									delAllDataInTable(DBHelper.TABLE_TBCUSTIMEPER);
									startAndEndTimeList.clear();
									adapter.notifyDataSetChanged();
									
									new AlertDialog.Builder(MainActivity.this)
											.setMessage(R.string.clear_success)
											.setPositiveButton(R.string.ok,null).show();
								}
							}).setNegativeButton(R.string.cancel, null).show();
			break;
		case R.id.btn_bottom4:
			StringBuilder sb = new StringBuilder();
			sb.append("\n");
			sb.append(getResources().getString(R.string.about_content_1));
			sb.append("\n");
			sb.append(getResources().getString(R.string.about_content_2));
			sb.append("\n\n");
			sb.append(getResources().getString(R.string.about_content_3));
			sb.append("\n");
			sb.append(getResources().getString(R.string.about_content_4));
			sb.append("\n");

			new AlertDialog.Builder(this).setTitle(R.string.about)
					.setIcon(R.drawable.ic_launcher).setMessage(sb.toString())
					.setPositiveButton(R.string.ok, null).show();
			break;
		default:
			break;
		}
	}

	/**
	 * 删除表里面的所有数据
	 * 
	 * @param tableName 表名
	 */
	protected void delAllDataInTable(String tableName) {
		DBHelper dbHelper = new DBHelper(this);
		dbHelper.deleteAllTableData(tableName);
		dbHelper.close();
	}

	/**
	 * 用户添加要查看的时间段(用户需要了解什么时间段内使用多少GPRS流量)
	 * 
	 * @param startTime
	 * @param endTime
	 */
	protected void addCustomTimePer(int startTime, int endTime) {
		DBHelper dbHelper = new DBHelper(this);
		dbHelper.insertCustomTimePer(startTime, endTime);
		dbHelper.close();

		new AlertDialog.Builder(MainActivity.this)
				.setMessage(R.string.add_successfully)
				.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								showUserSubscriptionList();
							}
						}).show();
	}

	protected void showUserSubscriptionList() {
		
		cusTimePers = new ArrayList<CusTimePerEntity>();

		DBHelper dbHelper = new DBHelper(this);
		Cursor cursor = null;
		
		try {
			cursor = dbHelper.selectCustomTimePer();
			
			if (null != cursor && cursor.moveToFirst()) {
				do {
					int ID = cursor.getInt(cursor.getColumnIndex("ID"));
					int startDate = cursor.getInt(cursor.getColumnIndex("StartDate"));
					int endDate = cursor.getInt(cursor.getColumnIndex("EndDate"));
					cusTimePers.add(new CusTimePerEntity(ID, startDate, endDate));
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) cursor.close();
		}

		dbHelper.close();

		startAndEndTimeList = new ArrayList<String>();

		for (CusTimePerEntity c : cusTimePers) {
			int startTime = c.getStartTime();
			int endTime = c.getEndTime();

			int startH = startTime / 60;
			int startM = startTime % 60;
			int endH = endTime / 60;
			int endM = endTime % 60;

			StringBuilder sbStartH = new StringBuilder();
			if (startH < 10)
				sbStartH.append(0).append(startH);
			else
				sbStartH.append(startH);

			StringBuilder sbStartM = new StringBuilder();
			if (startM < 10)
				sbStartM.append(0).append(startM);
			else
				sbStartM.append(startM);

			StringBuilder sbEndH = new StringBuilder();
			if (endH < 10)
				sbEndH.append(0).append(endH);
			else
				sbEndH.append(endH);

			StringBuilder sbEndM = new StringBuilder();
			if (endM < 10)
				sbEndM.append(0).append(endM);
			else
				sbEndM.append(endM);

			startAndEndTimeList.add("[ " + sbStartH + ":" + sbStartM + "~" + sbEndH + ":" + sbEndM + " ]");
		}

		lv = (ListView) findViewById(R.id.list);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, startAndEndTimeList);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());

				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;

				StringBuilder sbYM = new StringBuilder();
				if (month < 10)
					sbYM.append(year).append(0).append(month);
				else
					sbYM.append(year).append(month);

				int ym = Integer.parseInt(sbYM.toString());

				long queryOneFraffic = getOneTraffic(ym, cusTimePers.get(arg2).getStartTime(), cusTimePers.get(arg2).getEndTime());

				Double Gtotal_dob = Double.valueOf(queryOneFraffic);// byte
				Double Gtotal_dob_kb = Gtotal_dob / 1024;// KB
				Double Gtotal_dob_m = Gtotal_dob_kb / 1024;// M

				DecimalFormat df_kb = new DecimalFormat("0.00");
				String result_kb = df_kb.format(Gtotal_dob_kb);
				DecimalFormat df_m = new DecimalFormat("0.00");
				String result_m = df_m.format(Gtotal_dob_m);

				StringBuilder sb = new StringBuilder();
				sb.append("\n");
				sb.append(getResources().getString(R.string.check_out));
				sb.append("\n\n");
				sb.append("[ " + Gtotal_dob + " B ]\n");
				sb.append("= " + result_kb + " KB\n");
				sb.append("= " + result_m + " M\n");

				new AlertDialog.Builder(MainActivity.this)
						.setMessage(sb.toString())
						.setPositiveButton(R.string.ok, null).show();
			}
		});

		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				new AlertDialog.Builder(MainActivity.this)
						.setMessage(R.string.del_comfirm)
						.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										
										DBHelper dbHelper = new DBHelper(MainActivity.this);
										dbHelper.deleteItemCustomTime(cusTimePers.get(arg2).getUserId());
										dbHelper.close();

										cusTimePers.remove(arg2);
										startAndEndTimeList.remove(arg2);
										adapter.notifyDataSetChanged();

										userSubscriptionIsNull();
									}

								}).setNegativeButton(R.string.cancel, null).show();

				return false;
			}
		});

		userSubscriptionIsNull();
	}

	protected long getOneTraffic(int ym, int startTime, int endTime) {

		DBHelper dbHelper = new DBHelper(this);
		long oneTraffic = 0;
		
		try {
			oneTraffic = dbHelper.queryCustomTimePerTraffic(ym, startTime, endTime);
			if (oneTraffic <= 0) return 0;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != dbHelper) dbHelper.close();
		}
		return oneTraffic;
	}

	/**
	 * 如果用户没有定制时间段，给予提示
	 */
	private void userSubscriptionIsNull() {
		if (startAndEndTimeList.size() == 0)
			new AlertDialog.Builder(this).setMessage(R.string.no_info)
					.setPositiveButton(R.string.ok, null).show();
	}
}
