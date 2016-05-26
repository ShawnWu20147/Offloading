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
				//���ܽ��ܸ�������,��Ҫ������д
				switch(mp.type){
				case 0:
					//configuration pull, should not be here
					Log.i("shawnwu","��Ӧ���յ�0��");
					break;
				case 1:
					Log.i("shawnwu","��Ӧ���յ�1��,��Ϊ1�Ų�����ͬ�鷢����");
					break;
				case 2:
					//sensor_reg_unreg  ͬ�鷢����
					if(MainActivity.shower!=iwho || MainActivity.controller!=MainActivity.myid){
						Log.i("shawnwu","�յ�2��,����У�����");
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
					//����һ�������Ϣ
					
					if(MainActivity.controller!=iwho || MainActivity.shower!=MainActivity.myid){
						Log.i("shawnwu","�յ�3��,����У�����");
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
						Log.i("shawnwu","�յ�4��,����У�����");
						break;
					}
					
					//����һ�δ�����ֵ����Ϣ
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
					Log.i("shawnwu","��������,��Ȼ����01234");
					
				}
				
				
				
				
			} catch (ClassNotFoundException | IOException e) {
				
				e.printStackTrace();
			}
			
		}

	}

}
