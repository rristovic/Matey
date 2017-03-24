package com.mateyinc.marko.matey.activity.search;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.adapters.SearchAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.internet.events.SearchHintEvent;
import com.mateyinc.marko.matey.internet.operations.OperationType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SearchActivity extends MotherActivity {

    private AutoCompleteTextView tvSearchInput;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private OperationManager mManager;
    private SearchHintAdapter mSearchHintAdapter;
    private ViewPager mPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();
        setListeners();
    }

    private void init() {
        // Setup tabs and pager
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        TabLayout tabs = (TabLayout) findViewById(R.id.tlSearchTab);
        mPager = (ViewPager) findViewById(R.id.vpSearchPager);
        mPager.setAdapter(mSectionsPagerAdapter);
        tabs.setupWithViewPager(mPager);

        mManager = OperationManager.getInstance(this);
        mSearchHintAdapter = new SearchHintAdapter(this);
        tvSearchInput = (AutoCompleteTextView) findViewById(R.id.atvSearch);
        tvSearchInput.setAdapter(mSearchHintAdapter);
    }

    private void setListeners() {
        tvSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mManager.onSearchQuery(tvSearchInput.getText().toString()
                            ,true, mPager.getCurrentItem(), SearchActivity.this);
                    return true;
                }
                return false;
            }
        });

        tvSearchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mManager.onSearchQuery(tvSearchInput.getText().toString(), true,
                        mPager.getCurrentItem(), SearchActivity.this);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadEvent(SearchHintEvent event) {
        mSearchHintAdapter.setData(event.mData);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
        private SearchAdapter mAdapter;

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
            int secNum = getArguments().getInt(ARG_SECTION_NUMBER);
            RecyclerView recyclerView = new RecyclerView(getContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);

            mAdapter = new SearchAdapter((MotherActivity) getContext(), secNum);
            if (secNum == 0) mAdapter.showSection(true);
            recyclerView.setAdapter(mAdapter);

//            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
//            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return recyclerView;
        }

        @Override
        public void onStart() {
            super.onStart();
            EventBus.getDefault().register(this);
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onDownloadEvent(DownloadEvent event) {
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 0 &&
                    event.operationType == OperationType.SEARCH_TOP) {
                if (mAdapter != null) {
                    mAdapter.setData(DataAccess.getInstance(getContext()).mSearchResults);
                }
            }
        }

        @Override
        public void onStop() {
            super.onStop();
            EventBus.getDefault().unregister(this);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends android.support.v4.app.FragmentStatePagerAdapter {

        public SectionsPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
            //do nothing here! no call to super.restoreState(arg0, arg1);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
//            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.search_all);
                case 1:
                    return getString(R.string.search_people);
                case 2:
                    return getString(R.string.search_groups);
            }
            return null;
        }


    }
}
