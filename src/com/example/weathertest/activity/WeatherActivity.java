package com.example.weathertest.activity;

import com.example.weathertest.R;
import com.example.weathertest.utils.HttpUtil;
import com.example.weathertest.utils.HttpUtil.HttpCallbackListener;
import com.example.weathertest.utils.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity implements OnClickListener{
	
	private LinearLayout weatherInfoLayout;
	
	//显示城市名
	private TextView cityNameText;
	
	//显示发布时间
	private TextView publishText;
	
	//显示天气描述信息
	private TextView weatherDespText;
	
	//显示最低温度
	private TextView temp1Text;
	
	//显示最高温度
	private TextView temp2Text;
	
	//显示当前的日期
	private TextView currentDateText;
	
	//切换城市按钮
	private Button switchCity;
	
	//更新天气按钮
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题
		setContentView(R.layout.weather_layout);
		//初始化各个控件
		weatherInfoLayout=(LinearLayout)findViewById(R.id.weather_info_layout);
		cityNameText=(TextView)findViewById(R.id.city_name);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		temp1Text=(TextView)findViewById(R.id.temp1);
		temp2Text=(TextView)findViewById(R.id.temp2);
		currentDateText=(TextView)findViewById(R.id.current_date);
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		
		//获取县级代号用于查询天气
		String countyCode=getIntent().getStringExtra("county_code");
		//若有天气信号就去查询天气
		if (!TextUtils.isEmpty(countyCode)) {
			publishText.setText("同步中...");
			//设置布局不可见
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);//根据地区编码查询天气代号
		}else {
			//没有的话就显示存储过的天气 本地的
			showWeather();
		}
		
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent=new Intent(this, ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();//跳转之后结束当前的界面
			break;

		case R.id.refresh_weather:
			publishText.setText("同步中...");
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = prefs.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		default:
			break;
		}
	}
	
	//根据地区编码查询天气
	private void queryWeatherCode(String countyCode) {
		String address="http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}
	
	//根据天气代号查询对应的天气
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html"; 
		queryFromServer(address, "weatherCode"); 
	}
	
	//根据传入的地址与类型来查询对应的 天气代号或者天气
	private void queryFromServer(final String address,final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						String[] array=response.split("\\|");
						if (array!=null&&array.length==2) {
							String weatherCode=array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					//处理服务器返回的天气信息
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					//回到主线程更新UI
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				publishText.setText("同步失败");
			}
		});
	}
	
	//从sharedPrefrences文件中读取天气信息，显示到界面上
	public void showWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText( prefs.getString("city_name", ""));   
		temp1Text.setText(prefs.getString("temp1", ""));  
		temp2Text.setText(prefs.getString("temp2", ""));   
		weatherDespText.setText(prefs.getString("weather_desp", ""));   
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");   
		currentDateText.setText(prefs.getString("current_date", ""));   
		weatherInfoLayout.setVisibility(View.VISIBLE);   
		cityNameText.setVisibility(View.VISIBLE); 
	}
	
}
