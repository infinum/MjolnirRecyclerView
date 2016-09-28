package co.infinum.testapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.infinum.mjolnirrecyclerview.MjolnirRecyclerView;

public class MainActivity extends AppCompatActivity implements MainAdapter.OnClickListener<String> {

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

    private MainAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setEmptyView(emptyView);

        adapter = new MainAdapter(this);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.reset(ITEMS);
            }
        }, 5000);
    }

    @Override
    public void onClick(int index, String item) {
        Toast.makeText(this, item, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancel();
    }
}
