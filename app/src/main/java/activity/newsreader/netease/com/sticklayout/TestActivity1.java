package activity.newsreader.netease.com.sticklayout;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * @title: 测试 NRStickyLayout,嵌套滚动
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/6/6.
 */
public class TestActivity1 extends FragmentActivity {
    private String[] mTitles = new String[] { "简介", "评价", "Other" };

    private Fragment[] mFragments = new Fragment[mTitles.length];

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test1);

        mViewPager = (ViewPager) findViewById(R.id.id_nr_stickylayout_viewpage);
        initData();

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
        };
        mViewPager.setAdapter(mAdapter);
    }
}
