package com.shawnwu.oc;

import java.io.Serializable;

public class MyPackage implements Serializable{
	public int type;
		//0 used for  configuration pull
		//1 used for  startGame request
	
		//2 used for sensor_reg_unreg
			// i1 reg_or_unreg
			// i2 what_sensor
	
		//3 used for touch_point
			//s1 pointX
			//s2 pointY
	
		//4 used for sensory data
			//i1 what_type
			//s1 s2 s3 传感器三维度
	
		//5往后用于 控制权转移相关吧  这种要先发给server 不能像2,3,4一样自己做
			//发给服务器的,使用i1,i2,i3,i4表明oldc,olds,newc,news
			//服务器发给客户端平台的,i1表示类型,i2表示自己的身份(见xls文件,有0,1,2三种),i3,i4分别表示newc,news
	
		// 6用于 强行关闭相关
			// i1类型 发送给服务器的
				//0 控制者+显示者 关闭控制者,一同关闭显示者
				//1 控制者+显示者 一起关闭了
				//2 控制者主动关闭
				//3 显示者主动关闭
			//i1类型 发送给客户端的
				//0 显示者一同关闭
				//1控制者加上显示
	
	
	public int i1,i2,i3,i4,i5;
	public String s1,s2,s3,s4,s5;
	
	public int []i_array;
	public String []s_array;
}
