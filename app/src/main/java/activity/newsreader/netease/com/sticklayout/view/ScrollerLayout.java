package activity.newsreader.netease.com.sticklayout.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * @title:
 * @description: scroller 使用
 * 1. new 对象
 * 2. mScroller.startScroll()触发滚动
 * 3. 复写computeScroll，判断是否需要继续
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/6/4.
 */
public class ScrollerLayout extends ViewGroup {

    private Scroller mScroller;
    private int mTouchSlop;
    private int leftBorder;
    private int rightBorder;
    private float mXDown, mXLastMove, mXMove;

    public ScrollerLayout(Context context) {
        this(context, null);
    }

    public ScrollerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 第一步， new Scoller()
        mScroller = new Scroller(context);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childView = getChildAt(i);
                childView.layout(i * childView.getMeasuredWidth(), 0, (i + 1) * childView.getMeasuredWidth(), childView.getMeasuredHeight());
            }
            //初始化左右边界
            leftBorder = getChildAt(0).getLeft();
            rightBorder = getChildAt(getChildCount() - 1).getRight();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mXDown = ev.getRawX();
                mXLastMove = mXDown;
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getRawX();
                float diff = Math.abs(mXMove - mXDown);
                mXLastMove = mXMove;
                if (diff > mTouchSlop) {
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getRawX();
                float scrooledX = mXLastMove - mXMove;
                if (getScrollX() + scrooledX < leftBorder) {
                    scrollTo(leftBorder, 0);
                    return true;
                } else if (getScrollX() + getWidth() + scrooledX > rightBorder) {
                    scrollTo(rightBorder - getWidth(), 0);
                    return true;
                }
                scrollBy((int) scrooledX, 0);
                mXLastMove = mXMove;
                break;
            case MotionEvent.ACTION_UP:
                int targetIndex = (getScrollX() + getWidth()/2)/getWidth();
                int dx = targetIndex * getWidth() - getScrollX();

                // 第二步， 调用 startScroll()方法初始化滚动数据并刷新界面
                // param1滚动开始时 X 的左边；param2滚动开始时 Y 的坐标；
                // param3 横向滚动距离，正值向左； param4纵向滚动距离，正值向上
                mScroller.startScroll(getScrollX(), 0, dx, 0);
                invalidate();
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        // 第三步, 重新 computeScroll方法
        if (mScroller.computeScrollOffset()) {
            //true 表示滚动尚未完成；
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }
}
