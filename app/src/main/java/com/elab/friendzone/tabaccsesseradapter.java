package com.elab.friendzone;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class tabaccsesseradapter extends FragmentPagerAdapter {
    public tabaccsesseradapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {

            case 0:
                Chatsfragment chatsfragment = new Chatsfragment();
                return chatsfragment;
            case 1:
                Groupschatfragment groupsfragment=new Groupschatfragment();
                return groupsfragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {

            case 0:
                return "Chats";
            case 1:
                return "Groups";
            default:
                return null;

        }

    }
}
