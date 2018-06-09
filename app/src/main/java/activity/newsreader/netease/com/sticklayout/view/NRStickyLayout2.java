package activity.newsreader.netease.com.sticklayout.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.LinearLayout;
import android.widget.Scroller;

import activity.newsreader.netease.com.sticklayout.R;

/**
 * @title: 自己处理事件实现 viewpage 的 sticky layout
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/6/4.
 */
public class NRStickyLayout2 extends LinearLayout{
    private static final String TAG = "StickyLayout2";

    public static final int SCROLL_UP = 1;  //向上滚动
    public static final int SCROLL_DOWN = 2; //向下滚动

    private Scroller mScroller;
    private IScrollable mScrollable;
    private int mTouchSlop;     //系统判定是 move 的最小距离
    private int mMaximumFlingVelocity;  //最大的认定为fling 动作的最小速度
    private View mHeaderView;
    private ViewPager mViewPager;
    private int mHeaderHeight;
    private int mMaxScrollY;    //当前 view 可滚动的最大值
    private int mMinScrollY;    //当前 view 可滚动的最小值
    private VelocityTracker mVelocityTracker;
    private float mLastX,mLastY;
    private int mCurY;  //当前滚动过的距离
    private int mCurDirection = 0;
    private int mLastScrollerY;
    private boolean verticalScrollFlag = false;
    private float mDownX;  //第一次按下的x坐标
    private float mDownY;  //第一次按下的y坐标

    public NRStickyLayout2(Context context) {
        this(context, null);
    }

    public NRStickyLayout2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NRStickyLayout2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
        mScrollable = new ScrollableViewImp();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        setOrientation(VERTICAL);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取 view
        mHeaderView = findViewById(R.id.id_nr_stickylayout_top_view);
        View viewPager = findViewById(R.id.id_nr_stickylayout_viewpage);
        if (!(viewPager instanceof ViewPager)) {
            throw new RuntimeException("id_nr_stickylayout_viewpage must be viewpager");
        }
        mViewPager = ((ViewPager) viewPager);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //不限定 header 的高度
        measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, MeasureSpec.UNSPECIFIED, 0);
        mHeaderHeight = mHeaderView.getMeasuredHeight();
        mMaxScrollY = mHeaderHeight;
        //调整整个 view 的高度
        int newHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) + mMaxScrollY, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float currentX = ev.getX();
        float currentY = ev.getY();

        obtainVelocityTracker(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                verticalScrollFlag = false;
                mDownX = currentX;
                mDownY = currentY;
                mLastX = currentX;
                mLastY = currentY;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = currentY - mLastY;         //和上一次move相比的距离
                float shiftX = Math.abs(currentX - mDownX);     //当前触摸位置与第一次 down 事件发生时的便宜
                float shiftY = Math.abs(currentY - mDownY);     //当前触摸位置与第一次 down 事件发生时的便宜
                mLastY = currentY;
                //此处使用
                if (shiftY > shiftX && shiftY > mTouchSlop) {
                    //垂直滚动
                    verticalScrollFlag = true;
                } else if (shiftX > shiftY && shiftX > mTouchSlop){
                    //水平
                    verticalScrollFlag = false;
                }
                if (verticalScrollFlag && (!isStickied() || mScrollable.isTop())) {
                    scrollBy(0, (int) (-deltaY + 0.5));
                    if (mCurY > 0 && !isStickied()) {
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (verticalScrollFlag) {
                    //指定速度单位为1000毫秒，表示每1000毫秒允许fling 的最大距离为mMaximumFlingVelocity
                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                    float yVelocity = mVelocityTracker.getYVelocity();
                    mCurDirection = yVelocity > 0 ? SCROLL_DOWN : SCROLL_UP;
                    // 移动内容坐标系的方向与事件速度方向相反
                    mScroller.fling(0, getScrollY(), 0, (int) -yVelocity, 0, 0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
                    mLastScrollerY = getScrollY();
                    invalidate();//重新绘制，会引起 computeScroll方法的执行 ---- 如果不刷则会卡顿一下，因为不走computeScroll方法的执行
                }
                recycleVelocityTracker();
                break;
            case MotionEvent.ACTION_CANCEL:
                recycleVelocityTracker();
                break;

        }
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            //未执行完成返回 true
            final int currY = mScroller.getCurrY();
            if (mCurDirection == SCROLL_UP) {
                //手势向上
                if (isStickied()) {
                    //scroller.getFinalY()获取 fling 时最后的 y
                    int dy = mScroller.getFinalY() - currY; //剩余距离
                    int duration = mScroller.getDuration() - mScroller.timePassed();//剩余事件
                    mScrollable.smoothScrollBy((int) mScroller.getCurrVelocity(), dy, duration);
                    mScroller.abortAnimation();
                    return;
                } else {
                    scrollTo(0, currY); //移动外层布局
                }
            } else {
                //手势向下
                if (mScrollable.isTop()) {
                    //到顶部
                    int dy = currY - mLastScrollerY;
                    int toY = getScrollY() + dy;
                    scrollTo(0, toY);
                    if (mCurY <= mMinScrollY) {
                        mScroller.abortAnimation();
                        return;
                    }
                }
                //向下滑动时，初始状态可能不在顶部，所以要一直重绘，让computeScroll一直调用
                //确保代码能进入上面的if判断  --- key
                invalidate();
            }
            mLastScrollerY = currY;
        }
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y >= mMaxScrollY) {
            y = mMaxScrollY;
        } else if (y <= mMinScrollY) {
            y = mMinScrollY;
        }
        mCurY = y;
        super.scrollTo(x, y);
    }

    @Override
    public void scrollBy(int x, int y) {
        int scrollY = getScrollY();
        int toY = scrollY + y;
        if (toY >= mMaxScrollY) {
            toY = mMaxScrollY;
        } else if (toY <= mMinScrollY) {
            toY = mMinScrollY;
        }
        y = toY - scrollY;
        super.scrollBy(x, y);
    }

    public void setCurrentScrollableView(View scrollableView) {
        mScrollable.setScrollView(scrollableView);
    }

    public View getCurrentScrollableView() {
        return mScrollable.getScrollView();
    }


    private boolean isStickied() {
        return mCurY >= mMaxScrollY;
    }

    private VelocityTracker obtainVelocityTracker(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        return mVelocityTracker;
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

}
