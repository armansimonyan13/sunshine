package com.sunshine;

import android.content.Context;
import android.graphics.Point;
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

public class SatelliteMenu implements View.OnTouchListener, SatelliteMenuCore.OnItemClickListener {

	private final int touchBoundaryMargin;
	private int x;
	private int y;
	private PopupWindow popupWindow;
	private View parentView;
	private SatelliteMenuCore satelliteMenuCore;
	private View touchBoundary;
	private OnItemClickListener onItemClickListener;
	private OnMenuListener onMenuListener;
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

		public SatelliteMenu build() {
			return new SatelliteMenu(anchorView, originResource, activeOriginResource,
					itemRadiusResource, maxRadiusResource, itemSegmentResource,
					itemBackgroundResource, itemActiveBackgroundResource, labelBackgroundResource,
					highlightColorResource);
		}

	}

	SatelliteMenu(View view, @DrawableRes int originResource, @DrawableRes int originActiveResource,
	              @DimenRes int itemRadius, @DimenRes int maxRadius, @DimenRes int itemSegment,
	              @DrawableRes int itemBackgroundResource, @DrawableRes int itemActiveBackgroundResource,
	              @DrawableRes int labelBackgroundResource, @ColorRes int highlightColorResource) {
		this.parentView = view;
		this.itemBackgroundResource = itemBackgroundResource;
		this.itemActiveBackgroundResource = itemActiveBackgroundResource;
		this.labelBackgroundResource = labelBackgroundResource;
		this.highlightColorResource = highlightColorResource;

		popupWindow = new PopupWindow(view);
		satelliteMenuCore = new SatelliteMenuCore(view.getContext(),
				parentView.getResources().getDimensionPixelSize(itemRadius),
				parentView.getResources().getDimensionPixelSize(maxRadius),
				parentView.getResources().getDimension(itemSegment));
		satelliteMenuCore.setOriginResource(originResource);
		satelliteMenuCore.setOriginActiveResource(originActiveResource);
		popupWindow.setContentView(satelliteMenuCore);
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

		popupWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, x, y);

		if (onMenuListener != null) {
			onMenuListener.onMenuShow();
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
			initializeSatelliteMenuCore();
		}

		boolean result = satelliteMenuCore.processMotionEvent(event);

		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			default:
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
					if (onMenuListener != null) {
						onMenuListener.onMenuDismiss();
					}
				}
		}

		return result;
	}

	private void initializeSatelliteMenuCore() {
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

		satelliteMenuCore.setParentWidth(width);
		satelliteMenuCore.setParentHeight(height);
		satelliteMenuCore.setParentX(x);
		satelliteMenuCore.setParentY(y);
	}

	public void reset() {
		satelliteMenuCore.reset();
	}

	public void addItem(@DrawableRes int itemBackgroundResource,
	                    @DrawableRes int itemActiveBackgroundResource,
	                    @DrawableRes int itemImageResource,
	                    @DrawableRes int labelBackgroundResource,
	                    @StringRes int labelTextResource,
	                    @ColorRes int highlightColor,
	                    String tag) {
		satelliteMenuCore.addItem(
				itemBackgroundResource,
				itemActiveBackgroundResource,
				itemImageResource,
				labelBackgroundResource,
				labelTextResource,
				highlightColor,
				tag);
	}

	public void addItem(@DrawableRes int itemImageResource, @StringRes int labelTextResource, String tag) {
		satelliteMenuCore.addItem(
				itemBackgroundResource,
				itemActiveBackgroundResource,
				itemImageResource,
				labelBackgroundResource,
				labelTextResource,
				highlightColorResource,
				tag);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;

		satelliteMenuCore.setOnItemClickListener(this);
	}

	public void setOnMenuListener(OnMenuListener onMenuListener) {
		this.onMenuListener = onMenuListener;
	}

	public int getItemCount() {
		return satelliteMenuCore.getItemCount();
	}

	@Override
	public void onItemClicked(int index, String tag) {
		if (popupWindow.isShowing() && isInTouchBoundary()) {
			onItemClickListener.onItemClicked(index, tag);
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

	public interface OnItemClickListener {

		void onItemClicked(int index, String tag);

	}

	public interface OnMenuListener {

		void onMenuShow();

		void onMenuDismiss();

	}

}
