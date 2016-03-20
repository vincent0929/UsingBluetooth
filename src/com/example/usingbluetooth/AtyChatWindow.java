package com.example.usingbluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AtyChatWindow extends Activity {

	private TextView tvBackToMainAty;
	private TextView tvDeviceName;
	private ListView lvChatMsgList;
	private EditText etEditMsg;
	private Button btnSendMsg;

	private BluetoothDevice device = null;
	private BluetoothSocket clientSocket = null;
	private OutputStream os = null;

	private String deviceName = "";
	private String deviceAddress = "";

	private static ArrayList<String> chatMsgs;
	private MyChatMSgAdapter chatMsgAdapter;

	private AcceptData acceptData;
	
	private SharedPreferences sharedPreferences;
	private StringBuilder allChatMsg;
	
	public static ArrayList<String> getChatMsgs(){
		return chatMsgs;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aty_chat_window);

		tvBackToMainAty = (TextView) findViewById(R.id.tvBackToMainAty);
		tvBackToMainAty.setOnClickListener(listener);
		tvDeviceName = (TextView) findViewById(R.id.tvChatAtyDeviceName);
		lvChatMsgList = (ListView) findViewById(R.id.lvChatMsgList);
		etEditMsg = (EditText) findViewById(R.id.etChatAtyEditMsg);
		etEditMsg.clearFocus();
		btnSendMsg = (Button) findViewById(R.id.btnChatAtySendMsg);
		btnSendMsg.setOnClickListener(listener);

		chatMsgs=new ArrayList<String>();

		String currentDeviceInfo = getIntent().getStringExtra(MainActivity.CHAT_DEVICE_INFO);
		deviceName = currentDeviceInfo.substring(0,currentDeviceInfo.indexOf("#"));
		tvDeviceName.setText(deviceName);
		deviceAddress = currentDeviceInfo.substring(currentDeviceInfo.indexOf("#") + 1).trim();
		
		chatMsgAdapter = new MyChatMSgAdapter(this);
		lvChatMsgList.setAdapter(chatMsgAdapter);

		acceptData = new AcceptData();
		acceptData.start();
		
		connect(device);
	}

	private OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tvBackToMainAty:
				finish();
				overridePendingTransition(R.anim.push_left_in, R.anim.push_right_out);
				break;
			case R.id.btnChatAtySendMsg:
				if(etEditMsg.getText().toString().isEmpty()){
					Toast.makeText(getApplicationContext(), "发送消息不能为空", 500).show();
				} else {
					sendMsg(deviceAddress,etEditMsg.getText().toString().trim());
				}
				break;
			case R.id.llChatWindow:		
				break;
			default:
				break;
			}
		}
	};
	
	private void connect(BluetoothDevice device) {  
		try {
			if (device == null) {
				device = MainActivity.getbluetoothAdapter().getRemoteDevice(deviceAddress);
			}
			if (clientSocket == null) {
				clientSocket = device.createRfcommSocketToServiceRecord(MainActivity.MY_UUID);
				clientSocket.connect();
			}
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "连接失败",500).show();
			e.printStackTrace();
		}
    }

	private void sendMsg(String deviceAddress, String msg) {
		try {
			if (os == null) {
				os = clientSocket.getOutputStream();
			}
			Toast.makeText(getApplicationContext(), "开始发送", 500).show();
			os.write(Integer.parseInt(msg,16));
			Toast.makeText(getApplicationContext(), "发送成功", 500).show();
			chatMsgs.add(MainActivity.getbluetoothAdapter().getName() + ":" + Integer.parseInt(msg,16));
			refreshAdapter();
			etEditMsg.setText("");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void refreshAdapter() {
		chatMsgAdapter.notifyDataSetChanged();
		lvChatMsgList.smoothScrollToPosition(chatMsgAdapter.getCount());
	}
	
	public class AcceptData extends Thread {

		private BluetoothServerSocket serverSocket;
		private BluetoothSocket socket;
		private InputStream is;

		public AcceptData() {
			try {
				serverSocket = MainActivity.getbluetoothAdapter().
						listenUsingInsecureRfcommWithServiceRecord(MainActivity.NAME, MainActivity.MY_UUID);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				socket = serverSocket.accept();
				is = socket.getInputStream();
				while (true) {
					byte[] buffer = new byte[128];
					int count = is.read(buffer);
					Message msg = new Message();
					msg.obj = new String(buffer, 0, count, "UTF-8");
					handler.sendMessage(msg);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private Handler handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				chatMsgs.add(tvDeviceName.getText().toString()+":"+String.valueOf(msg.obj));
				refreshAdapter();
				super.handleMessage(msg);
			}
		};
	}
	
	@Override
	protected void onDestroy() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}
}
