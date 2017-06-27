package com.yifang.ivoice;

import com.google.gson.Gson;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.willblaschko.android.alexa.data.Event;
import com.willblaschko.android.alexa.utility.Util;
import com.yifang.ivoice.view.CountdownView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by xblu on 2017/4/21.
 */

public class YiFangTimerActivity extends Activity {

	private final String TAG = "YiFangTimerActivity";
	private Intent                         mIntent;
	private String                         time;
	private String                         token;
	private LinearLayout                   mLinearLayout;
	private List<Event.ALLAlerts.YFAlerts> beforeAlerts;
	private SimpleDateFormat               simpleDateFormat;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);
		mIntent = getIntent();
		init();
	}

	private void init(){
		time = mIntent.getStringExtra(Const.INTENT_TIME);
		token = mIntent.getStringExtra(Const.INTENT_TOKEN);
		time = time.replace("T"," ");
		TimeZone tz = TimeZone.getDefault();
		Log.d(TAG, ""+tz.getRawOffset());
		simpleDateFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		mLinearLayout = (LinearLayout) findViewById(R.id.ll);
		CountdownView countdownView = new CountdownView(this);
		countdownView.customTimeShow(false,true,true,true,true);
		mLinearLayout.addView(countdownView);
		countdownView.start(getLeftTime(time,tz));
		saveToSP();
		for (int i = 0;i < beforeAlerts.size();i ++){
			CountdownView countdownView1 = new CountdownView(this);
			countdownView1.customTimeShow(false,true,true,true,true);
			mLinearLayout.addView(countdownView1);
			countdownView1.start(getLeftTime(beforeAlerts.get(i).getScheduledTime().replace("T"," "),tz));
		}
	}

	private void saveToSP(){
		SharedPreferences sp = Util.getPreferences(this);
		String data = sp.getString(Event.SP_ALERTS,"");
		Event.ALLAlerts allAlerts;
		Gson gson = new Gson();
		Event.ALLAlerts.YFAlerts alerts = new Event.ALLAlerts.YFAlerts(Event.ALLAlerts.YFAlerts.TYPE_TIMER, token, time);
		List<Event.ALLAlerts.YFAlerts> alertsList = new ArrayList<>();
		beforeAlerts = new ArrayList<>();
		if(!"".equals(data)){
			allAlerts = gson.fromJson(data, Event.ALLAlerts.class);
			alertsList.addAll(allAlerts.getAlerts());
			beforeAlerts.addAll(allAlerts.getAlerts());
			alertsList.add(alerts);
			allAlerts.setAlerts(alertsList);
		} else {
			allAlerts = new Event.ALLAlerts(alertsList,new ArrayList<Event.ALLAlerts.YFAlerts>());
		}
		sp.edit().putString(Event.SP_ALERTS,gson.toJson(allAlerts)).apply();
	}

	private long getLeftTime(String time,TimeZone timeZone){
		long leftTime = 0;
		try {
			Date date = simpleDateFormat.parse(time);
			leftTime = date.getTime() + timeZone.getRawOffset() - System.currentTimeMillis();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Log.d(TAG,"left time="+leftTime);
		return leftTime;
	}
}
