package co.infinum.mjolnirrecyclerview;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
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

    private int nextPageOffset = 1;

    private View footerView;

    private View headerView;

    private UpdateItemsTask updateItemsTask;

    protected boolean isLoading = false;

    private RecyclerView.LayoutManager layoutManager;

    public MjolnirRecyclerAdapter(Context context, Collection<E> list) {
        this.context = context;
        this.items = new ArrayList<>(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

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
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(MjolnirRecyclerAdapter.ViewHolder holder, int position, List<Object> payloads) {

        //check what type of view our position is
        switch (getItemViewType(position)) {
            case TYPE_ITEM:
                position = calculateIndex(position, true);
                E item = items.get(position);
                holder.bind(item, position, payloads);

                if (nextPageListener != null && !isLoading
                        && position >= getCollectionCount() - getNextPageOffset()) {
                    isLoading = true;
                    nextPageListener.onScrolledToNextPage();
                }
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

    public void setOnNextPageListener(OnNextPageListener listener, int nextPageOffset) {
        this.nextPageOffset = nextPageOffset;
        setOnNextPageListener(listener);
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

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    // region ArrayAdapter methods

    public void add(E item) {
        int position = items.size();
        items.add(item);
        notifyItemInserted(calculateIndex(position, false));
    }

    public void addAll(Collection<E> collection) {
        int position = items.size();
        items.addAll(collection);
        notifyItemRangeInserted(calculateIndex(position, false), collection.size());
    }

    public void add(E item, int index) {
        if (index >= items.size()) {
            throw new IllegalStateException("Index is defined in wrong range!");
        } else {
            items.add(index, item);
            notifyItemInserted(calculateIndex(index, false));
        }
    }

    public void addAll(@NonNull Collection<E> collection, int index) {
        if (index >= items.size()) {
            throw new IllegalStateException("Index is defined in wrong range!");
        } else {
            items.addAll(index, collection);
            notifyItemRangeInserted(calculateIndex(index, false), collection.size());
        }
    }

    public void remove(@NonNull E item) {
        int position = items.indexOf(item);
        if (items.remove(item)) {
            notifyItemRemoved(calculateIndex(position, false));
        }
    }

    public void removeAll(@NonNull Collection<E> collection) {
        if (items.removeAll(collection)) {
            notifyDataSetChanged();
        }
    }

    public void remove(int index) {
        if (index >= items.size()) {
            throw new IllegalStateException("Index is defined in wrong range!");
        } else if (items.remove(index) != null) {
            notifyItemRemoved(calculateIndex(index, false));
        }
    }


    public E get(int index) {
        if (index >= items.size()) {
            throw new IllegalStateException("Index is defined in wrong range!");
        }
        return items.get(index);
    }

    public Collection<E> getAll() {
        return new ArrayList<>(items);
    }

    public void set(E item, int index) {
        items.set(index, item);
        notifyItemChanged(calculateIndex(index, false));
    }

    public void reset(Collection<E> collection) {
        reset(collection, null);
    }

    /**
     * Clears current items.
     */
    public void clear() {
        items.clear();
        notifyDataSetChanged();
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
            update(callback);
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * Update current adapter state by executing UpdateItems, which will execute UpdateItems task with {@param callback} as
     * input parameter.
     *
     * @param callback DiffUtil callback, which is used to update the items.
     */
    public void update(@NonNull DiffUtil.Callback callback) {
        updateItemsTask = new UpdateItemsTask(this);
        updateItemsTask.execute(callback);
    }

    /**
     * Calculate the correct item index because RecyclerView doesn't distinguish
     * between header rows and item rows.
     *
     * We have 2 possible cases, which are defined with {@param isViewBinding} value:
     *
     * 1. If we are trying to bind the view, than the index value has to be decremented by 1 if adapter contains header
     * view.
     *
     * 2. If we are trying to perform some action on the adapter, that the index value has to be incremented by 1
     * if adapter contains header view.
     *
     * @param index         RecyclerView row index.
     * @param isViewBinding boolean value, which indicates whether we are trying to bind the view or perform some action on adapter.
     * @return correct item index.
     */
    private int calculateIndex(int index, boolean isViewBinding) {
        if (isViewBinding) {
            index = index - (hasHeader() ? 1 : 0);

            if (index >= items.size()) {
                throw new IllegalStateException("Index is defined in wrong range!");
            } else {
                return index;
            }
        } else {
            index = index + (hasHeader() ? 1 : 0);
            return index;
        }
    }

    /**
     * Add a footer to this adapter.
     * This method has higher priority than {@link #addFooter(android.view.View)}.
     *
     * Deprecated in version 1.1.0, use  {@link #addFooter(int footerViewId, boolean shouldReplace)  instead.
     *
     * @param footerViewId layout resource id
     */
    @Deprecated
    public void addFooter(@LayoutRes int footerViewId) {
        int position = getCollectionCount() + (hasHeader() ? 1 : 0);
        this.footerViewId = footerViewId;
        notifyItemInserted(position);
    }

    /**
     * Add a footer view to this adapter. If footer already exists, it will be replaced depending on the {@param shouldReplace} value.
     *
     * @param footerViewId  layout view id.
     * @param shouldReplace should we replace footer if it already exists
     * @return true if footer was added/replaced, false otherwise.
     */
    public boolean addFooter(@LayoutRes int footerViewId, boolean shouldReplace) {
        if (shouldReplace || !hasFooter()) {
            removeFooter();
            int position = getCollectionCount() + (hasHeader() ? 1 : 0);
            this.footerViewId = footerViewId;
            notifyItemInserted(position);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Add a footer view to this adapter.
     * This method has lower priority than {@link #addFooter(int)}.
     *
     * Deprecated in version 1.1.0, use  {@link #addFooter(View headerView, boolean shouldReplace)} instead.
     *
     * @param footerView layout view
     */
    @Deprecated
    public void addFooter(View footerView) {
        int position = getCollectionCount() + (hasHeader() ? 1 : 0);
        this.footerView = footerView;
        notifyItemInserted(position);
    }

    /**
     * Add a footer view to this adapter. If layout params for the {@param footerView}} are missing, default layout params will be set with
     * the {@link #setDefaultLayoutParams(View)} method.
     * If footer already exists, it will be replaced depending on the {@param shouldReplace} value.
     *
     * @param footerView    layout view
     * @param shouldReplace should we replace footer if it already exists
     * @return true if footer was added/replaced, false otherwise.
     */
    public boolean addFooter(View footerView, boolean shouldReplace) {
        if (shouldReplace || !hasFooter()) {
            removeFooter();
            int position = getCollectionCount() + (hasHeader() ? 1 : 0);
            this.footerView = footerView;
            setDefaultLayoutParams(this.footerView);
            notifyItemInserted(position);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Sets the default layout params to the provided {@param view} if they are not yet set. Default params are MATCH_PARENT for layout
     * width and WRAP_CONTENT for layout height.
     *
     * @param view View for which we want to set default layout params.
     */
    private void setDefaultLayoutParams(View view) {
        if (getLayoutManager() != null && getLayoutManager() instanceof LinearLayoutManager) {
            RecyclerView.LayoutParams layoutParams;

            if (((LinearLayoutManager) getLayoutManager()).getOrientation() == LinearLayoutManager.VERTICAL) {
                layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(layoutParams);
            } else {
                layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT,
                        RecyclerView.LayoutParams.MATCH_PARENT);
                view.setLayoutParams(layoutParams);
            }
        }
    }

    /**
     * Add a header to this adapter.
     * This method has higher priority than {@link #addHeader(android.view.View)}.
     *
     * Deprecated in version 1.1.0, use  {@link #addHeader(int headerViewId, boolean shouldReplace)} instead.
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
     * @param headerViewId  layout view id.
     * @param shouldReplace should we replace header if it already exists
     * @return true if header was added/replaced, false otherwise.
     */
    public boolean addHeader(@LayoutRes int headerViewId, boolean shouldReplace) {
        if (shouldReplace || !hasHeader()) {
            removeHeader();
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
     * Add a header view to this adapter. If layout params for the {@param headerView}} are missing, default layout params will be set with
     * the {@link #setDefaultLayoutParams(View)} method.
     *
     * If header already exists, it will be replaced depending on the {@param shouldReplace} value.
     *
     * @param headerView    layout view
     * @param shouldReplace should we replace header if it already exists
     * @return true if header was added/replaced, false otherwise.
     */
    public boolean addHeader(View headerView, boolean shouldReplace) {
        if (shouldReplace || !hasHeader()) {
            removeHeader();
            this.headerView = headerView;
            setDefaultLayoutParams(this.headerView);
            notifyItemInserted(0);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes header view from the RecyclerView (if existing).
     */
    public void removeHeader() {
        if (hasHeader()) {
            headerView = null;
            headerViewId = 0;
            notifyItemRemoved(0);
        }
    }

    /**
     * Removes footer view from the RecyclerView (if existing).
     */
    public void removeFooter() {
        if (hasFooter()) {
            footerView = null;
            footerViewId = 0;

            int position = getCollectionCount() + (hasHeader() ? 1 : 0);
            notifyItemRemoved(position);
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

    public View getHeaderView() {
        return headerView;
    }

    public int getNextPageOffset() {
        return nextPageOffset;
    }

    public void setNextPageOffset(int nextPageOffset) {
        this.nextPageOffset = nextPageOffset;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return layoutManager;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        this.layoutManager = layoutManager;
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

        private WeakReference<MjolnirRecyclerAdapter> adapterWeakReference;

        public UpdateItemsTask(MjolnirRecyclerAdapter adapter) {
            this.adapterWeakReference = new WeakReference<>(adapter);
        }

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
            if (adapterWeakReference.get() != null && diffResult != null) {
                diffResult.dispatchUpdatesTo(adapterWeakReference.get());
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

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void bind(E item, int position, List<Object> payloads);
    }
}
