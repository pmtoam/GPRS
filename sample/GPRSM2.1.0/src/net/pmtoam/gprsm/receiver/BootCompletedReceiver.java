package net.pmtoam.gprsm.receiver;

import net.pmtoam.gprsm.util.Constants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;

/**
 * 重启启动服务类
 * 
 * @author 王月星
 *
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// 手机启动完成后自动开启服务
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			
			// 将服务销毁的状态值标志为重新启动
			Editor editor = context.getSharedPreferences(Constants.SP_FILENAME, Context.MODE_PRIVATE).edit();
			editor.putInt(Constants.DESTROY_FLAG, 1);
			editor.commit();
			
			// 开启服务(检测GPRS流量)
			context.startService(new Intent("net.pmtoam.gprsm.CORE_SERVICE"));
		}
	}
}
