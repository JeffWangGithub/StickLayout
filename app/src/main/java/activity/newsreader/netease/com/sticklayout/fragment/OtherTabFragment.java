package activity.newsreader.netease.com.sticklayout.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import activity.newsreader.netease.com.sticklayout.R;
import activity.newsreader.netease.com.sticklayout.view.NestedLinearLayout;

/**
 * @title:
 * @description:
 * @company: Netease
 * @author: GlanWang
 * @version: Created on 18/5/24.
 */
public class OtherTabFragment extends TabFragment {

    private NestedLinearLayout mNll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mNll = (NestedLinearLayout) view.findViewById(R.id.nested_lll);
    }


    @Override
    public View getScrollView() {
        return mNll;
    }
}
