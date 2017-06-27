package com.willblaschko.android.alexa.interfaces.card;

import com.willblaschko.android.alexa.data.Directive.ImageStructure;
import com.willblaschko.android.alexa.data.Directive.Temperature;
import com.willblaschko.android.alexa.data.Directive.WeatherForecast;
import com.willblaschko.android.alexa.data.Directive.Title;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xblu on 2017/6/22.
 */

public class WeatherTemplate extends BaseCard implements Serializable{

	private String                currentWeather;
	private ImageStructure        currentWeatherIcon;
	private Temperature           highTemperature;
	private Temperature           lowTemperature;
	private List<WeatherForecast> weatherForecast;

	public WeatherTemplate(String token,
						   Title title,
						   ImageStructure skillIcon,
						   String currentWeather,
						   ImageStructure currentWeatherIcon,
						   Temperature highTemperature,
						   Temperature lowTemperature,
						   List<WeatherForecast> weatherForecast) {
		super(token, title, skillIcon);
		this.currentWeather = currentWeather;
		this.currentWeatherIcon = currentWeatherIcon;
		this.highTemperature = highTemperature;
		this.lowTemperature = lowTemperature;
		this.weatherForecast = weatherForecast;
	}

	public String getCurrentWeather() {
		return currentWeather;
	}

	public ImageStructure getCurrentWeatherIcon() {
		return currentWeatherIcon;
	}

	public Temperature getHighTemperature() {
		return highTemperature;
	}

	public Temperature getLowTemperature() {
		return lowTemperature;
	}

	public List<WeatherForecast> getWeatherForecast() {
		return weatherForecast;
	}
}
