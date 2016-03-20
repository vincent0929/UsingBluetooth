package com.example.usingbluetooth;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemClickListener{

	private Button btnSearchDevice;
	private static BluetoothAdapter bluetoothAdapter;
	private ListView lvBondedDevicesList;
	private ListView lvUnbondedDevicesList;
	private ArrayList<String> bondedDevicesList;
	private ArrayList<String> unBondedDevicesList;
	private MyDeviceAdapter bondedDeviceAdapter;
	private MyDeviceAdapter unBondedDeviceAdapter;
	private boolean isRegister = false;
	
	private OnItemClickListener listenerBonded;
	private OnItemClickListener listenerUnBonded;
	
	private BluetoothSocket bluetoothSocket;

	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public static final String NAME = "Bluetooth_Socket";
	public static final String CHAT_DEVICE_INFO = "chatDeviceInfo";
	public static final int REQUST_CODE = 0;
	private static final int REQUEST_ENABLE = 1;

	public static BluetoothAdapter getbluetoothAdapter() {
		return bluetoothAdapter;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setView();
		setBluetooth();
		showBondedDevice();
	}

	private BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
				btnSearchDevice.setText("正在搜索......");
				btnSearchDevice.setEnabled(false);
			}else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
					unBondedDevicesList.add(remoteDevice.getName() + "#"+ remoteDevice.getAddress());
					unBondedDeviceAdapter.notifyDataSetChanged();
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				btnSearchDevice.setText("重新搜索");
				btnSearchDevice.setEnabled(true);
				if(lvUnbondedDevicesList.getCount()==0){
					Toast.makeText(getApplicationContext(), "没有搜索到蓝牙设备",Toast.LENGTH_SHORT).show();
					unBondedDeviceAdapter.notifyDataSetChanged();
				}
			}
		}
	};
	
	@Override
	protected void onStart() {
		if (!isRegister) {
			isRegister = true;
			IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			IntentFilter filterStart=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
			IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			IntentFilter filterBond=new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
			this.registerReceiver(deviceReceiver, filterStart);
			this.registerReceiver(deviceReceiver, filterFound);
			this.registerReceiver(deviceReceiver, filterEnd);
		}
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
			bluetoothAdapter.cancelDiscovery();
		}
		if (isRegister) {
			isRegister = false;
			unregisterReceiver(deviceReceiver);
		}
		super.onDestroy();
	}
	
	private void setView() {	
		bondedDevicesList=new ArrayList<String>();
		unBondedDevicesList=new ArrayList<String>();
		lvBondedDevicesList=(ListView) findViewById(R.id.lvBondedDevicesList);
		lvUnbondedDevicesList=(ListView) findViewById(R.id.lvUnbondedDevicesList);
		bondedDeviceAdapter=new MyDeviceAdapter(MainActivity.this,bondedDevicesList);
		unBondedDeviceAdapter=new MyDeviceAdapter(MainActivity.this, unBondedDevicesList);
		lvBondedDevicesList.setAdapter(bondedDeviceAdapter);
		lvUnbondedDevicesList.setAdapter(unBondedDeviceAdapter);	
		lvBondedDevicesList.setOnItemClickListener(this);
		lvUnbondedDevicesList.setOnItemClickListener(this);
		btnSearchDevice=(Button) findViewById(R.id.btnSearchBluetoothDevice);
		btnSearchDevice.setOnClickListener(listener);
	}
	
	private void setBluetooth() {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		if (bluetoothAdapter != null) {
			// 确认开启蓝牙
			if (!bluetoothAdapter.isEnabled()) {
				// 请求用户开启
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(intent, RESULT_FIRST_USER);
				// 使蓝牙设备可见，方便配对
				Intent in = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				in.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 200);
				startActivity(in);
//				// 直接开启，不经过提示
//				bluetoothAdapter.enable();
			}
		} else {
			new AlertDialog.Builder(this).setTitle("提示")
					.setMessage("你的设备不支持蓝牙").setNegativeButton("cancel", null)
					.show();
		}
	}
	
	private View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (bluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {// 如果蓝牙还没开启
				Toast.makeText(MainActivity.this, "请先打开蓝牙", Toast.LENGTH_SHORT).show();
				return;
			}
			if (bluetoothAdapter.isDiscovering())
				bluetoothAdapter.cancelDiscovery();
			unBondedDevicesList.clear();
			bluetoothAdapter.startDiscovery();
			btnSearchDevice.setText("正在搜索......");
			btnSearchDevice.setEnabled(false);
		}
	};

	public void showBondedDevice() {
		Object[] listDevice = bluetoothAdapter.getBondedDevices().toArray();
		for (int i = 0; i < listDevice.length; i++) {
			BluetoothDevice device = (BluetoothDevice) listDevice[i];
			String str = device.getName() + "#" + device.getAddress();
			bondedDevicesList.add(str); // 获取设备名称和mac地址
			bondedDeviceAdapter.notifyDataSetChanged();
		}
	}
	
	private void findAvalibleDevice() {
		// 获取可配对蓝牙设备
		Set<BluetoothDevice> device = bluetoothAdapter.getBondedDevices();

		if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
			unBondedDevicesList.clear();
			unBondedDeviceAdapter.notifyDataSetChanged();
		}
		if (device.size() > 0) { // 存在已经配对过的蓝牙设备
			bondedDevicesList.clear();
			for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();) {
				BluetoothDevice btd = it.next();
				bondedDevicesList.add(btd.getName() + '#' + btd.getAddress());
				bondedDeviceAdapter.notifyDataSetChanged();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case RESULT_OK:
			findAvalibleDevice();
			break;
		case RESULT_CANCELED:
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		if (bluetoothAdapter.isDiscovering()){
			bluetoothAdapter.cancelDiscovery();
			btnSearchDevice.setText("重新搜索");
			btnSearchDevice.setEnabled(true);
		}
		String str = bondedDevicesList.get(position);
		String[] values = str.split("#");
		String address = values[1];
		BluetoothDevice device= bluetoothAdapter.getRemoteDevice(address);
		Boolean returnValue = false;
		if (parent == lvBondedDevicesList) {
			try {
//				connect(device);
				Intent intent = new Intent(MainActivity.this, AtyChatWindow.class);
				intent.putExtra(CHAT_DEVICE_INFO, str);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
			} catch (Exception e) {
				Toast.makeText(MainActivity.this, "蓝牙连接失败", 1).show();
				e.printStackTrace();
			}
		} else if (parent == lvUnbondedDevicesList) {
			try {
				Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
				returnValue = (Boolean) createBondMethod.invoke(device);
				Intent intent = new Intent(MainActivity.this, AtyChatWindow.class);
				intent.putExtra(CHAT_DEVICE_INFO, str);
				startActivity(intent);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	private void startServerSocket(BluetoothAdapter bluetoothAdapter){
//		BluetoothServerSocket serverSocket
//	}
	
	private void connect(BluetoothDevice device) {  
		try {
			bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			bluetoothSocket.connect();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), "连接失败",500).show();
			e.printStackTrace();
		}
    }
}