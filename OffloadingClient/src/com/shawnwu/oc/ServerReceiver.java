package com.shawnwu.oc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.nfc.cardemulation.OffHostApduService;
import android.util.Log;

public class ServerReceiver implements Runnable {
	
	public static int temp_controller;
	public static int temp_shower;
	
	ObjectInputStream ois;
	ObjectOutputStream oos;
	public ServerReceiver(ObjectInputStream ois,ObjectOutputStream oos) {
		this.ois=ois;
		this.oos=oos;
	}
	@Override
	public void run() {
		while (true){
			try {
				MyPackage mp=(MyPackage) ois.readObject();
				//���ܽ��ܸ�������,��Ҫ������д
				
				if (mp.type==1){
					MainActivity.recv_pkg=mp;
					MainActivity.recv_ack=true;
					continue;
				}
				
				//���ǲ�����type=1 ͬ��������=0 ���� �����������Ŀ϶�ֻ�� Ȩ��ת�����������!!!
				if (mp.type==5){
					handleOnePkg(mp);
				}
				
				if (mp.type==6){
					int i1=mp.i1;
					if (i1==0){
						//Ҫ��ر���ʾ
						MainActivity.controller=MainActivity.shower=-1;
						
						Intent it_cs=new Intent();
						it_cs.setAction("android.intent.action_AskCloseShower");
						MainActivity.ct.sendBroadcast(it_cs);
						
						
					}
					else if (i1==1){
						//Ҫ������߼�����ʾ
						
						MainActivity.shower=MainActivity.controller;
						
						Intent it_os=new Intent();
						it_os.setAction("android.intent.action_AskOpenShower");
						MainActivity.ct.sendBroadcast(it_os);
						
					}
					else{
						Log.i("shawnwu","�յ�6�Ű�,����i1ֵ��");
					}
					
				}
				
				
				
				
			} catch (ClassNotFoundException | IOException e) {
				
				e.printStackTrace();
			}
			
		}

	}
	
	
	
	
	public void handleOnePkg(MyPackage mp){
		if (mp.type!=5){
			Log.i("shawnwu","�յ�һ����,ȴ����5��");
			return;
		}
		
		int newc=mp.i3;
		temp_controller=newc;
		int news=mp.i4;
		temp_shower=news;
		
		
		
		//ע����������������ͨ��OffloadingClient,���Բ�����ǰˢ��!!!
		
		//����,�ͻ���app���ܸ���û����,ҲҪ�ж�һ��!
		
		if (!checkRun()){
			//����˼�ѹ��û�д�app,��Ҫ���˼Ҵ�
			Intent intent = new Intent(Intent.ACTION_MAIN);  
			intent.addCategory(Intent.CATEGORY_LAUNCHER);   
			ComponentName cn = new ComponentName(MainActivity.PACKAGE_NAME, MainActivity.MAIN_ACT_NAME);              
			intent.setComponent(cn);  
			MainActivity.ct.startActivity(intent); 
			gotoFront();//��֤app�Ѿ���ǰ̨
		}
		else{
			gotoFront();//��֤app��ǰ̨
		}
		
		Intent it=new Intent();
		it.setAction("android.intent.action.Ask_SwitchPower");
		it.putExtra("type", mp.i1);
		it.putExtra("id", mp.i2);
		it.putExtra("controller", mp.i3);
		it.putExtra("shower", mp.i4);
		MainActivity.ct.sendBroadcast(it);
		
	
		
	}
	
	public void gotoFront(){
		ActivityManager am = (ActivityManager)MainActivity.ct.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> recentList = am.getRunningTasks(30);
        for (RunningTaskInfo info : recentList) {
            if (info.topActivity.getPackageName().equals(MainActivity.PACKAGE_NAME)) {
                am.moveTaskToFront(info.id, 0);
                return;
            }
        }
	}
	
	
	
	public boolean checkRun(){
		ActivityManager am = (ActivityManager)MainActivity.ct.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> recentList = am.getRunningTasks(30);
        for (RunningTaskInfo info : recentList) {
            if (info.topActivity.getPackageName().equals("com.shawnwu.offloadingdemo")) {
                return true;
            }
        }
        return false;

	}

}
