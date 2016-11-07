package co.infinum.testapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import co.infinum.testapp.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.button_simple)
    void onSimpleExampleClick() {
        Intent intent = new Intent(this, SimpleActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_scroll)
    void onScrollExampleClick() {
        Intent intent = new Intent(this, NextPageListenerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.button_empty_view)
    void onEmptyViewClick() {
        Intent intent = new Intent(this, EmptyViewActivity.class);
        startActivity(intent);
    }
}
