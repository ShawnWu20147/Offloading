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
			//TODO 认真写!!!
			//基本可以认为,控制权和显示权会是一起的
			Log.i("demo","recv BROADCAST:ASK_OPENAPP");
			int controller=intent.getIntExtra("controller", -2);
			int shower=intent.getIntExtra("shower", -2);
			int myid=intent.getIntExtra("myid", -2);
			
			if (controller==-2 || shower==-2 || myid==-2){
				ProxyActivity.myhander.sendMessage(generateMessage("在广播Ask_OpenApp中遇到异常情况"));
			}
			
			
			OffloadingManager.myid=myid;
			OffloadingManager.controller=controller;
			OffloadingManager.shower=shower;
			
			ProxyActivity.myhander.sendMessage(generateMessage("真实应用启动!本机是"+myid+",控制者是"+controller+",显示者是"+shower));
			
			if (controller==myid && shower==myid){
				//我都是 昂戳	
				Intent itt=new Intent(context,mc);
				context.startActivity(itt);	
				//暂时不用写别的内容,确实也不用
			}
			else if (controller==myid){
				//我仅仅是控制者,不可能
				Log.i("demo","我仅仅是控制者,不可能");	
			}
			else if (shower==myid){
				//我仅仅是显示者  显然需要开启 MainActivity
				Log.i("demo","我仅仅是显示者,不可能");	
			}
			else{
				ProxyActivity.myhander.sendMessage(generateMessage("遇到问题,我既不是控制者也不是显示者,却让我启动"));
			}
		}
		
		else if (action.equals("android.intent.action.Reply_Info")){
			//别人告知数据信息 例如传感器 触控等
			//传感器的注册/注销  单独有个action 叫 RU_Sensor		
			//  thing <-> sensor/touch
			//  values <-> float[]		
			String thing=intent.getStringExtra("thing");
			float[] vls=intent.getFloatArrayExtra("values");

			int myid=OffloadingManager.myid;
			int controller=OffloadingManager.controller;
			int shower=OffloadingManager.shower;
			
			if (myid==controller && myid==shower){
				//忽视即可 因为这种情况不可能!				
			}
			else if (myid==controller){
				//我是控制者,却给我数据,不可能!				
			}
			else if (myid==shower){
				// 别人发来新的 传感数据 和 触控数据
				//只有这里需要认真考虑!
				if (thing==null){
					Log.i("demo","出问题,thing居然是空");
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
				//自己不是任何角色,忽视即可
			}
			
		}
		
		else if (action.equals("android.intent.action_RUSensor")){
			//客户端平台要求注册 注销传感器
			//显然自己是 控制者
			
			if (OffloadingManager.myid!=OffloadingManager.controller){
				Log.i("demo","收到RUS广播,但是我不是控制者");
				return;
			}
			if (OffloadingManager.myid==OffloadingManager.shower){
				Log.i("demo","收到RUS广播,但是我同时还是显示者,并不应收到");
				return;
			}
			
			
			int what=intent.getIntExtra("what", -1);
			if (what==0){
				//OffloadingManager.actual_reg=true;  显然这个变量是跟显示者相关的,因此不用考虑
				
				OffloadingManager.registerHelperSensor();
			}
			else if (what==1){
				//OffloadingManager.actual_reg=false;
				
				OffloadingManager.unregisterHelperSensor();
			}
			else{
				Log.i("demo","出问题,要求注册注销,但是what值不对");
			}
		}
		else if (action.equals("android.intent.action.Ask_SwitchPower")){
			
			//TODO 重新写  注意搞完发送Ack_SwitchPower!
			int type=intent.getIntExtra("type", -1);
			int how=intent.getIntExtra("id", -1);
			int newc=intent.getIntExtra("controller", -1);
			int news=intent.getIntExtra("shower", -1);
			if (type==-1 || how==-1 || newc==-1 || news==-1){
				Log.i("demo","在AppReceiver中收到AskSwitchPower,但是有-1");
				return;
			}
			Log.i("demo","新的控制者是"+newc+",新的显示者是"+news);
			
			OffloadingManager.is_closing=true;	//注意,正在关闭  后面需要再搞为false
			
			switch(type){
			case 1:
				if (how==0){
					//原来控制加显示,现在控制
					((Activity)OffloadingManager.main_context).finish();
					
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));

					
				}
				else if (how==1){
					//原来没有,现在显示
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					
				
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));
				}
				break;
			case 2:
				if (how==0){
					//原来控制加显示,现在显示  我的策略:先关闭显示,设置权限,重开,即可
					
					((Activity)OffloadingManager.main_context).finish();
					
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));
					
				}
				else if (how==1){
					//1原来空,现在控制
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					//没有别的事情要做了
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));
					
				}
				break;
			case 3:
				if (how==0){
					//0不负责任何事情了
					((Activity)OffloadingManager.main_context).finish();
					
					
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//1全部都负责了(本身为空)		
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制和显示"));
				}
				
				break;
			case 4:
				if (how==0){
					//本身全部,后来空
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//原来空,现在是控制者
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));
				}
				else if (how==2){
					//原来空,现在是显示者
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));
				}
				
				break;
			case 5:
				if (how==0){
					//原来控制,现在还是控制
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备仍然负责控制"));
				}
				else if (how==1){
					//原来显示,现在没了
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if(how==2){
					//原来没有,现在显示
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));	
				}
				break;
			case 6:
				if (how==0){
					//原来控制,现在控制加显示
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制和显示"));	
				}
				else if (how==1){
					//原来显示,现在空
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				break;
			case 7:
				if (how==0){
					//原来控制,现在空
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));	
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//原来显示,现在控制加显示  跟上面2-0策略有一点像
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
		
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制加显示"));
					
				}
				
				
				break;
			case 8:
				if (how==0){
					//原来控制,现在空
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));	
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//原来显示,现在空
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==2){
					//原来空,现在全能
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制和显示"));
				}
				break;
				
			case 9:
				if (how==0){
					//原来控制,现在显示
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));
				}
				else if (how==1){
					//原来显示,现在控制
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));
					
				}
				
				break;
			case 10:
				if (how==0){
					//原来控制,现在显示
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;				
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));
				}
				else if (how==1){
					//原来显示,现在没有
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==2){
					//原来没有,现在控制
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;		
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));
				}
				
				break;
			case 11:
				if (how==0){
					//原来控制,现在没有
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//原来显示,现在控制
					((Activity)OffloadingManager.main_context).finish();
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));
					
				}
				else if (how==2){
					//原来没有,现在显示
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责显示"));	
				}
				break;
			case 12:
				if (how==0){
					//原来控制,现在没有
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备不再承担任务"));
					
					((Activity)OffloadingManager.proxy_context).finish();
					
				}
				else if (how==1){
					//原来显示,现在显示
					((Activity)OffloadingManager.main_context).finish();	//此时还是跟0号交互
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;	
					Intent itt=new Intent(context,mc);
					context.startActivity(itt);	
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备仍然负责显示"));
					
				}
				else if (how==2){
					//原来没有,现在控制
					OffloadingManager.controller=newc;
					OffloadingManager.shower=news;		
					ProxyActivity.myhander.sendMessage(generateMessage("正在offloading,本设备负责控制"));
					
				}
				
				break;
			default:
				break;
			}
			
			OffloadingManager.is_closing=false;
			
			//通知平台可以更新了
			Intent it_notify=new Intent();
			it_notify.setAction("android.intent.action.Ack_SwitchPower");
			OffloadingManager.broadcast_tct.sendBroadcast(it_notify);
			
			
		}
		else if (action.equals("android.intent.action_AskCloseShower")){
			//我是显示者,即将被关闭
			
			//关键是释放资源如何处理?
			OffloadingManager.is_self_closing=true;
			
			ProxyActivity.myhander.sendMessage(generateMessage("控制者退出,本控制也将退出"));	
			
			((Activity)OffloadingManager.main_context).finish();
			((Activity)OffloadingManager.proxy_context).finish();
			
			
			
			OffloadingManager.is_self_closing=false;
			
			OffloadingManager.controller=222;
			OffloadingManager.shower=333;
			
			
			
		}
		else if (action.equals("android.intent.action_AskOpenShower")){
			//我是控制者,即将显示
			
			OffloadingManager.unregisterHelperSensor();
			
			OffloadingManager.shower=OffloadingManager.controller;
			
			Intent itt=new Intent(context,mc);
			context.startActivity(itt);
			ProxyActivity.myhander.sendMessage(generateMessage("显示者退出,本控制设备成为显示者"));	
			
		}
		
	}
	

	
	private Message generateMessage(String info){
		Message msg=new Message();
		msg.what=0;
		msg.obj=info;
		return msg;
	}

}
