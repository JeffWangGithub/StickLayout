package activity.newsreader.netease.com.sticklayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import activity.newsreader.netease.com.sticklayout.fragment.OtherTabFragment;
import activity.newsreader.netease.com.sticklayout.fragment.TabFragment;
import activity.newsreader.netease.com.sticklayout.view.NRStickyLayout2;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/6/6.
 */
public class TestActivity3 extends FragmentActivity {

    private String[] mTitles = new String[] { "简介", "评价", "Other" };

    private Fragment[] mFragments = new Fragment[mTitles.length];

    private NRStickyLayout2 mSticktyLayout;
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test3);

        mSticktyLayout = (NRStickyLayout2) findViewById(R.id.sticky_view_layout);
        mViewPager = (ViewPager) findViewById(R.id.id_nr_stickylayout_viewpage);
        initData();

        if (mFragments[0] instanceof TabFragment) {
            View scrollView = ((TabFragment) mFragments[0]).getScrollView();
            mSticktyLayout.setCurrentScrollableView(scrollView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentItem = mViewPager.getCurrentItem();
        Fragment item = mAdapter.getItem(currentItem);
        if (item instanceof TabFragment) {
            View scrollView = ((TabFragment) item).getScrollView();
            mSticktyLayout.setCurrentScrollableView(scrollView);
        }
    }

    private void initData() {
        for (int i = 0; i < mTitles.length; i++) {
            if (i == mTitles.length - 1) {
                mFragments[i] = Fragment.instantiate(this, OtherTabFragment.class.getName());
            } else {
                mFragments[i] = Fragment.instantiate(this, TabFragment.class.getName());
            }
        }
        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public void setPrimaryItem(ViewGroup container, int position, Object object) {
                super.setPrimaryItem(container, position, object);
                setScrollView();
            }
        };
        mViewPager.setAdapter(mAdapter);
    }


    private void setScrollView() {
        View currentScrollableView = mSticktyLayout.getCurrentScrollableView();
        int currentItem = mViewPager.getCurrentItem();
        Fragment item = mAdapter.getItem(currentItem);
        if (item instanceof TabFragment) {
            View scrollView = ((TabFragment) item).getScrollView();
            if (currentScrollableView != scrollView) {
                mSticktyLayout.setCurrentScrollableView(scrollView);
            }
        }
    }
}
