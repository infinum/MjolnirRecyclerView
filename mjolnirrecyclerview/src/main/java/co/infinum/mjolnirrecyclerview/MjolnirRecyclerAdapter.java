package co.infinum.mjolnirrecyclerview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
public abstract class MjolnirRecyclerAdapter<E> extends RecyclerView.Adapter<MjolnirViewHolder> {

    public static final int TYPE_HEADER = 111;

    public static final int TYPE_FOOTER = 222;

    public static final int TYPE_ITEM = 333;

    protected OnClickListener<E> listener;

    protected OnNextPageListener nextPageListener;

    private Context context;

    protected List<E> items;

    private int nextPageOffset = 1;

    private View footerView;

    private View headerView;

    private Handler handler = new Handler(Looper.getMainLooper());

    protected boolean isCancelled = false;

    protected boolean isLoading = false;

    protected Queue<Collection<E>> pendingUpdates = new ArrayDeque<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private RecyclerView.LayoutManager layoutManager;

    public MjolnirRecyclerAdapter(Context context, Collection<E> list) {
        this.context = context;
        this.items = new ArrayList<>(list);
    }

    @Override
    public MjolnirViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Check if we have to inflate ItemViewHolder of HeaderFooterHolder
        switch (viewType) {
            case TYPE_HEADER:
                return onCreateHeaderViewHolder();
            case TYPE_FOOTER:
                return onCreateFooterViewHolder();
            default:
                return onCreateItemViewHolder(parent, viewType);
        }
    }

    /**
     * Override if you need a custom implementation.
     */
    protected MjolnirViewHolder onCreateFooterViewHolder() {
        return new MjolnirHeaderFooterViewHolder(footerView);
    }

    /**
     * Override if you need a custom implementation.
     */
    protected MjolnirViewHolder onCreateHeaderViewHolder() {
        return new MjolnirHeaderFooterViewHolder(headerView);

    }

    protected abstract MjolnirViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(MjolnirViewHolder holder, int position) {
        onBindViewHolder(holder, position, Collections.emptyList());
    }

    @Override
    public void onBindViewHolder(MjolnirViewHolder holder, int position, List<Object> payloads) {

        //check what type of view our position is
        switch (getItemViewType(position)) {
            case TYPE_HEADER:
            case TYPE_FOOTER:
                //Nothing, for now.
                break;
            default:
                position = calculateIndex(position, true);
                E item = items.get(position);
                holder.bind(item, position, payloads);

                if (nextPageListener != null && !isLoading
                        && position >= getCollectionCount() - getNextPageOffset() && !isCancelled) {
                    isLoading = true;

                    // If RecyclerView is currently computing a layout, it's in a lockdown state and any
                    // attempt to update adapter contents will result in an exception. In these cases, we need to postpone the change
                    // using a Handler.
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            nextPageListener.onScrolledToNextPage();
                        }
                    });
                }
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
     * Sets the isCancelled flag to true, which will cancel DiffUtil.DiffResult update dispatch to the adapter. Call this method when
     * your activity or fragment is about to be destroyed.
     */
    public void cancel() {
        isCancelled = true;
    }

    /**
     * Sets the isCancelled flag to false, which will enable DiffUtil.DiffResult update dispatch to the adapter.
     */
    public void reset() {
        isCancelled = false;
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
        if (index > items.size()) {
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

    /**
     * Clears current items.
     */
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    /**
     * Update the current adapter state. If {@param callback} is provided, an updated data set is calculated with DiffUtil, otherwise
     * current data set is clear and {@param newItems} are added to the internal items collection.
     *
     * @param newItems Collection of new items, which are added to adapter.
     * @param callback DiffUtil callback, which is used to update the items.
     */
    public void update(Collection<E> newItems, @Nullable DiffUtil.Callback callback) {
        if (callback != null) {
            pendingUpdates.add(newItems);
            if (pendingUpdates.size() == 1) {
                updateData(newItems, callback);
            }
        } else {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }
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

    // region Headers and Footers

    /**
     * Add a footer view to this adapter. If footer already exists, it will be replaced.
     * * <p>
     * Note: setFooter should be called only after {@link MjolnirRecyclerView#setAdapter(RecyclerView.Adapter)} otherwise the default
     * layout params wont apply to this view. For more info about the default layout params check {@link #setDefaultLayoutParams(View)}
     * documentation.
     *
     * @param footerViewId layout view id.
     */
    public void setFooter(@LayoutRes int footerViewId) {
        setFooter(LayoutInflater.from(getContext()).inflate(footerViewId, null, false));
    }

    /**
     * Add a footer view to this adapter. If layout params for the {@param footerView}} are missing, default layout params will be set with
     * the {@link #setDefaultLayoutParams(View)} method.
     * If footer already exists, it will be replaced.
     * <p>
     * Note: setFooter should be called only after {@link MjolnirRecyclerView#setAdapter(RecyclerView.Adapter)} otherwise the default
     * layout params wont apply to this view. For more info about the default layout params check {@link #setDefaultLayoutParams(View)}
     * documentation.
     *
     * @param footerView layout view
     * @return true if footer was added/replaced, false otherwise.
     */
    public void setFooter(View footerView) {
        boolean hadFooterBefore = hasFooter();

        int position = getCollectionCount() + (hasHeader() ? 1 : 0);
        this.footerView = footerView;
        setDefaultLayoutParams(this.footerView);

        if (hadFooterBefore) {
            notifyItemChanged(position);
        } else {
            notifyItemInserted(position);
        }
    }

    /**
     * Add a header view to this adapter. If header already exists, it will be replaced.
     * <p>
     * Note: setHeader should be called only after {@link MjolnirRecyclerView#setAdapter(RecyclerView.Adapter)} otherwise the default
     * layout params wont apply to this view. For more info about the default layout params check {@link #setDefaultLayoutParams(View)}
     * documentation.
     *
     * @param headerViewId layout view id.
     */
    public void setHeader(@LayoutRes int headerViewId) {
        setHeader(LayoutInflater.from(getContext()).inflate(headerViewId, null, false));
    }

    /**
     * Add a header view to this adapter. If layout params for the {@param headerView}} are missing, default layout params will be set with
     * the {@link #setDefaultLayoutParams(View)} method.
     * <p>
     * Note: setHeader should be called only after {@link MjolnirRecyclerView#setAdapter(RecyclerView.Adapter)} otherwise the default
     * layout params wont apply to this view. For more info about the default layout params check {@link #setDefaultLayoutParams(View)}
     * documentation.
     *
     * @param headerView layout view
     */
    public void setHeader(View headerView) {
        boolean hadHeaderBefore = hasHeader();

        this.headerView = headerView;
        setDefaultLayoutParams(this.headerView);

        if (hadHeaderBefore) {
            notifyItemChanged(0);
        } else {
            notifyItemInserted(0);
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
     * Removes header view from the RecyclerView (if existing).
     */
    public void removeHeader() {
        if (hasHeader()) {
            headerView = null;
            notifyItemRemoved(0);
        }
    }

    /**
     * Removes footer view from the RecyclerView (if existing).
     */
    public void removeFooter() {
        if (hasFooter()) {
            footerView = null;

            int position = getCollectionCount() + (hasHeader() ? 1 : 0);
            notifyItemRemoved(position);
        }
    }

    /**
     * @return true if {@param footerView} is not null, false otherwise
     */
    public boolean hasFooter() {
        return footerView != null;
    }

    /**
     * @return true if {@param headerView} is not null, false otherwise
     */
    public boolean hasHeader() {
        return headerView != null;
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

        // if layout manager is GridLayoutManager, we have to attach span size lookup in order to correctly setup header and footer view
        if (layoutManager instanceof GridLayoutManager) {
            setHeaderFooterViewsForGridLayoutManager((GridLayoutManager) layoutManager);
        }
    }

    /**
     * If adapter is using GridLayoutManager, we have to register custom SpanSizeLookup listener and manipulate with span size - if current
     * item is either header or footer, we return {@param layoutManager} span count as span size in order to position header or footer view
     * in it's own column.
     *
     * @param layoutManager GridLayoutManager for which we attach SpanSizeLookup listener.
     */
    private void setHeaderFooterViewsForGridLayoutManager(final GridLayoutManager layoutManager) {
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (isHeader(position) || isFooter(position)) {
                    return layoutManager.getSpanCount();
                } else {
                    return 1;
                }
            }
        });
    }

    // endregion

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

        return getAdditionalItemViewType(position);
    }

    /**
     * Override this method if you are using custom ItemViewType and provide correct implementation.
     *
     * @return +
     */
    protected int getAdditionalItemViewType(int position) {
        return TYPE_ITEM;
    }


    public interface OnClickListener<E> {

        void onClick(int index, E item);
    }

    public interface OnNextPageListener {

        void onScrolledToNextPage();
    }

    // endregion

    /**
     * Calculates provided {@param callback} DiffResult by using DiffUtils.
     *
     * @param newItems Collection of new items, with which our current items collection is updated.
     * @param callback DiffUtil.Callback on which DiffResult is calculated.
     */
    private void updateData(final Collection<E> newItems, final DiffUtil.Callback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(callback);
                postDiffResults(newItems, diffResult, callback);
            }
        });
    }

    /**
     * Dispatched {@param diffResult} DiffResults to the adapter if adapter has not been cancelled. If there are any queued pending updates,
     * it will peek the latest new items collection and once again update the adapter content.
     *
     * @param newItems   Collection of new items, with which our current items collection is updated.
     * @param diffResult DiffUtil.DiffResult which was calculated for {@param callback}.
     * @param callback   DiffUtil.Callback on which DiffResult was calculated.
     */
    private void postDiffResults(final Collection<E> newItems, final DiffUtil.DiffResult diffResult, final DiffUtil.Callback callback) {
        if (!isCancelled) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    pendingUpdates.remove();
                    diffResult.dispatchUpdatesTo(MjolnirRecyclerAdapter.this);
                    items.clear();
                    items.addAll(newItems);

                    if (pendingUpdates.size() > 0) {
                        updateData(pendingUpdates.peek(), callback);
                    }
                }
            });
        }
    }
}
