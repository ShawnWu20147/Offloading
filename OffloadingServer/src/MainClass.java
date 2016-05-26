import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

import com.shawnwu.oc.MyPackage;


public class MainClass {
	
	public static int controller=-1;
	public static int shower=-1;
	public static Socket []sk=new Socket[3];
	
	public static ObjectInputStream []ois=new ObjectInputStream[3];
	public static ObjectOutputStream []oos=new ObjectOutputStream[3];
	
	
	
	public static final String []ips=new String[]{
		"114.212.87.129","114.212.56.45","114.212.34.45"
	};
	
	//public static String PACKAGE_NAME="cn.gm.doj";
	//public static String MAIN_ACT_NAME="cn.gm.doj.MrGame";
	
	public static String PACKAGE_NAME="com.shawnwu.offloadingdemo";
	public static String MAIN_ACT_NAME="com.shawnwu.offloadingdemo.ProxyActivity";
	
	
	public static void main(String[] args) throws Exception{
		
		System.out.println("start working:");
		
		ServerSocket ss=new ServerSocket(3456);
		
		HashSet<String> left=new HashSet<String>();

		for (String s:ips) left.add(s);
		
		MyPackage mp=new MyPackage();
		mp.type=0;
		mp.s1=ips[0];
		mp.s2=ips[1];
		mp.s3=ips[2];
		
		mp.s4=PACKAGE_NAME;
		mp.s5=MAIN_ACT_NAME;
		
		
		while (left.size()>0){
		
		
			Socket s=ss.accept();
			
			String fullname=s.getRemoteSocketAddress().toString();
			String fullip=fullname.split(":")[0].substring(1);
			
			System.out.println(fullip);
			
			left.remove(fullip);
			
			if (fullip.equals(ips[0])){
				sk[0]=s;
				ois[0]=new ObjectInputStream(sk[0].getInputStream());
				oos[0]=new ObjectOutputStream(sk[0].getOutputStream());
				
				oos[0].writeObject(mp);
				
			}
			else if (fullip.equals(ips[1])){
				sk[1]=s;
				ois[1]=new ObjectInputStream(sk[1].getInputStream());
				oos[1]=new ObjectOutputStream(sk[1].getOutputStream());
				
				oos[1].writeObject(mp);
			}
			else if (fullip.equals(ips[2])){
				sk[2]=s;
				ois[2]=new ObjectInputStream(sk[2].getInputStream());
				oos[2]=new ObjectOutputStream(sk[2].getOutputStream());
				
				oos[2].writeObject(mp);
				
			}
			else System.out.println("meet a unknown device, ignore it");
			
		}
		
		//now send three devices info
		new Thread(new WaitForPkg(ois, oos, 0)).start();
		new Thread(new WaitForPkg(ois, oos, 1)).start();
		new Thread(new WaitForPkg(ois, oos, 2)).start();
		while (true){
			
		}
		
	}
}
