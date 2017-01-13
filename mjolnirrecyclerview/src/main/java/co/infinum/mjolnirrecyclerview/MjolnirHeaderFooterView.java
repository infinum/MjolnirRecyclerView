package co.infinum.mjolnirrecyclerview;

import android.view.View;

import java.util.List;

public class MjolnirHeaderFooterView<E> extends MjolnirViewHolder<E> {

    public MjolnirHeaderFooterView(View itemView) {
        super(itemView);
    }

    @Override
    protected void bind(E item, int position, List<Object> payloads) {
    }
}
