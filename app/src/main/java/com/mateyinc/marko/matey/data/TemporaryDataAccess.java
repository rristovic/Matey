package com.mateyinc.marko.matey.data;


import com.mateyinc.marko.matey.model.MModel;

import java.util.List;

/**
 * Class to hold temporary data.
 *
 * @param <T> data type, subclass od {@link MModel}
 */
public class TemporaryDataAccess<T extends MModel> {

    protected List<T> mData;
    private final boolean isFreshData;

    public TemporaryDataAccess(List<T> data, boolean isFreshData) {
        mData = data;
        this.isFreshData = isFreshData;
    }

    /**
     * Method for adding item to the list.
     *
     * @param item item to add.
     */
    public void addItem(T item) {
        mData.add(item);
    }

    /**
     * Returns total item count in data.
     *
     * @return number of items.
     */
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Return item based on position.
     *
     * @param position position of item in data list.
     * @return item from the list.
     */
    public T getItem(int position) {
        return mData.get(position);
    }

    /**
     * Method for loading data from another data access object.
     *
     * @param dao object from which data will be loaded.
     */
    public void loadDataFrom(TemporaryDataAccess<T> dao) {
        if (dao.isFreshData) {// Fresh data has been downloaded, so clear old first.
            this.mData.clear();
            this.mData = dao.mData;
        } else
            this.mData.addAll(dao.mData);
    }
}
