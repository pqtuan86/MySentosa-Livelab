package com.mysentosa.android.sg.map.custom_views;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/**
 * 
 * @author Viesturs Zarins
 * 
 *         This class draws a path line in given color.
 */
public class CustomPathOverlay extends Overlay {
	// ===========================================================
	// Fields
	// ===========================================================
	private int TRANSPARENCY = 140;
	
	public static class PathPoint extends Point {
		int color = Color.BLUE;
		public PathPoint(int latE6, int longE6, int color) {
			super(latE6,longE6);
			this.color = color;
		}
		public int getColor() {
			return color;
		}
	}
	
	
	/**
	 * Stores color for points
	 */
	int currentColor = Color.BLUE;
	
	/**
	 * Stores points, converted to the map projection.
	 */
	private ArrayList<PathPoint> mPoints;

	/**
	 * Number of points that have precomputed values.
	 */
	private int mPointsPrecomputed;

	/**
	 * Paint settings.
	 */
	protected Paint mPaint = new Paint();
	
	private final Path mPath = new Path();

	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();


	// ===========================================================
	// Constructors
	// ===========================================================

	public CustomPathOverlay(final Context ctx) {
		super(new DefaultResourceProxyImpl(ctx));
		this.mPaint.setColor(Color.BLUE);
		this.mPaint.setStrokeWidth(7.0f);
		this.mPaint.setStyle(Paint.Style.STROKE);
		this.clearPath();
	}


	// ===========================================================
	// Getter & Setter
	// ===========================================================

	private void clearPath() {
		this.mPoints = new ArrayList<PathPoint>();
		this.mPointsPrecomputed = 0;
		this.currentColor = Color.BLUE;
	}
	
	public void setStrokeWidth(float f) {
		this.mPaint.setStrokeWidth(f);
	}
	
	public void setTransparency(int alpha) {
		this.TRANSPARENCY = alpha;
	}
	
	public void setPoints(ArrayList<PathPoint> points) {
		clearPath();
		this.mPoints.addAll(points);
	}


	/**
	 * This method draws the line. Note - highly optimized to handle long paths, proceed with care.
	 * Should be fine up to 10K points.
	 */
	@Override
	protected void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {

		if (shadow) {
			return;
		}

		if (this.mPoints.size() < 2) {
			// nothing to paint
			return;
		}

		final Projection pj = mapView.getProjection();

		// precompute new points to the intermediate projection.
		final int size = this.mPoints.size();

		while (this.mPointsPrecomputed < size) {
			final Point pt = this.mPoints.get(this.mPointsPrecomputed);
			pj.toMapPixelsProjected(pt.x, pt.y, pt);

			this.mPointsPrecomputed++;
		}

		Point screenPoint0 = null; // points on screen
		Point screenPoint1 = null;
		Point projectedPoint0; // points from the points list
		Point projectedPoint1;

		mPath.rewind();
		projectedPoint0 = this.mPoints.get(size - 1);
		
		//set initial point and move to it
		screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
		mPath.moveTo(screenPoint0.x, screenPoint0.y);
		
		//set initial color and set it as the paint color
		currentColor = this.mPoints.get(size-2).getColor();
		this.mPaint.setColor(currentColor);
		
		for (int i = size - 2; i >= 0; i--) {
			// compute next points
			projectedPoint1 = this.mPoints.get(i);
			
			// the starting point may be not calculated, because previous segment was out of clip
			// bounds. but we are not using clip bounds any more. kept for reference
			//            if (screenPoint0 == null) {
			//                    screenPoint0 = pj.toMapPixelsTranslated(projectedPoint0, this.mTempPoint1);
			//                    mPath.moveTo(screenPoint0.x, screenPoint0.y);
			//            }
			
			screenPoint1 = pj.toMapPixelsTranslated(projectedPoint1, this.mTempPoint2);
			
			mPath.lineTo(screenPoint1.x, screenPoint1.y);
			
			// update starting point to next position
            projectedPoint0 = projectedPoint1;
            screenPoint0.x = screenPoint1.x;
            screenPoint0.y = screenPoint1.y;
			
			if(i>0 && !(this.mPoints.get(i-1).getColor()==currentColor)) {
				this.mPaint.setAlpha(this.TRANSPARENCY);
				canvas.drawPath(mPath, this.mPaint);
				mPath.rewind();
				mPath.moveTo(screenPoint1.x, screenPoint1.y);
				if(i>0) {
					currentColor = this.mPoints.get(i-1).getColor();
					this.mPaint.setColor(currentColor);
				}
			}

		}
		
		this.mPaint.setAlpha(this.TRANSPARENCY);
		canvas.drawPath(mPath, this.mPaint);
	}
	
}
