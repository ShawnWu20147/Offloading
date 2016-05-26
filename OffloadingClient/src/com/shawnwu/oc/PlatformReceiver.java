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
			//客户端app发送 三点触控之后 选择然后触发
			
			
			int oldc=intent.getIntExtra("oldc", -1);
			int newc=intent.getIntExtra("newc", -1);
			int olds=intent.getIntExtra("olds", -1);
			int news=intent.getIntExtra("news", -1);
			if (oldc==-1 || newc==-1 || olds==-1 || news==-1){
				Log.i("shawnwu","出问题,某数值为-1");
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
			//app已经处理完毕了,可以这里更新了
			MainActivity.controller=ServerReceiver.temp_controller;
			MainActivity.shower=ServerReceiver.temp_shower;
		}
		else if (ac.equals("android.intent.action.Request_ExitGame")){
			//客户端app发送  在MainActivity上点击back,然后触发  值得考虑
			//TODO 应该是自己是shower 所以被要求关闭
			
			
			
			
		}
		else if (ac.equals("android.intent.action.TellRUSensor")){
			//客户端app发送    显然客户端app是显示者,它要求通知控制者 注册/注销传感器
			
			int controller=MainActivity.controller;
			int r_u=intent.getIntExtra("what", -1);
			int tp=intent.getIntExtra("type", -1);
			
			//组装一个MyPackage 发送给controller
			
			MyPackage mp=new MyPackage();
			mp.type=2;
			mp.i1=r_u;
			mp.i2=tp;
			try {
				MainActivity.oos_sk[controller].writeObject(mp);
				Log.i("shawnwu","通知controller进行一次传感器注册/注销");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		else if (ac.equals("android.intent.action.TellPoint")){
			//是ProxyActivity发的,因此自己是控制者,且不是显示者,因为显示者不是这个Activity
			int shower=MainActivity.shower;
			
			float x=intent.getFloatExtra("X", 0.0f);
			float y=intent.getFloatExtra("Y", 0.0f);
			
			//组装一个MyPackage 发送给shower
			MyPackage mp=new MyPackage();
			mp.type=3;
			mp.s1=String.valueOf(x);
			mp.s2=String.valueOf(y);
			try {
				MainActivity.oos_sk[shower].writeObject(mp);
				Log.i("shawnwu","通知shower进行一次触控");
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
				//Log.i("demo","通知shower进行一个传感数值");
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
		else if (ac.equals("android.intent.action.ForceQuit")){
			Log.i("shawnwu","收到一个forcequit,type="+intent.getIntExtra("type", -1));
			//0 控制者+显示者, 主动关了显示,于是一同也关控制,仅仅通知一下
			//1 控制者+显示者 , 直接划掉了app,于是通知一下即可  基本同0
			//2控制者, 关掉app, 需要通知显示者也关闭
			//3显示者,关掉app,需要把显示权移交给控制者
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
				
				//发送给服务器
				break;
			case 2:
				//通知显示者关闭 
				int oldshower=MainActivity.shower;
				MainActivity.controller=-1;
				MainActivity.shower=-1;			
				//告诉服务器
				
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
				//通知控制者,同时可以显示
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
