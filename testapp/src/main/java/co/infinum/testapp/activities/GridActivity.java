package co.infinum.testapp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.infinum.mjolnirrecyclerview.MjolnirRecyclerView;
import co.infinum.testapp.R;
import co.infinum.testapp.adapters.SimpleAdapter;

/**
 * Created by Željko Plesac on 24/11/16.
 */

public class GridActivity extends AppCompatActivity implements SimpleAdapter.OnClickListener<String> {

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

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setEmptyView(emptyView);

        adapter = new SimpleAdapter(this);
        adapter.setOnClickListener(this);
        adapter.addAll(ITEMS);
        recyclerView.setAdapter(adapter);

        View footerView = getLayoutInflater().inflate(R.layout.view_footer, null);

        adapter.addHeader(R.layout.view_header, true);
        adapter.addFooter(footerView, false);
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