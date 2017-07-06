package com.qiangxi.switchview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qiangxi.switchview.callback.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author qiang_xi
 *         7月3号需要干的事情：
 *         1.把选中背景换成view
 *         2.使用ViewDragHelper，做好view 的滑动和点击
 *         3.研究如何在滑动的时候，让字体颜色展示在view上
 */
public class SwitchView extends FrameLayout {
    //默认值，单位dp
    private static final int DEFAULT_TOTAL_ITEM_COUNT = 4;//总item数量
    private static final int DEFAULT_ITEM_WIDTH = 80;//默认item宽度
    private static final int DEFAULT_ITEM_HEIGHT = 50;//默认item高度
    private static final int DEFAULT_TEXT_SIZE = 14;//默认文本大小
    private static final int INVALIDATE_POSITION = -1;//无效位置

    private OnItemClickListener mItemClickListener;
    private boolean mScrollEnable = true;//是否禁用滑动手势
    //position
    private int mLastSelectedPosition = INVALIDATE_POSITION;//滑块所处的上一个位置
    private int mSelectedPosition;//滑块所处的当前位置
    private int mLockedPosition = INVALIDATE_POSITION;//锁定的位置
    private int mTotalItemCount = DEFAULT_TOTAL_ITEM_COUNT;
    //颜色
    private int mNormalTextColor = Color.WHITE;//正常的文字颜色
    private int mSelectedTextColor = Color.YELLOW;//选中的文字颜色
    private int mSelectedItemBgColor = Color.WHITE;//选中的item背景颜色.
    //字体大小
    private float mNormalTextSize;
    private float mSelectedTextSize;
    //范围
    private RectF mSelectedItemBound = new RectF();//选中的item的范围
    private List<RectF> mItemBounds = new ArrayList<>();
    private List<PointF> mItemCenterPoint = new ArrayList<>();
    //画笔
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //尺寸
    private int mItemWidth;
    private int mItemHeight;
    private int[] mSelectedBgMarginArray = new int[4];//选中的item的背景的margin值，位置对应关系为：int[left,top,right,bottom]
    //文本
    private String[] mTextArray = new String[mTotalItemCount];
    //point
    private int mLastX;
    private int mLastY;
    //SlideView
    private TextView mSlideView;
    //SlideView bg
    private Drawable mSelectedDrawable;
    private boolean isSlideViewPressed;

    public SwitchView(Context context) {
        this(context, null);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mNormalTextSize = spToPx(DEFAULT_TEXT_SIZE);
        mItemWidth = dpToPx(DEFAULT_ITEM_WIDTH);
        mItemHeight = dpToPx(DEFAULT_ITEM_HEIGHT);
        mTextPaint.setDither(true);
        mBgPaint.setDither(true);
        generateDefaultTextArray();
        setupDefaultSelectedBgMargin();
        setupSlideView();
    }

    private void setupSlideView() {
        removeAllViews();
        mSelectedDrawable = getContext().getResources().getDrawable(R.drawable.bg_selected_drawable);
        mSlideView = new TextView(getContext());
        setupLayoutParam(mItemWidth, mItemHeight);
        mSlideView.setBackground(mSelectedDrawable);
        mSlideView.setTextColor(mSelectedTextColor);
        mSlideView.setGravity(Gravity.CENTER);
        mSlideView.setTextSize(DEFAULT_TEXT_SIZE);
        addView(mSlideView);
        mLastSelectedPosition = 0;//默认处于0位置
    }

    private void setupLayoutParam(int itemWidth, int itemHeight) {
        int width = itemWidth - mSelectedBgMarginArray[0] - mSelectedBgMarginArray[2];
        int height = itemHeight - mSelectedBgMarginArray[1] - mSelectedBgMarginArray[3];
        LayoutParams lp = new LayoutParams(width, height);
        lp.gravity = Gravity.CENTER_VERTICAL;
        lp.leftMargin = mSelectedBgMarginArray[0] + itemWidth * mSelectedPosition;
        mSlideView.setLayoutParams(lp);
        mSlideView.setText(mTextArray[mSelectedPosition]);
    }

    private void setupDefaultSelectedBgMargin() {
        mSelectedBgMarginArray[0] = dpToPx(5);
        mSelectedBgMarginArray[1] = dpToPx(5);
        mSelectedBgMarginArray[2] = dpToPx(5);
        mSelectedBgMarginArray[3] = dpToPx(5);
    }

    private void generateDefaultTextArray() {
        mTextArray[0] = "未知";
        mTextArray[1] = "休息";
        mTextArray[2] = "上班";
        mTextArray[3] = "下班";
    }

    /**
     * 设置item点击监听
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    /**
     * 设置是否禁用滑动手势
     *
     * @param scrollEnable true：不禁用，false：禁用,只能点击
     */
    public void setScrollEnable(boolean scrollEnable) {
        mScrollEnable = scrollEnable;
    }

    /**
     * 平滑移动到上一个位置
     */
    public void smoothScrollToLastPosition(int position) {
        if (position == INVALIDATE_POSITION) {
            return;
        }
        int rangeDistance = (mLastSelectedPosition - position) * mItemWidth;
        moveTo(position, rangeDistance, 200);
    }

    /**
     * 平滑移动到指定位置
     */
    public void smoothScrollTo(int position) {
        if (position == INVALIDATE_POSITION) {
            return;
        }
        int rangeDistance = (position - mLastSelectedPosition) * mItemWidth;
        moveTo(position, rangeDistance, 200);
    }

    private void moveTo(final int position, float scrollDistance, long duration) {
        mSlideView.animate()
                .xBy(scrollDistance)
                .setDuration(duration)
                .withEndAction(new TimerTask() {
                    @Override
                    public void run() {
                        if (position != INVALIDATE_POSITION) {
                            mSlideView.setText(mTextArray[position]);
                        }
                    }
                })
                .start();
    }

    /**
     * 锁定指定位置，锁定之后该位置不可点击和也不能滑动到该位置
     */
    public void lockPosition(int position) {
        mLockedPosition = position;
    }

    /**
     * 解锁指定位置
     */
    public void unlockPosition(int position) {
        mLockedPosition = -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        if (widthMode == MeasureSpec.AT_MOST) {
            width = mItemWidth * mTotalItemCount;
        } else {
            width = widthSize;
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            height = mItemHeight;
        } else {
            height = heightSize;
        }
        setMeasuredDimension(Math.min(width, widthSize), Math.min(height, heightSize));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mItemWidth = getWidth() / mTotalItemCount;
        mItemHeight = getHeight();
        setupItemBoundsAndPoint();
    }

    private void setupItemBoundsAndPoint() {
        mItemBounds.clear();
        for (int i = 0; i < mTotalItemCount; i++) {
            mItemBounds.add(new RectF(i, 0, mItemWidth * (i + 1), mItemHeight));
            mItemCenterPoint.add(new PointF(mItemWidth * i + mItemWidth / 2, mSlideView.getY()));
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mTotalItemCount; i++) {
            mTextPaint.setColor(mNormalTextColor);
            mTextPaint.setTextSize(mNormalTextSize);
            drawText(canvas, i);
        }
    }

    private void drawText(Canvas canvas, int position) {
        String textToDraw = mTextArray[position];
        int textWidth = getTextWidth(textToDraw);
        canvas.drawText(textToDraw,
                (mItemWidth - textWidth) / 2 + mItemWidth * position,
                getTextStartY(),
                mTextPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                int downPosition = findPositionByPoint(mLastX, mLastY);
                if (downPosition == mLockedPosition) {
                    return false;//点击的是被锁定的位置则不处理
                }
                isSlideViewPressed = downPosition == mLastSelectedPosition;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mScrollEnable && isSlideViewPressed) {
                    //横向滑动范围
                    float x = event.getX();
                    if (mSlideView.getX() > 0 && mSlideView.getX() < getWidth() - mItemWidth * 4 / 5) {
                        float distance = x - mLastX;
                        moveTo(INVALIDATE_POSITION, distance, 0);
                    }
                    mLastX = (int) x;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollEnable && isSlideViewPressed) {
                    float x = mSlideView.getX() + mSlideView.getWidth() / 2;
                    float y = mSlideView.getY();
                    mSelectedPosition = findPositionByPoint(x, y);
                    float distance = findDistanceFromCurrentToTarget(x, y);
                    moveTo(mSelectedPosition, distance, 50);
                } else {
                    float x = event.getX();
                    float y = event.getY();
                    mSelectedPosition = findPositionByPoint(x, y);
                    if (mSelectedPosition == INVALIDATE_POSITION) {
                        break;
                    }

                    if (mSelectedPosition == mLastSelectedPosition) {
                        break;
                    }
                    smoothScrollTo(mSelectedPosition);
                }
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mSelectedPosition);
                }
                mLastSelectedPosition = mSelectedPosition;

                break;
        }
        return true;
    }

    private int findPositionByPoint(float x, float y) {
        if (x < mItemBounds.get(0).left) {
            return 0;
        }

        if (x >= mItemBounds.get(mItemBounds.size() - 1).right) {
            return mItemBounds.size() - 1;
        }

        for (int i = 0; i < mItemBounds.size(); i++) {

            if (mItemBounds.get(i).contains(x, y)) {
                return i;
            }
        }
        return INVALIDATE_POSITION;
    }

    private float findDistanceFromCurrentToTarget(float x, float y) {
        int targetPosition = findPositionByPoint(x, y);
        if (targetPosition == INVALIDATE_POSITION) {
            return 0;
        }
        PointF targetCenterPoint = mItemCenterPoint.get(targetPosition);
        //计算x方向的差值
        return targetCenterPoint.x - x;//>0：右滑，<0：左滑
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Bundle bundle = new Bundle();
        //保存父类的实现代码
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("selectedPosition", mSelectedPosition);
        bundle.putInt("lastSelectedPosition", mLastSelectedPosition);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            //获取父类的实现代码,并赋值
            state = bundle.getParcelable("superState");
            mSelectedPosition = bundle.getInt("selectedPosition");
            mLastSelectedPosition = bundle.getInt("lastSelectedPosition");
            setupLayoutParam(mItemWidth, mItemHeight);
        }
        super.onRestoreInstanceState(state);
    }

    public int dpToPx(int dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    public float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getContext().getResources().getDisplayMetrics());
    }

    private int getTextWidth(String text) {
        return (int) mTextPaint.measureText(text);
    }

    /**
     * 获取垂直方向文本的绘制起点(精确测量)
     */
    private int getTextStartY() {
        Paint.FontMetricsInt fm = mTextPaint.getFontMetricsInt();
        return getHeight() / 2 - fm.descent + (fm.descent - fm.ascent) / 2;
    }
}
