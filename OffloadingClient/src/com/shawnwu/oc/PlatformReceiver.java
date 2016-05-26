package com.shawnwu.oc;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlatformReceiver extends BroadcastReceiver {


	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String ac=intent.getAction();
		if (ac.equals("android.intent.action.Request_SwitchPower")){
			//�ͻ���app���� ���㴥��֮�� ѡ��Ȼ�󴥷�
			
			
			int oldc=intent.getIntExtra("oldc", -1);
			int newc=intent.getIntExtra("newc", -1);
			int olds=intent.getIntExtra("olds", -1);
			int news=intent.getIntExtra("news", -1);
			if (oldc==-1 || newc==-1 || olds==-1 || news==-1){
				Log.i("shawnwu","������,ĳ��ֵΪ-1");
			}
			
			MyPackage mp=new MyPackage();
			mp.type=5;
			mp.i1=oldc;
			mp.i2=olds;
			mp.i3=newc;
			mp.i4=news;
			try {
				MainActivity.oos_server.writeObject(mp);
			} catch (IOException e) {
			}
		}
		else if (ac.equals("android.intent.action.Ack_SwitchPower")){
			//app�Ѿ����������,�������������
			MainActivity.controller=ServerReceiver.temp_controller;
			MainActivity.shower=ServerReceiver.temp_shower;
		}
		else if (ac.equals("android.intent.action.Request_ExitGame")){
			//�ͻ���app����  ��MainActivity�ϵ��back,Ȼ�󴥷�  ֵ�ÿ���
			//TODO Ӧ�����Լ���shower ���Ա�Ҫ��ر�
			
			
			
			
		}
		else if (ac.equals("android.intent.action.TellRUSensor")){
			//�ͻ���app����    ��Ȼ�ͻ���app����ʾ��,��Ҫ��֪ͨ������ ע��/ע��������
			
			int controller=MainActivity.controller;
			int r_u=intent.getIntExtra("what", -1);
			int tp=intent.getIntExtra("type", -1);
			
			//��װһ��MyPackage ���͸�controller
			
			MyPackage mp=new MyPackage();
			mp.type=2;
			mp.i1=r_u;
			mp.i2=tp;
			try {
				MainActivity.oos_sk[controller].writeObject(mp);
				Log.i("shawnwu","֪ͨcontroller����һ�δ�����ע��/ע��");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		else if (ac.equals("android.intent.action.TellPoint")){
			//��ProxyActivity����,����Լ��ǿ�����,�Ҳ�����ʾ��,��Ϊ��ʾ�߲������Activity
			int shower=MainActivity.shower;
			
			float x=intent.getFloatExtra("X", 0.0f);
			float y=intent.getFloatExtra("Y", 0.0f);
			
			//��װһ��MyPackage ���͸�shower
			MyPackage mp=new MyPackage();
			mp.type=3;
			mp.s1=String.valueOf(x);
			mp.s2=String.valueOf(y);
			try {
				MainActivity.oos_sk[shower].writeObject(mp);
				Log.i("shawnwu","֪ͨshower����һ�δ���");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		else if(ac.equals("android.intent.action.TellSensorData")){
			int shower=MainActivity.shower;
			float[] vls=intent.getFloatArrayExtra("values");
			int tp=intent.getIntExtra("type", -1);
			
			
			MyPackage mp=new MyPackage();
			mp.type=4;
			mp.i1=tp;
			mp.s1=String.valueOf(vls[0]);
			mp.s2=String.valueOf(vls[1]);
			mp.s3=String.valueOf(vls[2]);
			
			try {
				MainActivity.oos_sk[shower].writeObject(mp);
				//Log.i("demo","֪ͨshower����һ��������ֵ");
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		else if (ac.equals("android.intent.action.ForceQuit")){
			Log.i("shawnwu","�յ�һ��forcequit,type="+intent.getIntExtra("type", -1));
			//0 ������+��ʾ��, ����������ʾ,����һͬҲ�ؿ���,����֪ͨһ��
			//1 ������+��ʾ�� , ֱ�ӻ�����app,����֪ͨһ�¼���  ����ͬ0
			//2������, �ص�app, ��Ҫ֪ͨ��ʾ��Ҳ�ر�
			//3��ʾ��,�ص�app,��Ҫ����ʾȨ�ƽ���������
			int tp=intent.getIntExtra("type", -1);
			switch(tp){
			case 0:
				MainActivity.controller=-1;
				MainActivity.shower=-1;
				
				MyPackage mp_t0=new MyPackage();
				mp_t0.type=6;
				mp_t0.i1=0;
				try {
					MainActivity.oos_server.writeObject(mp_t0);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				break;
			case 1:
				MainActivity.controller=-1;
				MainActivity.shower=-1;
				
				MyPackage mp_t1=new MyPackage();
				mp_t1.type=6;
				mp_t1.i1=1;
				try {
					MainActivity.oos_server.writeObject(mp_t1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//���͸�������
				break;
			case 2:
				//֪ͨ��ʾ�߹ر� 
				int oldshower=MainActivity.shower;
				MainActivity.controller=-1;
				MainActivity.shower=-1;			
				//���߷�����
				
				MyPackage mp_t2=new MyPackage();
				mp_t2.type=6;
				mp_t2.i1=2;
				try {
					MainActivity.oos_server.writeObject(mp_t2);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				break;
			case 3:
				//֪ͨ������,ͬʱ������ʾ
				MyPackage mp_t3=new MyPackage();
				mp_t3.type=6;
				mp_t3.i1=3;
				try {
					MainActivity.oos_server.writeObject(mp_t3);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				break;
			default:
				break;
			}
			
			
			
		}
		

	}

}
