package com.helloweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
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
	 * 省列表
	 */
	private List<Province> provinceList;
	
	/*
	 * 市列表
	 */
	private List<City> cityList;
	
	/*
	 * 县列表
	 */
	private List<County> countyList;
	
	/*
	 * 选中的省份
	 */
	private Province selectedProvince;
	
	/*
	 * 选中的城市
	 */
	private City selectedCity;
	
	/*
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				}				
			}
			
		});
		queryProvinces();
	}
	
	/*
	 *查询全国所有的省， 优先从数据库查询， 如果没有查询到再去服务器上查询 
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
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else {
			queryFromServer(null, "province");
		}
	}
	
	/*
	 * 查询全国所有的市， 优先从数据库查询， 如果没有查询到再去服务器上查询
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
	 * 查询全国所有的县， 优先从数据库查询， 如果没有查询到再去服务器上查询
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
	 * 根据传入的代号和类型从服务器上查询省市县数据
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
					//返回主线程处理逻辑
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
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
					
				});
				
			}
			
		});
	}
	
	/*
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	/*
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if(progressDialog != null) {
			progressDialog.dismiss();
		}
	}
	
	/*
	 * 捕获Back按键，根据当前的级别来判断，此时应该返回市列表、省列表还是直接退出。
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
