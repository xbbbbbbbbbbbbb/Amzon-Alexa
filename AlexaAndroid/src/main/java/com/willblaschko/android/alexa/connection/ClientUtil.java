package com.willblaschko.android.alexa.connection;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;

/**
 * Create a singleton OkHttp client that, hopefully, will someday be able to make sure all connections are valid according to AVS's strict
 * security policy--this will hopefully fix the Connection Reset By Peer issue.
 *
 * Created by willb_000 on 6/26/2016.
 */
public class ClientUtil {

	private final static int CONNECT_TIMEOUT =60;
	private final static int READ_TIMEOUT=60;
	private final static int WRITE_TIMEOUT=60;
    private static OkHttpClient mDownChannelClient;
	private static OkHttpClient mEventClient;

    public static OkHttpClient getTLS12OkHttpClient(){
        if(mEventClient == null) {
            OkHttpClient.Builder client = new OkHttpClient.Builder()
				.readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
				.writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
				.connectTimeout(CONNECT_TIMEOUT,TimeUnit.SECONDS);
            if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
                try {

                    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                    trustManagerFactory.init((KeyStore) null);
                    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
                    if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                        throw new IllegalStateException("Unexpected default trust managers:"
                                + Arrays.toString(trustManagers));
                    }

                    X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

                    SSLContext sc = SSLContext.getInstance("TLSv1.2");
                    sc.init(null, null, null);

                    String[] enabled = sc.getSocketFactory().getDefaultCipherSuites();
                    String[] supported = sc.getSocketFactory().getSupportedCipherSuites();

                    client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

                    ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                            .tlsVersions(TlsVersion.TLS_1_2)
                            .cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                                    CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384)
                            .build();

                    List<ConnectionSpec> specs = new ArrayList<>();
                    specs.add(cs);

                    client.connectionSpecs(specs);
                } catch (Exception exc) {
                    Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
                }
            }
            client.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
                    return chain.proceed(request);
                }
            });
			mEventClient = client.build();
        }
        return mEventClient;
    }

	public static OkHttpClient getDownChannelClient(){
		if(mDownChannelClient == null) {
			OkHttpClient.Builder client = new OkHttpClient.Builder()
				.readTimeout(READ_TIMEOUT, TimeUnit.MINUTES)
				.writeTimeout(WRITE_TIMEOUT,TimeUnit.SECONDS)
				.connectTimeout(10,TimeUnit.SECONDS);
			if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 22) {
				try {

					TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					trustManagerFactory.init((KeyStore) null);
					TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
					if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
						throw new IllegalStateException("Unexpected default trust managers:"
														+ Arrays.toString(trustManagers));
					}

					X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

					SSLContext sc = SSLContext.getInstance("TLSv1.2");
					sc.init(null, null, null);

					String[] enabled = sc.getSocketFactory().getDefaultCipherSuites();
					String[] supported = sc.getSocketFactory().getSupportedCipherSuites();

					client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()), trustManager);

					ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
						.tlsVersions(TlsVersion.TLS_1_2)
						.cipherSuites(CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
									  CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384)
						.build();

					List<ConnectionSpec> specs = new ArrayList<>();
					specs.add(cs);

					client.connectionSpecs(specs);
				} catch (Exception exc) {
					Log.e("OkHttpTLSCompat", "Error while setting TLS 1.2", exc);
				}
			}
			client.addNetworkInterceptor(new Interceptor() {
				@Override
				public Response intercept(Chain chain) throws IOException {
					Request request = chain.request().newBuilder().addHeader("Connection", "close").build();
					return chain.proceed(request);
				}
			});
			mDownChannelClient = client.build();
		}
		return mDownChannelClient;
	}

}
