package com.neetoffice.flowview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;

public class FlowView extends AdapterView<Adapter> implements OnGestureListener  {
	public static final int Down = 0;
	public static final int Right = 1;
	public static final int Up = 2;
	public static final int Left = 3;
	private int translate = Right;
	private boolean touchLock = false;
	
	private static final int SNAP_VELOCITY = 1000;
	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private final static String TAG = "FlowView";
	private boolean firstLayout = true;
	private Scroller scroller;
	private int currentScreen;
	private int touchState = TOUCH_STATE_REST;
	private Adapter adapter;
	private int selectionPosition;
	private VelocityTracker velocityTracker;
	private GestureDetector gestureDetector;

	private int touchSlop;
	private int maximumVelocity;
	private float lastMotionX;
	private float lastMotionY;
	
	private boolean isClick;
	
	private android.widget.AdapterView.OnItemClickListener onItemClickListener;
	
	private ViewSwitchListener viewSwitchListener;
	
	public interface ViewSwitchListener{
		public void onSwitched(View view, int position);
	}
	public FlowView(Context context) {
		super(context);
		init(null);
	}

	public FlowView(Context context,int translate) {
		this(context);
		this.translate = translate;
	}

	public FlowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public FlowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	public void setTranslate(int translate) {
		this.translate = translate;
	}
	public int getTranslate() {
		return translate;
	}
	public boolean isTouchLock() {
		return touchLock;
	}
	public void setTouchLock(boolean touchLock) {
		this.touchLock = touchLock;
	}

	public void setOnViewSwitchListener(ViewSwitchListener viewSwitchListener) {
		this.viewSwitchListener = viewSwitchListener;
	}
	

	@SuppressLint("Recycle")
	private void init(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray styledAttrs = getContext().obtainStyledAttributes(attrs,R.styleable.FlowView);
			translate = styledAttrs.getInt(R.styleable.FlowView_translate, Down);
			touchLock = styledAttrs.getBoolean(R.styleable.FlowView_touchlock, false);
		}
		scroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		touchSlop = configuration.getScaledTouchSlop();
		maximumVelocity = configuration.getScaledMaximumFlingVelocity();
		gestureDetector = new GestureDetector(getContext(),this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		Log.d(TAG, "onMeasure");

		final int width = MeasureSpec.getSize(widthMeasureSpec);
		final int height = MeasureSpec.getSize(heightMeasureSpec);

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
			throw new IllegalStateException(
					"ViewFlow can only be used in EXACTLY mode.");
		}
		
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
		}

		if (firstLayout) {
			scroller.startScroll(0, 0, currentScreen * width, currentScreen	* height, 0);
			firstLayout = false;
		}
		
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		Log.d(TAG, "onLayout");
		Log.d(TAG, "Height : "+getHeight());
		Log.d(TAG, "Width : "+getWidth());
		
		final int count = getChildCount();
		Log.d(TAG, "count :" + count);
		int childTop = 0;
		int childLeft = 0;
		
		switch(translate){
		case Up:			
		case Down:		
			for (int index = 0; index < count; index++) {
				View child = getChildAt(index);
				if (child.getVisibility() != View.GONE) {
					int width = child.getMeasuredWidth();
					int height = child.getMeasuredHeight();
					child.layout(0, childTop, width, childTop + height);
					if(translate == Up && index == count-1)scroller.startScroll(0, 0, 0, childTop, 0);
					childTop += height;
				}
			}
			break;
		case Left:
		case Right:	
			for (int index = 0; index < count; index++) {
				View child = getChildAt(index);
				if (child.getVisibility() != View.GONE) {
					int width = child.getMeasuredWidth();
					int height = child.getMeasuredHeight();
					child.layout(childLeft, 0, childLeft + width, height);
					if(translate == Left && index == count-1)scroller.startScroll(0, 0, childLeft, 0, 0);
					childLeft += width;
				}
			}
			break;
		}
	}

	@Override
	public Adapter getAdapter() {
		return adapter;
	}

	@Override
	public View getSelectedView() {
		return getChildAt(selectionPosition);
	}

	@Override
	public void setAdapter(Adapter adapter) {
		Log.d(TAG, "setAdapter");
		if (adapter == null)
			return;
		this.adapter = adapter;
		removeAllViewsInLayout();
		final int count = adapter.getCount();
		View convertView = null;
		switch(translate){
		case Down:
		case Right:
			for (int index = 0; index < count; index++) {
				View child = adapter.getView(index, convertView, FlowView.this);
				if (child.getTag() != null) {
					convertView = child.findFocus();
				}
				LayoutParams params = child.getLayoutParams();
				if (params == null) {
					Log.d(TAG, "params is null");
					params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
				}
				addViewInLayout(child, index, params);
			}
			break;
		case Up:
		case Left:
			for (int index = 0; index < count; index++) {
				View child = adapter.getView(count-index-1, convertView, FlowView.this);
				if (child.getTag() != null) {
					convertView = child.findFocus();
				}
				LayoutParams params = child.getLayoutParams();
				if (params == null) {
					Log.d(TAG, "params is null");
					params = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
				}
				addViewInLayout(child, index, params);
			}
			break;
		}
		requestLayout();
	}

	@Override
	public void setSelection(int position) {
		selectionPosition = position;
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if(touchLock)return false;
		if (getChildCount() == 0)
			return false;
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastMotionX = x;
			lastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			final int deltaX = (int) Math.abs((lastMotionX - x));
			final int deltaY = (int) Math.abs((lastMotionY - y));
			Log.d(TAG, "deltaX : " + deltaX);
			Log.d(TAG, "deltaY : " + deltaY);
			switch(translate){
			case Up:
			case Down:
				return deltaY > deltaX ?true:false;
			case Left:
			case Right:
				return deltaX > deltaY ?true:false;
			}
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return false;
	}
	@SuppressLint("Recycle")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(touchLock)return true;
		Log.d(TAG, "Height : "+getHeight());
		Log.d(TAG, "Width : "+getWidth());
		if (getChildCount() == 0)
			return false;
		
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);

		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "MotionEvent ACTION DOWN");

			if (!scroller.isFinished()) {
				scroller.abortAnimation();
			}

			lastMotionX = x;
			lastMotionY = y;

			touchState = scroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;

			isClick = true;
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "MotionEvent ACTION MOVE");
			isClick = false;
			
			final int deltaX = (int) (lastMotionX - x);
			final int deltaY = (int) (lastMotionY - y);
			boolean moved = Math.abs(deltaX) > touchSlop || Math.abs(deltaY) > touchSlop;
			if (moved) {
				touchState = TOUCH_STATE_SCROLLING;
			}

			if (touchState == TOUCH_STATE_SCROLLING) {
				Log.d(TAG, "TOUCH_STATE_SCROLLING");
				switch(translate){
				case Down:
				case Up:
					lastMotionY = y;
					final int scrollY = getScrollY();
					Log.d(TAG, "scrollY : " + scrollY);
					if (deltaY < 0) {
						if (scrollY > 0) {
							scrollBy(0, Math.max(-scrollY, deltaY));
						}
					} else if (deltaY > 0) {
						final int availableToScroll = getChildAt(getChildCount() - 1).getBottom()- scrollY - getHeight();
						if (availableToScroll > 0) {
							scrollBy(0, Math.min(availableToScroll, deltaY));
						}
					}
					break;
				case Left:
				case Right:
					lastMotionX = x;
					final int scrollX = getScrollX();
					Log.d(TAG, "scrollX : " + scrollX);
					if (deltaX < 0) {
						if (scrollX > 0) {
							scrollBy(Math.max(-scrollX, deltaX), 0);
						}
					} else if (deltaX > 0) {
						final int availableToScroll = getChildAt(getChildCount() - 1).getRight()- scrollX - getWidth();
						if (availableToScroll > 0) {
							scrollBy(Math.min(availableToScroll, deltaX), 0);
						}
					}
					break;
				}
			}
			return true;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "MotionEvent ACTION UP");
			if(isClick && onItemClickListener !=null){
				final int screenHeight = getHeight();
				final int screenWidth = getWidth();
				int whichScreen = Math.max((getScrollY() + (screenHeight / 2))	/ screenHeight,(getScrollX() + (screenWidth / 2))	/ screenWidth);
				if(translate == Up || translate== Left){
					whichScreen = getChildCount()-whichScreen-1;
				}
				onItemClickListener.onItemClick(this, getChildAt(whichScreen), whichScreen, getItemIdAtPosition(whichScreen));
			}
			
			if (touchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = this.velocityTracker;
				velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
				final int velocityX = (int) velocityTracker.getXVelocity();
				final int velocityY = (int) velocityTracker.getYVelocity();
				final int velocity =Math.max(Math.abs(velocityX), Math.abs(velocityY));

				if (velocity > SNAP_VELOCITY && currentScreen > 0) {
					// Fling hard enough to move left
					snapToScreen(currentScreen - 1);
				} else if (velocity < -SNAP_VELOCITY && currentScreen < getChildCount() - 1) {
					// Fling hard enough to move right
					snapToScreen(currentScreen + 1);
				} else {
					snapToDestination();
				}

				if (this.velocityTracker != null) {
					this.velocityTracker.recycle();
					this.velocityTracker = null;
				}
			}
			snapToDestination();

			touchState = TOUCH_STATE_REST;
			break;
		case MotionEvent.ACTION_CANCEL:
			Log.d(TAG, "MotionEvent ACTION CANCEL");
			
			snapToDestination();
			touchState = TOUCH_STATE_REST;
			break;
		}
		return gestureDetector.onTouchEvent(event);
		
	}

	private void snapToDestination() {
		switch(translate){
		case Down:
		case Up:
			final int screenHeight = getHeight();
			final int whichScreenY = (getScrollY() + (screenHeight / 2))	/ screenHeight;
			snapToScreen(whichScreenY);
			break;
		case Left:
		case Right:
			final int screenWidth = getWidth();
			final int whichScreenX = (getScrollX() + (screenWidth / 2))	/ screenWidth;
			snapToScreen(whichScreenX);
			break;
		}
	}

	private void snapToScreen(int whichScreen) {
		if (!scroller.isFinished())return;
		
		Log.d(TAG, "snapToDestination");
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		
		switch(translate){
		case Down:
		case Up:
			final int newY = whichScreen * getHeight();
			final int deltaY = newY - getScrollY();
			scroller.startScroll(0, getScrollY(), 0, deltaY, Math.abs(deltaY) * 2);
			break;
		case Left:
		case Right:
			final int newX = whichScreen * getWidth();
			final int deltaX = newX - getScrollX();
			scroller.startScroll(getScrollX(), 0, deltaX, 0, Math.abs(deltaX) * 2);
			break;
		}
		if(viewSwitchListener !=null){
			View child = getChildAt(whichScreen);
			viewSwitchListener.onSwitched(child, whichScreen);
		}
		invalidate();
	}	
	
	@Override  
	public void computeScroll() {  
	    if (scroller.computeScrollOffset()) {  
	        scrollTo(scroller.getCurrX(),scroller.getCurrY());  
	        postInvalidate();  
	    }  
	}

	@Override
	public void setOnItemClickListener(android.widget.AdapterView.OnItemClickListener listener) {
		super.setOnItemClickListener(listener);
		onItemClickListener = listener;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,	float velocityY) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}
	
	public void startScroll(int startX, int startY, int dx, int dy, int duration){
	    if (!scroller.computeScrollOffset()) {  
	    	scroller.startScroll(startX, startY, dx, dy, duration);
			invalidate();
		}
	}
}
