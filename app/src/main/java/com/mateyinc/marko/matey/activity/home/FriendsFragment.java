package com.mateyinc.marko.matey.activity.home;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mateyinc.marko.matey.R;

import java.util.ArrayList;

/**
 * Created by Sarma on 8/27/2016.
 */
public class FriendsFragment extends Fragment {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private Context mContext;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * NotificationsFragment callback for when an item has been selected.
         */
        public void onItemSelected();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        TabLayout tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);

        pager.setAdapter(mSectionsPagerAdapter);
        tabs.setupWithViewPager(pager);

        // Iterate over all tabs and set the custom tab view
//        for (int i = 0; i < tabs.getTabCount(); i++) {
//            TabLayout.Tab tab = tabs.getTabAt(i);
//            tab.setCustomView(mSectionsPagerAdapter.getTabView(i));
//        }

        return rootView;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_notif_msg, container, false);
            ListView listView = (ListView) rootView;

            // Dummy data
            ArrayList<String> data = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                data.add("Prijatelj " + i);
            }

            // TODO - set custom adapters
            listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data));
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
//            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 3;
        }


        // TODO - define custom tab views
//        public View getTabView(int position) {
//            // Given you have a custom layout in `res/layout/custom_tab.xml` with a TextView and ImageView
//            View v = LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
//            TextView tv = (TextView) v.findViewById(R.id.textView);
//            tv.setText(tabTitles[position]);
//            ImageView img = (ImageView) v.findViewById(R.id.imgView);
//            img.setImageResource(imageResId[position]);
//            return v;
//        }

//        public SectionsPagerAdapter(FragmentManager fm) {
//            super(fm);
//        }

        //        @Override
//        public android.app.Fragment getItem(int position) {
//            // getItem is called to instantiate the fragment for the given page.
//            // Return a PlaceholderFragment (defined as a static inner class below).
//            return PlaceholderFragment.newInstance(position + 1);
//        }
//
//        @Override
//        public int getCount() {
//            // Show 3 total pages.
//            return 3;
//        }
//
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.friends_section_name);
                case 1:
                    return getString(R.string.frrequests_section_name);
                case 2:
                    return getString(R.string.suggested_section_name);
            }
            return null;
        }


    }
}
