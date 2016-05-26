import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.shawnwu.oc.MyPackage;


public class WaitForPkg implements Runnable{
	private ObjectInputStream[] ois;
	private ObjectOutputStream []oos;
	private int what;
	
	/** 
	���ڽ������Կͻ���ƽ̨����Ϣ<br>
	0 --������Ϣ���<br> 
	1 --�����Ӧ��<br> 
		----����ֵ��i1��,0�ɹ�,1ʧ��,2�Ѿ�ռ��<br>
	5 --Ȩ���л����<br>
	6 --������׼���ر�<br>
	
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
						//��Ҫ֪ͨ��ʾ��,һͬ�ر�
						int oldshower=MainClass.shower;
						MainClass.controller=-1;
						MainClass.shower=-1;
						
						MyPackage mp_c=new MyPackage();
						mp_c.type=6;
						mp_c.i1=0;
						MainClass.oos[oldshower].writeObject(mp_c);
						
					}
					else if (mp.i1==3){
						//��Ҫ֪ͨ������,������ʾ������
						
						MainClass.shower=MainClass.controller;
						
						MyPackage mp_c=new MyPackage();
						mp_c.type=6;
						mp_c.i1=1;
						MainClass.oos[MainClass.controller].writeObject(mp_c);
						
					}
					else{
						System.out.println("������,6�Ű���i1ֵ��Χ��");
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
		
		//һ����12�����
		if (oldc==olds && newc==oldc && news!=olds){
			//case 1
			mp_r.i1=1;
			mp_r.i3=newc;mp_r.i4=news;
			
			try {
				mp_r.i2=0;
				oos[newc].writeObject(mp_r);		//��֪ͨ�ɵ�ȫ��,�µĿ�����
				Thread.currentThread().sleep(500);
				mp_r.i2=1;
				oos[news].writeObject(mp_r);		//��֪ͨ�µ���ʾ��
				
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
				oos[newc].writeObject(mp_r);		//��֪ͨ�µĿ�����
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[news].writeObject(mp_r);		//��֪ͨ�ɵ�ȫ��,�µ���ʾ��
				
				
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
				oos[oldc].writeObject(mp_r);		//��֪ͨ�ɵ�ȫ��,�µĿ�
				//Thread.currentThread().sleep(500);
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);		//��֪ͨ�µĿ��Ƽ���ʾ��

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
				oos[oldc].writeObject(mp_r);		//��֪ͨ�ɵ�ȫ��,�µĿ�
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);		//��֪ͨ�µĿ�����
				Thread.currentThread().sleep(500);
				mp_r.i2=2;
				oos[news].writeObject(mp_r);		//���֪ͨ�µ���ʾ��

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
				oos[olds].writeObject(mp_r);	//��֪ͨ�ɵ���ʾ��
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//֪ͨ���µĿ�����
				Thread.currentThread().sleep(500);
				mp_r.i2=2;
				oos[news].writeObject(mp_r);	//֪ͨ�µ���ʾ��

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
				oos[olds].writeObject(mp_r);	//��֪ͨ�ɵ���ʾ��,�µĿ�
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//֪ͨ�ɵĿ�����,�µĿ�����ʾ��
				

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
			
			//ֱ����0�رմ�����,1�л�������ģʽ����
			//����Ƚϸ���һ��
			//���⴦��
			
			try {
				
				mp_r.i2=1;
				oos[newc].writeObject(mp_r);	//֪ͬ�ɵ���ʾ��,�µ�ȫ��,ֱ���л�
				Thread.currentThread().sleep(500);
				
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//֪ͨ�ɵĿ�����,�µĿ�,ֱ��ע��
				



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
				oos[olds].writeObject(mp_r);	//��֪ͨ�ɵ���ʾ��,�µĿ�
				Thread.currentThread().sleep(500);
				

				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//֪ͨ�ɵĿ�����,�µĿ�
				
				mp_r.i2=2;
				oos[newc].writeObject(mp_r);	//֪ͨ�ɵĿ�,�µ���ʾ������

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
				oos[newc].writeObject(mp_r);	//��֪ͨ�ɵ���ʾ��,�µĿ�����
				Thread.currentThread().sleep(500);
				mp_r.i2=0;
				oos[news].writeObject(mp_r);	//֪ͨ�ɵĿ�����,�µ���ʾ��
				
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
				oos[newc].writeObject(mp_r);	//��֪ͨ�ɵĿ�,�µĿ�����
				
				mp_r.i2=1;
				oos[olds].writeObject(mp_r);	//֪ͨ�ɵ���ʾ��,�µĿ�
				Thread.currentThread().sleep(500);
				
				mp_r.i2=0;
				oos[news].writeObject(mp_r);	//֪ͨ�ɵĿ�����,�µ���ʾ��
				
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
				oos[newc].writeObject(mp_r);	//��֪ͨ�ɵ���ʾ��,�µĿ�����
				
				Thread.currentThread().sleep(500);
				
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//֪ͨ�ɵĿ�����,�µĿ�
	
				mp_r.i2=2;
				oos[news].writeObject(mp_r);	//֪ͨ�ɵĿ�,�µ���ʾ��
				
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
				oos[newc].writeObject(mp_r);	//֪ͨ�ɵĿ�,�µĿ�����
				Thread.currentThread().sleep(500);
				
				mp_r.i2=1;
				oos[news].writeObject(mp_r);	//֪ͨ�ɵ���ʾ��,�µ���ʾ��
												
				
				mp_r.i2=0;
				oos[oldc].writeObject(mp_r);	//��֪ͨ�ɵĿ�����,�µĿ�   ֱ��ע�������� 
				
			
				
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
