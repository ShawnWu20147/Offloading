package com.shawnwu.oc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.content.Intent;
import android.nfc.cardemulation.OffHostApduService;
import android.util.Log;

public class SocketReceiver implements Runnable {
	ObjectInputStream ois;
	ObjectOutputStream oos;
	int iwho;
	public SocketReceiver(ObjectInputStream ois,ObjectOutputStream oos,int iwho) {
		this.ois=ois;
		this.oos=oos;
		this.iwho=iwho;
	}
	@Override
	public void run() {
		while (true){
			try {
				MyPackage mp=(MyPackage) ois.readObject();
				//可能接受各种内容,需要在这里写
				switch(mp.type){
				case 0:
					//configuration pull, should not be here
					Log.i("shawnwu","不应该收到0号");
					break;
				case 1:
					Log.i("shawnwu","不应该收到1号,因为1号不会是同伴发来的");
					break;
				case 2:
					//sensor_reg_unreg  同伴发来的
					if(MainActivity.shower!=iwho || MainActivity.controller!=MainActivity.myid){
						Log.i("shawnwu","收到2号,但是校验错误");
						break;
					}
					
					
					int r_u=mp.i1;
					int whats=mp.i2;
					
					Intent its=new Intent();
					its.setAction("android.intent.action_RUSensor");
					its.putExtra("what", r_u);
					its.putExtra("tp", whats);
					MainActivity.ct.sendBroadcast(its);
					break;
					
					
				case 3:
					//关于一个点的信息
					
					if(MainActivity.controller!=iwho || MainActivity.shower!=MainActivity.myid){
						Log.i("shawnwu","收到3号,但是校验错误");
						break;
					}
					
					float []fp=new float[2];
					fp[0]=Float.valueOf(mp.s1);
					fp[1]=Float.valueOf(mp.s2);
					
					Intent itp=new Intent();
					itp.setAction("android.intent.action.Reply_Info");
					itp.putExtra("thing", "touch");
					itp.putExtra("values", fp);
					MainActivity.ct.sendBroadcast(itp);
					break;
					
					
					
				case 4:
					if(MainActivity.controller!=iwho || MainActivity.shower!=MainActivity.myid){
						Log.i("shawnwu","收到4号,但是校验错误");
						break;
					}
					
					//关于一次传感数值的信息
					float []fs=new float[3];
					fs[0]=Float.valueOf(mp.s1);
					fs[1]=Float.valueOf(mp.s2);
					fs[2]=Float.valueOf(mp.s3);
					
					Intent itis=new Intent();
					itis.setAction("android.intent.action.Reply_Info");
					itis.putExtra("thing", "sensor");
					itis.putExtra("values", fs);
					MainActivity.ct.sendBroadcast(itis);
					break;
				default:
					Log.i("shawnwu","出问题了,居然不是01234");
					
				}
				
				
				
				
			} catch (ClassNotFoundException | IOException e) {
				
				e.printStackTrace();
			}
			
		}

	}

}
