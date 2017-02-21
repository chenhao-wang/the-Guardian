package com.android.theguardian;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyPagerAdapter extends FragmentStatePagerAdapter {
    private List<NewsFragment> fragmentList = new ArrayList<>();

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void init(List<String> list) {
        fragmentList.clear();
        for (String title : list) {
            fragmentList.add(NewsFragment.newInstance(title));
        }
    }

    public void refreshAllFragment(List<String> list) {
        for (String title : list) {
            for (NewsFragment fragment : fragmentList) {
                //最好使用唯一标示来判定是否刷了正确的Fragment 比如id
                String pageTitle = fragment.getTitle();
                if (pageTitle != null && pageTitle.equals(title)) {
                    fragment.refreshData(title);
                }
            }
        }
    }
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        if (fragmentList != null && position < fragmentList.size()) {
            return fragmentList.get(position);
        }
        return null;
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (getItem(position) instanceof BaseFragment) {
            return ((BaseFragment) getItem(position)).getTitle();
        }
        return super.getPageTitle(position);
    }


}
