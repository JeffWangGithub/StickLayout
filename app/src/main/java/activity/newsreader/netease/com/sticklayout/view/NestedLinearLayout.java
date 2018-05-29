package activity.newsreader.netease.com.sticklayout.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/5/24.
 */
public class NestedLinearLayout extends LinearLayout implements NestedScrollingChild {
    private NestedScrollingChildHelper mNestedScrollHelper;
    private float mLastTouchX, mLastTouchY;
    private final int[] offset = new int[2]; //偏移量
    private final int[] consumed = new int[2]; //消费
    private int mTouchSlop;
    private boolean isBeingNestedScrolling;//是否正在嵌套滚动过程中


    public NestedLinearLayout(Context context) {
        this(context, null);
    }

    public NestedLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mNestedScrollHelper = new NestedScrollingChildHelper(this);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(ViewConfiguration.get(context));
        setNestedScrollingEnabled(true);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isIntercept = false;
        if (isNestedScrollingEnabled()) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastTouchX = ev.getX();
                    mLastTouchY = ev.getY();
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = ev.getX() - mLastTouchX;
                    float dy = ev.getY() - mLastTouchY;
                    if (Math.abs(dy) > Math.abs(dx) && Math.abs(dy) >= mTouchSlop) {
                        isIntercept = true;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    stopNestedScroll();
                    break;
            }
        }
        return isIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isNestedScrollingEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    mLastTouchX = event.getX();
                    mLastTouchY = event.getY();
                    startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    mLastTouchX = mLastTouchY = 0;
                    stopNestedScroll();
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    final float x = event.getX();
                    final float y = event.getY();
                    float dx = mLastTouchX - x;
                    float dy = mLastTouchY - y;
                    if (isBeingNestedScrolling || Math.abs(dy) > Math.abs(dx) && Math.abs(dy) >= mTouchSlop) {
                        dispatchNestedPreScroll(0, (int) dy, consumed, offset);
                    }
                    break;
                }
            }
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        isBeingNestedScrolling = false;
        mNestedScrollHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        return mNestedScrollHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        isBeingNestedScrolling = true;
        return mNestedScrollHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
