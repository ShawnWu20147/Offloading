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
		//下一句应该是插桩的,如果不允许触控,那么应该什么事都不做
		if (OffloadingManager.myid!=OffloadingManager.shower) return true;	//我甚至不是显示者...似乎是一个永假判断
		//clearly, myid==shower
		
		if (OffloadingManager.agree_touch==false && OffloadingManager.myid!=OffloadingManager.controller) return true;
		//如果我不是控制者,同时agreetouch=false 则表明来的这个点是没有意义的
		
		if (OffloadingManager.myid==OffloadingManager.controller && event.getAction()==517 && event.getPointerCount()==3){
			//我是控制者, 这里要呼出菜单
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
