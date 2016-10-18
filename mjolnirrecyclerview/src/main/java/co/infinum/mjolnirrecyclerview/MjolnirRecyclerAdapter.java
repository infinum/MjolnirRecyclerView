package co.infinum.mjolnirrecyclerview;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Custom implementation of RecyclerView.Adapter, which has following features:
 * 1. Header and footer support
 * 2. DiffUtil support
 * 3. ArrayAdapter methods
 * <p>
 * Use it in combination with {@link co.infinum.mjolnirrecyclerview.MjolnirRecyclerView} to also get support for Empty views.
 * <p>
 * Created by Željko Plesac on 27/09/16.
 */
public abstract class MjolnirRecyclerAdapter<E> extends RecyclerView.Adapter<MjolnirRecyclerAdapter.ViewHolder> {

    public static final int TYPE_HEADER = 111;

    public static final int TYPE_FOOTER = 222;

    public static final int TYPE_ITEM = 333;

    protected OnClickListener<E> listener;

    protected OnNextPageListener nextPageListener;

    private Context context;

    private List<E> items;

    private int footerViewId;

    private int headerViewId;

    private View footerView;

    private View headerView;

    private UpdateItemsTask updateItemsTask;

    public MjolnirRecyclerAdapter(Context context, Collection<E> list) {
        this.context = context;
        this.items = new ArrayList<>(list);
    }

    @Override
    public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Check if we have to inflate ItemViewHolder of HeaderFooterHolder
        if (viewType == TYPE_ITEM) {
            return onCreateItemViewHolder(parent, viewType);
        } else if (viewType == TYPE_HEADER) {
            return onCreateHeaderViewHolder(headerViewId, parent);
        } else if (viewType == TYPE_FOOTER) {
            return onCreateFooterViewHolder(footerViewId, parent);
        }

        return null;
    }

    /**
     * Override if you need a custom implementation.
     */
    protected ViewHolder onCreateFooterViewHolder(int footerViewId, ViewGroup parent) {
        if (footerView == null) {
            footerView = LayoutInflater.from(getContext()).inflate(footerViewId, parent, false);
        }
        return new HeaderFooterViewHolder(footerView);

    }

    /**
     * Override if you need a custom implementation.
     */
    protected ViewHolder onCreateHeaderViewHolder(int headerViewId, ViewGroup parent) {
        if (headerView == null) {
            headerView = LayoutInflater.from(getContext()).inflate(headerViewId, parent, false);
        }
        return new HeaderFooterViewHolder(headerView);

    }

    protected abstract ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(MjolnirRecyclerAdapter.ViewHolder holder, int position) {
        //check what type of view our position is

        switch (getItemViewType(position)) {
            case TYPE_ITEM:
                E item = get(position);
                holder.bind(item, position, Collections.emptyList());
                break;
            default:
                //Nothing, for now.
                break;

        }
    }

    @Override
    public void onBindViewHolder(MjolnirRecyclerAdapter.ViewHolder holder, int position, List<Object> payloads) {

        //check what type of view our position is
        switch (getItemViewType(position)) {
            case TYPE_ITEM:
                E item = get(position);
                holder.bind(item, position, payloads);
                break;
            default:
                //Nothing, for now.
                break;
        }
    }

    /**
     * Item count is calculated as sum of items, headers and footers size.
     *
     * @return Adapter item count.
     */
    @Override
    public int getItemCount() {

        int itemCount = items != null ? items.size() : 0;

        if (hasFooter()) {
            itemCount++;
        }

        if (hasHeader()) {
            itemCount++;
        }

        return itemCount;

    }

    /**
     * Returns items size. In case if there are no headers or footers, the result will be the same as for getItemCount() method.
     */
    public int getCollectionCount() {
        return items.size();
    }

    public void setOnClickListener(OnClickListener<E> listener) {
        this.listener = listener;
    }

    public void setOnNextPageListener(OnNextPageListener nextPageListener) {
        this.nextPageListener = nextPageListener;
    }

    public Context getContext() {
        return context;
    }

    /**
     * Cancels the UpdateItems AsyncTask, so that we don't perform any UI updates. This method must be called when your activity
     * or fragment is about to be destroyed, so that we don't risk any UI exceptions.
     */
    public void cancel() {
        if (updateItemsTask != null) {
            updateItemsTask.cancel(true);
        }
    }

    public void add(E item) {
        int position = items.size();
        items.add(item);
        notifyItemInserted(position);
    }

    // region ArrayAdapter methods

    public void addAll(Collection<E> collection) {
        int position = items.size();
        items.addAll(collection);
        notifyItemRangeInserted(position, collection.size());
    }

    public void add(E item, int index) {
        index = calculateIndex(index);

        items.add(index, item);
        notifyItemInserted(index);
    }

    public void addAll(Collection<E> collection, int index) {
        index = calculateIndex(index);

        items.addAll(index, collection);
        notifyItemRangeInserted(index, collection.size());
    }

    public void remove(E item) {
        int position = items.indexOf(item);
        if (items.remove(item)) {
            notifyItemRemoved(position);
        }
    }

    public void removeAll(Collection<E> collection) {
        if (items.removeAll(collection)) {
            notifyDataSetChanged();
        }
    }

    public void remove(int index) {
        index = calculateIndex(index);

        if (items.remove(index) != null) {
            notifyItemRemoved(index);
        }
    }

    /**
     * Clears current items.
     */
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public E get(int index) {
        index = calculateIndex(index);
        return items.get(index);
    }

    public Collection<E> getAll() {
        return new ArrayList<>(items);
    }

    public void set(E item, int index) {
        index = calculateIndex(index);

        items.set(index, item);
        notifyItemChanged(index);
    }

    public void reset(Collection<E> collection) {
        reset(collection, null);
    }

    /**
     * Resets the current adapter state - clear current items, add new ones and execute UpdateItems task.
     *
     * @param collection Collection of new items, which are added to adapter.
     * @param callback   DiffUtil callback, which is used to update the items.
     */
    public void reset(Collection<E> collection, @Nullable DiffUtil.Callback callback) {
        items.clear();
        items.addAll(collection);
        if (callback != null) {
            updateItemsTask = new UpdateItemsTask();
            updateItemsTask.execute(callback);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * Calculate the correct item index - we have to subtract the number of headers of index value as the RecyclerView doesn't distinguish
     * between header rows and item rows.
     *
     * @param index RecyclerView row index.
     * @return correct item index.
     */
    private int calculateIndex(int index) {
        index = index - (hasHeader() ? 1 : 0);

        if (index >= items.size()) {
            throw new IllegalStateException("Index has to be defined in range from 0 to items.size() - 1!");
        } else {
            return index;
        }
    }

    /**
     * Add a footer to this adapter.
     * This method has higher priority than {@link #addFooter(android.view.View)}.
     *
     * @param footerViewId layout resource id
     */
    public void addFooter(@LayoutRes int footerViewId) {
        int position = getCollectionCount() + (hasHeader() ? 1 : 0);
        this.footerViewId = footerViewId;
        notifyItemInserted(position);
    }

    /**
     * Add a footer view to this adapter.
     * This method has lower priority than {@link #addFooter(int)}.
     *
     * @param footerView layout view
     */
    public void addFooter(View footerView) {
        int position = getCollectionCount() + (hasHeader() ? 1 : 0);
        this.footerView = footerView;
        notifyItemInserted(position);
    }

    /**
     * Add a header to this adapter.
     * This method has higher priority than {@link #addHeader(android.view.View)}.
     *
     * Deprecated in version 1.1.0, use  {@link #addHeader(View headerView, boolean shouldReplace)} instead.
     *
     * @param headerViewId layout resource id
     */
    @Deprecated
    public void addHeader(@LayoutRes int headerViewId) {
        this.headerViewId = headerViewId;
        notifyItemInserted(0);
    }

    /**
     * Add a header view to this adapter. If header already exists, it will be replaced depending on the {@param shouldReplace} value.
     *
     * @param headerViewId  layout view
     * @param shouldReplace should we replace header if it already exists
     * @return true if header was added/replaced, false otherwise.
     */
    public boolean addHeader(@LayoutRes int headerViewId, boolean shouldReplace) {
        if (!hasHeader() || hasHeader() && shouldReplace) {
            this.headerViewId = headerViewId;
            notifyItemInserted(0);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a header view to this adapter.
     * This method has lower priority than {@link #addHeader(int)}.
     *
     * Deprecated in version 1.1.0, use  {@link #addHeader(View headerView, boolean shouldReplace)} instead.
     *
     * @param headerView layout view
     */
    @Deprecated
    public void addHeader(View headerView) {
        this.headerView = headerView;
        notifyItemInserted(0);
    }

    /**
     * Add a header view to this adapter. If header already exists, it will be replaced depending on the {@param shouldReplace} value.
     *
     * @param headerView    layout view
     * @param shouldReplace should we replace header if it already exists
     * @return true if header was added/replaced, false otherwise.
     */
    public boolean addHeader(View headerView, boolean shouldReplace) {
        if (!hasHeader() || hasHeader() && shouldReplace) {
            this.headerView = headerView;
            notifyItemInserted(0);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return true if {@param footerViewId} is not 0 or if {@param footerView} is not null, false otherwise
     */
    private boolean hasFooter() {
        return footerViewId != 0 || footerView != null;
    }

    /**
     * @return true if {@param headerViewId} is not 0 or if {@param headerView} is not null, false otherwise
     */
    private boolean hasHeader() {
        return headerViewId != 0 || headerView != null;
    }

    /**
     * @return true if item at {@param postion} is footer
     */
    protected boolean isFooter(int position) {
        return hasFooter() && position == getItemCount() - 1;
    }

    /**
     * @return true if item at {@param postion} is header
     */
    protected boolean isHeader(int position) {
        return hasHeader() && position == 0;
    }

    public View getFooterView() {
        return footerView;
    }

    public void setFooterView(View footerView) {
        this.footerView = footerView;
    }

    public View getHeaderView() {
        return headerView;
    }

    public void setHeaderView(View headerView) {
        this.headerView = headerView;
    }

    // endregion

    // region Headers and Footers


    /**
     * @param position current adapter position
     * @return item view type base od {@param position}
     */
    @Override
    public int getItemViewType(int position) {

        //check what type our position is, based on the assumption that the order is headers > items > footers
        if (isHeader(position)) {
            return TYPE_HEADER;
        } else if (isFooter(position)) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }


    public interface OnClickListener<E> {

        void onClick(int index, E item);
    }

    public interface OnNextPageListener {

        void onScrolledToNextPage();
    }

    // endregion

    private class UpdateItemsTask extends AsyncTask<DiffUtil.Callback, Void, DiffUtil.DiffResult> {

        @Override
        protected DiffUtil.DiffResult doInBackground(DiffUtil.Callback... params) {
            if (params != null) {
                return DiffUtil.calculateDiff(params[0]);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            super.onPostExecute(diffResult);
            if (diffResult != null) {
                diffResult.dispatchUpdatesTo(MjolnirRecyclerAdapter.this);
            }
        }
    }

    public class HeaderFooterViewHolder extends ViewHolder {

        public HeaderFooterViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bind(E item, int position, List<Object> payloads) {
        }
    }

    protected abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void bind(E item, int position, List<Object> payloads);
    }
}
