package com.yifang.ivoice;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

/**
 * Created by xblu on 2017/4/25.
 */

public class AlertsStartActivity extends Activity {

	private MediaPlayer mMediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alerts);
		startAlarm();
		Button button = (Button) findViewById(R.id.stop_alerts);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mMediaPlayer.stop();
			}
		});
	}

	private Uri getSystemDefaultRingtoneUri() {
		return RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE);
	}

	private void startAlarm() {
		mMediaPlayer = MediaPlayer.create(this, getSystemDefaultRingtoneUri());
		mMediaPlayer.setLooping(true);
//		try {
//			mMediaPlayer.prepare();
//		} catch (IllegalStateException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		mMediaPlayer.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mMediaPlayer.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMediaPlayer.release();
		mMediaPlayer = null;
	}
}

