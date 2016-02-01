package com.sunshine;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

class Item {

	private Parent parent;
	private Resources resources;
	private int touchMargin;
	private String tag;

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
	private ValueAnimator zoomOutAnimator;
	private ValueAnimator zoomInAnimator;
	private ValueAnimator labelAnimator;

	private int labelTextWidth;

	private Rect srcRect;
	private Rect dstRect;

	private int index; // for test

	public Item(
			Parent parent,
			Resources resources,
			int itemWidthResource,
			int itemHeightResource,
			int labelWidthResource,
			int labelHeightResource,
			int labelBottomMarginResource,
			int labelTextSizeResource,
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
		this.touchMargin = touchMargin;
		this.tag = tag;

		if ("dimen".equals(resources.getResourceTypeName(itemWidthResource))) {
			itemWidth = resources.getDimensionPixelSize(itemWidthResource);
		} else if ("integer".equals(resources.getResourceTypeName(itemWidthResource))) {
			itemWidth = resources.getInteger(itemWidthResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(itemHeightResource))) {
			itemHeight = resources.getDimensionPixelSize(itemWidthResource);
		} else if ("integer".equals(resources.getResourceTypeName(itemHeightResource))) {
			itemHeight = resources.getInteger(itemWidthResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(labelWidthResource))) {
			labelWidth = resources.getDimensionPixelSize(labelWidthResource);
		} else if ("integer".equals(resources.getResourceTypeName(labelWidthResource))) {
			labelWidth = resources.getInteger(labelWidthResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(labelHeightResource))) {
			labelHeight = resources.getDimensionPixelSize(labelHeightResource);
		} else if ("integer".equals(resources.getResourceTypeName(labelHeightResource))) {
			labelHeight = resources.getInteger(labelHeightResource);
		}
		if ("dimane".equals(resources.getResourceTypeName(labelBottomMarginResource))) {
			labelBottomMargin = resources.getDimensionPixelSize(labelBottomMarginResource);
		} else if ("integer".equals(resources.getResourceTypeName(labelBottomMarginResource))) {
			labelBottomMargin = resources.getDimensionPixelOffset(labelBottomMarginResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(labelTextSizeResource))) {
			labelTextSize = resources.getDimensionPixelSize(labelTextSizeResource);
		} else if ("integer".equals(resources.getResourceTypeName(labelTextSizeResource))) {
			labelTextSize = resources.getDimensionPixelSize(labelTextSizeResource);
		}
		itemBackgroundDrawable = resources.getDrawable(itemBackgroundResource);
		itemActiveBackgroundDrawable = resources.getDrawable(itemActiveBackgroundResource);
		itemImageDrawable = resources.getDrawable(itemImageResource);
		labelBackgroundDrawable = resources.getDrawable(labelBackgroundResource);
		labelTextString = resources.getString(labelTextSizeResource);
		highlightColor = resources.getColor(highlightColorResource);

		this.labelTextString = resources.getString(labelTextResource);
		this.highlightColor = resources.getColor(highlightColorResource);

		paint.setTextSize(labelTextSize);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(Color.WHITE);
		Rect textBoundsRect = new Rect();
		paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
		int labelPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, resources.getDisplayMetrics());
		labelTextWidth = textBoundsRect.width() + 2 * labelPadding;
		if (labelWidth == -1) {
			labelWidth = labelTextWidth;
		}

		this.width = Math.max(itemWidth, labelWidth);
		this.height = itemHeight + labelBottomMargin + labelHeight;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setCenter(int cx, int cy) {
		this.cx = cx;
		this.cy = cy;
	}

	public void draw(Canvas c) {
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			canvas.setBitmap(bitmap);
		}
		update();
		render();

		srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		int left = (int) (cx - scale * bitmap.getWidth() / 2f);
		int top = (int) (cy - (scale * (bitmap.getHeight() - itemHeight / 2)));
		int right = (int) (left + (bitmap.getWidth() * scale));
		int bottom = (int) (top + (bitmap.getHeight() * scale));
		dstRect = new Rect(left, top, right, bottom);
		c.drawBitmap(bitmap, srcRect, dstRect, null);
	}

	private void update() {

	}

	private void render() {
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//		switch (index) {
//			case 0:
//				canvas.drawColor(Color.RED);
//				break;
//			case 1:
//				canvas.drawColor(Color.CYAN);
//				break;
//			case 2:
//				canvas.drawColor(Color.BLUE);
//				break;
//		}

		int cx = (int) (bitmap.getWidth() / 2f);
		int cy = (int) (bitmap.getHeight() - itemHeight / 2f);

		if (isSelected) {
			int left = (int) (cx - itemWidth / 2f);
			int top = (int) (cy - itemHeight / 2f);
			itemActiveBackgroundDrawable.setColorFilter(highlightColor, PorterDuff.Mode.MULTIPLY);
			itemActiveBackgroundDrawable.setBounds(left, top, left + itemWidth, top + itemHeight);
			itemActiveBackgroundDrawable.draw(canvas);
		} else {
			int left = (int) (cx - itemWidth / 2f);
			int top = (int) (cy - itemHeight / 2f);
			itemBackgroundDrawable.setBounds(left, top, left + itemWidth, top + itemHeight);
			itemBackgroundDrawable.draw(canvas);
		}
		int itemPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, resources.getDisplayMetrics()); // Todo: change padding to be injected
		int left = (int) (cx - (itemWidth - itemPadding) / 2f);
		int top = (int) (cy - (itemHeight - itemPadding) / 2f);
		itemImageDrawable.setBounds(left, top, left + itemWidth - itemPadding, top + itemHeight - itemPadding);
		itemImageDrawable.draw(canvas);

		if (labelScale > 0) {
			int scaledLabelWidth = (int) (labelWidth * labelScale);
			int scaledLabelHeight = (int) (labelHeight * labelScale);
			left = (int) (cx -  scaledLabelWidth / 2f);
			top = (int) ((labelHeight / 2f) - (scaledLabelHeight / 2f));
			labelBackgroundDrawable.setBounds(left, top, left + scaledLabelWidth, top + scaledLabelHeight);
			labelBackgroundDrawable.draw(canvas);

			int scaledLabelTextSize = (int) (labelTextSize * labelScale);
			paint.setTextSize(scaledLabelTextSize);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setColor(Color.WHITE);
			Rect textBoundsRect = new Rect();
			paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
			paint.setColorFilter(null);
			canvas.drawText(labelTextString, cx, cy - itemHeight / 2f - labelBottomMargin - labelHeight / 2f - textBoundsRect.exactCenterY(), paint);
		}
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
		int dx = x - this.cx;
		int dy = y - this.cy;
		return Math.sqrt(dx * dx + dy * dy) <= scale * itemWidth / 2f + touchMargin;
	}

	public boolean isScaled() {
		return scale != 1;
	}

	public interface Parent {

		void invalidate();

	}

}
