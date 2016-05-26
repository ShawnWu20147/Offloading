package com.shawnwu.offloadingdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.app.Activity;

public class OffloadingManager {
	
	//public static volatile boolean reply_platform;
	
	public static Class mainClass=MainActivity.class;
	
	
	
	public static volatile int myid=111;
	
	public static volatile int controller=222;
	public static volatile int shower=333;
	
	
	public static volatile boolean agree_touch=false;	
	public static View touch_view;
	
	static Context broadcast_tct;
	
	public static Context proxy_context;
	public static Context main_context;
	
	
	public static SensorEvent proxy_se;
	
	
	//public static 
	
	public static boolean is_closing=false;
	public static boolean is_self_closing=false;	//它用于主动释放显示,并且不要求对方注销资源,因为对方在本地已经注销过了
	
	public static SensorEventListener sel_actual=null;
	public static boolean actual_reg=false;
	
	
	public static SensorManager sm_provide;
	public static Sensor s_provide;
	public static final int SENSOR_DELAY=SensorManager.SENSOR_DELAY_UI;
	public static SensorEventListener sel_provide=new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (OffloadingManager.myid!=OffloadingManager.controller){
				//如果发现本机已经不是控制者
				unregisterHelperSensor();
				return;
			}
			float[] vls=event.values;
			Intent it=new Intent();
			it.setAction("android.intent.action.TellSensorData");
			it.putExtra("type", s_provide.getType());
			it.putExtra("values", vls);
			broadcast_tct.sendBroadcast(it);
			
			
		}	
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};
	
	
	public static void showDia(Context ct) {
		
		//broadcast_tct=ct;
		Activity uu=(Activity)ct;
		View savel=uu.getLayoutInflater().inflate(R.layout.option_offload, null);
		TextView tv1=(TextView)savel.findViewById(R.id.tv_show);
		tv1.setText("当前的控制者是"+OffloadingManager.controller+",当前的显示者是"+OffloadingManager.shower);
		
		final RadioButton rb0=(RadioButton)savel.findViewById(R.id.radioControlZero);
		final RadioButton rb1=(RadioButton)savel.findViewById(R.id.radioControlOne);
		final RadioButton rb2=(RadioButton)savel.findViewById(R.id.radioControlTwo);
		
		final RadioButton rbs0=(RadioButton)savel.findViewById(R.id.radioShowZero);
		final RadioButton rbs1=(RadioButton)savel.findViewById(R.id.radioShowOne);
		final RadioButton rbs2=(RadioButton)savel.findViewById(R.id.radioShowTwo);
		
		if (OffloadingManager.controller==2) rb2.setChecked(true);
		else if (OffloadingManager.controller==1) rb1.setChecked(true);
		
		if (OffloadingManager.shower==2) rbs2.setChecked(true);
		else if (OffloadingManager.shower==1) rbs1.setChecked(true);
		
		
		new AlertDialog.Builder(ct)
		.setView(savel)
		.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int who_control=-1;
				int who_show=-1;
				if (rb0.isChecked()) who_control=0;
				else if (rb1.isChecked()) who_control=1;
				else if (rb2.isChecked()) who_control=2;
				
				if (rbs0.isChecked()) who_show=0;
				else if (rbs1.isChecked()) who_show=1;
				else if (rbs2.isChecked()) who_show=2;
				
				
				
				ProxyActivity.myhander.sendMessage(generateMessage("控制者"+who_control+",显示者"+who_show));
				
				handleSwitch(who_control, who_show);
				
			}
		})
		.setNegativeButton("取消", null).show();
		
	}
	
	
	public static Message generateMessage(String info){
		Message msg=new Message();
		msg.what=0;
		msg.obj=info;
		return msg;
	}
	
	
	private static void handleSwitch(int newc,int news){
		if (newc==controller && news==shower) return;	
		if (myid!=controller){
			Log.i("demo","不可能,非controller无法呼出控制菜单");
			return;
		}
		Intent it_tell=new Intent();
		it_tell.setAction("android.intent.action.Request_SwitchPower");
		it_tell.putExtra("oldc", controller);
		it_tell.putExtra("olds", shower);
		it_tell.putExtra("newc", newc);
		it_tell.putExtra("news", news);
		broadcast_tct.sendBroadcast(it_tell);
	}
	
	
	public static void registerS(){	
		actual_reg=true;
		int myid=OffloadingManager.myid;
		int con=OffloadingManager.controller;
		int show=OffloadingManager.shower;
		
		
		if (myid==con && myid==show){
			sm_provide.registerListener(sel_actual, s_provide, SENSOR_DELAY);
		}
		else if (myid==con){
			Log.i("demo","不可能,因为到了MainActivity,有控制权就一定也有显示权");
		}
		else if (myid==show){

			Intent regs=new Intent();
			regs.setAction("android.intent.action.TellRUSensor");
			regs.putExtra("what", 0);	//0 means reg
			regs.putExtra("type", 1);	//1号传感器
			broadcast_tct.sendBroadcast(regs);
		}
		else{
			Log.i("demo","不可能,因为到了MainActivity");
		}
	}

	
	public static void unregisterS(){
		
		if (is_self_closing) return;
		
		actual_reg=false;
		int myid=OffloadingManager.myid;
		int con=OffloadingManager.controller;
		int show=OffloadingManager.shower;
		
		
		if (myid==con && myid==show){
			sm_provide.unregisterListener(sel_actual);
		}
		else if (myid==con){
			Log.i("demo","不可能,因为到了MainActivity,有控制权就一定也有显示权");
		}
		else if (myid==show){
			
			
			
			Intent regs=new Intent();
			regs.setAction("android.intent.action.TellRUSensor");
			regs.putExtra("what", 1);	//1 means unreg
			regs.putExtra("type", 1);	//1号传感器
			broadcast_tct.sendBroadcast(regs);
		}
		else{
			Log.i("demo","不可能,因为到了MainActivity");
		}
		
	}
	
	
	public static void registerHelperSensor(){
		sm_provide.registerListener(sel_provide, s_provide, SENSOR_DELAY);
		
	}
	
	public static void unregisterHelperSensor(){
		sm_provide.unregisterListener(sel_provide);
	}
	
	
	public static boolean IamActive(){
		return controller==myid || shower==myid;
	}
	
	public static void handleProxyExit(){
		//应该是控制权没了 或者是关了显示需要同时关控制界面  这时需要判断是怎么没的 单独的控制/控制加显示
		
		
		if (is_closing || is_self_closing) return;	//被动模式,无需处理
		
		if (!IamActive()){
			//我不是控制者,也不是显示者,关就关了
			ProxyActivity.myhander.sendMessage(generateMessage("自然关闭,没有任何影响"));
			return;
		}
		
		if (myid==controller && myid==shower){
			//划掉关闭app,注意handleMainExit中,我设置is_closing为true,避免了一个问题
			//原来控制+显示,现在都没了
			
			controller=222;
			shower=333;
			
			unregisterHelperSensor();
			
			Intent it_notify=new Intent();
			it_notify.setAction("android.intent.action.ForceQuit");
			it_notify.putExtra("type", 1);	//作为控制+显示者被主动关闭,需要通知服务器   不用做额外别的事情了
			broadcast_tct.sendBroadcast(it_notify);
			
			
			
		}
		else if (myid==controller){
			//直接关掉控制者,则要求通知显示者也关掉
			//TODO 设计为握手?还是直接本地关,同时对面也不要求这面返还资源?   应该本地关,实现在了  onSensorChanged里面
			
			
			controller=222;
			shower=333;
			
			unregisterHelperSensor();
			Intent it_askquit=new Intent();
			it_askquit.setAction("android.intent.action.ForceQuit");
			it_askquit.putExtra("type",2);	//作为控制者被主动关闭,需要通知显示者也关闭,注意显示者关闭时候可能遇到传感器相关问题的...
			
			
			
			broadcast_tct.sendBroadcast(it_askquit);
			
			
			
		}
		else if (myid==shower){
			//我是显示者,则要求控制者一同成为显示者  注意此时Main的onDestroy不会被调用的
			
			//由于Main的 onDestroy没有调用 所以资源不用还了  或者已经还过了
			
			shower=controller;
			
			Intent it_askquit=new Intent();
			it_askquit.setAction("android.intent.action.ForceQuit");
			it_askquit.putExtra("type",3);	//作为显示者直接关闭,则把显示权移交给控制者
			
			broadcast_tct.sendBroadcast(it_askquit);
			
			
			

		}
		
		
		
		
	}
	
	public static void handleMainExit(){
		
		if (is_closing || is_self_closing) return;	//被动模式,无需考虑
		
		if (controller!=myid || shower!=myid){
			Log.i("demo","出问题,mainActivity的onDestroy被调用,应该同时具有2个身份才可能");
			return;
		}
		
		//此时 本机的身份就是 controller和shower,本机的行为是back关闭了显示回到了控制,那么就要求一同关闭控制,因为没有可控制的内容了
		
		
		ProxyActivity.myhander.sendMessage(generateMessage("显示者关闭,将一同关闭本机的控制功能"));	
		Intent it_showback=new Intent();
		it_showback.setAction("android.intent.action.ForceQuit");
		it_showback.putExtra("type", 0);	//作为显示者被主动关闭,于是控制也回收了
		
		
		
		is_closing=true;
		
		broadcast_tct.sendBroadcast(it_showback);
		
		((Activity)proxy_context).finish();
		
		
		controller=222;
		shower=333;
		
		
		is_closing=false;
		
		

	}
	
}
