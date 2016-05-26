import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.shawnwu.oc.MyPackage;


public class WaitForPkg implements Runnable{
	private ObjectInputStream[] ois;
	private ObjectOutputStream []oos;
	private int what;
	
	/** 
	用于接收来自客户端平台的消息<br>
	0 --配置信息相关<br> 
	1 --请求打开应用<br> 
		----返回值在i1里,0成功,1失败,2已经占有<br>
	5 --权限切换相关<br>
	6 --控制器准备关闭<br>
	
	*/ 
	public WaitForPkg(ObjectInputStream[] ois,ObjectOutputStream[] oos,int what){
		this.ois=ois;
		this.oos=oos;
		this.what=what;
	}

	@Override
	public void run() {
		while (true){
			try {
				MyPackage mp=(MyPackage) ois[what].readObject();
				
				assert(mp.type!=0);
				
				switch(mp.type){
				case 1:
					int who=mp.i1;
					assert(what==who);
					
					MyPackage mp_ack=new MyPackage();
					mp_ack.type=1;
					
					
					if (MainClass.controller==-1){
						mp_ack.i1=0;
						MainClass.controller=who;
						MainClass.shower=who;
					}
					else if (MainClass.controller==who){
						mp_ack.i1=2;
					}
					else{
						mp_ack.i1=1;
					}
					
					mp_ack.i2=MainClass.controller;
					mp_ack.i3=MainClass.shower;
					
					
					oos[what].writeObject(mp_ack);
					break;
					
					
				case 5:
					handleOnePkg(mp);
					break;
				case 6:
					if (mp.i1==0 || mp.i1==1){
						MainClass.controller=-1;
						MainClass.shower=-1;
					}
					else if (mp.i1==2){
						//需要通知显示者,一同关闭
						int oldshower=MainClass.shower;
						MainClass.controller=-1;
						MainClass.shower=-1;
						
						MyPackage mp_c=new MyPackage();
						mp_c.type=6;
						mp_c.i1=0;
						MainClass.oos[oldshower].writeObject(mp_c);
						
					}
					else if (mp.i1==3){
						//需要通知控制者,加上显示的能力
						
						MainClass.shower=MainClass.controller;
						
						MyPackage mp_c=new MyPackage();
						mp_c.type=6;
						mp_c.i1=1;
						MainClass.oos[MainClass.controller].writeObject(mp_c);
						
					}
					else{
						System.out.println("出问题,6号包的i1值范围错");
					}
					break;
				case 3:
					break;
				case 4:
					break;
				default:
						
				}
				
				
				
			} catch (ClassNotFoundException | IOException e) {
				
				e.printStackTrace();
			}
		}
		
	}
	
	public void handleOnePkg(MyPackage mp){
		assert(mp.type==5);
		int oldc=mp.i1;
		int olds=mp.i2;
		int newc=mp.i3;
		int news=mp.i4;
		
		MyPackage mp_r=new MyPackage();
		mp_r.type=5;
		
		//一共有12种情况
		if (oldc==olds && newc==oldc && news!=olds){
			//case 1
			mp_r.i1=1;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=0;
				oos[newc].writeObject(mp_r);		//先通知旧的全能,新的控制者
				Thread.currentThread().sleep(500);
				mp_r.i2=1;
				oos[news].writeObject(mp_r);		//再通知新的显示者
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		else if (oldc==olds && news==oldc && newc!=oldc){
			//case 2
			mp_r.i1=2;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);		//先通知新的控制者
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[news].writeObject(mp_r);		//再通知旧的全能,新的显示者
				
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			
			
		}
		else if (oldc==olds && news==newc && newc!=oldc){
			//case 3
			mp_r.i1=3;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);		//先通知旧的全能,新的空
				//Thread.currentThread().sleep(500);
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);		//再通知新的控制兼显示者

			} catch (IOException e) {
				e.printStackTrace();
			} 
			
			
		}
		else if (oldc==olds && newc!=oldc && news!=olds && newc!=news){
			//case 4
			mp_r.i1=4;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);		//先通知旧的全能,新的空
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);		//再通知新的控制者
				Thread.currentThread().sleep(500);
				mp_r.i2=2;
				oos[news].writeObject(mp_r);		//最后通知新的显示者

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		else if (oldc!=olds && newc==oldc && news!=newc && news!=olds){
			//case 5
			mp_r.i1=5;
			mp_r.i3=newc;mp_r.i4=news;

			try {
				mp_r.i2=1;
				oos[olds].writeObject(mp_r);	//先通知旧的显示者
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//通知旧新的控制者
				Thread.currentThread().sleep(500);
				mp_r.i2=2;
				oos[news].writeObject(mp_r);	//通知新的显示者

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			
			
		}
		else if (oldc!=olds && newc==oldc && newc==news){
			//case 6
			mp_r.i1=6;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=1;
				oos[olds].writeObject(mp_r);	//先通知旧的显示者,新的空
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//通知旧的控制者,新的控制显示者
				

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			
			
		}
		else if (oldc!=olds && newc==olds && newc==news){
			//case 7
			mp_r.i1=7;
			mp_r.i3=newc;mp_r.i4=news;
			
			//直接让0关闭传感器,1切换到传感模式即可
			//这个比较复杂一点
			//特殊处理
			
			try {
				
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);	//同知旧的显示者,新的全能,直接切换
				Thread.currentThread().sleep(500);
				
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//通知旧的控制者,新的空,直接注销
				



			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		else if (oldc!=olds && newc!=oldc && newc!=olds && newc==news){
			//case 8
			mp_r.i1=8;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=1;
				oos[olds].writeObject(mp_r);	//先通知旧的显示者,新的空
				Thread.currentThread().sleep(500);
				

				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//通知旧的控制者,新的空
				
				mp_r.i2=2;
				oos[newc].writeObject(mp_r);	//通知旧的空,新的显示控制者

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
		}
		else if (oldc!=olds && newc==olds && news==oldc){
			//case 9
			mp_r.i1=9;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);	//先通知旧的显示者,新的控制者
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[news].writeObject(mp_r);	//通知旧的控制者,新的显示者
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		else if (oldc!=olds && news==oldc && newc!=oldc && newc!=olds){
			//case 10
			mp_r.i1=10;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=2;
				oos[newc].writeObject(mp_r);	//先通知旧的空,新的控制者
				
				mp_r.i2=1;
				oos[olds].writeObject(mp_r);	//通知旧的显示者,新的空
				Thread.currentThread().sleep(500);
				
				mp_r.i2=0;
				oos[news].writeObject(mp_r);	//通知旧的控制者,新的显示者
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
		}
		else if (oldc!=olds && newc==olds && news!=oldc && news!=olds){
			//case 11
			mp_r.i1=11;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);	//先通知旧的显示者,新的控制者
				
				Thread.currentThread().sleep(500);
				
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//通知旧的控制者,新的空
	
				mp_r.i2=2;
				oos[news].writeObject(mp_r);	//通知旧的空,新的显示者
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
			
			
			
		}
		else if (oldc!=olds && news==olds && newc!=oldc && newc!=olds){
			//case 12
			mp_r.i1=12;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				
				mp_r.i2=2;
				oos[newc].writeObject(mp_r);	//通知旧的空,新的控制者
				Thread.currentThread().sleep(500);
				
				mp_r.i2=1;
				oos[news].writeObject(mp_r);	//通知旧的显示者,新的显示者
												
				
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//先通知旧的控制者,新的空   直接注销传感器 
				
			
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			
		}
		
		MainClass.controller=newc;
		MainClass.shower=news;
		
		
	}

}
