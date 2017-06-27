package com.yifang.ivoice;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.willblaschko.android.alexa.AlexaManager;
import com.willblaschko.android.alexa.audioplayer.AlexaAudioPlayer;
import com.willblaschko.android.alexa.callbacks.AsyncCallback;
import com.willblaschko.android.alexa.callbacks.AuthorizationCallback;
import com.willblaschko.android.alexa.connection.ClientUtil;
import com.willblaschko.android.alexa.data.Event;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.OpenApp;
import com.willblaschko.android.alexa.interfaces.alerts.AvsDeleteAlertItem;
import com.willblaschko.android.alexa.interfaces.alerts.AvsSetAlertItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayAudioItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayContentItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayRemoteItem;
import com.willblaschko.android.alexa.interfaces.card.BodyTemplate1;
import com.willblaschko.android.alexa.interfaces.card.BodyTemplate2;
import com.willblaschko.android.alexa.interfaces.card.ListTemplate1;
import com.willblaschko.android.alexa.interfaces.card.WeatherTemplate;
import com.willblaschko.android.alexa.interfaces.errors.AvsResponseException;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsMediaNextCommandItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsMediaPauseCommandItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsMediaPlayCommandItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsMediaPreviousCommandItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceAllItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsReplaceEnqueuedItem;
import com.willblaschko.android.alexa.interfaces.playbackcontrol.AvsStopItem;
import com.willblaschko.android.alexa.interfaces.speaker.AvsAdjustVolumeItem;
import com.willblaschko.android.alexa.interfaces.speaker.AvsSetMuteItem;
import com.willblaschko.android.alexa.interfaces.speaker.AvsSetVolumeItem;
import com.willblaschko.android.alexa.interfaces.speechrecognizer.AvsExpectSpeechItem;
import com.willblaschko.android.alexa.interfaces.speechsynthesizer.AvsSpeakItem;
import com.willblaschko.android.alexa.requestbody.DataRequestBody;
import com.willblaschko.android.recorderview.RecorderView;
import com.yifang.ivoice.receiver.AlarmReceiver;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import ee.ioc.phon.android.speechutils.RawAudioRecorder;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = MainActivity.class.getName();

	public static final  String PRODUCT_ID                           = "avstablet";
	private static final String PRODUCT_DSN                          = "12349587";
	private static final int    MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
	public static final String ITEM = "item";

	private RecorderView recorderView;
	private TextView     mInfo;
	private ProgressBar  mLogInProgress;
	private AlexaManager alexaManager;
	private static final int AUDIO_RATE = 16000;
	private RawAudioRecorder recorder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "======onCreate=====");
		audioPlayer = AlexaAudioPlayer.getInstance(this);
		audioPlayer.addCallback(alexaAudioPlayerCallback);
		alexaManager = AlexaManager.getInstance(MainActivity.this, PRODUCT_ID);
		//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_main);
		initializeUI();
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
		} else {
			loin();
		}

	}

	private AlexaAudioPlayer.Callback alexaAudioPlayerCallback = new AlexaAudioPlayer.Callback() {

		private boolean almostDoneFired = false;
		private boolean playbackStartedFired = false;

		@Override
		public void playerPrepared(AvsItem pendingItem) {

		}

		@Override
		public void playerProgress(AvsItem item, long offsetInMilliseconds, float percent) {
			if (BuildConfig.DEBUG) {
				Log.i(TAG, "Player percent: " + percent);
			}
			if (item instanceof AvsPlayContentItem || item == null) {
				return;
			}
			if (!playbackStartedFired) {
				if (BuildConfig.DEBUG) {
					Log.i(TAG, "PlaybackStarted " + item.getToken() + " fired: " + percent);
				}
				playbackStartedFired = true;

			}
			if (!almostDoneFired && percent > .8f) {
				if (BuildConfig.DEBUG) {
					Log.i(TAG, "AlmostDone " + item.getToken() + " fired: " + percent);
				}
				almostDoneFired = true;
				if (item instanceof AvsPlayAudioItem) {
					sendPlaybackNearlyFinishedEvent((AvsPlayAudioItem) item, offsetInMilliseconds);

				}
			}
		}

		@Override
		public void itemComplete(AvsItem completedItem) {
			almostDoneFired = false;
			playbackStartedFired = false;
			avsQueue.remove(completedItem);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mInfo.setText("press button and speak");
				}
			});
			Log.d(TAG, "Audio itemComplete");

			if (completedItem instanceof AvsPlayContentItem || completedItem == null) {
				checkQueue();
				return;
			}
			if (BuildConfig.DEBUG) {
				Log.i(TAG, "Complete " + completedItem.getToken() + " fired");
			}

			sendPlaybackFinishedEvent(completedItem);
			checkQueue();
		}

		@Override
		public boolean playerError(AvsItem item, int what, int extra) {
			return false;
		}

		@Override
		public void dataError(AvsItem item, Exception e) {
			Log.d(TAG, "Audio err:" + e.toString());
			e.printStackTrace();
		}


	};

	/**
	 * Send an event back to Alexa that we're starting a speech event https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/audioplayer#PlaybackNearlyFinished
	 * Event
	 */
	private void sendPlaybackStartedEvent(AvsItem item) {
		alexaManager.sendPlaybackStartedEvent(item, null);
		Log.i(TAG, "Sending SpeechStartedEvent");
	}

	private void sendPlaybackFinishedEvent(AvsItem item) {
		if (item != null) {
			alexaManager.sendPlaybackFinishedEvent(item, null);
			Log.i(TAG, "Sending PlaybackFinishedEvent");
		}
	}

	private void sendPlaybackNearlyFinishedEvent(AvsPlayAudioItem item, long offsetInMilliseconds) {
		if (item != null) {
			alexaManager.sendPlaybackNearlyFinishedEvent(item, offsetInMilliseconds, null);
			Log.i(TAG, "Sending PlaybackNearlyFinishedEvent");
		}
	}

	private void loin() {
		alexaManager.logIn(new AuthorizationCallback() {
			@Override
			public void onCancel() {

			}

			@Override
			public void onSuccess() {
				alexaManager.sendOpenDownchannelDirective(downchannelRequestCallback);

			}

			@Override
			public void onError(Exception error) {

			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					loin();
				} else {
					finish();
				}

			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "======onResume========");
	}

	private AsyncCallback<AvsResponse, Exception> synchronizeStateRequestCallback = new AsyncCallback<AvsResponse, Exception>() {
		@Override
		public void start() {

		}

		@Override
		public void success(AvsResponse result) {

		}

		@Override
		public void failure(Exception error) {

		}

		private boolean isSend = true;

		@Override
		public void complete() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					recorderView.setVisibility(View.VISIBLE);
					if (isSend) {
						isSend = false;
						alexaManager.sendTextRequest("Who is XiJinPin", requestCallback);
					}

				}
			});

		}
	};

	private AsyncCallback<AvsResponse, Exception> downchannelRequestCallback = new AsyncCallback<AvsResponse, Exception>() {
		@Override
		public void start() {

		}

		@Override
		public void success(AvsResponse result) {

			Log.d(TAG, "downchannelRequestCallback success===");
			channelSuccess(result);
		}

		@Override
		public void failure(Exception error) {

		}

		@Override
		public void complete() {
			Log.d(TAG, "openDownchannel complete");
			//		synchronize our device
			if (recorderView.getVisibility() != View.VISIBLE) {
				alexaManager.sendSynchronizeStateEvent(synchronizeStateRequestCallback, getApplicationContext());
			}

		}
	};

	private void channelSuccess(AvsResponse result) {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		for (AvsItem avsItem : result) {
			if (avsItem instanceof AvsSetAlertItem) {
				String type = ((AvsSetAlertItem) avsItem).getType();
				Date date = null;
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss");
				try {
					date = simpleDateFormat.parse(((AvsSetAlertItem) avsItem).getScheduledTime().replace("T", " "));
				} catch (ParseException e) {
					Log.d(TAG, "simpleDateFormat err" + e.toString());
					e.printStackTrace();
				}
				Intent receiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
				receiverIntent.putExtra(Const.INTENT_TOKEN, avsItem.getToken());
				receiverIntent.setType(avsItem.getToken());
				PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, receiverIntent, 0);
				// Schedule the alarm!
				if (date != null) {

					long time = date.getTime() + TimeZone.getDefault().getRawOffset();
					Log.d(TAG, "alarm time =" + time);
					am.set(AlarmManager.RTC_WAKEUP, time, sender);
					AlexaManager.getInstance(this, MainActivity.PRODUCT_ID).sendSetAleterSuccess(avsItem.getToken());
					if (Event.ALLAlerts.YFAlerts.TYPE_TIMER.equals(type)) {
						Intent intent = new Intent(MainActivity.this, YiFangTimerActivity.class);
						intent.putExtra(Const.INTENT_TIME, ((AvsSetAlertItem) avsItem).getScheduledTime());
						intent.putExtra(Const.INTENT_TOKEN, avsItem.getToken());
						startActivity(intent);
					}
				} else {
					Log.d(TAG, "set alerts fail");
					AlexaManager.getInstance(this, MainActivity.PRODUCT_ID).sendSetAlertFailedEvent(avsItem.getToken());
				}
			} else if (avsItem instanceof AvsDeleteAlertItem) {
				Intent receiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
				receiverIntent.putExtra(Const.INTENT_TOKEN, avsItem.getToken());
				PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, receiverIntent, 0);
				receiverIntent.setType(avsItem.getToken());
				am.cancel(sender);
			}
		}
	}

	private AsyncCallback<AvsResponse, Exception> requestCallback = new AsyncCallback<AvsResponse, Exception>() {
		@Override
		public void start() {
			Log.i(TAG, "Event Start");
		}

		@Override
		public void success(AvsResponse result) {
			if (result != null && result.size() == 0) {
				Log.d(TAG, "Nothing come back ");
			}
			istalk = false;
			handleResponse(result);
			recorderView.setRmsdbLevel(1);
			Log.i(TAG, "Event Success");
			releaseRecorder();
		}

		@Override
		public void failure(Exception error) {
			error.printStackTrace();
			recorderView.setRmsdbLevel(1);
			Log.i(TAG, "Event Error" + error.toString());
			istalk = false;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mInfo.setText("press button and speak");
				}
			});
			releaseRecorder();
		}

		@Override
		public void complete() {
//			AssetFileDescriptor afd;
//			try {
//				audioPlayer.release();
//				afd = getAssets().openFd("1.mp3");
//				audioPlayer.getMediaPlayer().setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//				audioPlayer.getMediaPlayer().prepareAsync();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					Toast.makeText(MainActivity.this, "network anomaly", Toast.LENGTH_SHORT).show();
//				}
//			});
			releaseRecorder();
			recorderView.setRmsdbLevel(1);
			Log.i(TAG, "Event Complete");
			istalk = false;
		}
	};

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "======onStart========");
	}

	private DataRequestBody requestBody = new DataRequestBody() {
		@Override
		public void writeTo(BufferedSink sink) throws IOException {
			while (recorder != null && !recorder.isPausing()) {

				if (isExpectSpeech && System.currentTimeMillis() - startTime > timeOut) {
					Log.d(TAG, "sendExpectSpeechTimeoutEvent");
					isExpectSpeech = false;
					alexaManager.getSpeechSendAudio().cancelRequest();
					alexaManager.sendExpectSpeechTimeoutEvent(null);
					break;
				}
				if (recorder != null) {
					final float rmsdb = recorder.getRmsdb();
					if (recorderView != null) {
						recorderView.post(new Runnable() {
							@Override
							public void run() {
								recorderView.setRmsdbLevel(rmsdb);
							}
						});
					}
					if (sink != null && recorder != null) {
						sink.write(recorder.consumeRecording());
					}
					//					Log.i(TAG, "Received audio");
					//					Log.i(TAG, "RMSDB: " + rmsdb);
				}

				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG, "DataRequestBody---stopListening");
			stopListening();
		}

	};

	public void startListening() {
		mInfo.setText("listening");
		istalk = true;
		if (recorder == null) {
			recorder = new RawAudioRecorder(AUDIO_RATE);
		}
		recorder.start();
		alexaManager.sendAudioRequest(requestBody, requestCallback);
	}

	private void stopListening() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mInfo.setText("wait");
			}
		});

		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
		if (recorderView != null) {
			recorderView.setRmsdbLevel(1);
		}
	}

	private boolean istalk;

	/**
	 * Initializes all of the UI elements in the activity
	 */
	private void initializeUI() {
		recorderView = (RecorderView) findViewById(R.id.recorder);
		mInfo = (TextView) findViewById(R.id.profile_info);
		mLogInProgress = (ProgressBar) findViewById(R.id.log_in_progress);
		recorderView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (recorder == null && !istalk) {
					Log.d(TAG, "startListening");
					startListening();
				} else {
					stopListening();
				}


			}
		});
	}

	private List<AvsItem> avsQueue = new ArrayList<>();

	/**
	 * Handle the response sent back from Alexa's parsing of the Intent, these can be any of the AvsItem types (play, speak, stop, clear,
	 * listen)
	 *
	 * @param response a List<AvsItem> returned from the mAlexaManager.sendTextRequest() call in sendVoiceToAlexa()
	 */
	private void handleResponse(AvsResponse response) {
		boolean checkAfter = (avsQueue.size() == 0);
		if (response != null) {
			//if we have a clear queue item in the list, we need to clear the current queue before proceeding
			//iterate backwards to avoid changing our array positions and getting all the nasty errors that come
			//from doing that
			for (int i = response.size() - 1; i >= 0; i--) {
				if (response.get(i) instanceof AvsReplaceAllItem || response.get(i) instanceof AvsReplaceEnqueuedItem) {
					//clear our queue
					avsQueue.clear();
					//remove item
					response.remove(i);
				}
			}
			Log.i(TAG, "Adding " + response.size() + " items to our queue");
			if (BuildConfig.DEBUG) {
				for (int i = 0; i < response.size(); i++) {
					Log.i(TAG, "\tAdding: " + response.get(i).getToken());
				}
			}
			avsQueue.addAll(response);
		}
		if (checkAfter) {
			checkQueue();
		}
	}

	private AlexaAudioPlayer audioPlayer;

	private long    startTime;
	private long    timeOut;
	private boolean isExpectSpeech;

	/**
	 * Check our current queue of items, and if we have more to parse (once we've reached a play or listen callback) then proceed to the
	 * next item in our list.
	 *
	 * We're handling the AvsReplaceAllItem in handleResponse() because it needs to clear everything currently in the queue, before the new
	 * items are added to the list, it should have no function here.
	 */
	private void checkQueue() {
		//if we're out of things, hang up the phone and move on
		if (avsQueue.size() == 0) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {

				}
			});
			return;
		}

		final AvsItem current = avsQueue.get(0);

		Log.i(TAG, "checkQueue Item type " + current.getClass().getName());
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mInfo.setText("speaking");
			}
		});

		if (current instanceof AvsPlayRemoteItem) {
			//play a URL
			//            if (!audioPlayer.isPlaying()) {
			sendPlaybackStartedEvent(current);
			audioPlayer.playItem((AvsPlayRemoteItem) current);

			//            }
		} else if (current instanceof AvsPlayContentItem) {
			//play a URL
			//            if (!audioPlayer.isPlaying()) {
			sendPlaybackStartedEvent(current);
			audioPlayer.playItem((AvsPlayContentItem) current);
			//            }
		} else if (current instanceof AvsSpeakItem) {
			//play a sound file
			//            if (!audioPlayer.isPlaying()) {
			sendPlaybackStartedEvent(current);
			audioPlayer.playItem((AvsSpeakItem) current);
			//            }

		} else if (current instanceof AvsStopItem) {
			//stop our play
			audioPlayer.stop();
			avsQueue.remove(current);
		} else if (current instanceof AvsReplaceAllItem) {
			//clear all items
			//            mAvsItemQueue.clear();
			audioPlayer.stop();
			avsQueue.remove(current);
		} else if (current instanceof AvsReplaceEnqueuedItem) {
			//clear all items
			//mAvsItemQueue.clear();
			avsQueue.remove(current);
		} else if (current instanceof AvsExpectSpeechItem) {

			//listen for user input
			startListening();
			startTime = System.currentTimeMillis();
			timeOut = ((AvsExpectSpeechItem) current).getTimeoutInMiliseconds();
			isExpectSpeech = true;
			audioPlayer.stop();
			avsQueue.clear();
		} else if (current instanceof AvsSetVolumeItem) {
			//set our volume
			setVolume(((AvsSetVolumeItem) current).getVolume());
			avsQueue.remove(current);
		} else if (current instanceof AvsAdjustVolumeItem) {
			//adjust the volume
			adjustVolume(((AvsAdjustVolumeItem) current).getAdjustment());
			avsQueue.remove(current);
		} else if (current instanceof AvsSetMuteItem) {
			//mute/unmute the device
			setMute(((AvsSetMuteItem) current).isMute());
			avsQueue.remove(current);
		} else if (current instanceof AvsMediaPlayCommandItem) {
			//fake a hardware "play" press
			// sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PLAY);
			Log.i(TAG, "Media play command issued");
			avsQueue.remove(current);
		} else if (current instanceof AvsMediaPauseCommandItem) {
			//fake a hardware "pause" press
			// sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PAUSE);
			Log.i(TAG, "Media pause command issued");
			avsQueue.remove(current);
		} else if (current instanceof AvsMediaNextCommandItem) {
			//fake a hardware "next" press
			// sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_NEXT);
			Log.i(TAG, "Media next command issued");
			avsQueue.remove(current);
		} else if (current instanceof AvsMediaPreviousCommandItem) {
			//fake a hardware "previous" press
			// sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			Log.i(TAG, "Media previous command issued");
			avsQueue.remove(current);
		} else if (current instanceof OpenApp) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ComponentName cn = new ComponentName(((OpenApp) current).getPackageName(), ((OpenApp) current).getActivityName());
			intent.setComponent(cn);
			startActivity(intent);
		} else if (current instanceof AvsResponseException) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(MainActivity.this).setTitle("Error")
															  .setMessage(
																  ((AvsResponseException) current).getDirective().getPayload().getCode() +
																  ": " + ((AvsResponseException) current).getDirective()
																										 .getPayload()
																										 .getDescription())
															  .setPositiveButton(android.R.string.ok, null)
															  .show();
				}
			});

			//            avsQueue.remove(current);
			//            checkQueue();
		} else if(current instanceof BodyTemplate1){
			Intent intent = new Intent(this,BodyTemplateActivity.class);
			intent.putExtra(ITEM, current);
			startActivity(intent);
		} else if(current instanceof BodyTemplate2){
			Intent intent = new Intent(this,BodyTemplateActivity.class);
			intent.putExtra(ITEM, current);
			startActivity(intent);
		}else if(current instanceof ListTemplate1){

		}else if(current instanceof WeatherTemplate){
			Intent intent = new Intent(this,WeatherActivity.class);
			intent.putExtra(ITEM, current);
			startActivity(intent);
		}
	}

	private void adjustVolume(long adjust) {
		setVolume(adjust, true);
	}

	private void setVolume(long volume) {
		setVolume(volume, false);
	}

	private void setVolume(final long volume, final boolean adjust) {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		final int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		long vol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (adjust) {
			vol += volume * max / 100;
		} else {
			vol = volume * max / 100;
		}
		am.setStreamVolume(AudioManager.STREAM_MUSIC, (int) vol, AudioManager.FLAG_VIBRATE);

		alexaManager.sendVolumeChangedEvent(volume, vol == 0, null);

		Log.i(TAG, "Volume set to : " + vol + "/" + max + " (" + volume + ")");

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				if (adjust) {
					Toast.makeText(MainActivity.this, "Volume adjusted.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(MainActivity.this, "Volume set to: " + (volume / 10), Toast.LENGTH_SHORT).show();
				}
			}
		});

	}

	private void setMute(final boolean isMute) {
		AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
		am.setStreamMute(AudioManager.STREAM_MUSIC, isMute);

		alexaManager.sendMutedEvent(isMute, null);

		Log.i(TAG, "Mute set to : " + isMute);

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this, "Volume " + (isMute ? "muted" : "unmuted"), Toast.LENGTH_SHORT).show();
			}
		});
	}


	@Override
	protected void onStop() {
		super.onStop();
		releaseRecorder();
		if (alexaManager != null) {
			alexaManager.cancelAudioRequest();

		}
		ClientUtil.getTLS12OkHttpClient().dispatcher().cancelAll();
		ClientUtil.getDownChannelClient().dispatcher().cancelAll();
		if (audioPlayer != null) {
			audioPlayer.stop();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (audioPlayer != null) {
			//remove callback to avoid memory leaks
			audioPlayer.removeCallback(alexaAudioPlayerCallback);
			audioPlayer.release();
		}

	}

	private void releaseRecorder() {
		if (recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
			recorder = new RawAudioRecorder(AUDIO_RATE);
		}
	}
}
