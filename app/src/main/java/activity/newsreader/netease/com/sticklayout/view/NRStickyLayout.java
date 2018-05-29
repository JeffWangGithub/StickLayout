package activity.newsreader.netease.com.sticklayout.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import activity.newsreader.netease.com.sticklayout.R;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/5/24.
 */
public class NRStickyLayout extends LinearLayout {

    private static final String TAG = "NRStickyLayout";
    private OverScroller mScroller;
    private View mTopView;
    private View mStickyView;
    private ViewPager mViewPager;
    private int mTopViewHeight;
    private int TOP_CHILD_FLING_THRESHOLD = 3;
    private ValueAnimator mOffsetAnimator;
    private View mCurrentScrollView;
    private int mStickyViewMarginTop = 0;
    private TopViewScrollCallback mTopViewScrollCallback;
    private int mScrollY;
    private int mMaxViewPagerHeight = 0;


    public NRStickyLayout(Context context) {
        this(context, null);
    }

    public NRStickyLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NRStickyLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);

        mScroller = new OverScroller(context);
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
        mTopViewHeight = mTopView.getMeasuredHeight() - mStickyViewMarginTop;
    }


    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        Log.d(TAG, "onStartNestedScroll--" + "ViewCompat.SCROLL_AXIS_VERTICAL = " + ViewCompat.SCROLL_AXIS_VERTICAL + "; nestedScrollAxes= " + nestedScrollAxes);
        mCurrentScrollView = target;
//        if (target instanceof CommonStateView) {
//            return nestedScrollAxes == 0;
//        }
        //拦截垂直滚动
        return  (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
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
        Log.d(TAG, "onNestedPreScroll");
        //上滑
        boolean hiddenTop = dy > 0 && getScrollY() < mTopViewHeight;
        //下滑动，且子 view 不可滑动了
        boolean showTop = dy < 0 && getScrollY() >= 0 && !ViewCompat.canScrollVertically(target, -1);
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
        mCurrentScrollView = target;
        return false;
    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.d(TAG, "onNestedFling");
        mCurrentScrollView = target;
        if (velocityY == 0) {
            return true;
        }
        if (target instanceof RecyclerView && velocityY < 0) {
            //target是滚动的子 view
            RecyclerView recyclerView = (RecyclerView) target;
            View firstChild = recyclerView.getChildAt(0);
            int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
            consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
        }
        if (!consumed) {
            animateScroll(velocityY, computeDuration(0), consumed);
        } else {
            animateScroll(velocityY, computeDuration(velocityY), consumed);
        }
        return true;
    }

    @Override
    public int getNestedScrollAxes() {
        return 0;
    }


    /**
     * 根据速度指定动画持续的时间
     *
     * @param velocity
     * @return
     */
    private int computeDuration(float velocity) {
        final int distance;
        if (velocity > 0) {
            //向上 fling， velocity > 0
            distance = Math.abs(mTopView.getHeight() - getScrollY());
        } else {
            distance = Math.abs(mTopView.getHeight() - (mTopView.getHeight() - getScrollY()));
        }
        final int duration;
        velocity = Math.abs(velocity);
        if (velocity > 0) {
            duration = 3 * Math.round(1000 * (distance / velocity));
        } else {
            final float distanceRatio = (float) distance / getHeight();
            duration = (int) ((distanceRatio + 1) * 150);
        }
        return duration;
    }

    private void animateScroll(float velocityY, int duration, boolean consumed) {
        final int currentOffset = getScrollY();
        final int topHeight = mTopView.getHeight();
        if (mOffsetAnimator == null) {
            mOffsetAnimator = new ValueAnimator();
//            mOffsetAnimator.setInterpolator(mInterpolater);
            mOffsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (animation.getAnimatedValue() instanceof Integer) {
                        scrollTo(0, ((Integer) animation.getAnimatedValue()));
                    }
                }
            });
        } else {
            mOffsetAnimator.cancel();
        }
        mOffsetAnimator.setDuration(Math.min(duration, 500));
        if (velocityY >= 0) {
            //向上滚动, 从当前偏移位置到最大高度
            mOffsetAnimator.setIntValues(currentOffset, topHeight);
            mOffsetAnimator.start();
        } else {
            //向下滚动
            if (mCurrentScrollView instanceof RecyclerView) {
                ((RecyclerView) mCurrentScrollView).addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);
                        //监听 recyclerView 滚动  滚动到顶部且是 IDLE 状态时滚动 topView
                        View firstChild = recyclerView.getChildAt(0);
                        int firstChildAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
                        if (RecyclerView.SCROLL_STATE_IDLE == newState && firstChildAdapterPosition == 0) {
                            if (mOffsetAnimator != null) {
                                mOffsetAnimator.setIntValues(currentOffset, 0);
                                mOffsetAnimator.start();
                                if (mCurrentScrollView != null) {
                                    ((RecyclerView) mCurrentScrollView).removeOnScrollListener(this);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        y = y < 0 ? 0 : y;
        y = y > mTopViewHeight ? mTopViewHeight : y;
        if (y != getScrollY()) {
            super.scrollTo(x, y);
            if (mTopViewScrollCallback != null) {
                int scrollY = getScrollY();
                float per = mTopViewHeight == 0 ? 0 : ((float) scrollY)/mTopViewHeight;
                mTopViewScrollCallback.onTopViewScroll(scrollY, per);
                Log.d(TAG, "scroolY = " + scrollY + " percent = " + per);
            }
            mScrollY = getScrollY();
        } else {
            int scrollY = getScrollY();
            if (mScrollY != scrollY) {
                mScrollY = scrollY;
                if (mTopViewScrollCallback != null) {
                    float per = mTopViewHeight == 0 ? 0 : ((float) scrollY)/mTopViewHeight;
                    mTopViewScrollCallback.onTopViewScroll(mScrollY, per);
                    Log.d(TAG, "y == getScrollY" + " percent = " + per);
                }
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
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
}
