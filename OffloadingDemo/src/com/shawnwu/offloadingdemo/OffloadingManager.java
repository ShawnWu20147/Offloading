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
	public static boolean is_self_closing=false;	//�����������ͷ���ʾ,���Ҳ�Ҫ��Է�ע����Դ,��Ϊ�Է��ڱ����Ѿ�ע������
	
	public static SensorEventListener sel_actual=null;
	public static boolean actual_reg=false;
	
	
	public static SensorManager sm_provide;
	public static Sensor s_provide;
	public static final int SENSOR_DELAY=SensorManager.SENSOR_DELAY_UI;
	public static SensorEventListener sel_provide=new SensorEventListener() {
		
		@Override
		public void onSensorChanged(SensorEvent event) {
			if (OffloadingManager.myid!=OffloadingManager.controller){
				//������ֱ����Ѿ����ǿ�����
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
		tv1.setText("��ǰ�Ŀ�������"+OffloadingManager.controller+",��ǰ����ʾ����"+OffloadingManager.shower);
		
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
		.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			
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
				
				
				
				ProxyActivity.myhander.sendMessage(generateMessage("������"+who_control+",��ʾ��"+who_show));
				
				handleSwitch(who_control, who_show);
				
			}
		})
		.setNegativeButton("ȡ��", null).show();
		
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
			Log.i("demo","������,��controller�޷��������Ʋ˵�");
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
			Log.i("demo","������,��Ϊ����MainActivity,�п���Ȩ��һ��Ҳ����ʾȨ");
		}
		else if (myid==show){

			Intent regs=new Intent();
			regs.setAction("android.intent.action.TellRUSensor");
			regs.putExtra("what", 0);	//0 means reg
			regs.putExtra("type", 1);	//1�Ŵ�����
			broadcast_tct.sendBroadcast(regs);
		}
		else{
			Log.i("demo","������,��Ϊ����MainActivity");
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
			Log.i("demo","������,��Ϊ����MainActivity,�п���Ȩ��һ��Ҳ����ʾȨ");
		}
		else if (myid==show){
			
			
			
			Intent regs=new Intent();
			regs.setAction("android.intent.action.TellRUSensor");
			regs.putExtra("what", 1);	//1 means unreg
			regs.putExtra("type", 1);	//1�Ŵ�����
			broadcast_tct.sendBroadcast(regs);
		}
		else{
			Log.i("demo","������,��Ϊ����MainActivity");
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
		//Ӧ���ǿ���Ȩû�� �����ǹ�����ʾ��Ҫͬʱ�ؿ��ƽ���  ��ʱ��Ҫ�ж�����ôû�� �����Ŀ���/���Ƽ���ʾ
		
		
		if (is_closing || is_self_closing) return;	//����ģʽ,���账��
		
		if (!IamActive()){
			//�Ҳ��ǿ�����,Ҳ������ʾ��,�ؾ͹���
			ProxyActivity.myhander.sendMessage(generateMessage("��Ȼ�ر�,û���κ�Ӱ��"));
			return;
		}
		
		if (myid==controller && myid==shower){
			//�����ر�app,ע��handleMainExit��,������is_closingΪtrue,������һ������
			//ԭ������+��ʾ,���ڶ�û��
			
			controller=222;
			shower=333;
			
			unregisterHelperSensor();
			
			Intent it_notify=new Intent();
			it_notify.setAction("android.intent.action.ForceQuit");
			it_notify.putExtra("type", 1);	//��Ϊ����+��ʾ�߱������ر�,��Ҫ֪ͨ������   ������������������
			broadcast_tct.sendBroadcast(it_notify);
			
			
			
		}
		else if (myid==controller){
			//ֱ�ӹص�������,��Ҫ��֪ͨ��ʾ��Ҳ�ص�
			//TODO ���Ϊ����?����ֱ�ӱ��ع�,ͬʱ����Ҳ��Ҫ�����淵����Դ?   Ӧ�ñ��ع�,ʵ������  onSensorChanged����
			
			
			controller=222;
			shower=333;
			
			unregisterHelperSensor();
			Intent it_askquit=new Intent();
			it_askquit.setAction("android.intent.action.ForceQuit");
			it_askquit.putExtra("type",2);	//��Ϊ�����߱������ر�,��Ҫ֪ͨ��ʾ��Ҳ�ر�,ע����ʾ�߹ر�ʱ�����������������������...
			
			
			
			broadcast_tct.sendBroadcast(it_askquit);
			
			
			
		}
		else if (myid==shower){
			//������ʾ��,��Ҫ�������һͬ��Ϊ��ʾ��  ע���ʱMain��onDestroy���ᱻ���õ�
			
			//����Main�� onDestroyû�е��� ������Դ���û���  �����Ѿ�������
			
			shower=controller;
			
			Intent it_askquit=new Intent();
			it_askquit.setAction("android.intent.action.ForceQuit");
			it_askquit.putExtra("type",3);	//��Ϊ��ʾ��ֱ�ӹر�,�����ʾȨ�ƽ���������
			
			broadcast_tct.sendBroadcast(it_askquit);
			
			
			

		}
		
		
		
		
	}
	
	public static void handleMainExit(){
		
		if (is_closing || is_self_closing) return;	//����ģʽ,���迼��
		
		if (controller!=myid || shower!=myid){
			Log.i("demo","������,mainActivity��onDestroy������,Ӧ��ͬʱ����2����ݲſ���");
			return;
		}
		
		//��ʱ ��������ݾ��� controller��shower,��������Ϊ��back�ر�����ʾ�ص��˿���,��ô��Ҫ��һͬ�رտ���,��Ϊû�пɿ��Ƶ�������
		
		
		ProxyActivity.myhander.sendMessage(generateMessage("��ʾ�߹ر�,��һͬ�رձ����Ŀ��ƹ���"));	
		Intent it_showback=new Intent();
		it_showback.setAction("android.intent.action.ForceQuit");
		it_showback.putExtra("type", 0);	//��Ϊ��ʾ�߱������ر�,���ǿ���Ҳ������
		
		
		
		is_closing=true;
		
		broadcast_tct.sendBroadcast(it_showback);
		
		((Activity)proxy_context).finish();
		
		
		controller=222;
		shower=333;
		
		
		is_closing=false;
		
		

	}
	
}
