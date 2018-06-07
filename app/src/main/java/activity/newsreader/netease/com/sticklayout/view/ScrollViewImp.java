package activity.newsreader.netease.com.sticklayout.view;

import android.os.Build;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ScrollView;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/6/5.
 */
public class ScrollViewImp implements IScrollable {

    private View mScrollView;
    @Override
    public void smoothScrollBy(int yVelocity, int distance, int duration) {
        View scrollableView = getScrollView();
        if (scrollableView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) scrollableView;
            if (Build.VERSION.SDK_INT >= 21) {
                absListView.fling(yVelocity);
            } else {
                absListView.smoothScrollBy(distance, duration);
            }
        } else if (scrollableView instanceof ScrollView) {
            ((ScrollView) scrollableView).fling(yVelocity);
        } else if (scrollableView instanceof RecyclerView) {
            ((RecyclerView) scrollableView).fling(0, yVelocity);
        } else if (scrollableView instanceof WebView) {
            ((WebView) scrollableView).flingScroll(0, yVelocity);
        }
    }

    @Override
    public boolean isTop() {
        boolean result = false;
        View scrollableView = getScrollView();

        if (scrollableView instanceof AdapterView) {
            result = isAdapterViewTop((AdapterView) scrollableView);
        }
        if (scrollableView instanceof ScrollView) {
            result = isScrollViewTop((ScrollView) scrollableView);
        }
        if (scrollableView instanceof RecyclerView) {
            result = isRecyclerViewTop((RecyclerView) scrollableView);
        }
//        if (scrollableView instanceof ViewGroup) {
//            result = isViewGroupTop((ViewGroup)scrollableView);
//        }
        return result;
    }

    @Override
    public View getScrollView() {
        return mScrollView;
    }

    @Override
    public void setScrollView(View scrollView) {
        this.mScrollView = scrollView;
    }

    public ScrollViewImp() {

    }



    private boolean isAdapterViewTop(AdapterView adapterView) {
        if (adapterView != null) {
            int firstVisiblePosition = adapterView.getFirstVisiblePosition();
            View childAt = adapterView.getChildAt(0);
            if (childAt == null || (firstVisiblePosition == 0 && childAt.getTop() == 0)) {
                return true;
            }
        }
        return false;
    }

    private boolean isScrollViewTop(ScrollView scrollView) {
        if (scrollView != null) {
            int scrollViewY = scrollView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }


    private boolean isRecyclerViewTop(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                View childAt = recyclerView.getChildAt(0);
                if (childAt == null || (firstVisibleItemPosition == 0 && childAt.getTop() == 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isViewGroupTop(ViewGroup scrollableView) {
        if (scrollableView != null) {
            int scrollViewY = scrollableView.getScrollY();
            return scrollViewY <= 0;
        }
        return false;
    }


}
