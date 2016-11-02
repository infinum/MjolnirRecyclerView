package co.infinum.testapp.adapters;

import android.content.Context;
import android.os.Bundle;
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
import co.infinum.testapp.diffutils.ItemsDiffUtil;
import co.infinum.testapp.models.Item;

/**
 * Created by Željko Plesac on 02/11/16.
 */

public class UpdateAdapter extends MjolnirRecyclerAdapter<Item> {

    public UpdateAdapter(Context context) {
        super(context, Collections.<Item>emptyList());
    }

    @Override
    protected MjolnirRecyclerAdapter<Item>.ViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_adapter, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends MjolnirRecyclerAdapter<Item>.ViewHolder {

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
        protected void bind(final Item item, final int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                tvPosition.setText(String.valueOf(item.getId()));
                tvText.setText(item.getName());
            } else {
                Bundle bundle = (Bundle) payloads.get(0);
                tvText.setText(bundle.getString(ItemsDiffUtil.EXTRA_ITEM_DESCRIPTION));
            }

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
