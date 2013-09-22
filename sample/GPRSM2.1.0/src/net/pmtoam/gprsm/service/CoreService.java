package net.pmtoam.gprsm.service;

import java.util.Calendar;

import net.pmtoam.gprsm.sqlite.DBHelper;
import net.pmtoam.gprsm.util.Constants;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.IBinder;

public class CoreService extends Service {

	private static final long DELAY_TIME = 6000;      // 检测GPRS使用情况时间间隔

	private Handler handler = new Handler();
	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			
			long gTotal = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes();
			long temp_traffic = getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE).getLong(Constants.TEMP_TRAFFIC, 0);
			long gUsed = gTotal - temp_traffic;

			// 如果这一时间段里消耗的GPRS流量大于零
			if (gUsed > 0) {
				
				// 将相关数据保存到数据库
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis());
				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;
				int day = calendar.get(Calendar.DATE);
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);

				DBHelper dbHelper = new DBHelper(CoreService.this);
				dbHelper.insertDataToTable(gUsed, year, month, day, hour, minute);
				dbHelper.close();

				// 并更新临时流量变量
				Editor editor = getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE).edit();
				editor.putLong(Constants.TEMP_TRAFFIC, gTotal);
				editor.commit();
			}
			
			// 循环定时检测GPRS流量
			handler.postDelayed(runnable, DELAY_TIME);
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		int destroy_flag = getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE).getInt(Constants.DESTROY_FLAG, 1);
		
		// 如果服务创建基于重启
		if (destroy_flag == 1) {
			long gr = TrafficStats.getMobileRxBytes();
			long gt = TrafficStats.getMobileTxBytes();
			long gTotal = gr + gt;
			
			// 获取总消耗GPRS流量并保存到临时共享参数中
			Editor editor = getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE).edit();
			editor.putLong(Constants.TEMP_TRAFFIC, gTotal);
			editor.commit();
		}

		// 定时检测GPRS流量
		handler.postDelayed(runnable, DELAY_TIME);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// 将服务销毁的状态值标志为不是重新启动(未知)
		Editor editor = getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE).edit();
		editor.putInt(Constants.DESTROY_FLAG, 2);
		editor.commit();
		
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		
		// 重启服务
		handler.removeCallbacks(runnable);
		startService(new Intent("net.pmtoam.gprsm.CORE_SERVICE"));
	}

}