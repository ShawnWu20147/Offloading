package com.shawnwu.offloadingdemo;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorEvent;
import android.os.Message;
import android.os.SystemClock;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.MotionEvent;

public class AppReceiver extends BroadcastReceiver {	

	

	@Override
	public void onReceive(Context context, Intent intent) {
		
		Class mc=OffloadingManager.mainClass;
		
		String action=intent.getAction();
		if (action.equals("android.intent.action.Ask_OpenApp")){
			//TODO ����д!!!
			//����������Ϊ,����Ȩ����ʾȨ����һ���
			Log.i("demo","recv BROADCAST:ASK_OPENAPP");
			int controller=intent.getIntExtra("controller", -2);
			int shower=intent.getIntExtra("shower", -2);
			int myid=intent.getIntExtra("myid", -2);
			
			if (controller==-2 || shower==-2 || myid==-2){
				ProxyActivity.myhander.sendMessage(generateMessage("�ڹ㲥Ask_OpenApp�������쳣���"));
			}
			
			
			OffloadingManager.myid=myid;
			OffloadingManager.controller=controller;
			OffloadingManager.shower=shower;
			
			ProxyActivity.myhander.sendMessage(generateMessage("��ʵӦ������!������"+myid+",��������"+controller+",��ʾ����"+shower));
			
			if (controller==myid && shower==myid){
				//�Ҷ��� ����	
				Intent itt=new Intent(context,mc);
				context.startActivity(itt);	
				//��ʱ����д�������,ȷʵҲ����
			}
			else if (controller==myid){
				//�ҽ����ǿ�����,������
				Log.i("demo","�ҽ����ǿ�����,������");	
			}
			else if (shower==myid){
				//�ҽ�������ʾ��  ��Ȼ��Ҫ���� MainActivity
				Log.i("demo","�ҽ�������ʾ��,������");	
			}
			else{
				ProxyActivity.myhander.sendMessage(generateMessage("��������,�ҼȲ��ǿ�����Ҳ������ʾ��,ȴ��������"));
			}
		}
		
		else if (action.equals("android.intent.action.Reply_Info")){
			//���˸�֪������Ϣ ���紫���� ���ص�
			//��������ע��/ע��  �����и�action �� RU_Sensor		
			//  thing <-> sensor/touch
			//  values <-> float[]		
			String thing=intent.getStringExtra("thing");
			float[] vls=intent.getFloatArrayExtra("values");

			int myid=OffloadingManager.myid;
			int controller=OffloadingManager.controller;
			int shower=OffloadingManager.shower;
			
			if (myid==controller && myid==shower){
				//���Ӽ��� ��Ϊ�������������!				
			}
			else if (myid==controller){
				//���ǿ�����,ȴ��������,������!				
			}
			else if (myid==shower){
				// ���˷����µ� �������� �� ��������
				//ֻ��������Ҫ���濼��!
				if (thing==null){
					Log.i("demo","������,thing��Ȼ�ǿ�");
				}
				else if (thing.equals("sensor")){
					
					SensorEvent se=OffloadingManager.proxy_se;
					for (int i=0;i<vls.length;i++)
						se.values[i]=vls[i];
					if (OffloadingManager.actual_reg){
						OffloadingManager.sel_actual.onSensorChanged(se);
					}
					
				}
				else if (thing.equals("touch")){
					float x=vls[0];
					float y=vls[1];
					long downTime = SystemClock.uptimeMillis();
				    final MotionEvent downEvent = MotionEvent.obtain(downTime, downTime,
				        MotionEvent.ACTION_DOWN, x, y, 0);
				    downTime += 200;
				    final MotionEvent upEvent = MotionEvent.obtain(downTime, downTime,
				        MotionEvent.ACTION_UP, x, y, 0);
				    
				    OffloadingManager.agree_touch=true;
				    
				    OffloadingManager.touch_view.onTouchEvent(downEvent);
				    OffloadingManager.touch_view.onTouchEvent(upEvent);
				    downEvent.recycle();
				    upEvent.recycle();
				    
				    OffloadingManager.agree_touch=false;
					
				}			
			}
			else{
				//�Լ������κν�ɫ,���Ӽ���
			}
			
		}
		
		else if (action.equals("android.intent.action_RUSensor")){
			//�ͻ���ƽ̨Ҫ��ע�� ע��������
			//��Ȼ�Լ��� ������
			
			if (OffloadingManager.myid!=OffloadingManager.controller){
				Log.i("demo","�յ�RUS�㲥,�����Ҳ��ǿ�����");
				return;
			}
			if (OffloadingManager.myid==OffloadingManager.shower){
				Log.i("demo","�յ�RUS�㲥,������ͬʱ������ʾ��,����Ӧ�յ�");
				return;
			}
			
			
			int what=intent.getIntExtra("what", -1);
			if (what==0){
				//OffloadingManager.actual_reg=true;  ��Ȼ��������Ǹ���ʾ����ص�,��˲��ÿ���
				
				OffloadingManager.registerHelperSensor();
			}
			else if (what==1){
				//OffloadingManager.actual_reg=false;
				
				OffloadingManager.unregisterHelperSensor();
			}
			else{
				Log.i("demo","������,Ҫ��ע��ע��,����whatֵ����");
			}
		}
		else if (action.equals("android.intent.action.Ask_SwitchPower")){
			
			//TODO ����д  ע����귢��Ack_SwitchPower!
			int type=intent.getIntExtra("type", -1);
			int how=intent.getIntExtra("id", -1);
			int newc=intent.getIntExtra("controller", -1);
			int news=intent.getIntExtra("shower", -1);
			if (type==-1 || how==-1 || newc==-1 || news==-1){
				Log.i("demo","��AppReceiver���յ�AskSwitchPower,������-1");
				return;
			}
			Log.i("demo","�µĿ�������"+newc+",�µ���ʾ����"+news);
			
			OffloadingManager.is_closing=true;	//ע��,���ڹر�  ������Ҫ�ٸ�Ϊfalse
			
			switch(type){
			case 1:
				if (how==0){
					//ԭ�����Ƽ���ʾ,���ڿ���
					((Activity)OffloadingManager.main_context).finish();
					
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));

					
				}
				else if (how==1){
					//ԭ��û��,������ʾ
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					
				
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));
				}
				break;
			case 2:
				if (how==0){
					//ԭ�����Ƽ���ʾ,������ʾ  �ҵĲ���:�ȹر���ʾ,����Ȩ��,�ؿ�,����
					
					((Activity)OffloadingManager.main_context).finish();
					
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));
					
				}
				else if (how==1){
					//1ԭ����,���ڿ���
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					//û�б������Ҫ����
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));
					
				}
				break;
			case 3:
				if (how==0){
					//0�������κ�������
					((Activity)OffloadingManager.main_context).finish();
					
					
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//1ȫ����������(����Ϊ��)		
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ƺ���ʾ"));
				}
				
				break;
			case 4:
				if (how==0){
					//����ȫ��,������
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//ԭ����,�����ǿ�����
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));
				}
				else if (how==2){
					//ԭ����,��������ʾ��
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));
				}
				
				break;
			case 5:
				if (how==0){
					//ԭ������,���ڻ��ǿ���
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸��Ȼ�������"));
				}
				else if (how==1){
					//ԭ����ʾ,����û��
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if(how==2){
					//ԭ��û��,������ʾ
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));	
				}
				break;
			case 6:
				if (how==0){
					//ԭ������,���ڿ��Ƽ���ʾ
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ƺ���ʾ"));	
				}
				else if (how==1){
					//ԭ����ʾ,���ڿ�
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				break;
			case 7:
				if (how==0){
					//ԭ������,���ڿ�
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));	
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//ԭ����ʾ,���ڿ��Ƽ���ʾ  ������2-0������һ����
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
		
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������Ƽ���ʾ"));
					
				}
				
				
				break;
			case 8:
				if (how==0){
					//ԭ������,���ڿ�
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));	
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//ԭ����ʾ,���ڿ�
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==2){
					//ԭ����,����ȫ��
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ƺ���ʾ"));
				}
				break;
				
			case 9:
				if (how==0){
					//ԭ������,������ʾ
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));
				}
				else if (how==1){
					//ԭ����ʾ,���ڿ���
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));
					
				}
				
				break;
			case 10:
				if (how==0){
					//ԭ������,������ʾ
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));
				}
				else if (how==1){
					//ԭ����ʾ,����û��
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==2){
					//ԭ��û��,���ڿ���
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;		
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));
				}
				
				break;
			case 11:
				if (how==0){
					//ԭ������,����û��
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//ԭ����ʾ,���ڿ���
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));
					
				}
				else if (how==2){
					//ԭ��û��,������ʾ
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸������ʾ"));	
				}
				break;
			case 12:
				if (how==0){
					//ԭ������,����û��
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸���ٳе�����"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//ԭ����ʾ,������ʾ
					((Activity)OffloadingManager.main_context).finish();	//��ʱ���Ǹ�0�Ž���
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;	
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);	
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸��Ȼ������ʾ"));
					
				}
				else if (how==2){
					//ԭ��û��,���ڿ���
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;		
					ProxyActivity.myhander.sendMessage(generateMessage("����offloading,���豸�������"));
					
				}
				
				break;
			default:
				break;
			}
			
			OffloadingManager.is_closing=false;
			
			//֪ͨƽ̨���Ը�����
			Intent it_notify=new Intent();
			it_notify.setAction("android.intent.action.Ack_SwitchPower");
			OffloadingManager.broadcast_tct.sendBroadcast(it_notify);
			
			
		}
		else if (action.equals("android.intent.action_AskCloseShower")){
			//������ʾ��,�������ر�
			
			//�ؼ����ͷ���Դ��δ���?
			OffloadingManager.is_self_closing=true;
			
			ProxyActivity.myhander.sendMessage(generateMessage("�������˳�,������Ҳ���˳�"));	
			
			((Activity)OffloadingManager.main_context).finish();
			((Activity)OffloadingManager.proxy_context).finish();
			
			
			
			OffloadingManager.is_self_closing=false;
			
			OffloadingManager.controller=222;
			OffloadingManager.shower=333;
			
			
			
		}
		else if (action.equals("android.intent.action_AskOpenShower")){
			//���ǿ�����,������ʾ
			
			OffloadingManager.unregisterHelperSensor();
			
			OffloadingManager.shower=OffloadingManager.controller;
			
			Intent itt=new Intent(context,mc);
			context.startActivity(itt);
			ProxyActivity.myhander.sendMessage(generateMessage("��ʾ���˳�,�������豸��Ϊ��ʾ��"));	
			
		}
		
	}
	

	
	private Message generateMessage(String info){
		Message msg=new Message();
		msg.what=0;
		msg.obj=info;
		return msg;
	}

}
