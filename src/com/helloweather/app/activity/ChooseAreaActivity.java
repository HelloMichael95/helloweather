package com.helloweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.helloweather.app.R;
import com.helloweather.app.db.HelloWeatherDB;
import com.helloweather.app.model.City;
import com.helloweather.app.model.County;
import com.helloweather.app.model.Province;
import com.helloweather.app.util.HttpCallbackListener;
import com.helloweather.app.util.HttpUtil;
import com.helloweather.app.util.Utility;

public class ChooseAreaActivity extends Activity{

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private HelloWeatherDB helloWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	/*
	 * ʡ�б�
	 */
	private List<Province> provinceList;
	
	/*
	 * ���б�
	 */
	private List<City> cityList;
	
	/*
	 * ���б�
	 */
	private List<County> countyList;
	
	/*
	 * ѡ�е�ʡ��
	 */
	private Province selectedProvince;
	
	/*
	 * ѡ�еĳ���
	 */
	private City selectedCity;
	
	/*
	 * ��ǰѡ�еļ���
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false)) {
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		helloWeatherDB = HelloWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if(currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(index);
					queryCities();
				}else if(currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(index);
					queryCounties();
				}else if(currentLevel == LEVEL_COUNTY) {
					String provinceCode = selectedProvince.getProvinceCode();
					String cityCode = selectedCity.getCityCode();
					String countyCode = countyList.get(index).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("province_code", provinceCode);
					intent.putExtra("city_code", cityCode);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}				
			}
			
		});
		queryProvinces();
	}
	
	/*
	 *��ѯȫ�����е�ʡ�� ���ȴ����ݿ��ѯ�� ���û�в�ѯ����ȥ�������ϲ�ѯ 
	 */
	private void queryProvinces(){
		provinceList = helloWeatherDB.loadProvinces();
		if(provinceList.size() > 0) {
			dataList.clear();
			for(Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "province");
		}
	}
	
	/*
	 * ��ѯȫ�����е��У� ���ȴ����ݿ��ѯ�� ���û�в�ѯ����ȥ�������ϲ�ѯ
	 */
	private void queryCities() {
		cityList = helloWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size() > 0) {
			dataList.clear();
			for(City city : cityList) {
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(), "city");
		}
	}
	
	/*
	 * ��ѯȫ�����е��أ� ���ȴ����ݿ��ѯ�� ���û�в�ѯ����ȥ�������ϲ�ѯ
	 */
	private void queryCounties() {
		countyList = helloWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size() > 0) {
			dataList.clear();
			for(County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else {
			queryFromServer(selectedCity.getCityCode(), "county");
		}
	}
	
	/*
	 * ���ݴ���Ĵ��ź����ʹӷ������ϲ�ѯʡ��������
	 */
	private void queryFromServer(final String code, final String type) {
		String address;
		if("province".equals(type)) {
			address = "http://www.weather.com.cn/data/city3jdata/china.html";
		}else if("city".equals(type)){
			address = "http://www.weather.com.cn/data/city3jdata/provshi/" + code + ".html";
		}else {
			String finalCode = selectedProvince.getProvinceCode() + code;			
			address = "http://www.weather.com.cn/data/city3jdata/station/" + finalCode + ".html";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {

			@Override
			public void onFinish(String response) {
				boolean result = false;
				if("province".equals(type)) {
					result = Utility.handleProvincesResponse(helloWeatherDB, response);
				}else if("city".equals(type)) {
					result = Utility.handleCitiesResponse(helloWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)) {
					result = Utility.handleCountiesResponse(helloWeatherDB, response, selectedCity.getId());
				}			
				if(result) {
					//�������̴߳����߼�
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)) {
								queryProvinces();
							}else if("city".equals(type)) {
								queryCities();
							}else if("county".equals(type)) {
								queryCounties();
							}
						}
						
					});
				}
			}

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_SHORT).show();
					}
					
				});
				
			}
			
		});
	}
	
	/*
	 * ��ʾ���ȶԻ���
	 */
	private void showProgressDialog() {
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ���...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * �رս��ȶԻ���
	 */
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/*
	 * ����Back���������ݵ�ǰ�ļ������жϣ���ʱӦ�÷������б�ʡ�б���ֱ���˳���
	 */
	public void onBackPressed() {
		if(currentLevel == LEVEL_COUNTY) {
			queryCities();
		}else if(currentLevel == LEVEL_CITY) {
			queryProvinces();
		}else {
			finish();
		}		
	}
}
