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
			//s1 s2 s3 ��������ά��
	
		//5�������� ����Ȩת����ذ�  ����Ҫ�ȷ���server ������2,3,4һ���Լ���
			//������������,ʹ��i1,i2,i3,i4����oldc,olds,newc,news
			//�����������ͻ���ƽ̨��,i1��ʾ����,i2��ʾ�Լ������(��xls�ļ�,��0,1,2����),i3,i4�ֱ��ʾnewc,news
	
		// 6���� ǿ�йر����
			// i1���� ���͸���������
				//0 ������+��ʾ�� �رտ�����,һͬ�ر���ʾ��
				//1 ������+��ʾ�� һ��ر���
				//2 �����������ر�
				//3 ��ʾ�������ر�
			//i1���� ���͸��ͻ��˵�
				//0 ��ʾ��һͬ�ر�
				//1�����߼�����ʾ
	
	
	public int i1,i2,i3,i4,i5;
	public String s1,s2,s3,s4,s5;
	
	public int []i_array;
	public String []s_array;
}
