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
    //    private String[] textArray = {"文本一", "文本二", "文本三", "文本四", "文本五", "文本六"};
//    private String[] textArray = {"文本一", "文本二", "文本三"};
    private SwitchView mSwitchView;

    private int[] marginArray = {20, 20, 20, 20};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSwitchView = (SwitchView) findViewById(switchView);
//        mSwitchView.setTextArray(textArray);//有效
        mSwitchView.setLockPosition(0);//有效
        mSwitchView.setSelectedBgMarginArray(marginArray);
        mSwitchView.setDefaultSelectedPosition(0);
        mSwitchView.setInterpolator(new OvershootInterpolator());
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

//        mSwitchView.setScrollEnable(false);//有效
//        mSwitchView.setNormalTextColor(Color.GREEN);//有效
//        mSwitchView.setNormalTextSize(25);//有效
//        mSwitchView.setSelectedDrawableResId(R.mipmap.ic_launcher);//有效
//        mSwitchView.setSelectedTextColor(Color.BLUE);//有效
//        mSwitchView.setSelectedTextSize(50);//有效


//        mSwitchView.unlockPosition();//有效

    }
}
