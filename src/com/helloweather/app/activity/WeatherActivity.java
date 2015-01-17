package com.helloweather.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.helloweather.app.R;
import com.helloweather.app.util.HttpCallbackListener;
import com.helloweather.app.util.HttpUtil;
import com.helloweather.app.util.Utility;

public class WeatherActivity extends Activity implements OnClickListener{

	private LinearLayout weatherInfoLayout;
	
	/*
	 * ������ʾ������
	 */
	private TextView cityNameText;
	
	/*
	 * ������ʾ����ʱ��
	 */
	private TextView publishText;
	
	/*
	 * ������ʾ����������Ϣ
	 */
	private TextView weatherDespText;
	
	/*
	 * ������ʾ����1
	 */
	private TextView temp1Text;
	
	/*
	 * ������ʾ����2
	 */
	private TextView temp2Text;
	
	/*
	 * ������ʾ��ǰ����
	 */
	private TextView currentDateText;
	
	/*
	 * �л����а�ť
	 */
	private Button switchCity;
	
	/*
	 * ����������ť
	 */
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ���ؼ�
		weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText = (TextView)findViewById(R.id.city_name);
		publishText = (TextView)findViewById(R.id.publish_text);
		weatherDespText = (TextView)findViewById(R.id.weather_desp);
		temp1Text = (TextView)findViewById(R.id.temp1);
		temp2Text = (TextView)findViewById(R.id.temp2);
		currentDateText = (TextView)findViewById(R.id.current_date);
		switchCity = (Button)findViewById(R.id.switch_city);
		refreshWeather = (Button)findViewById(R.id.refresh_weather);
		
		String provinceCode = getIntent().getStringExtra("province_code");
		String cityCode = getIntent().getStringExtra("city_code");
		String countyCode = getIntent().getStringExtra("county_code");
		
		if(!TextUtils.isEmpty(countyCode)) {
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(provinceCode, cityCode, countyCode);
		}else {
			showWeather();
		}
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		
	}
	


	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("ͬ����...");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if(!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfoFromServer(weatherCode);
			}
			break;
		default:
			break;
		}		
	}
	
	/*
	 * ��ѯ�ؼ����Ŷ�Ӧ����������
	 */
	private void queryWeatherCode(String provinceCode, String cityCode, String countyCode) {
		String code = provinceCode + cityCode + countyCode;
		queryWeatherInfoFromServer(code);
	}
	
	/*
	 * ��ѯ�������Ŷ�Ӧ������
	 */
	private void queryWeatherInfoFromServer(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				Utility.handleWeatherResponse(WeatherActivity.this, response);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						showWeather();						
					}				
				});
				
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");						
					}					
				});				
			}
		});
		
	}
	
	/*
	 * ��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�������ϡ�
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText(prefs.getString("publish_time", "") + "����");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
	
}
