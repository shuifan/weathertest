package com.example.weathertest.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.example.weathertest.db.CoolWeatherDB;
import com.example.weathertest.model.City;
import com.example.weathertest.model.County;
import com.example.weathertest.model.Province;

/*
 * 此类包含处理  号码|城市 数据的方法
 */

public class Utility {
	
	//解析省级数据
	public synchronized static boolean handleProvincesResponse(CoolWeatherDB coolWeatherDB,String response) {
		
		if (!TextUtils.isEmpty(response)) {
			String[] allProvinces=response.split(",");
			if (allProvinces!=null && allProvinces.length>0) {
				for (String p : allProvinces) {
					String[] array=p.split("\\|");
					Province province=new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					coolWeatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	//解析服务器返回的市级数据
	public  static boolean handleCitiesResponse(CoolWeatherDB coolWeatherDB,String response,int provinceId) {
		
		if (!TextUtils.isEmpty(response)) {
			String[] allCities=response.split(",");
			if (allCities!=null && allCities.length>0) {
				for (String c : allCities) {
					String[] array=c.split("\\|");
					City city=new City();
					city.setProvinceId(provinceId);
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					coolWeatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	
	//解析县级数据
	public static boolean handleCountiesResponse(CoolWeatherDB coolWeatherDB,String response,int cityId) {
		
		if (!TextUtils.isEmpty(response)) {
			String[] allCounties=response.split(",");
			if (allCounties!=null && allCounties.length>0) {
				for (String c : allCounties) {
					String[] array=c.split("\\|");
					County county=new County();
					county.setCityId(cityId);
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					coolWeatherDB.saveCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
	
	//解析服务器返回的天气数据，并且将其存储到本地
	public static void handleWeatherResponse(Context context,String response) {
		try {
			JSONObject jsonObject=new JSONObject(response);
			JSONObject weatherInfo=jsonObject.getJSONObject("weatherinfo");
			String cityName=weatherInfo.getString("city");
			String weatherCode=weatherInfo.getString("cityid");
			String temp1=weatherInfo.getString("temp1");
			String temp2=weatherInfo.getString("temp2");
			String weatherDesp=weatherInfo.getString("weather");
			String publishTime=weatherInfo.getString("ptime");
			//将此数据保存起来
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,publishTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//将解析完成的天气信息存储到SharedPreferences文件中
	public static void saveWeatherInfo(Context context,String cityName,String weatherCode,
			String temp1,String temp2,String weatherDesp,String publishTime) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));//将当前的日期格式化为 需要的格式
		editor.commit();//提交
	}
	
	
	
	
	
	
}
