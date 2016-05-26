package com.shawnwu.oc;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import javax.xml.datatype.Duration;

import org.apache.http.conn.util.InetAddressUtils;




import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	Button sets,opena,pull;
	TextView tip;
	
	Button debug;
	
	private static final int DEBUG=1;
	
	public static Context ct=null;
	
	public static String PACKAGE_NAME="cn.gm.doj";
	public static String MAIN_ACT_NAME="cn.gm.doj.MrGame";
	
	
	public static volatile boolean recv_ack=false;
	public static MyPackage recv_pkg=null;
	
	
	public static int controller,shower;		//0,1,2
	public static int myid;						//0,1,2
	public static Socket []sk;					//0,1,2 surely there is a NULL
	public static ObjectInputStream []ois_sk;	//0,1,2 surely there is a NULL
	public static ObjectOutputStream []oos_sk;	//0,1,2 surely there is a NULL
	
	public static String []sip;
	
	public static Socket conn_server=null;
	public static ObjectInputStream ois_server;
	public static ObjectOutputStream oos_server;
	
	
	public static boolean configure_done=false;
	public static boolean start_enabled=false;
	
	
	public static Handler myhandler;
	
	/*
	public static Socket conn_app=null;
	public static ObjectInputStream ois_app=null;
	public static ObjectOutputStream oos_app=null;
	*/
	
	
	private PlatformReceiver pr;
	
	private Message generateMessage(String info){
		Message msg=new Message();
		msg.what=0;
		msg.obj=info;
		return msg;
	}
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		configure_done=false;
		
		
		setTitle(getTitle()+":"+getLocalHostIp());
		
		Log.i("shawnwu","Create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		sets=(Button)findViewById(R.id.sets);
		opena=(Button)findViewById(R.id.opena);
		pull=(Button)findViewById(R.id.pull);
		tip=(TextView)findViewById(R.id.tip);
		
		opena.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				
				if (!configure_done){
					myhandler.sendMessage(generateMessage("配置工作没有完成,无法启动应用"));
					return;
				}
				
				startGame();
				
				
				
				
				
				
				
				
				
				
			}

			
		});
		
		pull.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (configure_done){
					myhandler.sendMessage(generateMessage("配置信息已经拉取,无需再拉"));
					return;
				}
				Thread tr=
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							
							Log.i("shawnwu",tip.getText().toString());
							
							conn_server=new Socket(tip.getText().toString(),3456);
							
							ois_server=new ObjectInputStream(conn_server.getInputStream());
							oos_server=new ObjectOutputStream(conn_server.getOutputStream());
							
							MyPackage mp=(MyPackage) ois_server.readObject();
							
							
							assert(mp.type==0);
							
							PACKAGE_NAME=mp.s4;
							MAIN_ACT_NAME=mp.s5;
							
							
							sip=new String[3];
							sip[0]=mp.s1;
							sip[1]=mp.s2;
							sip[2]=mp.s3; 
							for (int i=0;i<3;i++){
								if (sip[i].equals(getLocalHostIp())){
									myid=i;
									break;
								}
							}
							
							sk[myid]=null;
							
							switch(myid){
							case 0:
								HashSet<String> hs=new HashSet<String>();
								hs.add(sip[1]);
								hs.add(sip[2]);
								ServerSocket ss_12=new ServerSocket(5555);
								while (hs.size()>0){
									Socket ts=ss_12.accept();
									String fullname=ts.getRemoteSocketAddress().toString();
									String fullip=fullname.split(":")[0].substring(1);
									hs.remove(fullip);
									
									if (fullip.equals(sip[1])){
										sk[1]=ts;
										oos_sk[1]=new ObjectOutputStream(sk[1].getOutputStream());
										ois_sk[1]=new ObjectInputStream(sk[1].getInputStream());
									}
									if (fullip.equals(sip[2])){
										sk[2]=ts;
										oos_sk[2]=new ObjectOutputStream(sk[2].getOutputStream());
										ois_sk[2]=new ObjectInputStream(sk[2].getInputStream());
									}
									
								}
								break;
								//now I hold sk[0],sk[1] and sk[2]
								
							case 1:
								hs=new HashSet<String>();
								hs.add(sip[2]);
								
								ServerSocket ss_2=new ServerSocket(5555);
								while (hs.size()>0){
									Socket ts=ss_2.accept();
									String fullname=ts.getRemoteSocketAddress().toString();
									String fullip=fullname.split(":")[0].substring(1);
									hs.remove(fullip);
									
									if (fullip.equals(sip[2])){
										sk[2]=ts;
										oos_sk[2]=new ObjectOutputStream(sk[2].getOutputStream());
										ois_sk[2]=new ObjectInputStream(sk[2].getInputStream());
									}
									
								}
								
								sk[0]=new Socket(sip[0],5555);
								oos_sk[0]=new ObjectOutputStream(sk[0].getOutputStream());
								ois_sk[0]=new ObjectInputStream(sk[0].getInputStream());
								break;
								
								
							case 2:
								sk[0]=new Socket(sip[0],5555);
								oos_sk[0]=new ObjectOutputStream(sk[0].getOutputStream());
								ois_sk[0]=new ObjectInputStream(sk[0].getInputStream());
								
								sk[1]=new Socket(sip[1],5555);
								oos_sk[1]=new ObjectOutputStream(sk[1].getOutputStream());
								ois_sk[1]=new ObjectInputStream(sk[1].getInputStream());
								
								break;
								
							default:
									
							}
							
							configure_done=true;
							for (int ii=0;ii<3;ii++){
								if (ii==myid) continue;
								new Thread(new SocketReceiver(ois_sk[ii], oos_sk[ii],ii)).start();
								//每2个设备之间建立监听通道
							}
							
							
							new Thread(new ServerReceiver(ois_server, oos_server)).start();
							//为平台和服务端之间建立监听通道,注意此时0号包已经不可能再发了!
							
							myhandler.sendMessage(generateMessage("配置信息拉取成功,各设备连接建立成功"));
						
							
							
							
							
						} catch (UnknownHostException e) {
							Log.i("shawnwu",e.getLocalizedMessage());
						} catch (IOException e) {
							Log.i("shawnwu",e.getLocalizedMessage());
						} catch (ClassNotFoundException e) {
							Log.i("shawnwu",e.getLocalizedMessage());
						}
						
					}
				});
				
				tr.start();
				try {
					tr.join();
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				
				//if(configure_done) Log.i("shawnwu","load info succ");
				
			}
		});
		
		
		sets.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				final EditText et = new EditText(MainActivity.this);
				et.setText(tip.getText());
				final AlertDialog.Builder ab=new AlertDialog.Builder(MainActivity.this)  
				.setTitle("请输入服务器的IP地址")  
				.setView(et)  
				.setPositiveButton("确认",new DialogInterface.OnClickListener() { 
		            @Override 
		            public void onClick(DialogInterface dialog, int which) {
		            	tip.setText(et.getText().toString());
						
		            } 
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() { 
		            @Override 
		            public void onClick(DialogInterface dialog, int which) {
		            	
						
		            } 
				});
				 ab.show();	
			}
		});
		
		
		if (DEBUG==1){
			debug=(Button)findViewById(R.id.debug);
			debug.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					Intent open=new Intent();
					
					/*
					open.setAction("android.intent.action.Ask_OpenApp");
					open.putExtra("controller", 1);
					open.putExtra("shower", 1);
					open.putExtra("myid",1);
					sendBroadcast(open);
					*/
					
			
				}
			});
		}
		
		
		myhandler=new Handler(){
			@Override
	          public void handleMessage(Message msg) { 
				switch(msg.what){
				case 0:
					String show=(String) msg.obj;
					Toast.makeText(getApplicationContext(), show, Toast.LENGTH_SHORT).show();
				default:
					//
				}
			}
			
		};
		
		sk=new Socket[3];
		oos_sk=new ObjectOutputStream[3];
		ois_sk=new ObjectInputStream[3];
		
		
		
		pr=new PlatformReceiver();
		IntentFilter filter = new IntentFilter();  
		//     filter.addAction("android.intent.action.Request_StartGame");  //for starting game           no need now!
		filter.addAction("android.intent.action.Request_SwitchPower"); //for offloading
		filter.addAction("android.intent.action.Request_ExitGame");	//only for controller
		filter.addAction("android.intent.action.TellRUSensor");
		filter.addAction("android.intent.action.TellPoint");
		filter.addAction("android.intent.action.TellSensorData");
		filter.addAction("android.intent.action.Ack_SwitchPower");
		
		filter.addAction("android.intent.action.ForceQuit");
		
		registerReceiver(pr, filter);
				
		
		
	}

	@Override
	protected void onStart() {
		Log.i("shawnwu","Start");
		super.onStart();
	}

	@Override
	protected void onRestart() {
		Log.i("shawnwu","Restart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		ct=this;
		Log.i("shawnwu","Resume");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i("shawnwu","Pause");
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.i("shawnwu","Start");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.i("shawnwu","Destroy");
		
		unregisterReceiver(pr);	//important
		
		super.onDestroy();
	}

	public String getLocalHostIp(){
		String ipaddress="";
		try{
			Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()){
				NetworkInterface nif=e.nextElement();
				Enumeration<InetAddress> ei=nif.getInetAddresses();
				while (ei.hasMoreElements()){
					InetAddress ip=ei.nextElement();
					if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress())){
						ipaddress=ip.getHostAddress();
						return ipaddress;
					}
				}
			}
		}
		catch(Exception e){
		}
		return ipaddress;
	}
	
	
	@Override
	public void onBackPressed() {
		
		new AlertDialog.Builder(MainActivity.this)   
		.setTitle("确定要退出客户端平台吗(真的不再使用了?!!)")  
		.setMessage("请确保所有工作已完成")  
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
	
	
	private void startGame() {
		Thread tr=
			new Thread(new Runnable() {
					
				@Override
				public void run() {
						MyPackage mp=new MyPackage();
						mp.type=1;
						mp.i1=myid;
						
						try {
							oos_server.writeObject(mp);
							
							recv_ack=false;
							while (!recv_ack);
							
							recv_ack=false;
							
							MyPackage mp_ack=recv_pkg;
							
							//assert(mp_ack.type==1);
							
							
							MainActivity.controller=mp_ack.i2;
							MainActivity.shower=mp_ack.i3;
							
							int answer=mp_ack.i1;
							if (answer==0){
								
								//myhandler.sendMessage(generateMessage("成功打开应用"));
								//似乎应该app发送广播给platform来输出
								start_enabled=true;
							}
							else if (answer==1){
								//myhandler.sendMessage(generateMessage("别人已经打开应用,打开失败"));
								start_enabled=false;
							}
							else if (answer==2){
								//myhandler.sendMessage(generateMessage("你已经打开了应用"));
								start_enabled=true;
							}
							
							//客户端app启动时发送广播,然后广播告知结果!!!!
							//注意conn_app的建立时间!!!!
							
							
							
						} catch (IOException e) {
							e.printStackTrace();
						} 
						
						
				}
			});
		
		tr.start();
		try {

			tr.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		
		

		//打开应用后,app的广播接收也就注册了
		
		//moveTaskToBack(true);
		
		if (start_enabled==true){
			Intent intent = new Intent(Intent.ACTION_MAIN);  
			intent.addCategory(Intent.CATEGORY_LAUNCHER);   
			ComponentName cn = new ComponentName(PACKAGE_NAME, MAIN_ACT_NAME);              
			intent.setComponent(cn);  
			startActivity(intent); 
			gotoFront();
			
			
			
			
			Intent open=new Intent();
			open.setAction("android.intent.action.Ask_OpenApp");
			open.putExtra("controller", MainActivity.controller);
			open.putExtra("shower", MainActivity.shower);
			open.putExtra("myid", MainActivity.myid);
			sendBroadcast(open);
			
			//myhandler.sendMessage(generateMessage("应用已启动!"));
			
			//此时功能和显示都在本机,是否要做别的事情?
			
			start_enabled=false;	//TODO 是否需要?
			
			
		}
		else{
			myhandler.sendMessage(generateMessage("别的设备已经开启此应用,暂时无法使用应用功能!"));
		}
		
		
		
		
	}
	
	public void gotoFront(){
		ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> recentList = am.getRunningTasks(30);
        for (RunningTaskInfo info : recentList) {
            if (info.topActivity.getPackageName().equals(PACKAGE_NAME)) {
                am.moveTaskToFront(info.id, 0);
                return;
            }
        }
	}

	
}
