package com.qiangxi.switchviewproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.qiangxi.switchview.SwitchView;
import com.qiangxi.switchview.callback.OnItemClickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SwitchView switchView = (SwitchView) findViewById(R.id.switchView);
        switchView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Log.e("tag", "position=" + position);
            }
        });
    }
}
