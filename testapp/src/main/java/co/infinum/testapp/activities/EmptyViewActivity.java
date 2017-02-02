package co.infinum.testapp.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.infinum.mjolnirrecyclerview.MjolnirRecyclerView;
import co.infinum.testapp.R;
import co.infinum.testapp.adapters.SimpleAdapter;

public class EmptyViewActivity extends AppCompatActivity {

    private static final List<String> ITEMS = Collections.unmodifiableList(Arrays.asList(
            "First",
            "Second",
            "Third",
            "Fourth",
            "Fifth",
            "Sixth",
            "Seventh",
            "Eight",
            "Ninth",
            "Tenth"
    ));

    @BindView(R.id.recycler_view)
    MjolnirRecyclerView recyclerView;

    @BindView(R.id.empty_view)
    View emptyView;

    private SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(emptyView, true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    adapter = new SimpleAdapter(EmptyViewActivity.this);
                    recyclerView.setAdapter(adapter);
                    adapter.addAll(ITEMS);
                }
            }
        }, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancel();
    }
}
