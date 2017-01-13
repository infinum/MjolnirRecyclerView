package co.infinum.testapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.infinum.mjolnirrecyclerview.MjolnirRecyclerAdapter;
import co.infinum.mjolnirrecyclerview.MjolnirViewHolder;
import co.infinum.testapp.R;

/**
 * Created by Željko Plesac on 27/09/16.
 */
public class SimpleAdapter extends MjolnirRecyclerAdapter<String> {

    public SimpleAdapter(Context context) {
        super(context, Collections.<String>emptyList());
    }

    @Override
    protected MjolnirViewHolder<String> onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_adapter, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MjolnirViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(MjolnirViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    public class TestViewHolder extends MjolnirViewHolder<String> {

        @BindView(R.id.tv_position)
        TextView tvPosition;

        @BindView(R.id.tv_text)
        TextView tvText;

        @BindView(R.id.root_view)
        View rootView;

        public TestViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        protected void bind(final String item, final int position, List<Object> payloads) {
            tvPosition.setText(String.valueOf(position).concat("."));
            tvText.setText(item);

            rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onClick(position, item);
                    }
                }
            });
        }
    }
}
