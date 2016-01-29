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
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("ViewConstructor")
class Core extends View implements Item.Parent {

	private final float MAX_SCALE = 1.4f;
	private final float SEGMENT_ANGLE_RADIAN;// = 0.8f;
	private final float MAX_RADIUS;
	private final int ITEM_RADIUS;
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
	private List<Item> items = new ArrayList<>();
	private OnItemClickListener onItemClickListener;

	public Core(Context context, int itemRadius, int maxRadius, float itemSegment) {
		super(context);

		ITEM_RADIUS = itemRadius;
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
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).inCircle(x, y) && items.get(i).isSelected() && isOpened && onItemClickListener != null) {
					onItemClickListener.onItemClicked(i, items.get(i).getTag());
					break;
				}
			}
		}

		double angle = normalizeAngle(Math.atan2(y - originY, x - originX));

		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			double itemCenterAngle = normalizeAngle(Math.atan2(item.getY() - originY, item.getX() - originX));
			double itemStartAngle = itemCenterAngle - SEGMENT_ANGLE_RADIAN / 2f;
			double itemFinishAngle = itemCenterAngle + SEGMENT_ANGLE_RADIAN / 2f;
			boolean inCurrentSegment;
			if (itemStartAngle < angle && angle < itemFinishAngle) {
				int dx = x - item.getX();
				int dy = y - item.getY();
				double distance = Math.sqrt(dx * dx + dy * dy);
				float scale = (float) (MAX_SCALE - (MAX_SCALE - 1) * distance / MAX_RADIUS);
				if (scale < 1) {
					scale = 1;
				}
				if (item.isScaled()) {
					item.setScale(scale);
				} else {
					item.zoomInAnimated(scale);
				}
				inCurrentSegment = true;
			} else {
				if (isOpened) {
					if (item.isScaled()) {
						item.zoomOutAnimated();
					}
				}
				inCurrentSegment = false;
			}

			if (isOpened) {
				if (item.inCircle(x, y)) {
					item.setSelected(inCurrentSegment);
				} else {
					item.setSelected(false);
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
		Rect dstRect = new Rect(originX - ITEM_RADIUS, originY - ITEM_RADIUS, originX + ITEM_RADIUS, originY + ITEM_RADIUS);
		canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

		if (reversedOrder) {
			for (int i = items.size() - 1; i >= 0; i--) {
				items.get(i).draw(canvas);
			}
		} else {
			for (int i = 0; i < items.size(); i++) {
				items.get(i).draw(canvas);
			}
		}

//		renderLines(canvas);
	}

	private boolean isInOrigin(int x, int y) {
		int dx = x - originX;
		int dy = y - originY;
		return Math.sqrt(dx * dx + dy * dy) <= ITEM_RADIUS;
	}

	@SuppressWarnings("unused")
	private void renderLines(Canvas canvas) {
		Paint paint = new Paint();
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(1);
		paint.setAntiAlias(true);
		for (int i = 0; i < items.size(); i++) {
			canvas.drawLine(originX, originY, items.get(i).getX(), items.get(i).getY(), paint);
		}
	}

	public void setOriginResource(@DrawableRes int originResource) {
		this.originBitmap = BitmapFactory.decodeResource(getResources(), originResource);
	}

	public void setOriginActiveResource(@DrawableRes int originActiveResource) {
		this.originActiveBitmap = BitmapFactory.decodeResource(getResources(), originActiveResource);
	}

	public void reset() {
		items.clear();
	}

	public void addItem(@DrawableRes int itemBackgroundResource,
	                    @DrawableRes int itemActiveBackgroundResource,
	                    @DrawableRes int itemImageResource,
	                    @DrawableRes int labelBackgroundResource,
	                    @StringRes int labelTextResource,
	                    @ColorRes int highlightColor,
	                    String tag) {
		items.add(new Item(
				this,
				BitmapFactory.decodeResource(getResources(), itemBackgroundResource),
				BitmapFactory.decodeResource(getResources(), itemActiveBackgroundResource),
				BitmapFactory.decodeResource(getResources(), itemImageResource),
				BitmapFactory.decodeResource(getResources(), labelBackgroundResource),
				getResources().getString(labelTextResource),
				getResources().getColor(highlightColor),
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics()),
				tag
		));

		calculateStartAngle();
		updateItemCoordinates();
	}

	private void calculateStartAngle() {
		offsetX = (int) (MAX_SCALE * items.get(0).getWidth() / 2f);

		final float startAngleOffset = (items.size() - 1) * 0.5f * SEGMENT_ANGLE_RADIAN;
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
			startAngle = (float) (Math.PI + Math.acos((originX - offsetX) / MAX_RADIUS));
			reversedOrder = false;
		} else if (tooCloseToRightEdge()) {
			startAngle = (float) ((2 * Math.PI - Math.acos((getParentWidth() - originX - offsetX) / MAX_RADIUS))
					- (items.size() - 1) * SEGMENT_ANGLE_RADIAN);
			reversedOrder = true;
		} else {
			reversedOrder = false;
		}

		if (startAngle < 0) {
			startAngle = (float) (2 * Math.PI + startAngle);
		}
	}

	private int getItemHeight() {
		return items.get(0).getHeight();
	}

	private boolean tooCloseToRightEdge() {
		return Math.abs(MAX_RADIUS * Math.cos(startAngle + (items.size() - 1)
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
		Item item;
		for (int i = 0; i < items.size(); i++) {
			float angle = startAngle + i * SEGMENT_ANGLE_RADIAN;
			int centerX = (int) (originX + radius * Math.cos(angle));
			int centerY = (int) (originY + radius * Math.sin(angle));
			item = items.get(i);
			item.update(centerX, centerY, ITEM_RADIUS);
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
				for (Item item : items) {
					item.setScale(animation.getAnimatedFraction());
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
				for (Item item : items) {
					item.setScale(1);
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

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public int getItemCount() {
		return items.size();
	}

	interface OnItemClickListener {

		public void onItemClicked(int index, String tag);

	}

}
