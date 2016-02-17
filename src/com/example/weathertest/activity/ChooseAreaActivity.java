package com.example.weathertest.activity;

import java.util.ArrayList;
import java.util.List;

import com.example.weathertest.R;
import com.example.weathertest.db.CoolWeatherDB;
import com.example.weathertest.model.City;
import com.example.weathertest.model.County;
import com.example.weathertest.model.Province;
import com.example.weathertest.utils.HttpUtil;
import com.example.weathertest.utils.Utility;
import com.example.weathertest.utils.HttpUtil.HttpCallbackListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

	/*
	 * 此活动遍历并显示省市县的城市名称
	 */

public class ChooseAreaActivity extends Activity {
	//定义省市县为三个级别
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	//省列表
	private List<Province> provinceList;
	//市列表
	private List<City> cityList;
	//县列表
	private List<County> countyList;
	//选中的省份
	private Province selectedProvince;
	//选中的城市
	private City selectedCity;
	
	//当前的级别
	private int currentLevel;
	
	@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			
			//判断下文件中是否有城市被选中
//			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
//			if (prefs.getBoolean("city_selected", false)) {
//				Intent intent=new Intent(this, WeatherActivity.class);
//				startActivity(intent);
//				finish();
//				return;//程序不再向下执行
//			}
			requestWindowFeature(Window.FEATURE_NO_TITLE); 
			setContentView(R.layout.choose_area);
			
			listView=(ListView)findViewById(R.id.list_view);
			titleText=(TextView)findViewById(R.id.title_text);
			
			adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
			listView.setAdapter(adapter);
			
			coolWeatherDB=CoolWeatherDB.getInstance(this);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if (currentLevel == LEVEL_PROVINCE) {
						selectedProvince=provinceList.get(position);
						queryCities();
					}else if (currentLevel == LEVEL_CITY) {
						selectedCity=cityList.get(position);
						queryCounties();
					}else if (currentLevel == LEVEL_COUNTY) {
						String countyCode=countyList.get(position).getCountyCode();
						Intent intent=new Intent(ChooseAreaActivity.this, WeatherActivity.class);
						intent.putExtra("county_code", countyCode);
						startActivity(intent);
						finish();
					}
				}
			});
			queryProvinces();//加载省级数据
		}
	
	//得到省级城市列表  优先从数据库查询  没有再从服务器获取
	private void queryProvinces() {
		provinceList=coolWeatherDB.loadProvinces();
		if (provinceList.size()>0) {
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			//得到数据之后 通知适配器更新
			adapter.notifyDataSetChanged();
			listView.setSelection(0);//定位到顶端
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else {
			queryFromServer(null,"province");
		}
	}
	
	//得到某个省的城市列表  优先从数据库查询  没有再从服务器获取
	private void queryCities() {
		cityList=coolWeatherDB.loadCities(selectedProvince.getId());
		if (cityList.size()>0) {
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			//通知适配器更新
			adapter.notifyDataSetChanged();
			//将listView的指针移动到第一个
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}else {
			queryFromServer(selectedProvince.getProvinceCode(),"city"); 
		}
	}
	
	//得到城市中所有的县列表  优先从数据库查询  没有再从服务器获取
	private void queryCounties() {
		countyList=coolWeatherDB.loadCounties(selectedCity.getId());
		if (countyList.size()>0) {
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();//通知适配器更新
			listView.setSelection(0);//将指针移动至首行  方便观察
			//更改标题
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}else {
			queryFromServer(selectedCity.getCityCode(),"county"); 
		}
	}
	
	//根据传入的城市编码和对应的类型 从服务器端获取数据
	private void queryFromServer(final String code,final String type) {
		String address=new String();
		if (!TextUtils.isEmpty(code)) {
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else {
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		
		showProgressDialog();//从服务器端获取数据要花时间，所以给一个进度条
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result=false;
				if (type.equals("province")) {
					result=Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if (type.equals("city")) {
					result=Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if (type.equals("county")) {
					result=Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				
				if (result) {
					//通过runOnUiThread()方法回到主线程处理逻辑  方便 查询完成的时候更新UI
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							
							closeProgressDialog();//耗时操作完成记得关闭主线程
							
							if (type.equals("province")) {
								queryProvinces();//再次通过此方法从数据库获取
							}else if (type.equals("city")) {
								queryCities();
							}else if (type.equals("county")) {
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()回到主线程逻辑
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						closeProgressDialog();//关闭进度条
						Toast.makeText(ChooseAreaActivity.this, "加载失败  T_T  O.O", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	//显示进度条
	private void showProgressDialog(){
		if (progressDialog == null) {
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("加载中...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	//关闭进度条
	private void closeProgressDialog(){
		if (progressDialog!=null) {
			progressDialog.dismiss();
		}
	}
	
	//捕获Back按键  根据当前的级别进行相应的跳转 或 退出
	@Override
		public void onBackPressed() {
			//super.onBackPressed();    此方法不可调用
			if (currentLevel == LEVEL_COUNTY) {
				queryCities();
			}else if (currentLevel == LEVEL_CITY) {
				queryProvinces();
			}else {
				finish();
			}
		}
}
