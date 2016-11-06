package co.infinum.testapp.activities;

import android.app.ProgressDialog;
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
import co.infinum.testapp.R;
import co.infinum.testapp.adapters.SimpleAdapter;

/**
 * Created by Željko Plesac on 02/11/16.
 */

public class NextPageListenerActivity extends AppCompatActivity
        implements SimpleAdapter.OnClickListener<String>, SimpleAdapter.OnNextPageListener {

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
            "Tenth",
            "Eleventh",
            "Twelfth",
            "Thirteenth",
            "Fourteenth",
            "Fifteenth"
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
        recyclerView.setEmptyView(emptyView);

        adapter = new SimpleAdapter(this);
        adapter.setOnClickListener(this);
        recyclerView.setAdapter(adapter);

        adapter.addAll(ITEMS);
        adapter.setOnNextPageListener(this);
    }

    @Override
    public void onScrolledToNextPage() {
        // Simulate network call by showing progress dialog and adding data to adapter with some delay.

//        final ProgressDialog pd = new ProgressDialog(this);
//        pd.setMessage(getString(R.string.loading));
//        pd.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    adapter.addAll(ITEMS);
//                    pd.dismiss();
                }
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
