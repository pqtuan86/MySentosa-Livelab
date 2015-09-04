// Created by naveen (f yeah!) on 20:30:06 - 20.03.2012
package com.mysentosa.android.sg.map.custom_views;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.MotionEvent;

import com.mysentosa.android.sg.R;

public class CustomBalloonOverlay extends Overlay {

	public static final int cDESCRIPTION_BOX_PADDING = 15;
	public static final int cTEXT_CENTER_PADDING = 2;
	public static final int cDESCRIPTION_BOX_CORNERWIDTH = 8;
	public static final int cCONST_DESCRIPTION_LINE_HEIGHT = 12, cDESCRIPTION_FONT_SIZE = 12;
	public static final int cDESCRIPTION_LINE_HEIGHT = 0;
	public static final int cCONST_DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 14, cTITLE_FONT_SIZE = 12;
	protected static int cDESCRIPTION_MAXWIDTH = 175;
	
	public static int TEXT_CENTER_PADDING = 4;
	public static int DESCRIPTION_BOX_PADDING = 15;
	public static int DESCRIPTION_BOX_CORNERWIDTH = 8;
	public static int CONST_DESCRIPTION_LINE_HEIGHT = 14, DESCRIPTION_FONT_SIZE = 14;
	public static int DESCRIPTION_LINE_HEIGHT = 0;
	public static int CONST_DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 16, TITLE_FONT_SIZE = 15;
	public static int DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 16;
	protected static int DESCRIPTION_MAXWIDTH = 150;

	public static int TITLE_MAX_CHARS = 30;
	public int WIDTH_RIGHT_ARROW = 0;
	 
	protected static final int DARK_ORANGE = 0xFFBC4202; //dark orange
	protected static final int LIGHT_ORANGE = 0xFFFD5E0A; //light orange
	protected static final int DARK_RED = 0xFF9F0611; //dark red
	protected static final int LIGHT_RED = 0xFFD40903; //light red
	protected static final int FONT_COLOR = Color.WHITE;

	
	
	private boolean shouldShowBalloon = false, isNodeOverlay = true;
	private OverlayItem focusedItem = null;
	private final Point mFocusedScreenCoords = new Point();
	protected Drawable mMarkerFocusedBase_1, mMarkerFocusedBase_2, mMarkerRightArrow;
	protected final Paint mMarkerBackgroundPaint, mDescriptionPaint, mTitlePaint;
	private Point mTouchScreenPoint = new Point(0,0);
	private int rightArrowCenterY = 0, rightArrowCenterX = 0;
	protected OnItemGestureListener<OverlayItem> mOnItemGestureListener = null;
	private int markerBaseOffset = 0;
	private int type = 1;
	private RectF boundingRect;
	
	public CustomBalloonOverlay(final Context ctx, OnItemGestureListener<OverlayItem> onItemGestureListener) {
		super(new DefaultResourceProxyImpl(ctx));
		Resources r = ctx.getResources();
		float densityMultiplier = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
		scaleNumbers(densityMultiplier);
		
		this.mOnItemGestureListener = onItemGestureListener;
		
		this.mMarkerFocusedBase_1 = setBoundsForDrawable(ctx.getResources().getDrawable(R.drawable.marker_default_focused_base_1),0.2f, true);
		this.mMarkerFocusedBase_2 = setBoundsForDrawable(ctx.getResources().getDrawable(R.drawable.marker_default_focused_base_2),0.2f, true);
		
		this.mMarkerRightArrow = setBoundsForDrawable(ctx.getResources().getDrawable(R.drawable.list_arrow),0.5f, false);
		WIDTH_RIGHT_ARROW = Math.round(mMarkerRightArrow.getIntrinsicWidth()*mScale);
		
		//Setting paint values
		this.mMarkerBackgroundPaint = new Paint(); // Color is set in onDraw(...)

		this.mDescriptionPaint = new Paint();
		this.mDescriptionPaint.setColor(FONT_COLOR);
		this.mDescriptionPaint.setAntiAlias(true);
		this.mDescriptionPaint.setFakeBoldText(true);
		this.mDescriptionPaint.setTextSize(DESCRIPTION_FONT_SIZE);
		
		this.mTitlePaint = new Paint();
		this.mTitlePaint.setColor(FONT_COLOR);
		this.mTitlePaint.setFakeBoldText(true);
		this.mTitlePaint.setTextSize(TITLE_FONT_SIZE);
		this.mTitlePaint.setAntiAlias(true);
	}

	private void scaleNumbers(float densityMultiplier) {
		CONST_DESCRIPTION_LINE_HEIGHT=Math.round(cCONST_DESCRIPTION_LINE_HEIGHT*densityMultiplier);
		DESCRIPTION_FONT_SIZE=Math.round(cDESCRIPTION_FONT_SIZE*densityMultiplier);
		TITLE_FONT_SIZE=Math.round(cTITLE_FONT_SIZE*densityMultiplier);
		CONST_DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT=Math.round(cCONST_DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT*densityMultiplier);
		DESCRIPTION_MAXWIDTH=Math.round(cDESCRIPTION_MAXWIDTH*densityMultiplier);
		DESCRIPTION_BOX_PADDING=Math.round(cDESCRIPTION_BOX_PADDING*densityMultiplier);
		TEXT_CENTER_PADDING=Math.round(cTEXT_CENTER_PADDING*densityMultiplier);
		DESCRIPTION_BOX_CORNERWIDTH=Math.round(cDESCRIPTION_BOX_CORNERWIDTH*densityMultiplier);
	}
	
	protected synchronized Drawable setBoundsForDrawable(final Drawable marker, float scaleFactor, boolean isMarkerBase) {
		final int markerWidth = (int) (marker.getIntrinsicWidth() * mScale * scaleFactor);
		final int markerHeight = (int) (marker.getIntrinsicHeight() * mScale * scaleFactor);
		mRect.set(0, 0, 0 + markerWidth, 0 + markerHeight);
		if(isMarkerBase) {
			mRect.offset(-markerWidth / 2, -markerHeight);
		} else {
			mRect.offset(-markerWidth / 2, -markerHeight/2);
		}
		marker.setBounds(mRect);
		return marker;
	}
	
	public void setMarkerBaseOffset(int offset) {
		this.markerBaseOffset = offset;
		this.isNodeOverlay = offset==0?true:false;
	}
	
	@Override
	public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
		if(boundingRect==null || !shouldShowBalloon)
			return super.onSingleTapUp(event, mapView);
		
		final Projection pj = mapView.getProjection();
		final int eventX = (int) event.getX();
		final int eventY = (int) event.getY();

		/* These objects are created to avoid construct new ones every cycle. */
		pj.fromMapPixels(eventX, eventY, mTouchScreenPoint);
		
		if(boundingRect.contains(mTouchScreenPoint.x, mTouchScreenPoint.y)) {
			if(mOnItemGestureListener!=null) {
				this.mOnItemGestureListener.onItemSingleTapUp(0, focusedItem);
			}
			return true;
		}
		
		return super.onSingleTapUp(event, mapView);
	}
	private String itemTitle = "", itemDescription = "";
	
	public void setBalloonOverlay(OverlayItem item, int type) {
		this.type = type;
		this.focusedItem = item;
		shouldShowBalloon = true;
		/* Strings of the OverlayItem, we need. */
		itemTitle = (focusedItem.mTitle == null) ? "" : focusedItem.mTitle;
		itemDescription = (focusedItem.mDescription == null) ? "": focusedItem.mDescription;
		if(itemDescription.equals("")) {
			DESCRIPTION_LINE_HEIGHT = 0;
		} else {
			DESCRIPTION_LINE_HEIGHT = CONST_DESCRIPTION_LINE_HEIGHT;
		}
		if(itemTitle.equals("")) {
			DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = 0;
		} else {
			DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT = CONST_DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT;
		}
	}
	
	public void hideBalloonOverlay() {
		shouldShowBalloon = false;
	}
	
	public int getBaseColor(boolean isBG) {
		if(isBG) {
			return type==1?DARK_ORANGE:DARK_RED;
		} else {
			return type==1?LIGHT_ORANGE:LIGHT_RED;
		}
	}
	
	@Override
	protected void draw(final Canvas canvas, final MapView osmv, final boolean shadow) {
		if (shadow) {
			return;
		}

		if (!shouldShowBalloon) {
			// dont display balloon
			return;
		}

		/* Calculate and set the bounds of the marker. */
		osmv.getProjection().toMapPixels(focusedItem.mGeoPoint, mFocusedScreenCoords);

		mMarkerFocusedBase_1.copyBounds(mRect);
		mRect.offset(mFocusedScreenCoords.x, mFocusedScreenCoords.y+markerBaseOffset);

		/*
		 * Store the width needed for each char in the description to a float array. This is pretty
		 * efficient.
		 */
		final float[] widths = new float[itemDescription.length()];
		this.mDescriptionPaint.getTextWidths(itemDescription, widths);

		final StringBuilder sb = new StringBuilder();
		int maxWidth = 0;
		int curLineWidth = 0;
		int lastStop = 0;
		int i;
		int lastwhitespace = 0;
		/*
		 * Loop through the charwidth array and harshly insert a linebreak, when the width gets
		 * bigger than DESCRIPTION_MAXWIDTH.
		 */
		for (i = 0; i < widths.length; i++) {
			if (!Character.isLetter(itemDescription.charAt(i))) {
				lastwhitespace = i;
			}

			final float charwidth = widths[i];

			if (curLineWidth + charwidth > DESCRIPTION_MAXWIDTH) {
				if (lastStop == lastwhitespace) {
					i--;
				} else {
					i = lastwhitespace;
				}

				sb.append(itemDescription.subSequence(lastStop, i));
				sb.append('\n');

				lastStop = i;
				maxWidth = Math.max(maxWidth, curLineWidth);
				curLineWidth = 0;
			}

			curLineWidth += charwidth;
		}
		/* Add the last line to the rest to the buffer. */
		if (i != lastStop) {
			final String rest = itemDescription.substring(lastStop, i);
			maxWidth = Math.max(maxWidth, (int) this.mDescriptionPaint.measureText(rest));
			sb.append(rest);
		}
		final String[] lines = sb.toString().split("\n");

		/*
		 * The title also needs to be taken into consideration for the width calculation.
		 */
		String title = itemTitle.toString();
		if(title.length()>=TITLE_MAX_CHARS) {
			title = title.substring(0,TITLE_MAX_CHARS-3);
			title = title+"...";
		}
		
		final int titleWidth = (int) this.mTitlePaint.measureText(title);

		maxWidth = Math.max(maxWidth, titleWidth);
		final int descWidth = Math.min(maxWidth, DESCRIPTION_MAXWIDTH);

		/* Calculate the bounds of the Description box that needs to be drawn. */
		final int descBoxLeft = mRect.left - descWidth / 2 - DESCRIPTION_BOX_PADDING
				+ mRect.width() / 2;
		final int descBoxRight = descBoxLeft + descWidth + DESCRIPTION_BOX_PADDING + (isNodeOverlay?(2*WIDTH_RIGHT_ARROW):DESCRIPTION_BOX_PADDING);
		final int descBoxBottom = mRect.top;
		final int descBoxTop = descBoxBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT
				- ((lines.length) * DESCRIPTION_LINE_HEIGHT) /* +1 because of the title. */
				- 2 * DESCRIPTION_BOX_PADDING;
		
		rightArrowCenterY = (descBoxBottom + descBoxTop)/2;
		rightArrowCenterX = descBoxLeft + descWidth + DESCRIPTION_BOX_PADDING + (isNodeOverlay?WIDTH_RIGHT_ARROW:0);

		/* Twice draw a RoundRect, once in black with 1px as a small border. */
		this.mMarkerBackgroundPaint.setColor(getBaseColor(true));
		boundingRect = new RectF(descBoxLeft - 3, descBoxTop - 3, descBoxRight + 3,
				descBoxBottom + 3);
		canvas.drawRoundRect(boundingRect, DESCRIPTION_BOX_CORNERWIDTH, DESCRIPTION_BOX_CORNERWIDTH,
				this.mMarkerBackgroundPaint);
		
		this.mMarkerBackgroundPaint.setColor(getBaseColor(false)); //light orange
		canvas.drawRoundRect(new RectF(descBoxLeft, descBoxTop, descBoxRight, descBoxBottom),
				DESCRIPTION_BOX_CORNERWIDTH, DESCRIPTION_BOX_CORNERWIDTH,
				this.mMarkerBackgroundPaint);

		final int descLeft = descBoxLeft + DESCRIPTION_BOX_PADDING;
		int descTextLineBottom = descBoxBottom - DESCRIPTION_BOX_PADDING - TEXT_CENTER_PADDING;

		/* Draw all the lines of the description. */
		for (int j = lines.length - 1; j >= 0; j--) {
			canvas.drawText(lines[j].trim(), descLeft, descTextLineBottom, this.mDescriptionPaint);
			descTextLineBottom -= DESCRIPTION_LINE_HEIGHT;
		}
		/* Draw the title. */
		if(DESCRIPTION_LINE_HEIGHT!=0)
			canvas.drawText(title, descLeft, descTextLineBottom - DESCRIPTION_TITLE_EXTRA_LINE_HEIGHT,
					this.mTitlePaint);
		else
			canvas.drawText(title, descLeft, descTextLineBottom,
					this.mTitlePaint);

		//draw the right arrow
		if(isNodeOverlay) Overlay.drawAt(canvas, mMarkerRightArrow, rightArrowCenterX, rightArrowCenterY, false);
		
		/*
		 * Finally draw the marker base. This is done in the end to make it look better.
		 */
		Overlay.drawAt(canvas, type==1?mMarkerFocusedBase_1:mMarkerFocusedBase_2, mFocusedScreenCoords.x, mFocusedScreenCoords.y+markerBaseOffset, false);
	}
	
}
