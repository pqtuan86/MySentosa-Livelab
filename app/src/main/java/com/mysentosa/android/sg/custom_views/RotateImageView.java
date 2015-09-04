package com.mysentosa.android.sg.custom_views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RotateImageView extends ImageView {

	public RotateImageView(Context context) {
		super(context);
	}

	public RotateImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean isInEditMode() {
		// TODO Auto-generated method stub
		return super.isInEditMode();
	}

	// Used for SDK < 11
	private float mRotation = 0;
	private int centerX, centerY;
	
	
	@Override
	protected void onDraw(Canvas canvas) {
//		LogHelper.d(" testing"," testing ondraw");
		canvas.save();
		canvas.rotate(mRotation,centerX, centerY);
		super.onDraw(canvas);
		canvas.restore();
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		centerX = w/2;
		centerY = h/2;
	}

	public void rotateTo(float pieRotation) {
		mRotation = pieRotation;
		invalidate();
	}
	
	public void setRotation(float pieRotation) {
		mRotation = pieRotation;
	}
}