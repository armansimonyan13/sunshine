package com.sunshine;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntegerRes;
import android.support.annotation.StringRes;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
class Core extends View implements Ray.Parent {

	interface OnItemSelectListener {

		void onItemSelected(int index, String tag);

	}

	private final float MAX_SCALE = 1.4f;
	private final float SEGMENT_ANGLE_RADIAN;// = 0.8f;
	private final float MAX_RADIUS;

	private int x;
	private int y;
	private int originX;
	private int originY;
	private float radius;
	private float startAngle;
	private float offsetX;
	private float offsetY;
	private int parentX;
	private int parentY;
	private int parentWidth;
	private int parentHeight;
	private Bitmap originBitmap;
	private Bitmap originActiveBitmap;
	private Paint paint = new Paint();
	private boolean reversedOrder;
	private boolean isOpened;
	private List<Ray> rays = new ArrayList<>();
	private OnItemSelectListener onItemSelectListener;
	private int selectedItemIndex;
	private int rayWidth;
	private int rayHeight;

	public Core(
			Context context,
			int width,
			int height,
			int maxRadius,
			float itemSegment) {
		super(context);

		this.rayWidth = width;
		this.rayHeight = height;

		MAX_RADIUS = maxRadius;
		SEGMENT_ANGLE_RADIAN = itemSegment;
	}

	private static double normalizeAngle(double angle) {
		if (angle < 0) {
			angle = 2 * Math.PI + angle;
		}
		return angle;
	}

	public boolean processMotionEvent(MotionEvent event) {
		update(event);
		invalidate();
		return false;
	}

	public int getParentWidth() {
		return parentWidth;
	}

	public void setParentWidth(int parentWidth) {
		this.parentWidth = parentWidth;
	}

	public int getParentHeight() {
		return parentHeight;
	}

	public void setParentHeight(int parentHeight) {
		this.parentHeight = parentHeight;
	}

	public int getParentX() {
		return parentX;
	}

	public void setParentX(int parentX) {
		this.parentX = parentX;
	}

	public int getParentY() {
		return parentY;
	}

	public void setParentY(int parentY) {
		this.parentY = parentY;
	}

	private void update(MotionEvent event) {
		x = (int) event.getRawX() - parentX;
		y = (int) event.getRawY() - parentY;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			originX = x;
			originY = y;
		}

		if (event.getAction() == MotionEvent.ACTION_UP) {
			for (int i = 0; i < rays.size(); i++) {
				if (rays.get(i).inCircle(x, y) && rays.get(i).isSelected() && isOpened && onItemSelectListener != null) {
					onItemSelectListener.onItemSelected(i, rays.get(i).getTag());
					break;
				}
			}
		}

		double angle = normalizeAngle(Math.atan2(y - originY, x - originX));

		for (int i = 0; i < rays.size(); i++) {
			Ray ray = rays.get(i);
			double itemCenterAngle = normalizeAngle(Math.atan2(ray.getBodyCenterY() - originY, ray.getBodyCenterX() - originX));
			double itemStartAngle = itemCenterAngle - SEGMENT_ANGLE_RADIAN / 2f;
			double itemFinishAngle = itemCenterAngle + SEGMENT_ANGLE_RADIAN / 2f;
			boolean inCurrentSegment;
			if (itemStartAngle < angle && angle < itemFinishAngle) {
				int dx = x - ray.getBodyCenterX();
				int dy = y - ray.getBodyCenterY();
				double distance = Math.sqrt(dx * dx + dy * dy);
				float scale = (float) (MAX_SCALE - (MAX_SCALE - 1) * distance / MAX_RADIUS);
				if (scale < 1) {
					scale = 1;
				}
				if (ray.isScaled()) {
					ray.setScale(scale);
				} else {
					ray.zoomInAnimated(scale);
				}
				inCurrentSegment = true;
			} else {
				if (isOpened) {
					if (ray.isScaled()) {
						ray.zoomOutAnimated();
					}
				}
				inCurrentSegment = false;
			}

			if (isOpened) {
				if (ray.inCircle(x, y)) {
					ray.setSelected(inCurrentSegment);
				} else {
					ray.setSelected(false);
				}
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		render(canvas);
	}

	private void render(Canvas canvas) {
		canvas.drawColor(Color.parseColor("#40000000"));

		paint.setAntiAlias(true);
		Bitmap bitmap;
		if (isInOrigin(x, y)) {
			bitmap = originActiveBitmap;
		} else {
			bitmap = originBitmap;
		}
		Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		int left = (int) (originX - rayWidth / 2f);
		int top =  (int) (originY - rayHeight / 2f);
		Rect dstRect = new Rect(left, top, left + rayWidth, top + rayHeight);
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

		if (reversedOrder) {
			for (int i = rays.size() - 1; i >= 0; i--) {
				Ray ray = rays.get(i);
				ray.setIndex(i);
				if (ray.isSelected()) {
					selectedItemIndex = i;
				} else {
					ray.draw(canvas);
				}
			}
			rays.get(selectedItemIndex).draw(canvas);
		} else {
			for (int i = 0; i < rays.size(); i++) {
				Ray ray = rays.get(i);
				ray.setIndex(i);
				if (ray.isSelected()) {
					selectedItemIndex = i;
				} else {
					ray.draw(canvas);
				}
			}
			rays.get(selectedItemIndex).draw(canvas);
		}

//		renderLines(canvas);
	}

	private boolean isInOrigin(int x, int y) {
		int dx = x - originX;
		int dy = y - originY;
		return Math.sqrt(dx * dx + dy * dy) <= rayWidth / 2f;
	}

	@SuppressWarnings("unused")
	private void renderLines(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(1);
		paint.setAntiAlias(true);
		for (int i = 0; i < rays.size(); i++) {
			canvas.drawLine(originX, originY, rays.get(i).getBodyCenterX(), rays.get(i).getBodyCenterY(), paint);
		}
	}

	public void setOriginResource(@DrawableRes int originResource) {
		this.originBitmap = BitmapFactory.decodeResource(getResources(), originResource);
	}

	public void setOriginActiveResource(@DrawableRes int originActiveResource) {
		this.originActiveBitmap = BitmapFactory.decodeResource(getResources(), originActiveResource);
	}

	public void reset() {
		rays.clear();
	}

	public void addItem(
			@DimenRes @IntegerRes int itemWidthResource,
			@DimenRes @IntegerRes int itemHeightResource,
			@DimenRes @IntegerRes int bodyTouchMarginResource,
			@DimenRes @IntegerRes int bodyPaddingResource,
			@DimenRes @IntegerRes int labelWidthResource,
			@DimenRes @IntegerRes int labelHeightResource,
			@DimenRes @IntegerRes int labelBottomMarginResource,
			@DimenRes @IntegerRes int labelTextSizeResource,
			@DrawableRes int itemBackgroundResource,
			@DrawableRes int itemActiveBackgroundResource,
			@DrawableRes int itemImageResource,
			@DrawableRes int labelBackgroundResource,
			@StringRes int labelTextResource,
			@ColorRes int highlightColor,
			String tag) {
		rays.add(new Ray(
				this,
				getResources(),
				itemWidthResource,
				itemHeightResource,
				bodyTouchMarginResource,
				bodyPaddingResource,
				labelWidthResource,
				labelHeightResource,
				labelBottomMarginResource,
				labelTextSizeResource,
				itemBackgroundResource,
				itemActiveBackgroundResource,
				itemImageResource,
				labelBackgroundResource,
				labelTextResource,
				highlightColor,
				tag
		));

		calculateStartAngle();
		updateItemCoordinates();
	}

	private void calculateStartAngle() {
		offsetX = (int) (MAX_SCALE * rays.get(0).getWidth() / 2f);

		final float startAngleOffset = (rays.size() - 1) * 0.5f * SEGMENT_ANGLE_RADIAN;
		startAngle = (float) (1.5 * Math.PI) - startAngleOffset;

		if (tooCloseToTopEdge()) {
			if (closerToRightEdge()) {
				startAngle = (float) (startAngle - startAngleOffset - Math.acos((originY - MAX_SCALE * getItemHeight()) / MAX_RADIUS));
				reversedOrder = true;
			} else {
				startAngle = (float) (startAngle + startAngleOffset + Math.acos((originY - MAX_SCALE * getItemHeight()) / MAX_RADIUS));
				reversedOrder = false;
			}
		} else if (tooCloseToLeftEdge()) {
			float acos = (originX - offsetX) / MAX_RADIUS;
			if (acos < -1) {
				acos++;
			}
			startAngle = (float) (Math.PI + Math.acos(acos));
			reversedOrder = false;
		} else if (tooCloseToRightEdge()) {
			float acos = (getParentWidth() - originX - offsetX) / MAX_RADIUS;
			if (acos < -1) {
				acos++;
			}
			startAngle = (float) ((2 * Math.PI - Math.acos(acos)) - (rays.size() - 1) * SEGMENT_ANGLE_RADIAN);
			reversedOrder = true;
		} else {
			reversedOrder = false;
		}

		if (startAngle < 0) {
			startAngle = (float) (2 * Math.PI + startAngle);
		}
	}

	private int getItemHeight() {
		return rays.get(0).getHeight();
	}

	private boolean tooCloseToRightEdge() {
		return Math.abs(MAX_RADIUS * Math.cos(startAngle + (rays.size() - 1)
				* SEGMENT_ANGLE_RADIAN)) + originX + offsetX > getParentWidth();
	}

	private boolean tooCloseToLeftEdge() {
		return Math.abs(MAX_RADIUS * Math.cos(startAngle)) + offsetX > originX;
	}

	private boolean closerToRightEdge() {
		return originX >= getParentWidth() / 2;
	}

	private boolean tooCloseToTopEdge() {
		return MAX_RADIUS + getItemHeight() > originY;
	}

	private void updateItemCoordinates() {
		Ray ray;
		for (int i = 0; i < rays.size(); i++) {
			float angle = startAngle + i * SEGMENT_ANGLE_RADIAN;
			int centerX = (int) (originX + radius * Math.cos(angle));
			int centerY = (int) (originY + radius * Math.sin(angle));
			ray = rays.get(i);
			ray.setBodyCenter(centerX, centerY);
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		calculateStartAngle();
		updateItemCoordinates();

		ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, MAX_RADIUS);
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				radius = ((Number) animation.getAnimatedValue()).floatValue();
				for (Ray ray : rays) {
					ray.setScale(animation.getAnimatedFraction());
				}

				updateItemCoordinates();
				invalidate();
			}
		});

		valueAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				isOpened = false;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				radius = MAX_RADIUS;
				for (Ray ray : rays) {
					ray.setScale(1);
				}

				updateItemCoordinates();
				invalidate();
				isOpened = true;
			}
		});

		valueAnimator.setInterpolator(new OvershootInterpolator(2f));
		valueAnimator.setDuration(400);
		valueAnimator.start();
	}

	public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
		this.onItemSelectListener = onItemSelectListener;
	}

	public int getItemCount() {
		return rays.size();
	}

}
