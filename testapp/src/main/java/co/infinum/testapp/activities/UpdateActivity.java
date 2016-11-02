package co.infinum.testapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.infinum.mjolnirrecyclerview.MjolnirRecyclerView;
import co.infinum.testapp.R;
import co.infinum.testapp.adapters.UpdateAdapter;
import co.infinum.testapp.diffutils.ItemsDiffUtil;
import co.infinum.testapp.models.Item;

/**
 * Created by Željko Plesac on 02/11/16.
 */
public class UpdateActivity extends AppCompatActivity {

    private List<Item> items;

    @BindView(R.id.recycler_view)
    MjolnirRecyclerView recyclerView;

    @BindView(R.id.empty_view)
    View emptyView;

    private UpdateAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_simple);
        ButterKnife.bind(this);

        items = new ArrayList<>();
        items.add(new Item(1, "Car"));
        items.add(new Item(2, "Plane"));
        items.add(new Item(3, "Train"));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(emptyView);

        adapter = new UpdateAdapter(this);
        recyclerView.setAdapter(adapter);
        adapter.addAll(items);
    }

    @OnClick(R.id.button_update)
    void onUpdateButtonClicked() {
        List<Item> newList = new ArrayList<>();
        newList.add(new Item(1, "Car"));
        newList.add(new Item(2, "Plane"));
        newList.add(new Item(3, UUID.randomUUID().toString()));

        adapter.update(new ItemsDiffUtil(items, newList));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancel();
    }
}
