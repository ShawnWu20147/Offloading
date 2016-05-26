package com.shawnwu.offloadingdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	TextView tv1,tv2,tv3;
	SensorManager sm;
	Sensor s;
	SensorEventListener sel;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tv1=(TextView)findViewById(R.id.myid);
		tv2=(TextView)findViewById(R.id.controller);
		tv3=(TextView)findViewById(R.id.shower);
		
		sm=(SensorManager)getSystemService(SENSOR_SERVICE);
		s=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sel=new SensorEventListener() {
			
			@Override
			public void onSensorChanged(SensorEvent event) {
				
				tv1.setText(String.valueOf(event.values[0]));
				tv2.setText(String.valueOf(event.values[1]));
				tv3.setText(String.valueOf(event.values[2]));
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub
				
			}
		};
	}

	@Override
	protected void onResume() {
		OffloadingManager.sel_actual=sel;	//第一次,记录sensoreventlistener
		OffloadingManager.registerS();	
		OffloadingManager.main_context=this;
		super.onResume();
	}

	@Override
	protected void onPause() {
		
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		Log.i("demo","onDestroy, 可能是主动退出,也可能是切换offloading");
		
		OffloadingManager.unregisterS();	//其实应该放在onPause里面
		
		
		OffloadingManager.handleMainExit();
		

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		//显然,必须既是控制者,又是显示者  因为只有显示才能到Main 只有控制下面的if才能执行
		if (OffloadingManager.controller==OffloadingManager.myid)	//当且仅当我是控制者的时候,允许退出
			super.onBackPressed();
		else
			ProxyActivity.myhander.sendMessage(OffloadingManager.generateMessage("显示者无权主动退出app"));
	}

	
}
