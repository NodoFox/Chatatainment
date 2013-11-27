package com.example.testgcm;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessageAdapter extends BaseAdapter {

	private Context context;
	private List<Message> msg;
	

	public MessageAdapter(Context context, List<Message> msg) {
		super();
		this.context = context;
		this.msg = msg;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String currentDateToDisplay = null;
		if(position>0){
			currentDateToDisplay = Utils.dateForChat.format( msg.get(position-1).getTimestamp());
		}
		Message message = msg.get(position);
		ViewHolder holder;
		holder = new ViewHolder();
		if(Message.Types.NEW_GAME_REQUEST.equals(message.getType())){
			convertView = LayoutInflater.from(context).inflate(
					R.layout.message_notification, parent, false);
		}else{
			
			if (message.getIsMine() == true){
				convertView = LayoutInflater.from(context).inflate(
						R.layout.message_row_me, parent, false);
			}
			else{
				convertView = LayoutInflater.from(context).inflate(
						R.layout.message_row_other, parent, false);			
			}
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.time = (TextView) convertView.findViewById(R.id.time_text);
			holder.dateDisplay = (TextView) convertView.findViewById(R.id.date_display);
			String messageDate = Utils.dateForChat.format(message.getTimestamp());
			if (position==0 || !messageDate.equals(currentDateToDisplay)) {
				holder.dateDisplay.setText(messageDate);
				currentDateToDisplay = messageDate;
				holder.dateDisplay.setVisibility(View.VISIBLE);
			}else{
				holder.dateDisplay.setVisibility(View.GONE);
			}
			holder.message.setText(message.getMsg());
			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a",
					Locale.getDefault());
			holder.time.setText(formatter.format(message.getTimestamp()));
		}
		convertView.setTag(holder);
		return convertView;
	}

	private static class ViewHolder {
		TextView message;
		TextView time;
		TextView dateDisplay;
	}

	@Override
	public int getCount() {
		return msg.size();
	}

	@Override
	public Object getItem(int pos) {
		// TODO Auto-generated method stub
		return msg.get(pos);
	}

	@Override
	public long getItemId(int arg0) {
		return 0;
	}
}
