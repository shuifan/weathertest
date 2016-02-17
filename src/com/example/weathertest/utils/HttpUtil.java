package com.example.weathertest.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 *此类包含发起网络请求的方法 
 */

public class HttpUtil {

	public interface HttpCallbackListener {
		
		public void onFinish(String response);
		
		public void onError(Exception e);
	}
	
	//此方法用于发起网络请求
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener) {
		//网络请求为耗时操作，开启线程
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url=new URL(address);
					connection=(HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in=connection.getInputStream();
					BufferedReader reader=new BufferedReader(new InputStreamReader(in));
					StringBuilder response=new StringBuilder();
					String line=new String();
					while ((line=reader.readLine()) != null) {
						response.append(line);
					}
					
					if (listener!=null) {
						listener.onFinish(response.toString());
					}
					
				} catch (Exception e) {
					if (listener != null) {
						listener.onError(e);
					}
				}finally{
					if (connection!=null) {
						connection.disconnect();
					}
				}
			}
		}).start();
	}
}
