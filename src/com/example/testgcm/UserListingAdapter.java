package com.example.testgcm;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class UserListingAdapter extends ArrayAdapter<User> {
	private final Context context;
	private final List<User> users;

	public UserListingAdapter(Context context, List<User> users) {
		super(context, R.layout.userrowlayout, users);
		this.context = context;
		this.users = users;
	}
	
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.userrowlayout, parent, false);
		if(position%2==0)
			rowView.setBackgroundColor(0xFFfafafa);
		TextView userNameView = (TextView) rowView.findViewById(R.id.username);
		TextView statusView = (TextView) rowView.findViewById(R.id.status);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.profilepic);
		userNameView.setText(users.get(position).getName());
		statusView.setText(users.get(position).getStatus());
		BitmapFactory.Options bmOptions;
		bmOptions = new BitmapFactory.Options();
		bmOptions.inSampleSize = 1;
		Bitmap bm = LoadImage(users.get(position).getPic(), bmOptions);
		imageView.setBackgroundResource(R.drawable.ic_launcher);
		return rowView;
	}

	public Bitmap LoadImage(String URL, BitmapFactory.Options options) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = OpenHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in, null, options);
			in.close();
		} catch (IOException e1) {
		}
		return bitmap;
	}

	public InputStream OpenHttpConnection(String strURL) throws IOException {
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpConn.getInputStream();
			}
		} catch (Exception ex) {
			ex.getMessage();
		}
		return inputStream;
	}
}
