package com.nkart.neo.wallpapers.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import com.android.launcher3.R;

import java.util.Calendar;
import java.util.Date;

public class AnalogClock extends View {
	
	/** center X. */
	private float x;
	/** center Y. */
	private float y;
	private int radius;
	private Calendar cal;
	private Paint paint;
	private Bitmap clockDial = BitmapFactory.decodeResource(getResources(),
			R.drawable.widgetdial);
	private int sizeScaled = -1;
	private Bitmap clockDialScaled;
	/** Hands colors. */
	private int[] colors;
	private boolean displayHandSec;

	public AnalogClock(Context context) {
		super(context);
		cal = Calendar.getInstance();
	}
	
	public void config(float x, float y, int size, Date date, Paint paint, int[] colors, boolean displayHandSec) {
		this.x = x;
		this.y = y;
		this.paint = paint;
		this.colors = colors;
		this.displayHandSec = displayHandSec;
		
		cal.setTime(date);
		
		// scale bitmap if needed
		if (size != sizeScaled) {
			clockDialScaled = Bitmap.createScaledBitmap(clockDial, size, size, false);
			radius = size / 2;
		}
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (paint != null) {
			// draw clock img
			canvas.drawBitmap(clockDialScaled, x - radius, y - radius, null);
			
			float sec = cal.get(Calendar.SECOND);
			float min = cal.get(Calendar.MINUTE);
			float hour = cal.get(Calendar.HOUR_OF_DAY);
			//draw hands
			paint.setColor(colors[0]);
			canvas.drawLine(x, y, (float) (x + (radius * 0.5f) * Math.cos(Math.toRadians((hour / 12.0f * 360.0f) - 90f))),
					(float) (y + (radius * 0.5f) * Math.sin(Math.toRadians((hour / 12.0f * 360.0f) - 90f))), paint);
			canvas.save();
			paint.setColor(colors[1]);
			canvas.drawLine(x, y, (float) (x + (radius * 0.6f) * Math.cos(Math.toRadians((min / 60.0f * 360.0f) - 90f))),
					(float) (y + (radius * 0.6f) * Math.sin(Math.toRadians((min / 60.0f * 360.0f) - 90f))), paint);
			canvas.save();
			
			if (displayHandSec) {
				paint.setColor(colors[2]);
				canvas.drawLine(x, y, (float) (x + (radius * 0.7f) * Math.cos(Math.toRadians((sec / 60.0f * 360.0f) - 90f))),
					(float) (y + (radius * 0.7f) * Math.sin(Math.toRadians((sec / 60.0f * 360.0f) - 90f))), paint);
			}
		}
	}
}