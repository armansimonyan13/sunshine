package com.sunshine;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

class Item {

	int index;

	private Parent parent;
	private Resources resources;
	private int itemWidthResource;
	private int itemHeightResource;
	private int labelWidthResource;
	private int labelHeightResource;
	private int labelBottomMarginResource;
	private int textSizeResource;
	private int itemBackgroundResource;
	private int itemActiveBackgroundResource;
	private int itemImageResource;
	private int labelBackgroundResource;
	private int labelTextResource;
	private int highlightColorResource;

	private int itemWidth;
	private int itemHeight;
	private int labelWidth;
	private int labelHeight;
	private int labelBottomMargin;
	private int labelTextSize;
	private Drawable itemBackgroundDrawable;
	private Drawable itemActiveBackgroundDrawable;
	private Drawable itemImageDrawable;
	private Drawable labelBackgroundDrawable;
	private String labelTextString;
	private int highlightColor;

	private Bitmap itemBackgroundBitmap;
	private Bitmap itemActiveBackgroundBitmap;
	private Bitmap itemImageBitmap;

	private int labelBackgroundHeight;

	private int cx;
	private int cy;
	private int width;
	private int height;
	private float scale = 1;
	private float labelScale = 0;
	private boolean isSelected;
	private Paint paint = new Paint();
	private Bitmap bitmap;
	private Canvas canvas = new Canvas();
	private int touchMargin;
	private String tag;
	private ValueAnimator zoomOutAnimator;
	private ValueAnimator zoomInAnimator;
	private ValueAnimator labelAnimator;

	private int labelTextWidth;

	private Rect srcRect;
	private Rect dstRect;

	public Item(
			Parent parent,
			Resources resources,
			int itemWidthResource,
			int itemHeightResource,
			int labelWidthResource,
			int labelHeightResource,
			int labelBottomMarginResource,
			int textSizeResource,
			int itemBackgroundResource,
			int itemActiveBackgroundResource,
			int itemImageResource,
			int labelBackgroundResource,
			int labelTextResource,
			int highlightColorResource,
			int touchMargin,
			String tag) {
		this.parent = parent;
		this.resources = resources;
		this.itemWidthResource = itemWidthResource;
		this.itemHeightResource = itemHeightResource;
		this.labelWidthResource = labelWidthResource;
		this.labelHeightResource = labelHeightResource;
		this.labelBottomMarginResource = labelBottomMarginResource;
		this.textSizeResource = textSizeResource;
		this.itemBackgroundResource = itemBackgroundResource;
		this.itemActiveBackgroundResource = itemActiveBackgroundResource;
		this.itemImageResource = itemImageResource;
		this.labelBackgroundResource = labelBackgroundResource;
		this.labelTextResource = labelTextResource;
		this.highlightColorResource = highlightColorResource;

		this.labelBackgroundHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, resources.getDisplayMetrics());

		this.labelBackgroundDrawable = resources.getDrawable(labelBackgroundResource);

		this.itemBackgroundBitmap = BitmapFactory.decodeResource(resources, itemBackgroundResource);
		this.itemActiveBackgroundBitmap = BitmapFactory.decodeResource(resources, itemActiveBackgroundResource);
		this.itemImageBitmap = BitmapFactory.decodeResource(resources, itemImageResource);
		this.labelTextString = resources.getString(labelTextResource);
		this.highlightColor = resources.getColor(highlightColorResource);

		this.touchMargin = touchMargin;
		this.tag = tag;

		this.width = itemWidth;


		int textSize = (int) (labelBackgroundHeight * 0.33f);
		paint.setTextSize(textSize);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(Color.WHITE);
		Rect textBoundsRect = new Rect();
		paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
		labelTextWidth = textBoundsRect.width();
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setCenter(int cx, int cy) {
		this.cx = cx;
		this.cy = cy;

//		float ratio = (itemBackgroundBitmap.getHeight() + labelBackgroundBitmap.getHeight()) / (float) itemBackgroundBitmap.getWidth();
//		this.width = radius * 2;
//		this.height = (int) (ratio * width);
	}

	public void draw(Canvas c) {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(Math.max(itemBackgroundBitmap.getWidth(), labelTextWidth), itemBackgroundBitmap.getHeight() + labelBackgroundHeight, Bitmap.Config.ARGB_8888);
			canvas.setBitmap(bitmap);
		}
		update();
		render();

		srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		int left = cx - bitmap.getWidth() / 2;
		int top = cy - itemBackgroundBitmap.getHeight() / 2 - labelBackgroundHeight;
		dstRect = new Rect(left, top, left + bitmap.getWidth(), top + bitmap.getHeight());
		c.drawBitmap(bitmap, srcRect, dstRect, null);

//		if (bitmap == null) {
//			bitmap = Bitmap.createBitmap(getInternalWidth(), itemBackgroundBitmap.getHeight() + labelBackgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//			canvas.setBitmap(bitmap);
//		}

//		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//		canvas.drawColor(Color.RED);

//		int cx = (int) (bitmap.getWidth() / 2f);
//		int cy = (int) (bitmap.getHeight() - cx);
//
//		if (isSelected) {
//			PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(highlightColor, PorterDuff.Mode.MULTIPLY);
//			paint.setColorFilter(porterDuffColorFilter);
//			canvas.drawBitmap(itemActiveBackgroundBitmap, cx - itemBackgroundBitmap.getWidth() / 2f, cy - itemBackgroundBitmap.getHeight() / 2f, paint);
//		} else {
//			canvas.drawBitmap(itemBackgroundBitmap, cx - itemBackgroundBitmap.getWidth() / 2f, cy - itemBackgroundBitmap.getHeight() / 2f, null);
//		}
//		canvas.drawBitmap(itemImageBitmap, cx - itemImageBitmap.getWidth() / 2f, cy - itemImageBitmap.getHeight() / 2f, null);
//		int left;
//		int top;
//		Rect srcRect;
//		Rect dstRect;

//		if (labelScale > 0) {
//			left = (int) (cx - labelBackgroundBitmap.getWidth() * labelScale / 2f);
//			top = (int) (cy - itemBackgroundBitmap.getHeight() / 2f - labelBackgroundBitmap.getHeight() / 2f - labelBackgroundBitmap.getHeight() / 2f * labelScale);
//			srcRect = new Rect(0, 0, labelBackgroundBitmap.getWidth(), labelBackgroundBitmap.getHeight());
//			dstRect = new Rect(left, top, (int) (left + labelBackgroundBitmap.getWidth() * labelScale), (int) (top + labelBackgroundBitmap.getHeight() * labelScale));
//			canvas.drawBitmap(labelBackgroundBitmap, srcRect, dstRect, null);
//			int textSize = (int) (labelBackgroundBitmap.getHeight() * 0.33f * labelScale);
//			paint.setTextSize(textSize);
//			paint.setTextAlign(Paint.Align.CENTER);
//			paint.setColor(Color.WHITE);
//			Rect textBoundsRect = new Rect();
//			paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
//			paint.setColorFilter(null);
//			canvas.drawText(labelTextString, cx, cy - itemBackgroundBitmap.getHeight() / 2f - labelBackgroundBitmap.getHeight() / 2f - textBoundsRect.exactCenterY(), paint);
//		}

//		srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//		left = (int) (this.x - (getScaledWidth() / 2f));
//		top = (int) (this.y - (getScaledHeight() - getWidth() * getScale() / 2f));
//		dstRect = new Rect(left, top, left + getScaledWidth(), top + getScaledHeight());
//		c.drawBitmap(bitmap, srcRect, dstRect, null);
	}

	private void update() {

	}

	private void render() {
//		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		switch (index) {
			case 0:
				canvas.drawColor(Color.RED);
				break;
			case 1:
				canvas.drawColor(Color.CYAN);
				break;
			case 2:
				canvas.drawColor(Color.BLUE);
				break;
		}

		int cx = (int) (bitmap.getWidth() / 2f);
		int cy = (int) (bitmap.getHeight() - itemBackgroundBitmap.getHeight() / 2);

		if (isSelected) {
			PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(highlightColor, PorterDuff.Mode.MULTIPLY);
			paint.setColorFilter(porterDuffColorFilter);
			canvas.drawBitmap(itemActiveBackgroundBitmap, cx - itemBackgroundBitmap.getWidth() / 2f, cy - itemBackgroundBitmap.getHeight() / 2f, paint);
		} else {
			canvas.drawBitmap(itemBackgroundBitmap, cx - itemBackgroundBitmap.getWidth() / 2f, cy - itemBackgroundBitmap.getHeight() / 2f, null);
		}
		canvas.drawBitmap(itemImageBitmap, cx - itemImageBitmap.getWidth() / 2f, cy - itemImageBitmap.getHeight() / 2f, null);

		if (labelScale > 0) {
//			int left = (int) (cx - labelTextWidth * labelScale / 2f);
//			int top = (int) (cy - itemBackgroundBitmap.getHeight() / 2f - labelBackgroundHeight / 2f - labelBackgroundHeight / 2f * labelScale);

//			srcRect = new Rect(0, 0, labelTextWidth, labelBackgroundHeight);
//			dstRect = new Rect(left, top, (int) (left + labelTextWidth * labelScale), (int) (top + labelBackgroundHeight * labelScale));
//			canvas.drawBitmap(labelBackgroundBitmap, srcRect, dstRect, null);
			labelBackgroundDrawable.setBounds(0, 0, labelTextWidth, labelBackgroundHeight);
			labelBackgroundDrawable.draw(canvas);
			int textSize = (int) (labelBackgroundHeight * 0.33f * labelScale);
			paint.setTextSize(textSize);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setColor(Color.WHITE);
			Rect textBoundsRect = new Rect();
			paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
			paint.setColorFilter(null);
			canvas.drawText(labelTextString, cx, cy - itemBackgroundBitmap.getHeight() / 2f - labelBackgroundHeight / 2f - textBoundsRect.exactCenterY(), paint);
		}
	}

	private int getInternalWidth() {
		return Math.max(itemImageBitmap.getWidth(), labelTextWidth);
	}

	public int getCx() {
		return cx;
	}

	public int getCy() {
		return cy;
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
		return (int) (getInternalWidth() * getScale());
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
		int dx = x - this.cx;
		int dy = y - this.cy;
		return Math.sqrt(dx * dx + dy * dy) <= scale * width / 2f + touchMargin;
	}

	public boolean isScaled() {
		return scale != 1;
	}

	public interface Parent {

		public void invalidate();

	}

}
