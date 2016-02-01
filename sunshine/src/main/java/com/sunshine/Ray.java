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
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import android.util.TypedValue;

class Ray {

	private Parent parent;
	private String tag;

	private int bodyWidth;
	private int bodyHeight;
	private int bodyPadding;
	private int bodyTouchMargin;
	private int labelWidth;
	private int labelHeight;
	private int labelBottomMargin;
	private int labelTextSize;
	private Drawable bodyBackgroundDrawable;
	private Drawable bodyActiveBackgroundDrawable;
	private Drawable bodyImageDrawable;
	private Drawable labelBackgroundDrawable;
	private String labelTextString;
	private int highlightColor;

	private int bodyCenterX;
	private int bodyCenterY;
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

	private int index; // for test

	public Ray(
			Parent parent,
			Resources resources,
			@DimenRes @IntegerRes int bodyWidthResource,
			@DimenRes @IntegerRes int bodyHeightResource,
			@DimenRes @IntegerRes int bodyTouchMarginResource,
			@DimenRes @IntegerRes int bodyPaddingResource,
			@DimenRes @IntegerRes int labelWidthResource,
			@DimenRes @IntegerRes int labelHeightResource,
			@DimenRes @IntegerRes int labelBottomMarginResource,
			@DimenRes @IntegerRes int labelTextSizeResource,
			@DrawableRes int bodyBackgroundResource,
			@DrawableRes int bodyActiveBackgroundResource,
			@DrawableRes int bodyImageResource,
			@DrawableRes int labelBackgroundResource,
			@StringRes int labelTextResource,
			@ColorRes int highlightColorResource,
			String tag) {
		this.parent = parent;
		this.labelTextString = resources.getString(labelTextResource);
		this.highlightColor = resources.getColor(highlightColorResource);
		this.tag = tag;

		if ("dimen".equals(resources.getResourceTypeName(bodyWidthResource))) {
			bodyWidth = resources.getDimensionPixelSize(bodyWidthResource);
		} else if ("integer".equals(resources.getResourceTypeName(bodyWidthResource))) {
			bodyWidth = resources.getInteger(bodyWidthResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(bodyHeightResource))) {
			bodyHeight = resources.getDimensionPixelSize(bodyWidthResource);
		} else if ("integer".equals(resources.getResourceTypeName(bodyHeightResource))) {
			bodyHeight = resources.getInteger(bodyWidthResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(bodyTouchMarginResource))) {
			this.bodyTouchMargin = resources.getDimensionPixelSize(bodyTouchMarginResource);
		} else if ("integer".equals(resources.getResourceTypeName(bodyTouchMarginResource))) {
			this.bodyTouchMargin = resources.getInteger(bodyTouchMarginResource);
		}
		if ("dimen".equals(resources.getResourceTypeName(bodyPaddingResource))) {
			this.bodyPadding = resources.getDimensionPixelSize(bodyPaddingResource);
		} else if ("integer".equals(resources.getResourceTypeName(bodyPaddingResource))) {
			this.bodyPadding = resources.getInteger(bodyPaddingResource);
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
		bodyBackgroundDrawable = resources.getDrawable(bodyBackgroundResource);
		bodyActiveBackgroundDrawable = resources.getDrawable(bodyActiveBackgroundResource);
		bodyImageDrawable = resources.getDrawable(bodyImageResource);
		labelBackgroundDrawable = resources.getDrawable(labelBackgroundResource);
		labelTextString = resources.getString(labelTextResource);
		highlightColor = resources.getColor(highlightColorResource);

		if (labelWidth == -1) {
			paint.setTextSize(labelTextSize);
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setColor(Color.WHITE);
			Rect textBoundsRect = new Rect();
			paint.getTextBounds(labelTextString, 0, labelTextString.length(), textBoundsRect);
			int labelPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, resources.getDisplayMetrics());
			labelWidth = textBoundsRect.width() + 2 * labelPadding;
		}

		this.width = Math.max(bodyWidth, labelWidth);
		this.height = bodyHeight + labelBottomMargin + labelHeight;

		bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		canvas.setBitmap(bitmap);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setBodyCenter(int bodyCenterX, int bodyCenterY) {
		this.bodyCenterX = bodyCenterX;
		this.bodyCenterY = bodyCenterY;
	}

	/**
	 * Do not modify this method
	 * If you need to change drawing of this object
	 * consider to change {@link Ray#update()} or {@link Ray#render()}
	 */
	public final void draw(Canvas c) {
		update();
		render();

		Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		int left = (int) (bodyCenterX - scale * bitmap.getWidth() / 2f);
		int top = (int) (bodyCenterY - (scale * (bitmap.getHeight() - bodyHeight / 2)));
		int right = (int) (left + (bitmap.getWidth() * scale));
		int bottom = (int) (top + (bitmap.getHeight() * scale));
		Rect dstRect = new Rect(left, top, right, bottom);
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
		int cy = (int) (bitmap.getHeight() - bodyHeight / 2f);

		if (isSelected) {
			int left = (int) (cx - bodyWidth / 2f);
			int top = (int) (cy - bodyHeight / 2f);
			bodyActiveBackgroundDrawable.setColorFilter(highlightColor, PorterDuff.Mode.MULTIPLY);
			bodyActiveBackgroundDrawable.setBounds(left, top, left + bodyWidth, top + bodyHeight);
			bodyActiveBackgroundDrawable.draw(canvas);
		} else {
			int left = (int) (cx - bodyWidth / 2f);
			int top = (int) (cy - bodyHeight / 2f);
			bodyBackgroundDrawable.setBounds(left, top, left + bodyWidth, top + bodyHeight);
			bodyBackgroundDrawable.draw(canvas);
		}
		int imageWidth = bodyImageDrawable.getIntrinsicWidth();
		int imageHeight = bodyImageDrawable.getIntrinsicHeight();
		int top, left;
		float widthScaleFactor = (bodyWidth - 2f * bodyPadding) / imageWidth;
		float heightScaleFactor = (bodyHeight - 2f * bodyPadding) / imageHeight;
		float scaleFactor;
		if (widthScaleFactor < heightScaleFactor) {
			scaleFactor = widthScaleFactor;
		} else {
			scaleFactor = heightScaleFactor;
		}
		int scaledImageWidth = (int) (scaleFactor * imageWidth);
		int scaledImageHeight = (int) (scaleFactor * imageHeight);
		left = (int) (cx - scaledImageWidth / 2f);
		top = (int) (cy - scaledImageHeight / 2f);
		bodyImageDrawable.setBounds(left, top, left + scaledImageWidth, top + scaledImageHeight);
		bodyImageDrawable.draw(canvas);

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
			canvas.drawText(labelTextString, cx, cy - bodyHeight / 2f - labelBottomMargin - labelHeight / 2f - textBoundsRect.exactCenterY(), paint);
		}
	}

	public int getBodyCenterX() {
		return bodyCenterX;
	}

	public int getBodyCenterY() {
		return bodyCenterY;
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
				Ray.this.scale = (Float) animation.getAnimatedValue();
				parent.invalidate();
			}
		});
		zoomInAnimator.start();
	}

	public boolean inCircle(int x, int y) {
		int dx = x - this.bodyCenterX;
		int dy = y - this.bodyCenterY;
		return Math.sqrt(dx * dx + dy * dy) <= scale * bodyWidth / 2f + bodyTouchMargin;
	}

	public boolean isScaled() {
		return scale != 1;
	}

	public interface Parent {

		void invalidate();

	}

}
