package com.example.converter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    //private String TAB_TITLES[] = new String[]{String.valueOf(R.string.tab_text_1), R.string.tab_text_2, R.string.tab_text_3};
    private String TAB_TITLES[] = new String[]{"Валюты", "Конвертер"};

    private BlankFragment1 m1stFragment;
    private BlankFragment2 m2ndFragment;
    //private final Context context;

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    // Here we can finally safely save a reference to the created
    // Fragment, no matter where it came from (either getItem() or
    // FragmentManger). Simply save the returned Fragment from
    // super.instantiateItem() into an appropriate reference depending
    // on the ViewPager position.
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        switch (position) {
            case 0:
                m1stFragment = (BlankFragment1) createdFragment;
                break;
            case 1:
                m2ndFragment = (BlankFragment2) createdFragment;
                break;
        }
        return createdFragment;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
//        return PlaceholderFragment.newInstance(position + 1);
        switch (position) {
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