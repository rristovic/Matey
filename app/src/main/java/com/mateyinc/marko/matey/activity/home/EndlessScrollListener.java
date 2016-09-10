package com.mateyinc.marko.matey.activity.home;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    // The minimum amount of items to have below your current scroll position
    // before mLoading more.
    private int mVisibleThreshold = 5;
    // The current offset index of data you have loaded
    private int mCurrentPage = 0;
    // The total number of items in the dataset after the last load
    private int mPreviousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    public boolean mLoading = true;
    // Sets the starting page index
    private int mStartingPageIndex = 0;

    RecyclerView.LayoutManager mLayoutManager;

    public EndlessScrollListener(LinearLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    public EndlessScrollListener(GridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        mVisibleThreshold = mVisibleThreshold * layoutManager.getSpanCount();
    }

    public EndlessScrollListener(StaggeredGridLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
        mVisibleThreshold = mVisibleThreshold * layoutManager.getSpanCount();
    }

    public int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            }
            else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null);
            // get maximum element within the list
            lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        } else if (mLayoutManager instanceof GridLayoutManager) {
            lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
        }

//        // If the total item count is zero and the previous isn't, assume the
//        // list is invalidated and should be reset back to initial state
//        if (totalItemCount < mPreviousTotalItemCount) {
//            this.mCurrentPage = this.mStartingPageIndex;
//            this.mPreviousTotalItemCount = totalItemCount;
//            if (totalItemCount == 0) {
//                this.mLoading = true;
//            }
//        }
//        // If it’s still mLoading, we check to see if the dataset count has
//        // changed, if so we conclude it has finished mLoading and update the current page
//        // number and total item count.
//        if (mLoading && (totalItemCount > mPreviousTotalItemCount)) {
//            mLoading = false;
//            mPreviousTotalItemCount = totalItemCount;
//        }

        // check to see if we have breached
        // the mVisibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!mLoading && (lastVisibleItemPosition + mVisibleThreshold) > totalItemCount) {
            mCurrentPage++;
            onLoadMore(mCurrentPage, totalItemCount);
            mLoading = true;
        }
    }

    // Defines the process for actually mLoading more data based on page
    public abstract void onLoadMore(int page, int totalItemsCount);

    public void setVisibleThreshold(int visibleThreshold) {
        this.mVisibleThreshold = visibleThreshold;
    }

}
