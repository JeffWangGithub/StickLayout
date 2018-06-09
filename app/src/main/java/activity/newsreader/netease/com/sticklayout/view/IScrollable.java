package activity.newsreader.netease.com.sticklayout.view;

import android.view.View;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/6/5.
 */
public interface IScrollable {
    /**
     * 根据速度，距离，和事件进行平滑滚动
     * @param yVelocit
     * @param distance
     * @param duration
     */
    void smoothScrollBy(int yVelocit, int distance, int duration);

    /**
     * 是否滑动到顶部
     */
    boolean isTop();


    View getScrollView();

    void setScrollView(View scrollView);

}
