package com.yifang.ivoice;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.willblaschko.android.alexa.data.Directive;
import com.willblaschko.android.alexa.interfaces.card.WeatherTemplate;
import com.yifang.ivoice.view.WeatherView;

import java.util.List;

/**
 * Created by xblu on 2017/6/22.
 */

public class WeatherActivity extends Activity {

	private ImageView       iv_currentWeather;
	private TextView        tv_currentWeather;
	private ImageView       iv_highWeather;
	private TextView        tv_highWeather;
	private ImageView       iv_lowWeather;
	private TextView        tv_lowWeather;
	private WeatherView     weatherView1;
	private WeatherView     weatherView2;
	private WeatherView     weatherView3;
	private WeatherView     weatherView4;
	private WeatherView     weatherView5;
	private TextView        mainTitle;
	private TextView        subTitle;
	private ImageView       skillIcon;
	private WeatherTemplate mWeatherTemplate;
	private Picasso         mPicasso;

	private final int WEATHER1 = 0;
	private final int WEATHER2 = 1;
	private final int WEATHER3 = 2;
	private final int WEATHER4 = 3;
	private final int WEATHER5 = 4;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_weather);
		initView();
		mWeatherTemplate = (WeatherTemplate) getIntent().getSerializableExtra(MainActivity.ITEM);
		Log.d("xb",(mWeatherTemplate == null)+"");
		mPicasso = Picasso.with(this);
		setView();
	}

	private void setView() {
		mainTitle.setText(mWeatherTemplate.getTitle().getMainTitle());
		subTitle.setText(mWeatherTemplate.getTitle().getSubTitle());
		if(mWeatherTemplate.getSkillIcon() != null){
			mPicasso.load(mWeatherTemplate.getSkillIcon().getSources().get(0).getUrl()).into(skillIcon);
		}else{
			skillIcon.setVisibility(View.GONE);
		}
		mPicasso.load(mWeatherTemplate.getCurrentWeatherIcon().getSources().get(2).getUrl()).into(iv_currentWeather);
		tv_currentWeather.setText(mWeatherTemplate.getCurrentWeather()+"F");
		mPicasso.load(mWeatherTemplate.getHighTemperature().getArrow().getSources().get(1).getUrl()).into(iv_highWeather);
		tv_highWeather.setText(mWeatherTemplate.getHighTemperature().getValue());
		mPicasso.load(mWeatherTemplate.getLowTemperature().getArrow().getSources().get(1).getUrl()).into(iv_lowWeather);
		tv_lowWeather.setText(mWeatherTemplate.getLowTemperature().getValue());
		List<Directive.WeatherForecast> weatherForecast = mWeatherTemplate.getWeatherForecast();
		weatherView1.setImage(weatherForecast.get(WEATHER1).getImage().getSources().get(1).getUrl());
		weatherView1.setDay(weatherForecast.get(WEATHER1).getDay());
		weatherView1.setTemperature(weatherForecast.get(WEATHER1).getHighTemperature()+"|"+weatherForecast.get(0).getLowTemperature());
		weatherView2.setImage(weatherForecast.get(WEATHER2).getImage().getSources().get(1).getUrl());
		weatherView2.setDay(weatherForecast.get(WEATHER2).getDay());
		weatherView2.setTemperature(weatherForecast.get(WEATHER2).getHighTemperature()+"|"+weatherForecast.get(0).getLowTemperature());
		weatherView3.setImage(weatherForecast.get(WEATHER3).getImage().getSources().get(1).getUrl());
		weatherView3.setDay(weatherForecast.get(WEATHER3).getDay());
		weatherView3.setTemperature(weatherForecast.get(WEATHER3).getHighTemperature()+"|"+weatherForecast.get(0).getLowTemperature());
		weatherView4.setImage(weatherForecast.get(WEATHER4).getImage().getSources().get(1).getUrl());
		weatherView4.setDay(weatherForecast.get(WEATHER4).getDay());
		weatherView4.setTemperature(weatherForecast.get(WEATHER4).getHighTemperature()+"|"+weatherForecast.get(0).getLowTemperature());
		weatherView5.setImage(weatherForecast.get(WEATHER5).getImage().getSources().get(1).getUrl());
		weatherView5.setDay(weatherForecast.get(WEATHER5).getDay());
		weatherView5.setTemperature(weatherForecast.get(WEATHER5).getHighTemperature()+"|"+weatherForecast.get(0).getLowTemperature());
	}

	private void initView() {
		iv_currentWeather = (ImageView) findViewById(R.id.image_current_weather);
		tv_currentWeather = (TextView) findViewById(R.id.tv_current_weather);
		iv_highWeather = (ImageView) findViewById(R.id.image_high_weather);
		tv_highWeather = (TextView) findViewById(R.id.tv_high_weather);
		iv_lowWeather = (ImageView) findViewById(R.id.image_low_weather);
		tv_lowWeather = (TextView) findViewById(R.id.tv_low_weather);
		weatherView1 = (WeatherView) findViewById(R.id.weather1);
		weatherView2 = (WeatherView) findViewById(R.id.weather2);
		weatherView3 = (WeatherView) findViewById(R.id.weather3);
		weatherView4 = (WeatherView) findViewById(R.id.weather4);
		weatherView5 = (WeatherView) findViewById(R.id.weather5);
		mainTitle = (TextView) findViewById(R.id.card_main_title);
		subTitle = (TextView) findViewById(R.id.card_subtitle);
		skillIcon = (ImageView) findViewById(R.id.skill_icon);
	}
}
