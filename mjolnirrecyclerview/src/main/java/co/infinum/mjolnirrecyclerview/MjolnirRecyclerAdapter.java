package co.infinum.mjolnirrecyclerview;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Custom implementation of RecyclerView.Adapter, which has following features:
 * 1. Header and footer support
 * 2. DiffUtil support
 * 3. ArrayAdapter methods
 *
 * Use it in combination with {@link co.infinum.mjolnirrecyclerview.MjolnirRecyclerView} to also get support for Empty views.
 *
 * Created by Željko Plesac on 27/09/16.
 */

public abstract class MjolnirRecyclerAdapter<E> extends RecyclerView.Adapter<MjolnirRecyclerAdapter.ViewHolder> {

    public static final int TYPE_HEADER = 111;

    public static final int TYPE_FOOTER = 222;

    public static final int TYPE_ITEM = 333;

    private Context context;

    private List<E> items;

    private List<View> headers = new ArrayList<>();

    private List<View> footers = new ArrayList<>();

    protected OnClickListener<E> listener;

    protected OnNextPageListener nextPageListener;

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
        } else {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            frameLayout
                    .setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderFooterViewHolder(frameLayout);
        }
    }

    protected abstract ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(MjolnirRecyclerAdapter.ViewHolder holder, int position) {
        //check what type of view our position is
        if (position < headers.size()) {
            View v = headers.get(position);
            //add our view to a header view and display it
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        } else if (position >= headers.size() + items.size()) {
            View v = footers.get(position - items.size() - headers.size());
            //add our view to a footer view and display it
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        } else {
            E item = get(position);
            holder.bind(item, position, Collections.emptyList());
        }
    }

    @Override
    public void onBindViewHolder(MjolnirRecyclerAdapter.ViewHolder holder, int position, List<Object> payloads) {
        //check what type of view our position is
        if (position < headers.size()) {
            View v = headers.get(position);
            //add our view to a header view and display it
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        } else if (position >= headers.size() + items.size()) {
            View v = footers.get(position - items.size() - headers.size());
            //add our view to a footer view and display it
            prepareHeaderFooter((HeaderFooterViewHolder) holder, v);
        } else {
            E item = get(position);
            holder.bind(item, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return headers.size() + items.size() + footers.size();
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

    // region ArrayAdapter methods

    public void add(E item) {
        int position = items.size();
        items.add(item);
        notifyItemInserted(position);
    }

    public void addAll(Collection<E> collection) {
        int position = items.size();
        items.addAll(collection);
        notifyItemRangeInserted(position, collection.size());
    }

    public void add(E item, int index) {
        index = index - headers.size() - footers.size();

        items.add(index, item);
        notifyItemInserted(index);
    }

    public void addAll(Collection<E> collection, int index) {
        index = index - headers.size() - footers.size();

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
        index = index - headers.size() - footers.size();

        if (items.remove(index) != null) {
            notifyItemRemoved(index);
        }
    }

    public E get(int index) {
        index = index - headers.size() - footers.size();
        return items.get(index);
    }

    public Collection<E> getAll() {
        return new ArrayList<>(items);
    }

    public void set(E item, int index) {
        index = index - headers.size() - footers.size();

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

    // endregion

    // region Headers and Footers

    public class HeaderFooterViewHolder extends ViewHolder {

        FrameLayout base;

        public HeaderFooterViewHolder(View itemView) {
            super(itemView);
            this.base = (FrameLayout) itemView;
        }

        @Override
        protected void bind(E item, int position, List<Object> payloads) {
            this.base = (FrameLayout) itemView;
        }
    }

    //add a footer to the adapter
    public void addFooter(View footer) {
        if (!footers.contains(footer)) {
            footers.add(footer);
            //animate
            notifyItemInserted(headers.size() + items.size() + footers.size() - 1);
        }
    }

    //remove a footer from the adapter
    public void removeFooter(View footer) {
        if (footers.contains(footer)) {
            //animate
            notifyItemRemoved(headers.size() + items.size() + footers.indexOf(footer));
            footers.remove(footer);
            if (footer.getParent() != null) {
                ((ViewGroup) footer.getParent()).removeView(footer);
            }
        }
    }

    //add a header to the adapter
    public void addHeader(View header) {
        if (!headers.contains(header)) {
            headers.add(header);
            //animate
            notifyItemInserted(headers.size() - 1);
        }
    }

    //remove a header from the adapter
    public void removeHeader(View header) {
        if (headers.contains(header)) {
            //animate
            notifyItemRemoved(headers.indexOf(header));
            headers.remove(header);
            if (header.getParent() != null) {
                ((ViewGroup) header.getParent()).removeView(header);
            }
        }
    }

    private void prepareHeaderFooter(HeaderFooterViewHolder vh, View view) {
        //empty out our FrameLayout and replace with our header/footer
        vh.base.removeAllViews();
        vh.base.addView(view);
    }

    public List<View> getHeaders() {
        return headers;
    }

    public List<View> getFooters() {
        return footers;
    }

    @Override
    public int getItemViewType(int position) {
        //check what type our position is, based on the assumption that the order is headers > items > footers
        if (position < headers.size()) {
            return TYPE_HEADER;
        } else if (position >= headers.size() + items.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    // endregion

    protected abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

        protected abstract void bind(E item, int position, List<Object> payloads);
    }


    public interface OnClickListener<E> {

        void onClick(int index, E item);
    }

    public interface OnNextPageListener {

        void onScrolledToNextPage();
    }
}
