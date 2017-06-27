package com.yifang.ivoice.receiver;

import com.google.gson.Gson;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.willblaschko.android.alexa.AlexaManager;
import com.willblaschko.android.alexa.data.Event;
import com.willblaschko.android.alexa.utility.Util;
import com.yifang.ivoice.AlertsStartActivity;
import com.yifang.ivoice.Const;
import com.yifang.ivoice.MainActivity;
import com.yifang.ivoice.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by xblu on 2017/4/21.
 */

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Alerts","onReceive");
		String token = intent.getStringExtra(Const.INTENT_TOKEN);
		AlexaManager alexaManager = AlexaManager.getInstance(context, MainActivity.PRODUCT_ID);
		alexaManager.sendAlertStartedEvent(token);
		SharedPreferences sp = Util.getPreferences(context);
		String data = sp.getString(Event.SP_ALERTS,"");
		if(!"".equals(data)){
			Gson gson = new Gson();
			Event.ALLAlerts allAlerts = gson.fromJson(data, Event.ALLAlerts.class);
			List<Event.ALLAlerts.YFAlerts> alertsList = allAlerts.getAlerts();
			for (int i = 0;i < alertsList.size();i ++){
				if(alertsList.get(i).getToken().equals(token)){
					alertsList.remove(alertsList.get(i));
					allAlerts.setAlerts(alertsList);
					sp.edit().putString(Event.SP_ALERTS,gson.toJson(allAlerts)).apply();
				}
			}
		}
		startActivity(context);
	}

	private void startActivity(Context context){
		Intent intent = new Intent(context, AlertsStartActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
