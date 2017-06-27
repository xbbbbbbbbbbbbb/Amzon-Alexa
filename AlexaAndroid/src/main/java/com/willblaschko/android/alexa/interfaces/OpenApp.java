package com.willblaschko.android.alexa.interfaces;

/**
 * Created by xblu on 2017/5/18.
 */

public class OpenApp extends AvsItem {

	private String packageName;
	private String activityName;

	public OpenApp(String token,String packageName,String activityName) {
		super(token);
		this.packageName = packageName;
		this.activityName = activityName;
	}

	public String getPackageName(){
		return packageName;
	}

	public String getActivityName(){
		return activityName;
	}
}
