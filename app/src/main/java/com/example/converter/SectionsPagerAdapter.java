package com.example.converter;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    //private String TAB_TITLES[] = new String[]{String.valueOf(R.string.tab_text_1), R.string.tab_text_2, R.string.tab_text_3};
    private String TAB_TITLES[] = new String[] { "Валюты", "Конвертер" };
    private final Context context;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
//        return PlaceholderFragment.newInstance(position + 1);
        switch (position){
            case 0:
                return new BlankFragment1();
            case 1:
                return new BlankFragment2();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }
}