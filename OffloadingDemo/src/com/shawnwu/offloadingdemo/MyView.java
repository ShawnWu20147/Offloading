package com.shawnwu.offloadingdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyView extends View {

	public float currentX=40;
	public float currentY=50;
	
	Paint p=new Paint();
	Context ct;
	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.ct=context;
		
		OffloadingManager.touch_view=this;
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//��һ��Ӧ���ǲ�׮��,�����������,��ôӦ��ʲô�¶�����
		if (OffloadingManager.myid!=OffloadingManager.shower) return true;	//������������ʾ��...�ƺ���һ�������ж�
		//clearly, myid==shower
		
		if (OffloadingManager.agree_touch==false && OffloadingManager.myid!=OffloadingManager.controller) return true;
		//����Ҳ��ǿ�����,ͬʱagreetouch=false ����������������û�������
		
		if (OffloadingManager.myid==OffloadingManager.controller && event.getAction()==517 && event.getPointerCount()==3){
			//���ǿ�����, ����Ҫ�����˵�
			OffloadingManager.showDia(ct);
			return true; 
		}
		
		
		
		
		this.currentX=event.getX();
		this.currentY=event.getY();
		
		this.invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		p.setColor(Color.RED);
		canvas.drawCircle(currentX, currentY, 15, p);
	}
	
	
	
	
	

}
