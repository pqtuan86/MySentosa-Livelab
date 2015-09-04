// Created by plusminus on 21:46:22 - 25.09.2008
package com.mysentosa.android.sg.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;

import com.mysentosa.android.sg.R;

public class DrawableUtils {
	
	public static BitmapDrawable numberedPathDirectionDrawable(Resources r, int number){

        Bitmap bm = BitmapFactory.decodeResource(r, R.drawable.path_direction_marker).copy(Bitmap.Config.ARGB_8888, true);

        Paint paint = new Paint(); 
        paint.setStyle(Style.FILL);  
        paint.setColor(Color.BLACK); 
        paint.setTextSize(25); 
        paint.setFakeBoldText(true);
        paint.setTextAlign(Align.CENTER); 

        Canvas canvas = new Canvas(bm);
//        canvas.drawText(String.valueOf((char) (64+number)), bm.getWidth()/2f, bm.getHeight()/2.2f, paint);
        canvas.drawText(""+number, bm.getWidth()/2f, bm.getHeight()/2.2f, paint);

        return new BitmapDrawable(bm);
    }

}
