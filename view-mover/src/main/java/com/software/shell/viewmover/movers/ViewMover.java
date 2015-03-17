/*
 * Copyright 2015 Shell Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * File created: 2015-03-08 21:41:24
 */

package com.software.shell.viewmover.movers;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import com.software.shell.viewmover.configuration.MovingDetails;

/**
 * Abstract class, which contains the base view movement logic
 * <p>
 * Is extended by subclasses, which implements specific movement logic
 *
 * @author shell
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class ViewMover {

	/**
	 * Logging tag
	 */
	private static final String LOG_TAG = String.format("[FAB][%s]", ViewMover.class.getSimpleName());

	/**
	 * {@link android.view.View}, which is to be moved
	 */
	private final View view;

	/**
	 * Creates an instance of the {@link com.software.shell.viewmover.movers.ViewMover}
	 *
	 * @param view {@link android.view.View}, which is to be moved
	 */
	ViewMover(View view) {
		this.view = view;
	}

	/**
	 * Is called to calculate the end X point of the view's left bound
	 * <p>
	 * Used to check whether there is enough space inside parent container to move the view
	 * to the left
	 *
	 * @param xAxisDelta X-axis delta in actual pixels
	 * @return end X point of the view's left bound
	 */
	abstract int calculateEndLeftBound(float xAxisDelta);

	/**
	 * Is called to calculate the end X point of the view's right bound
	 * <p>
	 * Used to check whether there is enough space inside parent container to move the view
	 * to the right
	 *
	 * @param xAxisDelta X-axis delta in actual pixels
	 * @return end X point of the view's right bound
	 */
	abstract int calculateEndRightBound(float xAxisDelta);

	/**
	 * Is called to calculate the end Y point of the view's top bound
	 * <p>
	 * Used to check whether there is enough space inside parent container to move the view
	 * to the top
	 *
	 * @param yAxisDelta Y-axis delta in actual pixels
	 * @return end Y point of the view's top bound
	 */
	abstract int calculateEndTopBound(float yAxisDelta);

	/**
	 * Is called to calculate the end Y point of the view's bottom bound
	 * <p>
	 * Used to check whether there is enough space inside parent container to move the view
	 * to the bottom
	 *
	 * @param yAxisDelta Y-axis delta in actual pixels
	 * @return end Y point of the view's bottom bound
	 */
	abstract int calculateEndBottomBound(float yAxisDelta);

	/**
	 * Is called when move animation completes
	 * <p>
	 * Used to change the view position withing its parent container
	 *
	 * @param xAxisDelta X-axis delta in actual pixels
	 * @param yAxisDelta Y-axis delta in actual pixels
	 */
	abstract void changeViewPosition(float xAxisDelta, float yAxisDelta);

	/**
	 * Returns the view, which is to be moved
	 *
	 * @return view to be moved
	 */
	View getView() {
		return view;
	}

	/**
	 * Returns the parent container of the view, which is to be moved
	 *
	 * @return parent container of the view to be moved
	 */
	View getParentView() {
		return (View) view.getParent();
	}

	/**
	 * Moves the view based on the {@link com.software.shell.viewmover.configuration.MovingDetails}
	 *
	 * @param details details of the move action
	 */
	public void move(MovingDetails details) {
		if (isPreviousAnimationCompleted()) {
			final MovingDetails mDetails = createUpdatedMovingDetails(details);
			if (isMoveNonZero(mDetails)) {
				final Animation moveAnimation = createAnimation(mDetails);
				Log.v(LOG_TAG, String.format("View is about to be moved at: delta X-axis = %s, delta Y-axis = %s",
						mDetails.getXAxisDelta(), mDetails.getYAxisDelta()));
				view.startAnimation(moveAnimation);
			}
		}
	}

	/**
	 * Checks whether previous animation on the view completed
	 *
	 * @return true if previous animation on the view completed, otherwise false
	 */
	boolean isPreviousAnimationCompleted() {
		final Animation previousAnimation = view.getAnimation();
		final boolean previousAnimationCompleted = previousAnimation == null || previousAnimation.hasEnded();
		if (!previousAnimationCompleted) {
			Log.w(LOG_TAG, "Unable to move the view. View is being currently moving");
		}
		return previousAnimationCompleted;
	}

	/**
	 * Checks whether X-axis and Y-axis delta of the moving details are not {@code null}
	 *
	 * @param details moving details, which needs to be checked
	 * @return true, if X-axis and Y-axis delta of the moving details are not {@code null},
	 *         otherwise false
	 */
	boolean isMoveNonZero(MovingDetails details) {
		final boolean moveNonZero = details.getXAxisDelta() != 0.0f
				|| details.getYAxisDelta() != 0.0f;
		if (!moveNonZero) {
			Log.w(LOG_TAG, "Zero movement detected. No movement will be performed");
		}
		return moveNonZero;
	}

	/**
	 * Creates an updated copy of the {@link com.software.shell.viewmover.configuration.MovingDetails}
	 * with X-axis and Y-axis deltas updated based on calculations returned from
	 * {@link #updateXAxisDelta(com.software.shell.viewmover.configuration.MovingDetails)} and
	 * {@link #updateYAxisDelta(com.software.shell.viewmover.configuration.MovingDetails)}
	 *
	 * @param details moving details, which needs to be updated
	 */
	private MovingDetails createUpdatedMovingDetails(final MovingDetails details) {
		final MovingDetails mDetails = new MovingDetails(details);
		updateXAxisDelta(mDetails);
		updateYAxisDelta(mDetails);
		Log.v(LOG_TAG, String.format("Updated moving details values: X-axis from %s to %s, Y-axis from %s to %s",
				details.getXAxisDelta(), mDetails.getXAxisDelta(), details.getYAxisDelta(), mDetails.getYAxisDelta()));
		return mDetails;
	}

	/**
	 * Updates the X-axis delta in moving details based on checking whether
	 * there is enough space left to move the view horizontally
	 *
	 * @param details moving details, which X-axis delta needs to be updated in
	 */
	private void updateXAxisDelta(MovingDetails details) {
		if (!hasHorizontalSpaceToMove(details.getXAxisDelta())) {
			Log.w(LOG_TAG, "Unable to move the view horizontally. No horizontal space left to move");
			details.setXAxisDelta(0.0f);
		}
	}

	/**
	 * Updates the Y-axis delta in moving details based on checking whether
	 * there is enough space left to move the view vertically
	 *
	 * @param details moving details, which Y-axis delta needs to be updated in
	 */
	private void updateYAxisDelta(MovingDetails details) {
		if (!hasVerticalSpaceToMove(details.getYAxisDelta())) {
			Log.w(LOG_TAG, "Unable to move the view vertically. No vertical space left to move");
			details.setYAxisDelta(0.0f);
		}
	}

	/**
	 * Checks whether there is enough space left to move the view horizontally within
	 * its parent container
	 * <p>
	 * Calls {@link #calculateEndLeftBound(float)} and {@link #calculateEndRightBound(float)}
	 * to calculate the resulting X coordinate of view's left and right bounds
	 *
	 * @param xAxisDelta X-axis delta in actual pixels
	 * @return true if there is enough space to move the view horizontally, otherwise false
	 */
	private boolean hasHorizontalSpaceToMove(float xAxisDelta) {
		final int parentWidth = getParentView().getWidth();
		Log.v(LOG_TAG, "Parent view width is: " + parentWidth);
		final int endLeftBound = calculateEndLeftBound(xAxisDelta);
		final int endRightBound = calculateEndRightBound(xAxisDelta);
		Log.v(LOG_TAG, String.format("Calculated end bounds: left = %s, right = %s", endLeftBound, endRightBound));
		return endLeftBound >= 0 && endRightBound <= parentWidth;
	}

	/**
	 * Checks whether there is enough space left to move the view vertically within
	 * its parent container
	 * <p>
	 * Calls {@link #calculateEndTopBound(float)} and {@link #calculateEndBottomBound(float)}
	 * to calculate the resulting Y coordinate of view's top and bottom bounds
	 *
	 * @param yAxisDelta Y-axis delta in actual pixels
	 * @return true if there is enough space to move the view vertically, otherwise false
	 */
	private boolean hasVerticalSpaceToMove(float yAxisDelta) {
		final int parentHeight = getParentView().getHeight();
		Log.v(LOG_TAG, "Parent view height is: " + parentHeight);
		final int endTopBound = calculateEndTopBound(yAxisDelta);
		final int endBottomBound = calculateEndBottomBound(yAxisDelta);
		Log.v(LOG_TAG, String.format("Calculated end bounds: top = %s, bottom = %s", endTopBound, endBottomBound));
		return endTopBound >= 0 && endBottomBound <= parentHeight;
	}

	/**
	 * Creates the moving animation
	 * <p>
	 * Configures the moving animation based on moving details
	 *
	 * @param details details, which is used to configure the moving animation
	 * @return moving animation
	 */
	private Animation createAnimation(MovingDetails details) {
		final Animation animation = new TranslateAnimation(0, details.getXAxisDelta(), 0, details.getYAxisDelta());
		animation.setFillEnabled(true);
		animation.setFillBefore(false);
		animation.setDuration(details.getAnimationDuration());
		final Interpolator interpolator = details.getAnimationInterpolator();
		if (interpolator != null) {
			animation.setInterpolator(interpolator);
		}
		animation.setAnimationListener(new MoveAnimationListener(details));
		return animation;
	}

	/**
	 * Move animation listener class
	 * <p>
	 * Used to listen the animation and call the {@link #changeViewPosition(float, float)}
	 * when animation completes
	 */
	private class MoveAnimationListener implements Animation.AnimationListener {

		/**
		 * Moving details
		 */
		private final MovingDetails details;

		/**
		 * Creates an instance of the
		 * {@link com.software.shell.viewmover.movers.ViewMover.MoveAnimationListener}
		 *
		 * @param details moving details
		 */
		private MoveAnimationListener(MovingDetails details) {
			this.details = details;
		}

		@Override
		public void onAnimationStart(Animation animation) {
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
		}

		/**
		 * Is called when animation completes
		 * <p>
		 * Calls the {@link #changeViewPosition(float, float)} giving the subclasses
		 * the ability to change the position of the view based on their logic
		 *
		 * @param animation moving animation
		 */
		@Override
		public void onAnimationEnd(Animation animation) {
			changeViewPosition(details.getXAxisDelta(), details.getYAxisDelta());
		}

	}

}
