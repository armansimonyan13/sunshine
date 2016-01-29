package com.sunshine;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;

class Item {

	private Parent parent;
	private int x;
	private int y;
	private int width;
	private int height;
	private float scale = 1;
	private float labelScale = 0;
	private boolean isSelected;
	private int highlightColor;
	private Paint paint = new Paint();
	private Bitmap bitmap;
	private Canvas canvas = new Canvas();
	private int touchMargin;
	private String tag;
	private Bitmap itemBackgroundBitmap;
	private Bitmap itemActiveBackgroundBitmap;
	private Bitmap itemImageBitmap;
	private Bitmap labelBackgroundBitmap;
	private String labelTextString;
	private ValueAnimator zoomOutAnimator;
	private ValueAnimator zoomInAnimator;
	private ValueAnimator labelAnimator;
	public Item(Parent parent,
				Bitmap itemBackgroundBitmap,
				Bitmap itemActiveBackgroundBitmap,
				Bitmap itemImageBitmap,
				Bitmap labelBackgroundBitmap,
				String labelTextString,
				int highlightColor,
				int touchMargin,
				String tag) {
		this.parent = parent;
		this.itemBackgroundBitmap = itemBackgroundBitmap;
		this.itemActiveBackgroundBitmap = itemActiveBackgroundBitmap;
		this.itemImageBitmap = itemImageBitmap;
		this.labelBackgroundBitmap = labelBackgroundBitmap;
		this.labelTextString = labelTextString;
		this.highlightColor = highlightColor;
		this.touchMargin = touchMargin;
		this.tag = tag;
	}

	public void update(int x, int y, int radius) {
		this.x = x;
		this.y = y;

		float ratio = (itemBackgroundBitmap.getHeight() + labelBackgroundBitmap.getHeight()) / (float) itemBackgroundBitmap.getWidth();
		this.width = radius * 2;
		this.height = (int) (ratio * width);
	}

	public void draw(Canvas c) {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(itemBackgroundBitmap.getWidth(), itemBackgroundBitmap.getHeight() + labelBackgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
			canvas.setBitmap(bitmap);
		}

		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		int cx = (int) (bitmap.getWidth() / 2f);
		int cy = (int) (bitmap.getHeight() - cx);

		if (isSelected) {
			PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(highlightColor, PorterDuff.Mode.MULTIPLY);
			paint.setColorFilter(porterDuffColorFilter);
			canvas.drawBitmap(itemActiveBackgroundBitmap, cx - itemBackgroundBitmap.getWidth() / 2f, cy - itemBackgroundBitmap.getHeight() / 2f, paint);
		} else {
			canvas.drawBitmap(itemBackgroundBitmap, cx - itemBackgroundBitmap.getWidth() / 2f, cy - itemBackgroundBitmap.getHeight() / 2f, null);
		}
		canvas.drawBitmap(itemImageBitmap, cx - itemImageBitmap.getWidth() / 2f, cy - itemImageBitmap.getHeight() / 2f, null);
		int left;
		int top;
		Rect srcRect;
		Rect dstRect;

		if (labelScale > 0) {
			left = (int) (cx - labelBackgroundBitmap.getWidth() * labelScale / 2f);
			top = (int) (cy - itemBackgroundBitmap.getHeight() / 2f - labelBackgroundBitmap.getHeight() / 2f - labelBackgroundBitmap.getHeight() / 2f * labelScale);
			srcRect = new Rect(0, 0, labelBackgroundBitmap.getWidth(), labelBackgroundBitmap.getHeight());
			dstRect = new Rect(left, top, (int) (left + labelBackgroundBitmap.getWidth() * labelScale), (int) (top + labelBackgroundBitmap.getHeight() * labelScale));
			canvas.drawBitmap(labelBackgroundBitmap, srcRect, dstRect, null);
			int textSize = (int) (labelBackgroundBitmap.getHeight() * 0.33f * labelScale);
			paint.setTextSize(textSize);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setColor(Color.WHITE);
			Rect textBoundsRect = new Rect();
			paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
			paint.setColorFilter(null);
			canvas.drawText(labelTextString, cx, cy - itemBackgroundBitmap.getHeight() / 2f - labelBackgroundBitmap.getHeight() / 2f - textBoundsRect.exactCenterY(), paint);
		}

		srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		left = (int) (this.x - (getScaledWidth() / 2f));
		top = (int) (this.y - (getScaledHeight() - getScaledWidth() / 2f));
		dstRect = new Rect(left, top, left + getScaledWidth(), top + getScaledHeight());
		c.drawBitmap(bitmap, srcRect, dstRect, null);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		if ((zoomInAnimator != null && zoomInAnimator.isStarted())
				|| (zoomOutAnimator != null && zoomOutAnimator.isStarted())) {
			return;
		}
		this.scale = scale;
	}

	public int getScaledWidth() {
		return (int) (getWidth() * getScale());
	}

	public int getScaledHeight() {
		return (int) (getHeight() * getScale());
	}

	public void setSelected(boolean isSelected) {
		if (this.isSelected == isSelected) {
			return;
		}

		this.isSelected = isSelected;

		if (isSelected) {
			showLabelAnimated();
		} else {
			hideLabelAnimated();
		}
	}

	public boolean isSelected() {
		return isSelected;
	}

	public String getTag() {
		return tag;
	}

	private void showLabelAnimated() {
		if (labelAnimator != null && labelAnimator.isStarted()) {
			labelAnimator.cancel();
		}

		labelAnimator = ValueAnimator.ofFloat(0, 1);
		labelAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				labelScale = (Float) animation.getAnimatedValue();
				parent.invalidate();
			}
		});
		labelAnimator.start();
	}

	private void hideLabelAnimated() {
		if (labelAnimator != null && labelAnimator.isStarted()) {
			labelAnimator.cancel();
		}

		labelAnimator = ValueAnimator.ofFloat(1, 0);
		labelAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				labelScale = (Float) animation.getAnimatedValue();
				parent.invalidate();
			}
		});
		labelAnimator.start();
	}

	public void zoomOutAnimated() {
		if (zoomOutAnimator != null && zoomOutAnimator.isStarted()) {
			return;
		}

		if (zoomInAnimator != null) {
			zoomInAnimator.cancel();
		}

		zoomOutAnimator = ValueAnimator.ofFloat(scale, 1);
		zoomInAnimator.setDuration(200);
		zoomOutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				scale = (Float) animation.getAnimatedValue();
				parent.invalidate();
			}
		});
		zoomOutAnimator.start();
	}

	public void zoomInAnimated(float scale) {
		if (zoomInAnimator != null && zoomInAnimator.isStarted()) {
			return;
		}

		if (zoomOutAnimator != null) {
			zoomOutAnimator.cancel();
		}

		zoomInAnimator = ValueAnimator.ofFloat(1, scale);
		zoomInAnimator.setDuration(200);
		zoomInAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				Item.this.scale = (Float) animation.getAnimatedValue();
				parent.invalidate();
			}
		});
		zoomInAnimator.start();
	}

	public boolean inCircle(int x, int y) {
		int dx = x - this.x;
		int dy = y - this.y;
		return Math.sqrt(dx * dx + dy * dy) <= scale * width / 2f + touchMargin;
	}

	public boolean isScaled() {
		return scale != 1;
	}

	public interface Parent {

		public void invalidate();

	}

}
