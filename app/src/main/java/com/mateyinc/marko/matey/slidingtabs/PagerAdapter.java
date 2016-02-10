package com.mateyinc.marko.matey.slidingtabs;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.mateyinc.marko.matey.tabfragments.AllFriendsFragment;
import com.mateyinc.marko.matey.tabfragments.GroupsFragment;
import com.mateyinc.marko.matey.tabfragments.HomeFragment;

public class PagerAdapter extends FragmentPagerAdapter {

	CharSequence Titles[]; // This will Store the Titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    int NumbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
 
 
    // Build a Constructor and assign the passed Values to appropriate values in the class
    public PagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
 
        this.Titles = mTitles;
        this.NumbOfTabs = mNumbOfTabsumb;
 
    }
 
    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {
 
        if(position == 0)
        {
            HomeFragment tab1 = new HomeFragment();
            return tab1;
        }
        else if (position == 1)
        {
            AllFriendsFragment tab2 = new AllFriendsFragment();
            return tab2;
        }
        else
        {
        	GroupsFragment tab3 = new GroupsFragment();
            return tab3;
        }
 
 
    }
 
    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return Titles[position];
    }
 
    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return NumbOfTabs;
    }

}
