package com.yifang.ivoice;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Intent;
import android.os.Bundle;

import com.willblaschko.android.alexa.AlexaManager;
import com.yifang.ivoice.view.CountdownView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by xblu on 2017/4/21.
 */

public class YiFangTimerActivity extends Activity {

	private CountdownView tv_timer;
	private Intent        mIntent;
	private String        time;
	private long          leftTime;
	private String token;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);

		mIntent = getIntent();
		init();
	}

	private void init(){
		time = mIntent.getStringExtra("time");
		token = mIntent.getStringExtra("token");
		time = time.replace("T"," ");
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
		try {
			Date date = simpleDateFormat.parse(time);
			leftTime = System.currentTimeMillis() - date.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		tv_timer = (CountdownView) findViewById(R.id.cv_countdownViewTest);
		tv_timer.start(leftTime);
		AlexaManager.getInstance(this,MainActivity.PRODUCT_ID).sendSetAleterSuccess(token);
	}
}
