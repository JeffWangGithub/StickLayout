package activity.newsreader.netease.com.sticklayout.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Scroller;

import activity.newsreader.netease.com.sticklayout.R;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/5/24.
 */
public class NRStickyLayout extends LinearLayout {

    public static final int SCROLL_UP = 1;  //向上滚动
    public static final int SCROLL_DOWN = 2; //向下滚动

    private static final String TAG = "NRStickyLayout";
    private Scroller mScroller;
    private View mTopView;
    private View mStickyView;
    private ViewPager mViewPager;
    private int mMaxScrollY;
    private int mMinScrollY;
    private int mStickyViewMarginTop = 0;
    private TopViewScrollCallback mTopViewScrollCallback;
    private int mScrollY;
    private int mMaxViewPagerHeight = 0;
    private IScrollable mScrollable;
    private int mCurDirection;

    public NRStickyLayout(Context context) {
        this(context, null);
    }

    public NRStickyLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NRStickyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);

        mScroller = new Scroller(context);
        mScrollable = new ScrollableViewImp();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取 view
        mTopView = findViewById(R.id.id_nr_stickylayout_top_view);
        mStickyView = findViewById(R.id.id_nr_stickylayout_sticky_view);
        View viewPager = findViewById(R.id.id_nr_stickylayout_viewpage);
        if (!(viewPager instanceof ViewPager)) {
            throw new RuntimeException("id_nr_stickylayout_viewpage must be viewpager");
        }
        mViewPager = ((ViewPager) viewPager);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeight = MeasureSpec.getSize(heightMeasureSpec) - mStickyViewMarginTop;
        int newHeightSpec = MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.getMode(heightMeasureSpec));
        super.onMeasure(widthMeasureSpec, newHeightSpec);
        if (mTopView != null) {
            //不限制顶部 view 的高度
            mTopView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            ViewGroup.LayoutParams params = mViewPager.getLayoutParams();
            mMaxViewPagerHeight = Math.max(mMaxViewPagerHeight, (getMeasuredHeight() - mStickyView.getMeasuredHeight()));
            params.height = mMaxViewPagerHeight;
            setMeasuredDimension(getMeasuredWidth(), mTopView.getMeasuredHeight() + mStickyView.getMeasuredHeight() + mViewPager.getMeasuredHeight());
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mMaxScrollY = mTopView.getMeasuredHeight() - mStickyViewMarginTop;
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.d(TAG, "onStartNestedScroll--" + "ViewCompat.SCROLL_AXIS_VERTICAL = " + ViewCompat.SCROLL_AXIS_VERTICAL + "; nestedScrollAxes= " + nestedScrollAxes);
        //拦截垂直滚动
        mScrollable.setScrollView(target);
        return  (nestedScrollAxes & getNestedScrollAxes()) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.d(TAG, "onNestedScrollAccepted--");

    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.d(TAG, "onStopNestedScroll");
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.d(TAG, "onNestedScroll");
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.d(TAG, "onNestedPreScroll dy = " + dy);
        //上滑
        boolean hiddenTop = dy > 0 && getScrollY() < mMaxScrollY;
        //下滑动，且子 view 不可滑动了
        boolean showTop = dy < 0 && getScrollY() > 0 && mScrollable.isTop();
        if (hiddenTop || showTop) {
            // 滚动自己
            scrollBy(0, dy);
            //消费掉 dy
            consumed[1] = dy;
        }
    }


    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.d(TAG, "onNestedPreFling");
        if (velocityY == 0) {
            return false;
        }
        mCurDirection = velocityY < 0 ? SCROLL_DOWN : SCROLL_UP;
        if (mScrollY > 0) {
            mScroller.fling(0, getScrollY(), (int)velocityX, (int)velocityY, 0, 0,
                    -Integer.MAX_VALUE, Integer.MAX_VALUE);
            invalidate();
            return true;
        }
        return false;
    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.d(TAG, "onNestedFling");
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            final int currY = mScroller.getCurrY();
            if (mCurDirection == SCROLL_UP) {
                //手势向上
                if (isSticked()) {
                    int dy = mScroller.getFinalY() - currY;
                    if (dy > 0) {
                        int duration = mScroller.getDuration() - mScroller.timePassed();
                        mScrollable.smoothScrollBy((int) mScroller.getCurrVelocity(), dy, duration);
                        mScroller.abortAnimation();
                    }
                    Log.d("wgc", "upup---dy" + dy);
                } else {
                    int dy = mScroller.getCurrY() - mScrollY;
                    int toY = getScrollY() + dy;
                    scrollTo(0, toY);
                    invalidate();
                }
            } else {
                //手势向下
                if (mScrollable.isTop()) {
                    int dy = currY - mScrollY;
                    int toY = getScrollY() + dy;
                    scrollTo(0, toY);
                    if (mScrollY <= mMinScrollY) {
                        mScroller.abortAnimation();
                    }
                } else {
                    int dy = mScroller.getFinalY() - mScrollY;
                    int duration = mScroller.getDuration() - mScroller.timePassed();
                    mScrollable.smoothScrollBy(-(int) mScroller.getCurrVelocity(), dy, duration);
                }
                //刷新调用computeScroll() 方法,时时判断是否滚动 top 状态
                invalidate();
            }
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return ViewCompat.SCROLL_AXIS_VERTICAL;
    }


    @Override
    public void scrollTo(int x, int y) {
        y = y < 0 ? 0 : y;
        y = y > mMaxScrollY ? mMaxScrollY : y;
        if (y != getScrollY()) {
            super.scrollTo(x, y);
            if (mTopViewScrollCallback != null) {
                int scrollY = getScrollY();
                float per = mMaxScrollY == 0 ? 0 : ((float) scrollY)/ mMaxScrollY;
                mTopViewScrollCallback.onTopViewScroll(scrollY, per);
                Log.d("aaa", "scroolY = " + scrollY + " percent = " + per);
            }
            mScrollY = getScrollY();
        } else {
            int scrollY = getScrollY();
            if (mScrollY != scrollY) {
                mScrollY = scrollY;
                if (mTopViewScrollCallback != null) {
                    float per = mMaxScrollY == 0 ? 0 : ((float) scrollY)/ mMaxScrollY;
                    mTopViewScrollCallback.onTopViewScroll(mScrollY, per);
                    Log.d("aaa", "y == getScrollY" + " percent = " + per);
                }
            }
        }
    }



    /**
     * 设置 stickyView 悬浮之后距离顶部的高度
     * @param margintTop
     */
    public void setStickyViewMarginTop(int margintTop) {
        if (margintTop < 0)  {
            return;
        }
        mStickyViewMarginTop = margintTop;
    }


    public void setTopViewScrollCallback(TopViewScrollCallback callback) {
        mTopViewScrollCallback = callback;
    }


    public interface TopViewScrollCallback {
        void onTopViewScroll(int currentScrolledY, float scrolledYPercent);
    }


    private boolean isSticked() {
        return mScrollY >= mMaxScrollY;
    }
}
