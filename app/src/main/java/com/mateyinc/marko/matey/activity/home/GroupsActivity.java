package com.mateyinc.marko.matey.activity.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.adapters.GroupsRecyclerViewAdapter;
import com.mateyinc.marko.matey.inall.InsideActivity;

import java.util.ArrayList;

/**
 * Created by Sarma on 8/30/2016.
 */
public class GroupsActivity extends InsideActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        init();
    }

    private void init() {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);

        pager.setAdapter(mSectionsPagerAdapter);
        tabs.setupWithViewPager(pager);
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
                data.add("Grupa " + i);
            }

            // TODO - set custom adapters
            listView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, data));
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class GroupsFriendsFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private GroupsRecyclerViewAdapter mFriendsAdapter;

        public GroupsFriendsFragment() {
        }

        public static GroupsFriendsFragment newInstance(int sectionNumber) {
            GroupsFriendsFragment fragment = new GroupsFriendsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

//            mFriendsAdapter = new GroupsRecyclerViewAdapter(getContext());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_bulletins, container, false);
            RecyclerView listView = (RecyclerView) rootView;

            // Dummy data
            ArrayList<String> data = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                data.add("Grupa " + i);
            }

            // TODO - set custom adapters
            listView.setAdapter(mFriendsAdapter);
//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class GroupsDiscoverFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";
        private GroupsRecyclerViewAdapter mFriendsAdapter;

        public GroupsDiscoverFragment() {
        }

        public static GroupsDiscoverFragment newInstance(int sectionNumber) {
            GroupsDiscoverFragment fragment = new GroupsDiscoverFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mFriendsAdapter = new GroupsRecyclerViewAdapter(getContext());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_groups_discover, container, false);
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rvMightInterestYou);

            // Dummy data
            ArrayList<String> data = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                data.add("Grupa " + i);
            }

            // TODO - set custom adapters
            // Give it a horizontal LinearLayoutManager to make it a horizontal ListView
            LinearLayoutManager layoutManager = new LinearLayoutManager(this.getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mFriendsAdapter);
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
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return GroupsFriendsFragment.newInstance(position + 1);
                case 1:
                    return GroupsDiscoverFragment.newInstance(position + 1);
                case 2:
                    return PlaceholderFragment.newInstance(position + 1);
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }


        // TODO - define custom tab views
//        public View getTabView(int position) {
//        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.groups_joined_section_name);
                case 1:
                    return getString(R.string.groups_discover_section_name);
                case 2:
                    return getString(R.string.groups_invites_section_name);
            }
            return null;
        }


    }


}
