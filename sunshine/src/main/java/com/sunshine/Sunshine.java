package com.sunshine;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

public class Sunshine implements View.OnTouchListener, Core.OnItemSelectListener {

	public interface OnItemSelectListener {

		void onItemSelected(int index, String tag);

	}

	public interface OnActionListener {

		void onMenuShow();

		void onMenuDismiss();

	}

	private final int touchBoundaryMargin;
	private int x;
	private int y;
	private PopupWindow popupWindow;
	private View parentView;
	private Core core;
	private View touchBoundary;
	private OnItemSelectListener onItemSelectListener;
	private OnActionListener onActionListener;
	private boolean isWindowCovered;

	@DrawableRes
	private int itemBackgroundResource;
	@DrawableRes
	private int itemActiveBackgroundResource;
	@DrawableRes
	private int labelBackgroundResource;
	@ColorRes
	private int highlightColorResource;

	public static class Builder {

		private View anchorView;
		@DrawableRes
		private int originResource;
		@DrawableRes
		private int activeOriginResource;
		@DimenRes
		private int itemRadiusResource;
		@DimenRes
		private int maxRadiusResource;
		@DimenRes
		private int itemSegmentResource;
		@DrawableRes
		private int itemBackgroundResource;
		@DrawableRes
		private int itemActiveBackgroundResource;
		@DrawableRes
		private int labelBackgroundResource;
		@ColorRes
		private int highlightColorResource;

		public Builder(View view) {
			this.anchorView = view;

			originResource = R.drawable.ic_menu_satellite__origin;
			activeOriginResource = R.drawable.ic_menu_satellite__origin_active;
			itemRadiusResource = R.dimen.menu_satellite__item_radius;
			maxRadiusResource = R.dimen.menu_satellite__max_radius;
			itemSegmentResource = R.dimen.menu_satellite__item_segment;

			itemBackgroundResource = R.drawable.ic_menu_satellite_image_background;
			itemActiveBackgroundResource = R.drawable.ic_menu_satellite_image_background_active;
			labelBackgroundResource = R.drawable.ic_menu_satellite_text_background;
			highlightColorResource = R.color.orange;
		}

		public Builder setOriginResource(@DrawableRes int originResource) {
			this.originResource = originResource;
			return this;
		}

		public Builder setActiveOriginResource(@DrawableRes int activeOriginResource) {
			this.activeOriginResource = activeOriginResource;
			return this;
		}

		public Builder setItemRadiusResource(@DimenRes int itemRadiusResource) {
			this.itemRadiusResource = itemRadiusResource;
			return this;
		}

		public Builder setMaxRadiusResource(@DimenRes int maxRadiusResource) {
			this.maxRadiusResource = maxRadiusResource;
			return this;
		}

		public Builder setItemSegmentResource(@DimenRes int itemSegmentResource) {
			this.itemSegmentResource = itemSegmentResource;
			return this;
		}

		public Builder setItemBackgroundResource(@DrawableRes int itemBackgroundResource) {
			this.itemBackgroundResource = itemBackgroundResource;
			return this;
		}

		public Builder setItemActiveBackgroundResource(@DrawableRes int itemActiveBackgroundResource) {
			this.itemActiveBackgroundResource = itemActiveBackgroundResource;
			return this;
		}

		public Builder setLabelBackgroundResource(@DrawableRes int labelBackgroundResource) {
			this.labelBackgroundResource = labelBackgroundResource;
			return this;
		}

		public Builder setHighlightColorResource(@ColorRes int highlightColorResource) {
			this.highlightColorResource = highlightColorResource;
			return this;
		}

		public Sunshine build() {
			return new Sunshine(anchorView, originResource, activeOriginResource,
					itemRadiusResource, maxRadiusResource, itemSegmentResource,
					itemBackgroundResource, itemActiveBackgroundResource, labelBackgroundResource,
					highlightColorResource);
		}

	}

	private Sunshine(View view, @DrawableRes int originResource, @DrawableRes int originActiveResource,
			 @DimenRes int itemRadius, @DimenRes int maxRadius, @DimenRes int itemSegment,
			 @DrawableRes int itemBackgroundResource, @DrawableRes int itemActiveBackgroundResource,
			 @DrawableRes int labelBackgroundResource, @ColorRes int highlightColorResource) {
		this.parentView = view;
		this.itemBackgroundResource = itemBackgroundResource;
		this.itemActiveBackgroundResource = itemActiveBackgroundResource;
		this.labelBackgroundResource = labelBackgroundResource;
		this.highlightColorResource = highlightColorResource;

		popupWindow = new PopupWindow(view);
		core = new Core(view.getContext(),
				parentView.getResources().getDimensionPixelSize(itemRadius),
				parentView.getResources().getDimensionPixelSize(maxRadius),
				parentView.getResources().getDimension(itemSegment));
		core.setOriginResource(originResource);
		core.setOriginActiveResource(originActiveResource);
		popupWindow.setContentView(core);
		touchBoundaryMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15,
				parentView.getResources().getDisplayMetrics());

		parentView.setOnTouchListener(this);
	}

	public void show() {
		if (touchBoundary != null) {
			if (!isInTouchBoundary()) {
				return;
			}
		}

		int x;
		int y;
		int width;
		int height;
		if (isWindowCovered) {
			x = 0;
			y = 0;
			WindowManager windowManager = (WindowManager) parentView.getContext().getSystemService(Context.WINDOW_SERVICE);
			Point size = new Point();
			windowManager.getDefaultDisplay().getSize(size);
			width = size.x;
			height = size.y;
		} else {
			int[] parentLocation = new int[2];
			parentView.getLocationOnScreen(parentLocation);
			x = parentLocation[0];
			y = parentLocation[1];
			width = parentView.getWidth();
			height = parentView.getHeight();
		}

		popupWindow.setWidth(width);
		popupWindow.setHeight(height);
		if (Build.VERSION.SDK_INT >= 22) {
			popupWindow.setAttachedInDecor(false);
		}

		popupWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, x, y);

		if (onActionListener != null) {
			onActionListener.onMenuShow();
		}
	}

	private boolean isInTouchBoundary() {
		if (touchBoundary != null) {
			int[] touchBoundaryLocation = new int[2];
			touchBoundary.getLocationOnScreen(touchBoundaryLocation);
			return (touchBoundaryLocation[0] - touchBoundaryMargin < x
					&& x < touchBoundaryLocation[0] + touchBoundary.getWidth() + touchBoundaryMargin
					&& touchBoundaryLocation[1] - touchBoundaryMargin < y
					&& y < touchBoundaryLocation[1] + touchBoundary.getHeight() + touchBoundaryMargin);
		} else {
			return true;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		x = (int) event.getRawX();
		y = (int) event.getRawY();

		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			initCore();
		}

		boolean result = core.processMotionEvent(event);

		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			default:
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
					if (onActionListener != null) {
						onActionListener.onMenuDismiss();
					}
				}
		}

		return result;
	}

	private void initCore() {
		int width;
		int height;
		int x;
		int y;
		if (isWindowCovered) {
			WindowManager windowManager = (WindowManager) parentView.getContext().getSystemService(Context.WINDOW_SERVICE);
			Point size = new Point();
			windowManager.getDefaultDisplay().getSize(size);
			width = size.x;
			height = size.y;
			x = 0;
			y = 0;
		} else {
			width = parentView.getWidth();
			height = parentView.getHeight();
			int[] parentLocation = new int[2];
			parentView.getLocationOnScreen(parentLocation);
			x = parentLocation[0];
			y = parentLocation[1];
		}

		core.setParentWidth(width);
		core.setParentHeight(height);
		core.setParentX(x);
		core.setParentY(y);
	}

	public void reset() {
		core.reset();
	}

	public void addItem(
			@DrawableRes int itemBackgroundResource,
			@DrawableRes int itemActiveBackgroundResource,
			@DrawableRes int itemImageResource,
			@DrawableRes int labelBackgroundResource,
			@StringRes int labelTextResource,
			@ColorRes int highlightColor,
			String tag) {
		core.addItem(
				R.dimen.body_width,
				R.dimen.body_height,
				R.dimen.body_touch_margin,
				R.dimen.body_padding,
				R.integer.label_width,
				R.dimen.label_height,
				R.dimen.label_bottom_margin,
				R.dimen.text_size,
				itemBackgroundResource,
				itemActiveBackgroundResource,
				itemImageResource,
				labelBackgroundResource,
				labelTextResource,
				highlightColor,
				tag);
	}

	public void addItem(@DrawableRes int itemImageResource, @StringRes int labelTextResource, String tag) {
		addItem(
				itemBackgroundResource,
				itemActiveBackgroundResource,
				itemImageResource,
				labelBackgroundResource,
				labelTextResource,
				highlightColorResource,
				tag);
	}

	public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
		this.onItemSelectListener = onItemSelectListener;

		core.setOnItemSelectListener(this);
	}

	public void setOnActionListener(OnActionListener onActionListener) {
		this.onActionListener = onActionListener;
	}

	public int getItemCount() {
		return core.getItemCount();
	}

	@Override
	public void onItemSelected(int index, String tag) {
		if (popupWindow.isShowing() && isInTouchBoundary()) {
			onItemSelectListener.onItemSelected(index, tag);
		}
	}

	public void setTouchBoundary(View touchBoundary) {
		this.touchBoundary = touchBoundary;
	}

	public boolean isWindowCovered() {
		return isWindowCovered;
	}

	public void setWindowCovered(boolean isWindowCovered) {
		this.isWindowCovered = isWindowCovered;
	}

}
