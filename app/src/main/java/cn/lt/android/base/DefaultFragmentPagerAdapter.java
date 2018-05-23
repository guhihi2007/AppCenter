package cn.lt.android.base;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

public class DefaultFragmentPagerAdapter extends FragmentPagerAdapter  {

    private List<BaseFragment> fragmentList = null;
    private List<String> titleList = null;

    public DefaultFragmentPagerAdapter(FragmentManager fm, List<BaseFragment> fragmentList,
                                       List<String> titleList) {
        super(fm);
        this.fragmentList = fragmentList;
        this.titleList = titleList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titleList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    public void setFragment(int pos) {
        fragmentList.get(pos).setUserVisibleHint(true);
    }
}
