package leaf.prod.app.adapter;

import java.util.List;

import androidx.core.app.Fragment;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentStatePagerAdapter;

/**
 *
 */
public class ViewPageAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> data;

    private String[] titles;

    public ViewPageAdapter(FragmentManager fm, List<Fragment> data, String[] titles) {
        super(fm);
        this.data = data;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
