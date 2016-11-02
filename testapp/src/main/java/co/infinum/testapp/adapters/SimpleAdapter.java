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
import co.infinum.testapp.R;

/**
 * Created by Željko Plesac on 27/09/16.
 */
public class SimpleAdapter extends MjolnirRecyclerAdapter<String> {

    public SimpleAdapter(Context context) {
        super(context, Collections.<String>emptyList());
    }

    @Override
    protected MjolnirRecyclerAdapter<String>.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_adapter, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends MjolnirRecyclerAdapter<String>.ViewHolder {

        @BindView(R.id.tv_position)
        TextView tvPosition;

        @BindView(R.id.tv_text)
        TextView tvText;

        @BindView(R.id.root_view)
        View rootView;

        public ViewHolder(View itemView) {
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
