package com.example.usingbluetooth;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyDeviceAdapter extends BaseAdapter {
	
	private LayoutInflater inflater;
	private ArrayList<String> list;
	
	public MyDeviceAdapter(Context context,ArrayList<String> list){
		inflater=LayoutInflater.from(context);
		this.list=list;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public String getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LinearLayout ll=null;
		String[] strs;
		String strDeviceName="";
		String strDeviceAddress="";
		
		if(list.size()==0){
			return convertView;
		}else{
			strs = getItem(position).split("#");
			strDeviceName = strs[0];
			strDeviceAddress = strs[1];
		}
		
		if(convertView!=null){
			ll=(LinearLayout) convertView;
		}else{
			ll=(LinearLayout) inflater.inflate(R.layout.device_list_cell, null);
		}
		ImageView imgDeviceIcon=(ImageView) ll.findViewById(R.id.imgDeviceIcon);
		TextView tvDeviceName=(TextView) ll.findViewById(R.id.tvDeviceName);
		TextView tvDeviceAddress=(TextView) ll.findViewById(R.id.tvDeviceAddress);
		
		imgDeviceIcon.setImageResource(R.drawable.phone_icon);
		tvDeviceName.setText(strDeviceName);
		tvDeviceAddress.setText(strDeviceAddress);
		
		
		return ll;
	}

}
