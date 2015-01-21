package com.helloweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.helloweather.app.db.HelloWeatherDB;
import com.helloweather.app.model.City;
import com.helloweather.app.model.County;
import com.helloweather.app.model.Province;

public class Utility {

	/*
	 * 解析和处理服务器返回的省级数据
	 */
	public synchronized static boolean handleProvincesResponse(
			HelloWeatherDB helloWeatherDB, String response) {
		if (!TextUtils.isEmpty(response)) {
			response = response.substring(1, response.length() - 1);
			String[] allProvinces = response.split(",");
			if (allProvinces != null && allProvinces.length > 0) {
				for (String p : allProvinces) {
					String[] array = p.split("\\:");
					Province province = new Province();
					String provinceCode = array[0].substring(1,
							array[0].length() - 1);
					String provinceName = array[1].substring(1,
							array[1].length() - 1);
					province.setProvinceCode(provinceCode);
					province.setProvinceName(provinceName);
					helloWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * 解析和处理服务器返回的市级数据
	 */
	public synchronized static boolean handleCitiesResponse(
			HelloWeatherDB helloWeatherDB, String response, int provinceId) {
		if (!TextUtils.isEmpty(response)) {
			response = response.substring(1, response.length() - 1);
			String[] allCities = response.split(",");
			if (allCities != null && allCities.length > 0) {
				for (String c : allCities) {
					String[] array = c.split("\\:");
					City city = new City();
					String cityCode = array[0].substring(1,
							array[0].length() - 1);
					String cityName = array[1].substring(1,
							array[1].length() - 1);
					city.setCityCode(cityCode);
					city.setCityName(cityName);
					city.setProviceId(provinceId);
					helloWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * 解析和处理服务器返回的县级数据
	 */
	public synchronized static boolean handleCountiesResponse(
			HelloWeatherDB helloWeatherDB, String response, int cityId) {
		if (!TextUtils.isEmpty(response)) {
			response = response.substring(1, response.length() - 1);
			String[] allCounties = response.split(",");
			if (allCounties != null && allCounties.length > 0) {
				for (String c : allCounties) {
					String[] array = c.split("\\:");
					County county = new County();
					String countyCode = array[0].substring(1,
							array[0].length() - 1);
					String countyName = array[1].substring(1,
							array[1].length() - 1);
					county.setCountyCode(countyCode);
					county.setCountyName(countyName);
					county.setCityId(cityId);
					helloWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}

	/*
	 * 解析服务器返回的JSON数据，并保存到本地
	 */
	public static void handleWeatherResponse(Context context, String response) {
		try {
Log.d("MainActivity", response);			
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 将服务器返回的天气信息存储到SharedPreferences文件中
	 */
	public static void saveWeatherInfo(Context context, String cityName,
			String weatherCode, String temp1, String temp2, String weatherDesp,
			String publishTime) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putBoolean("city_selected", true);
			editor.putString("city_name", cityName);
			editor.putString("weather_code", weatherCode);
			editor.putString("temp1", temp1);
			editor.putString("temp2", temp2);
			editor.putString("weather_desp", weatherDesp);
			editor.putString("publish_time", publishTime);
			editor.putString("current_date", sdf.format(new Date()));
			editor.commit();
	}
}
