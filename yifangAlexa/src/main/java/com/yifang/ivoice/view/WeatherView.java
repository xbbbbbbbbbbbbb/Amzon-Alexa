package com.yifang.ivoice.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.yifang.ivoice.R;

/**
 * Created by xblu on 2017/6/22.
 */

public class WeatherView extends LinearLayout {

	private ImageView image;
	private TextView day;
	private TextView temperature;
	private Context mContext;

	public WeatherView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		mContext = context;
		View v = LayoutInflater.from(context).inflate(R.layout.weather_view, this, true);
		image = (ImageView) v.findViewById(R.id.image_forecast);
		day = (TextView) v.findViewById(R.id.forecast_day);
		temperature = (TextView) v.findViewById(R.id.forecast_temperatur);
	}

	public WeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public WeatherView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initView(context);
	}

	public WeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initView(context);
	}

	public void setImage(String url){
		Picasso.with(mContext).load(url).into(image);
	}

	public void setDay(String day){
		this.day.setText(day);
	}

	public void setTemperature(String temperature){
		this.temperature.setText(temperature);
	}

}
