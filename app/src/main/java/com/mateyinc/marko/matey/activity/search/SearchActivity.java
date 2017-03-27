package com.mateyinc.marko.matey.activity.search;

import android.content.Context;
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
import com.mateyinc.marko.matey.activity.EndlessScrollListener;
import com.mateyinc.marko.matey.adapters.SearchAdapter;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.internet.events.DownloadEvent;
import com.mateyinc.marko.matey.internet.events.SearchHintEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class SearchActivity extends MotherActivity {

    public interface FragmentChangedListener {
        void fragmentBecameVisible();
        void onSearchPerformed();
    }


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
        super.setChildSupportActionBar();

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
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                FragmentChangedListener fragment = (FragmentChangedListener)
                        mSectionsPagerAdapter.instantiateItem(mPager, position);
                if (fragment != null) {
                    fragment.fragmentBecameVisible();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        tvSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }


        });

        tvSearchInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                performSearch();
            }
        });
    }

    void performSearch() {
        performSearch(true, mPager.getCurrentItem());
    }

    void performSearch(boolean isFreshSearch, int fragPosition) {
        mManager.onSearchQuery(tvSearchInput.getText().toString(), isFreshSearch,
                fragPosition, SearchActivity.this);
        tvSearchInput.dismissDropDown();
        ((FragmentChangedListener)mSectionsPagerAdapter.getItem(fragPosition)).onSearchPerformed();
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

    @Override
    protected void onDestroy() {
        DataAccess.getInstance(this).clearSearch();
        super.onDestroy();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment implements FragmentChangedListener {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private SearchAdapter mAdapter;
        private EndlessScrollListener mScrollListener;
        private RecyclerView mRecycleView;
        private SearchActivity mActivity;

        private int fragPos;

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
        public void onAttach(Context context) {
            super.onAttach(context);
            mActivity = (SearchActivity) context;
            this.fragPos = getArguments().getInt(ARG_SECTION_NUMBER);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int secNum = getArguments().getInt(ARG_SECTION_NUMBER);
            mRecycleView = new RecyclerView(getContext());
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecycleView.setLayoutManager(layoutManager);

            // Don't place scroll listener on TOP SEARCH FRAGMENT
            if (fragPos != 0)
                mScrollListener = new EndlessScrollListener(layoutManager) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        mActivity.performSearch(false, getArguments().getInt(ARG_SECTION_NUMBER));
                    }
                };

            mAdapter = new SearchAdapter((MotherActivity) getContext(), secNum);
            if (secNum == 0) mAdapter.showSection(true);
            mRecycleView.setAdapter(mAdapter);

            return mRecycleView;
        }

        @Override
        public void onResume() {
            super.onResume();
            EventBus.getDefault().register(this);

            if (fragPos != 0)
                mRecycleView.addOnScrollListener(mScrollListener);
            mAdapter.notifyDataChanged();
        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onDownloadEvent(DownloadEvent event) {
            if (mAdapter != null) {
                mAdapter.notifyDataChanged();
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            if (fragPos != 0)
                mRecycleView.removeOnScrollListener(mScrollListener);
            EventBus.getDefault().unregister(this);
        }

        @Override
        public void fragmentBecameVisible() {
            mAdapter.notifyDataChanged();
        }

        @Override
        public void onSearchPerformed() {
            mScrollListener.resetState();
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
            return 4;
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
                case 3:
                    return getString(R.string.search_posts);
            }
            return null;
        }


    }
}
