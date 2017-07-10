package com.qiangxi.switchviewproject;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.qiangxi.switchview.SwitchView;
import com.qiangxi.switchview.callback.OnItemClickListener;

import java.util.TimerTask;

import static com.qiangxi.switchviewproject.R.id.switchView;

public class MainActivity extends AppCompatActivity {
//    private String[] textArray = {"文本一", "文本二"};
    private SwitchView mSwitchView;

    private int[] marginArray = {20, 20, 20, 20};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwitchView = (SwitchView) findViewById(switchView);
//        mSwitchView.setTextArray(textArray);
//        mSwitchView.setLockPosition(0);
//        mSwitchView.setSelectedBgMarginArray(marginArray);
//        mSwitchView.setDefaultSelectedPosition(0);
        mSwitchView.setInterpolator(new OvershootInterpolator());
//        mSwitchView.setShowTextWhenScrolling(false);
        mSwitchView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int currentSelectedPosition, final int lastSelectedPosition) {
                new Handler().postDelayed(new TimerTask() {
                    @Override
                    public void run() {
//                        mSwitchView.smoothScrollTo(lastSelectedPosition);//setDefaultSelectedPosition(2);
                    }
                }, 2000);
                Log.e("tag", "currentSelectedPosition=" + currentSelectedPosition + "，lastSelectedPosition=" + lastSelectedPosition);
            }
        });
    }

    public void click(View view) {

//        mSwitchView.setScrollEnable(false);
//        mSwitchView.setNormalTextColor(Color.GREEN);
//        mSwitchView.setNormalTextSize(25);
//        mSwitchView.setSelectedDrawableResId(R.mipmap.ic_launcher);
//        mSwitchView.setSelectedTextColor(Color.BLUE);
//        mSwitchView.setSelectedTextSize(50);


//        mSwitchView.unlockPosition();//有效

    }
}
