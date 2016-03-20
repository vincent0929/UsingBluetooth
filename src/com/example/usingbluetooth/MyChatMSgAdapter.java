package com.example.usingbluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyChatMSgAdapter extends BaseAdapter {

	private LayoutInflater inflater;
	private SharedPreferences sharedPreferences;
	
	
	public MyChatMSgAdapter(Context context){
		inflater=LayoutInflater.from(context);
	}
	
	@Override
	public int getCount() {
		return AtyChatWindow.getChatMsgs().size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return AtyChatWindow.getChatMsgs().get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		String chatMsg=AtyChatWindow.getChatMsgs().get(position);
		String[] strs=chatMsg.split(":");
		String deviceName=strs[0];
		String chatContent=strs[1];
		
		System.out.println(deviceName+":"+chatContent+"\n");
		
		RelativeLayout rl;
		if(convertView!=null){
			rl=(RelativeLayout) convertView;
		}else{
			rl=(RelativeLayout) inflater.inflate(R.layout.chat_window_list_cell, null);
		}
		
		TextView tvChatContent=(TextView) rl.findViewById(R.id.tvChatMsgDialog);
		RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		if(deviceName.equals(MainActivity.getbluetoothAdapter().getName())){
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		}else{
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		}
		params.setMargins(10, 10, 10, 10);
		tvChatContent.setLayoutParams(params);
		tvChatContent.setText(chatContent);
		
		
		return rl;
	}
}
