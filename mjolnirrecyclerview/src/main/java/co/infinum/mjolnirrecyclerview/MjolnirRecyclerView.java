package co.infinum.mjolnirrecyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Custom implementation of RecyclerView, which has support for Empty view.
 *
 * Created by Željko Plesac on 27/09/16.
 */
public class MjolnirRecyclerView extends RecyclerView {

    private View emptyView;

    private final AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    public MjolnirRecyclerView(Context context) {
        super(context);
    }

    public MjolnirRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MjolnirRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Checks if the adapter collection is empty. If it is, hide the RecyclerView content and show the empty view.
     */
    private void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible = ((MjolnirRecyclerAdapter) getAdapter()).getCollectionCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    /**
     * Sets the RecyclerView's adapter. It also checks whether the adapter collection is empty - if it is, it will automatically show
     * empty view. Otherwise, the adapter's content will be shown.
     * @param adapter
     */
    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    /**
     * Sets the empty view. RecyclerView can have only one empty view at the time.
     * @param emptyView
     */
    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    /**
     * Checks whether the RecyclerView is showing empty view.
     * @return true is empty view is shown, false otherwise.
     */
    public boolean isEmptyViewShown() {
        return emptyView != null && emptyView.getVisibility() == VISIBLE;
    }
}
