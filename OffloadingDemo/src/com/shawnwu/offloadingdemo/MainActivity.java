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
		OffloadingManager.sel_actual=sel;	//��һ��,��¼sensoreventlistener
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
		Log.i("demo","onDestroy, �����������˳�,Ҳ�������л�offloading");
		
		OffloadingManager.unregisterS();	//��ʵӦ�÷���onPause����
		
		
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
		//��Ȼ,������ǿ�����,������ʾ��  ��Ϊֻ����ʾ���ܵ�Main ֻ�п��������if����ִ��
		if (OffloadingManager.controller==OffloadingManager.myid)	//���ҽ������ǿ����ߵ�ʱ��,�����˳�
			super.onBackPressed();
		else
			ProxyActivity.myhander.sendMessage(OffloadingManager.generateMessage("��ʾ����Ȩ�����˳�app"));
	}

	
}
