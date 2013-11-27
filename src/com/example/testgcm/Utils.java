package com.example.testgcm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class Utils {

	public static final SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateForChat = new SimpleDateFormat(
			"MMMM dd, yyyy");

//	public static String SERVER_URL = "http://192.168.0.30:9090/ChatServer/";
	public static String SERVER_URL = "http://192.168.0.9:9090/ChatServer/";
//	public static String SERVER_URL = "http://cs-server.usc.edu:39112/ChatServer/";

	public static String sendToServer(JSONObject json, String servletName) {
		StringBuffer sb = new StringBuffer();
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost postReq = new HttpPost(SERVER_URL + servletName);
			postReq.setEntity(new StringEntity(json.toString()));
			postReq.setHeader("Content-Type", "application/json");
			HttpResponse response = client.execute(postReq);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = null;
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
		} catch (Exception e) {
			Log.e("CHAT_APP", "sendToServer - Error in connecting to server");
			return null;
		}
		return sb.toString();
	}

}
