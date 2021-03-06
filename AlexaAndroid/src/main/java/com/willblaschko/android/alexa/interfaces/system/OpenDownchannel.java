package com.willblaschko.android.alexa.interfaces.system;

import android.content.Context;
import android.util.Log;

import com.willblaschko.android.alexa.callbacks.AsyncCallback;
import com.willblaschko.android.alexa.connection.ClientUtil;
import com.willblaschko.android.alexa.interfaces.AvsException;
import com.willblaschko.android.alexa.interfaces.AvsResponse;
import com.willblaschko.android.alexa.interfaces.SendEvent;
import com.willblaschko.android.alexa.interfaces.response.ResponseParser;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongycastle.crypto.tls.ContentType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

import static okhttp3.internal.Util.UTF_8;

/**
 * Open Down Channel {@link com.willblaschko.android.alexa.data.Event} to open a persistent connection with the Alexa server. Currently doesn't seem to work as expected.
 *
 * {@link com.willblaschko.android.alexa.data.Event}
 *
 * @author will on 5/21/2016.
 */
public class OpenDownchannel extends SendEvent {

    private static final String TAG = "OpenDownchannel";
    private Call currentCall;
    private OkHttpClient client;
    private String url;
    private AsyncCallback<AvsResponse, Exception> callback;

    public OpenDownchannel(final String url, final AsyncCallback<AvsResponse, Exception> callback) {
        this.callback = callback;
        this.url = url;
        this.client = ClientUtil.getDownChannelClient();
    }

    /**
     * Open the connection
     * @param accessToken
     * @return true if canceled externally
     * @throws IOException
     */
    public void connect(String accessToken) throws IOException {
        if (callback != null) {
            callback.start();
        }

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Response response = null;
        try {

            currentCall = client.newCall(request);

			response = currentCall.execute();
			Log.d(TAG, "openDownChannel===" + response.code()+response.headers());

            final String boundary = getBoundary(response);
			Log.d(TAG,"boundary ==="+boundary);
			if (response.code() == 204){
				return ;
			}
			Log.d(TAG,"length = "+ response.body().contentLength());

            BufferedSource source = response.body().source();
            Buffer buffer = new Buffer();
			if(response.code() == 200){
				callback.complete();
			}
            while (!source.exhausted()) {
				Log.d(TAG,"openDownChannel while");
                source.read(buffer, 8192);
                AvsResponse val = new AvsResponse();

                try {
                    val = ResponseParser.parseResponse(buffer.inputStream(), boundary, true);
                } catch (Exception exp) {
                    exp.printStackTrace();
                }

                if (callback != null) {
                    callback.success(val);
                }
            }
        } catch (IOException e) {
			Log.d(TAG,"openDownchannel IOException"+ e.toString());
            onError(callback, e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void closeConnection() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    private void onError(final AsyncCallback<AvsResponse, Exception> callback, Exception e) {
        if (callback != null) {
            callback.failure(e);
        }
    }

    @Override
    @NotNull
    protected String getEvent(Context context) {
        return "";
    }
}
