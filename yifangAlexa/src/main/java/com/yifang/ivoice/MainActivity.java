package com.yifang.ivoice;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.ScopeFactory;
import com.amazon.identity.auth.device.api.workflow.RequestContext;
import com.willblaschko.android.alexa.AlexaManager;
import com.willblaschko.android.alexa.audioplayer.AlexaAudioPlayer;
import com.willblaschko.android.alexa.callbacks.AsyncCallback;
import com.willblaschko.android.alexa.callbacks.AuthorizationCallback;
import com.willblaschko.android.alexa.interfaces.AvsItem;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayAudioItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayContentItem;
import com.willblaschko.android.alexa.interfaces.audioplayer.AvsPlayRemoteItem;
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
import com.willblaschko.android.alexa.utility.Util;
import com.willblaschko.android.recorderview.RecorderView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ee.ioc.phon.android.speechutils.RawAudioRecorder;
import okio.BufferedSink;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private static final String PRODUCT_ID = "avstablet";
    private static final String PRODUCT_DSN = "12349587";
	private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

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
		alexaManager = AlexaManager.getInstance(MainActivity.this,PRODUCT_ID);
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
			if(BuildConfig.DEBUG) {
				//Log.i(TAG, "Player percent: " + percent);
			}
			if(item instanceof AvsPlayContentItem || item == null){
				return;
			}
			if(!playbackStartedFired){
				if(BuildConfig.DEBUG) {
					Log.i(TAG, "PlaybackStarted " + item.getToken() + " fired: " + percent);
				}
				playbackStartedFired = true;
				sendPlaybackStartedEvent(item);
			}
			if(!almostDoneFired && percent > .8f){
				if(BuildConfig.DEBUG) {
					Log.i(TAG, "AlmostDone " + item.getToken() + " fired: " + percent);
				}
				almostDoneFired = true;
				if(item instanceof AvsPlayAudioItem) {
					sendPlaybackNearlyFinishedEvent((AvsPlayAudioItem) item, offsetInMilliseconds);
				}
			}
		}

		@Override
		public void itemComplete(AvsItem completedItem) {
			almostDoneFired = false;
			playbackStartedFired = false;
			avsQueue.remove(completedItem);
			checkQueue();
			if(completedItem instanceof AvsPlayContentItem || completedItem == null){
				return;
			}
			if(BuildConfig.DEBUG) {
				Log.i(TAG, "Complete " + completedItem.getToken() + " fired");
			}
			sendPlaybackFinishedEvent(completedItem);
		}

		@Override
		public boolean playerError(AvsItem item, int what, int extra) {
			return false;
		}

		@Override
		public void dataError(AvsItem item, Exception e) {
			e.printStackTrace();
		}


	};

	/**
	 * Send an event back to Alexa that we're starting a speech event
	 * https://developer.amazon.com/public/solutions/alexa/alexa-voice-service/reference/audioplayer#PlaybackNearlyFinished Event
	 */
	private void sendPlaybackStartedEvent(AvsItem item){
		alexaManager.sendPlaybackStartedEvent(item, null);
		Log.i(TAG, "Sending SpeechStartedEvent");
	}

	private void sendPlaybackFinishedEvent(AvsItem item){
		if (item != null) {
			alexaManager.sendPlaybackFinishedEvent(item, null);
			Log.i(TAG, "Sending PlaybackFinishedEvent");
		}
	}

	private void sendPlaybackNearlyFinishedEvent(AvsPlayAudioItem item, long offsetInMilliseconds){
		if (item != null) {
			alexaManager.sendPlaybackNearlyFinishedEvent(item, offsetInMilliseconds, requestCallback);
			Log.i(TAG, "Sending PlaybackNearlyFinishedEvent");
		}
	}

	private void loin(){
		alexaManager.logIn(new AuthorizationCallback() {
			@Override
			public void onCancel() {

			}

			@Override
			public void onSuccess() {
//				alexaManager.sendTextRequest("How is the weather in London?",requestCallback);
				recorderView.setVisibility(View.VISIBLE);
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
//		startListening();
    }

    private AsyncCallback<AvsResponse, Exception> requestCallback = new AsyncCallback<AvsResponse, Exception>() {
        @Override
        public void start() {
            Log.i(TAG, "Event Start");
        }

        @Override
        public void success(AvsResponse result) {
            if (result != null && result.size() == 0){
				Log.d(TAG,"Nothing come back ");
			}
			istalk = false;
            handleResponse(result);
			recorderView.setRmsdbLevel(1);
            Log.i(TAG, "Event Success");
        }

        @Override
        public void failure(Exception error) {
            error.printStackTrace();
			recorderView.setRmsdbLevel(1);
            Log.i(TAG, "Event Error"+error.toString());
			istalk = false;
        }

        @Override
        public void complete() {
			recorderView.setRmsdbLevel(1);
            Log.i(TAG, "Event Complete");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //Log.i(TAG, "Total request time: "+totalTime+" miliseconds");
                }
            });
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
				if(recorder != null) {
					final float rmsdb = recorder.getRmsdb();
					if(recorderView != null) {
						recorderView.post(new Runnable() {
							@Override
							public void run() {
								recorderView.setRmsdbLevel(rmsdb);
							}
						});
					}
					if(sink != null && recorder != null) {
						sink.write(recorder.consumeRecording());
					}

					Log.i(TAG, "Received audio");
					Log.i(TAG, "RMSDB: " + rmsdb);

				}

				try {
					Thread.sleep(25);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Log.d(TAG,"DataRequestBody---stopListening");
			stopListening();
		}

	};

	public void startListening() {
		istalk = true;
		if(recorder == null){
			recorder = new RawAudioRecorder(AUDIO_RATE);
		}
		recorder.start();
		alexaManager.sendAudioRequest(requestBody, requestCallback);
	}

	private void stopListening(){

		if(recorder != null) {
			recorder.stop();
			recorder.release();
			recorder = null;
		}
		if (recorderView != null){
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

				if(recorder == null ) {
					startListening();
				}else{
					stopListening();
				}


			}
		});
    }

    private List<AvsItem> avsQueue = new ArrayList<>();

    /**
     * Handle the response sent back from Alexa's parsing of the Intent, these can be any of the AvsItem types (play, speak, stop, clear, listen)
     * @param response a List<AvsItem> returned from the mAlexaManager.sendTextRequest() call in sendVoiceToAlexa()
     */
    private void handleResponse(AvsResponse response){
        boolean checkAfter = (avsQueue.size() == 0);
        if(response != null){
            //if we have a clear queue item in the list, we need to clear the current queue before proceeding
            //iterate backwards to avoid changing our array positions and getting all the nasty errors that come
            //from doing that
            for(int i = response.size() - 1; i >= 0; i--){
                if(response.get(i) instanceof AvsReplaceAllItem || response.get(i) instanceof AvsReplaceEnqueuedItem){
                    //clear our queue
                    avsQueue.clear();
                    //remove item
                    response.remove(i);
                }
            }
            Log.i(TAG, "Adding "+response.size()+" items to our queue");
            if(BuildConfig.DEBUG){
                for (int i = 0; i < response.size(); i++){
                    Log.i(TAG, "\tAdding: "+response.get(i).getToken());
                }
            }
            avsQueue.addAll(response);
        }
        if(checkAfter) {
            checkQueue();
        }
    }

    private AlexaAudioPlayer audioPlayer;

    /**
     * Check our current queue of items, and if we have more to parse (once we've reached a play or listen callback)
	 * then proceed to the next item in our list.
     *
     * We're handling the AvsReplaceAllItem in handleResponse() because it needs to clear everything currently in the queue, before
     * the new items are added to the list, it should have no function here.
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

        Log.i(TAG, "Item type " + current.getClass().getName());

        if (current instanceof AvsPlayRemoteItem) {
            //play a URL
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playItem((AvsPlayRemoteItem) current);

            }
        } else if (current instanceof AvsPlayContentItem) {
            //play a URL
            if (!audioPlayer.isPlaying()) {
                audioPlayer.playItem((AvsPlayContentItem) current);
            }
        } else if (current instanceof AvsSpeakItem) {
            //play a sound file
            if (!audioPlayer.isPlaying()) {
//				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//
//				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
//
//				try {
//					startActivityForResult(intent, 1);
//				} catch ( Exception e) {
//					Log.d(TAG," Your device doesn't support Speech to Text");
//
//				}
//                audioPlayer.playItem((AvsSpeakItem) current);
				audioPlayer.playItem((AvsSpeakItem) current);
            }

        } else if (current instanceof AvsStopItem) {
            //stop our play
            audioPlayer.stop();
            avsQueue.remove(current);
        } else if (current instanceof AvsReplaceAllItem) {
            //clear all items
            //mAvsItemQueue.clear();
            audioPlayer.stop();
            avsQueue.remove(current);
        } else if (current instanceof AvsReplaceEnqueuedItem) {
            //clear all items
            //mAvsItemQueue.clear();
            avsQueue.remove(current);
        } else if (current instanceof AvsExpectSpeechItem) {

            //listen for user input
            audioPlayer.stop();
            avsQueue.clear();
        } else if (current instanceof AvsSetVolumeItem) {
            //set our volume
           // setVolume(((AvsSetVolumeItem) current).getVolume());
            avsQueue.remove(current);
        } else if(current instanceof AvsAdjustVolumeItem){
            //adjust the volume
          //  adjustVolume(((AvsAdjustVolumeItem) current).getAdjustment());
            avsQueue.remove(current);
        } else if(current instanceof AvsSetMuteItem){
            //mute/unmute the device
           // setMute(((AvsSetMuteItem) current).isMute());
            avsQueue.remove(current);
        }else if(current instanceof AvsMediaPlayCommandItem){
            //fake a hardware "play" press
           // sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PLAY);
            Log.i(TAG, "Media play command issued");
            avsQueue.remove(current);
        }else if(current instanceof AvsMediaPauseCommandItem){
            //fake a hardware "pause" press
           // sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PAUSE);
            Log.i(TAG, "Media pause command issued");
            avsQueue.remove(current);
        }else if(current instanceof AvsMediaNextCommandItem){
            //fake a hardware "next" press
           // sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_NEXT);
            Log.i(TAG, "Media next command issued");
            avsQueue.remove(current);
        }else if(current instanceof AvsMediaPreviousCommandItem){
            //fake a hardware "previous" press
           // sendMediaButton(this, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            Log.i(TAG, "Media previous command issued");
            avsQueue.remove(current);

        }else if(current instanceof AvsResponseException){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error")
                            .setMessage(((AvsResponseException) current).getDirective().getPayload().getCode() + ": "
                                        + ((AvsResponseException) current).getDirective().getPayload().getDescription())
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            });

            avsQueue.remove(current);
            checkQueue();
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 1: {
				if (resultCode == RESULT_OK && null != data) {

					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					Log.d(TAG,text.get(0));
					mInfo.setText(text.get(0));
				}
				break;
			}
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(recorder != null){
			recorder.stop();
			recorder.release();
			recorder = null;
		}
		if(audioPlayer != null){
			audioPlayer.stop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(audioPlayer != null){
			//remove callback to avoid memory leaks
			audioPlayer.removeCallback(alexaAudioPlayerCallback);
			audioPlayer.release();
		}
	}
}
