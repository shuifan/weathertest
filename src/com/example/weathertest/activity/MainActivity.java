package com.example.weathertest.activity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.weathertest.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 * 此类作为访问 中国天气网的天气api的一个测试类
 * 实现 获取当前时间 的 天气预报的功能
 */
public class MainActivity extends Activity implements android.view.View.OnClickListener{

	//此变量作为handler中辨别接收数据来源的标志
	private static final int SHOW_TEXT=0; 
	
	private TextView textView;
	
	private Button sendRequest;
	
	//用于加密的APIKey 来自中国天气网
	private final String APIKey = "39272f_SmartWeatherAPI_1f5a3b6"; 
	
	//用handler来接收子线程的数据处理并更新UI
	private Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			
			case SHOW_TEXT:
				String text=(String)msg.obj;
				try {
					JSONObject jsonObject=new JSONObject(text);
					
					JSONObject c=jsonObject.getJSONObject("c");
					String county=c.getString("c3");
					String city=c.getString("c5");
					String province=c.getString("c7");
					
					JSONObject f=jsonObject.getJSONObject("f");
					String date=f.getString("f0");
					
					JSONArray weather=f.getJSONArray("f1");
					JSONObject today=weather.getJSONObject(0);
					String dayTemp=today.getString("fc");
					String nightTemp=today.getString("fd");
					
					Toast.makeText(MainActivity.this, province+city+county+"  "+date, Toast.LENGTH_SHORT).show();
					Toast.makeText(MainActivity.this, dayTemp+"~"+nightTemp, Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
				textView.setText(text);
				break;

			default:
				break;
			}
		};
	};
	
	//初始化ui实例
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView=(TextView)findViewById(R.id.text);
        sendRequest=(Button)findViewById(R.id.send_request);
        sendRequest.setOnClickListener(this);
    }
    
    //设置按钮的点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.send_request:
			getWeather();
			break;

		default:
			break;
		}
		
	}
	
    private void getWeather() {
		//开启子线程处理耗时操作
    	new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection=null;
				try {
					//得到当前的时间 以 yyyyMMddHHmm 格式
					SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmm", Locale.CHINA);
					String currentDate=sdf.format(new Date());
					
					//拼接成对应的编码地址
					String whiteUrlS="http://open.weather.com.cn/data/?areaid=101190502&type=forecast_v&date="+currentDate+"&appid=bcbccb513492a6c5";
					String whiteUrl="http://open.weather.com.cn/data/?areaid=101190502&type=forecast_v&date="+currentDate+"&appid=bcbccb";
					//得到加密后的url
					String finalUrl=getSecretiveUrl(whiteUrlS,whiteUrl);
					
					//判断加密运算是否成功
					if (finalUrl != null) {
						URL url=new URL(finalUrl);
						connection=(HttpURLConnection)url.openConnection();
						connection.setRequestMethod("GET");
						connection.setConnectTimeout(8000);
						connection.setReadTimeout(8000);
						
						InputStream inputStream=connection.getInputStream();
						BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
						
						StringBuilder builder=new StringBuilder();
						String line=new String();
						while ((line=reader.readLine())!=null) {
							builder.append(line);
						}
						
						Message message=new Message();
						message.what=SHOW_TEXT;
						message.obj=builder.toString();
						handler.sendMessage(message);
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					if (connection != null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}
	
    //得到加密之后地址的函数
    private String getSecretiveUrl(String whiteUrlS,String whiteUrl) {
    	
    	try {
    		
    		String key=GetSecretiveWeatherUrl.standardURLEncoder(whiteUrlS, APIKey);
    		
    		String finalUrl=whiteUrl+"&key="+key;
    		return finalUrl;
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		return null;
		
	}

  
    
}
