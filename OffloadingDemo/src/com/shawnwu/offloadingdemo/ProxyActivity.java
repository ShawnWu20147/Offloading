package com.shawnwu.offloadingdemo;

import java.util.List;



import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class ProxyActivity extends Activity {

	public static Handler myhander;
	AppReceiver ar;
	
	
	static SensorManager sm_proxy;
	static Sensor s_proxy;
	static SensorEventListener sel_proxy;
	
	
	static class MySensorEventListener implements SensorEventListener{
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {		
		}

		@Override
		public void onSensorChanged(SensorEvent arg0) {	
			OffloadingManager.proxy_se=arg0;
			sm_proxy.unregisterListener(sel_proxy);
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		OffloadingManager.broadcast_tct=this;
		
		super.onCreate(savedInstanceState);
		
		//以下的代码用于搞出一个SensorEvent供offloading使用
		sm_proxy=(SensorManager)getSystemService(SENSOR_SERVICE);
		s_proxy=sm_proxy.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sel_proxy=new MySensorEventListener();
		OffloadingManager.sm_provide=sm_proxy;
		OffloadingManager.s_provide=s_proxy;
		sm_proxy.registerListener(sel_proxy, s_proxy, SensorManager.SENSOR_DELAY_UI);
		
		
		
		
		
		ar=new AppReceiver();
		IntentFilter filter = new IntentFilter();  
		
		filter.addAction("android.intent.action.Ask_OpenApp"); 		// 平台要求打开,显然是一定可以打开的 自己打开只能在这个activity上
		filter.addAction("android.intent.action.Reply_Info"); //for switching information,like sensor data and touch position
		filter.addAction("android.intent.action.Ask_SwitchPower");  //平台通知我,权限变化相关事宜
		filter.addAction("android.intent.action_RUSensor"); //要求我注册注销处传感器,显然我是 控制者,不是别的
		filter.addAction("android.intent.action_AskCloseShower"); //要求我关闭显示,这是因为控制者主动退出
		filter.addAction("android.intent.action_AskOpenShower"); //要求我打开显示,这是因为显示者自己退出,则要归还显示给控制者
		
		registerReceiver(ar, filter);
		
		
		myhander=new Handler(){
			@Override
	          public void handleMessage(Message msg) { 
				switch(msg.what){
				case 0:
					String show=(String) msg.obj;
					Toast.makeText(getApplicationContext(), show, Toast.LENGTH_SHORT).show();
					break;
				case 1:
					//launch proxied activity

					
				
				
					
					break;
					
				default:
					//
				}
			}
			
		};
		
	}

	@Override
	protected void onResume() {
		
	
		//Log.i("demo","P:onResume");
		super.onResume();
		
		OffloadingManager.proxy_context=this;
		
		OffloadingManager.broadcast_tct=this;
		
		
		/*
		Intent it=new Intent(ProxyActivity.this,MainActivity.class);
		startActivity(it);
		Log.i("demo","ac");
		*/
		
		
	}

	@Override
	protected void onDestroy() {
		
		OffloadingManager.handleProxyExit();	//处理一下是不是异常关闭

		
		unregisterReceiver(ar);
		
		super.onDestroy();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (OffloadingManager.controller!=OffloadingManager.myid) return true;
		//如果我不是控制者,那么在这个入口界面上做手势没有意义
		
		int count=event.getPointerCount();
		int act=event.getAction();
		if (OffloadingManager.controller==OffloadingManager.myid && act==517 && count==3){
			// logic here
			/*
			Intent open=new Intent();
			open.setAction("android.intent.action.Ask_OpenApp");
			open.putExtra("controller", 2);
			open.putExtra("shower", 1);
			open.putExtra("myid",0);
			sendBroadcast(open);
			*/
			//表明这是一个offloading option呼出操作
			
			OffloadingManager.showDia(this);

			return true;
			
		}
		
		//这里需要把 touch数值交给 显示者
		Intent tellp=new Intent();
		tellp.setAction("android.intent.action.TellPoint");
		tellp.putExtra("X", event.getX());
		tellp.putExtra("Y", event.getY());
		sendBroadcast(tellp);
		return true;
		
	}
	
	
	
	
	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(ProxyActivity.this)   
		.setTitle("确定要退出客户端app吗")  
		.setMessage("请再次确定")  
		.setPositiveButton("否", new DialogInterface.OnClickListener(){ 
            
			@Override 
            public void onClick(DialogInterface dialog, int which) {
            	
				
				
            }
		})  
		.setNegativeButton("是", new DialogInterface.OnClickListener(){ 
            @Override 
            public void onClick(DialogInterface dialog, int which) {
            	setClose();
            }
            })  
		.show();  
	}

	
	public void setClose(){
		super.onBackPressed();
	}
	
	
	private Message generateMessage(String info){
		Message msg=new Message();
		msg.what=0;
		msg.obj=info;
		return msg;
	}
	
	
	


}
