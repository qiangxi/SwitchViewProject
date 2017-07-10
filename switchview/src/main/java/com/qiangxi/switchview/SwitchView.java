package com.qiangxi.switchview;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiangxi.switchview.callback.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 * @author qiang_xi
 */
public class SwitchView extends LinearLayout {
    //默认值
    private static final int DEFAULT_ITEM_WIDTH = 80;//默认item宽度，单位dp
    private static final int DEFAULT_ITEM_HEIGHT = 50;//默认item高度，单位dp
    private static final int DEFAULT_TEXT_SIZE = 14;//默认文本大小
    private static final int INVALIDATE_POSITION = -1;//无效位置

    //position
    private int mLastSelectedPosition = INVALIDATE_POSITION;//滑块所处的上一个位置
    private int mSelectedPosition;//滑块所处的当前位置
    private int mLockedPosition = INVALIDATE_POSITION;//锁定的位置
    //颜色
    private int mNormalTextColor;//正常的文字颜色
    private int mSelectedTextColor;//选中的文字颜色
    private int mSelectedDrawableResId;//选中item的背景drawable id
    //字体大小
    private float mNormalTextSize;
    private float mSelectedTextSize;
    //范围
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
    private String[] mTextArray = {"未知", "休息", "上班", "下班"};

    private int mLastX; //point

    private OnItemClickListener mItemClickListener;
    private TextView mSlideView; //the SlideView
    private TimeInterpolator mInterpolator; //插值器
    private boolean mScrollEnable = true;//是否禁用滑动手势
    private boolean isEnable = true; //是否禁用一切手势,默认不禁用
    private boolean isSlideViewPressed;
    private boolean isShowText = true;

    public SwitchView(Context context) {
        this(context, null);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwitchView);
        mScrollEnable = a.getBoolean(R.styleable.SwitchView_scrollEnable, true);
        isShowText = a.getBoolean(R.styleable.SwitchView_showTextWhenScrolling, true);
        mNormalTextColor = a.getColor(R.styleable.SwitchView_normalTextColor, Color.WHITE);
        mNormalTextSize = a.getDimensionPixelSize(R.styleable.SwitchView_normalTextSize, DEFAULT_TEXT_SIZE);
        mSelectedTextColor = a.getColor(R.styleable.SwitchView_selectedTextColor, Color.RED);
        mSelectedTextSize = a.getDimensionPixelSize(R.styleable.SwitchView_selectedTextSize, DEFAULT_TEXT_SIZE);
        mSelectedDrawableResId = a.getResourceId(R.styleable.SwitchView_selectedDrawableResId, R.drawable.bg_selected_drawable);
        mLockedPosition = a.getInt(R.styleable.SwitchView_lockedPosition, INVALIDATE_POSITION);
        CharSequence[] textArray = a.getTextArray(R.styleable.SwitchView_textArray);
        mSelectedPosition = mLastSelectedPosition = a.getInt(R.styleable.SwitchView_defaultSelectedPosition, 0);
        a.recycle();
        convertCharSequenceArrayToStringArray(textArray);
        init();
    }

    private void generateDefaultSelectedBgMargin() {
        mSelectedBgMarginArray[0] = dpToPx(5);
        mSelectedBgMarginArray[1] = dpToPx(5);
        mSelectedBgMarginArray[2] = dpToPx(5);
        mSelectedBgMarginArray[3] = dpToPx(5);
    }

    private void convertCharSequenceArrayToStringArray(CharSequence[] array) {
        if (array != null) {
            mTextArray = null;
            mTextArray = new String[array.length];
            for (int i = 0; i < array.length; i++) {
                mTextArray[i] = array[i].toString();
            }
        }
    }

    private void init() {
        mInterpolator = new LinearInterpolator();
        mItemWidth = dpToPx(DEFAULT_ITEM_WIDTH);
        mItemHeight = dpToPx(DEFAULT_ITEM_HEIGHT);
        mTextPaint.setDither(true);
        mBgPaint.setDither(true);
        generateDefaultSelectedBgMargin();
        setupSlideView();
    }

    private void setupSlideView() {
        mSlideView = null;
        removeAllViews();
        mSlideView = new TextView(getContext());
        setupLayoutParameter(mItemWidth, mItemHeight);
        mSlideView.setBackgroundResource(mSelectedDrawableResId);
        mSlideView.setTextColor(mSelectedTextColor);
        mSlideView.setGravity(Gravity.CENTER);
        mSlideView.setTextSize(mSelectedTextSize);
        addView(mSlideView);
    }

    private int oldItemWidth;
    private int oldItemHeight;

    /**
     * 增加oldItemWidth，oldItemHeight，用来防止滑动时抖动。
     * 抖动的原因：属性动画对采用xBy的方式位移时，每次都会回调onLayout方法，
     * 由于在onLayout方法中调用了这个方法，导致每次都会重设SlideView的LayoutParameter，
     * 但实际上宽高并没有发生改变，没必要每次都重设SlideView的LayoutParameter，
     * 所以添加oldItemWidth，oldItemHeight，用来过滤掉一些非必要的调用，
     * 既解决了抖动的问题，又节省了大量性能。
     */
    private void setupLayoutParameter(int itemWidth, int itemHeight) {
        if (oldItemWidth != itemWidth || oldItemHeight != itemHeight) {
            int width = itemWidth - mSelectedBgMarginArray[0] - mSelectedBgMarginArray[2];
            int height = itemHeight - mSelectedBgMarginArray[1] - mSelectedBgMarginArray[3];
            LayoutParams lp = new LayoutParams(width, height);
            lp.gravity = Gravity.CENTER_VERTICAL;
            lp.leftMargin = mSelectedBgMarginArray[0] + itemWidth * mSelectedPosition;
            mSlideView.setLayoutParams(lp);
        }
        oldItemWidth = itemWidth;
        oldItemHeight = itemHeight;
        mSlideView.setText(mTextArray[mSelectedPosition]);
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
     * 平滑移动到指定位置
     */
    public void smoothScrollTo(int position) {
        if (position == INVALIDATE_POSITION) {
            return;
        }
        int rangeDistance = (position - mLastSelectedPosition) * mItemWidth;
        moveTo(position, rangeDistance, 200);
    }

    /**
     * 让SlideView在指定时间内移动指定距离
     *
     * @param position       移动到的目标位置
     * @param scrollDistance 移动的距离
     * @param duration       时长
     */
    private void moveTo(final int position, float scrollDistance, long duration) {
        if (!isShowText) {
            mSlideView.setText("");
        }
        if (position != INVALIDATE_POSITION) {
            mLastSelectedPosition = position;
        }
        mSlideView.animate().xBy(scrollDistance).setDuration(duration).setInterpolator(mInterpolator)
                .withEndAction(new TimerTask() {
                    @Override
                    public void run() {
                        if (position != INVALIDATE_POSITION && position < mTextArray.length) {
                            mSlideView.setText(mTextArray[position]);
                        }
                    }
                });
    }

    /**
     * 在SlideView滑动的时候是否展示文本
     */
    public void setShowTextWhenScrolling(boolean isShowText) {
        this.isShowText = isShowText;
    }

    /**
     * 设置插值器
     */
    public void setInterpolator(TimeInterpolator interpolator) {
        if (interpolator == null) {
            return;
        }
        mInterpolator = interpolator;
    }

    /**
     * 锁定指定位置，锁定之后该位置不可点击和也不能滑动到该位置
     */
    public void setLockPosition(int position) {
        mLockedPosition = position;
    }

    /**
     * 解锁指定位置
     */
    public void unlockPosition() {
        mLockedPosition = -1;
    }

    public int getLockPosition() {
        return mLockedPosition;
    }

    /**
     * 获取item总数量
     */
    public int getTotalItemCount() {
        return mTextArray.length;
    }

    /**
     * 设置未选中的item的文本颜色
     */
    public void setNormalTextColor(int normalTextColor) {
        mNormalTextColor = normalTextColor;
        invalidate();
    }

    /**
     * 设置选中的item的文本颜色
     */
    public void setSelectedTextColor(int selectedTextColor) {
        mSelectedTextColor = selectedTextColor;
        if (mSlideView != null) {
            mSlideView.setTextColor(mSelectedTextColor);
        }
    }

    /**
     * 设置选中的item的背景drawable
     */
    public void setSelectedDrawableResId(@DrawableRes int selectedDrawableResId) {
        if (selectedDrawableResId == 0) {
            return;
        }
        mSelectedDrawableResId = selectedDrawableResId;
        if (mSlideView != null) {
            mSlideView.setBackgroundResource(mSelectedDrawableResId);
        }
    }

    /**
     * 设置未选中的item的字体大小
     */
    public void setNormalTextSize(float normalTextSize) {
        mNormalTextSize = normalTextSize;
        invalidate();
    }

    /**
     * 设置选中的item的字体大小
     */
    public void setSelectedTextSize(float selectedTextSize) {
        mSelectedTextSize = selectedTextSize;
        if (mSlideView != null) {
            mSlideView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTextSize);
        }
    }

    /**
     * 设置选中的item的四周的margin值
     *
     * @param selectedBgMarginArray 位置关系：int[left,top，right，bottom]
     */
    public void setSelectedBgMarginArray(int[] selectedBgMarginArray) {
        if (selectedBgMarginArray == null || selectedBgMarginArray.length != 4) {
            return;
        }
        mSelectedBgMarginArray = selectedBgMarginArray;
    }

    /**
     * 该方法用来设置默认选中位置
     * 其他需要移动位置的情况请使用{@link #smoothScrollTo(int)}
     */
    public void setDefaultSelectedPosition(int position) {
        mSelectedPosition = position;
        setupLayoutParameter(mItemWidth, mItemHeight);
        mLastSelectedPosition = position;
    }

    /**
     * 设置item中填充的文本【运行时不可动态更改】
     */
    public void setTextArray(String[] textArray) {
        if (textArray == null) {
            throw new IllegalArgumentException("textArray不可为null");
        }
        mTextArray = textArray;
    }

    /**
     * 返回SlideView
     *
     * @return
     */
    public TextView getSlideView() {
        return mSlideView;
    }

    public boolean isEnable() {
        return isEnable;
    }

    /**
     * 是否禁用一切手势
     *
     * @param enable false:禁用,true不禁用
     */
    public void setEnable(boolean enable) {
        isEnable = enable;
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
            width = mItemWidth * mTextArray.length;
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
        mItemWidth = getWidth() / mTextArray.length;
        mItemHeight = getHeight();
        setupLayoutParameter(mItemWidth, mItemHeight);
        setupItemBoundsAndPoint();
    }

    private void setupItemBoundsAndPoint() {
        mItemBounds.clear();
        mItemCenterPoint.clear();
        for (int i = 0; i < mTextArray.length; i++) {
            //每个position的范围，用来判断任意点是否在某个范围内
            mItemBounds.add(new RectF(i, 0, mItemWidth * (i + 1), mItemHeight));
            //每个item的中心点（顶部居中），用来获取手指抬起时，SlideView的顶部中心点与某个item中心点的距离
            mItemCenterPoint.add(new PointF(mItemWidth * i + mItemWidth / 2, mSlideView.getY()));
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mTextPaint.setColor(mNormalTextColor);
        mTextPaint.setTextSize(spToPx(mNormalTextSize));
        for (int i = 0; i < mTextArray.length; i++) {
            drawText(canvas, i);
        }
        super.dispatchDraw(canvas);
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
                if (!isEnable) {
                    return false;
                }
                mLastX = (int) event.getX();
                int lastY = (int) event.getY();
                int downPosition = findPositionByPoint(mLastX, lastY);
                //判断按下的是否是SlideView
                isSlideViewPressed = downPosition == mLastSelectedPosition;
                //若按下的是SlideView，则捕获该次事件
                if (isSlideViewPressed) {
                    break;
                }
                //按下的是被锁定的位置则不处理
                if (downPosition == mLockedPosition) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mScrollEnable && isSlideViewPressed) {
                    float x = event.getX();
                    //mSlideView横向滑动范围,在这个范围内都可滑动
                    if (mSlideView.getX() > 0 && mSlideView.getX() < getWidth() - mItemWidth * 4 / 5) {
                        float distance = x - mLastX;
                        moveTo(INVALIDATE_POSITION, distance, 0);
                    }
                    mLastX = (int) x;
                }
                break;
            case MotionEvent.ACTION_UP:
                int lastSelectedPosition = mLastSelectedPosition;
                if (mScrollEnable && isSlideViewPressed) {
                    float x = mSlideView.getX() + mSlideView.getWidth() / 2;
                    float y = mSlideView.getY();
                    int targetPosition = findPositionByPoint(x, y);
                    //若目标位置是锁定位置，则返回到上一个位置
                    if (targetPosition == mLockedPosition) {
                        float distance = calculateDistanceFromCurrentToTarget(x, mLastSelectedPosition);
                        moveTo(mLastSelectedPosition, distance, 50);
                        break;
                    } else {
                        mSelectedPosition = targetPosition;
                        float distance = calculateDistanceFromCurrentToTarget(x, mSelectedPosition);
                        //当前选中位置与上次选中位置是否相等，若相等则只滑动，不触发回调，否则既滑动又触发回调
                        if (mSelectedPosition == mLastSelectedPosition) {
                            moveTo(mSelectedPosition, distance, 50);
                            break;
                        } else {
                            moveTo(mSelectedPosition, distance, 50);
                        }
                    }
                } else if (!isSlideViewPressed) {
                    float x = event.getX();
                    float y = event.getY();
                    int targetPosition = findPositionByPoint(x, y);
                    //若目标位置是锁定位置，则返回到上一个位置
                    if (targetPosition == mLockedPosition) {
                        break;
                    }
                    mSelectedPosition = targetPosition;
                    if (mSelectedPosition == INVALIDATE_POSITION) {
                        break;
                    }
                    if (mSelectedPosition == mLastSelectedPosition) {
                        break;
                    }
                    smoothScrollTo(mSelectedPosition);
                }
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mSelectedPosition, lastSelectedPosition);
                }
                break;
        }
        return true;
    }

    /**
     * 根据任意点x，y，找到该点对应的position
     *
     * @param x 给定点的x坐标
     * @param y 给定点的x坐标
     * @return 给定点对应的position
     */
    private int findPositionByPoint(float x, float y) {
        for (int i = 0; i < mItemBounds.size(); i++) {
            if (mItemBounds.get(i).contains(x, y)) {
                return i;
            }
        }
        //纠偏，防止数组越界
        if (x < mItemBounds.get(0).left) {
            return 0;
        }
        //纠偏，防止数组越界
        if (x >= mItemBounds.get(mItemBounds.size() - 1).right) {
            return mItemBounds.size() - 1;
        }
        return INVALIDATE_POSITION;
    }

    /**
     * 计算手指抬起点到目标点的距离，用于滑动
     *
     * @param x 给定点的x坐标
     * @return 任意点到目标点的距离
     * @see #findPositionByPoint(float x, float y);根据该方法查找目标点
     */
    private float calculateDistanceFromCurrentToTarget(float x, int targetPosition) {
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
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("selectedPosition", mSelectedPosition);
        bundle.putInt("lastSelectedPosition", mLastSelectedPosition);
        bundle.putInt("oldItemWidth", oldItemWidth);
        bundle.putInt("oldItemHeight", oldItemHeight);
        bundle.putInt("normalTextColor", mNormalTextColor);
        bundle.putFloat("normalTextSize", mNormalTextSize);
        bundle.putInt("selectedTextColor", mSelectedTextColor);
        bundle.putFloat("selectedTextSize", mSelectedTextSize);
        bundle.putInt("selectedDrawableRedId", mSelectedDrawableResId);
        bundle.putBoolean("scrollEnable", mScrollEnable);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            state = bundle.getParcelable("superState");
            mSelectedPosition = bundle.getInt("selectedPosition");
            mLastSelectedPosition = bundle.getInt("lastSelectedPosition");
            oldItemWidth = bundle.getInt("oldItemWidth");
            oldItemHeight = bundle.getInt("oldItemHeight");
            mNormalTextColor = bundle.getInt("normalTextColor");
            mNormalTextSize = bundle.getFloat("normalTextSize");
            mSelectedTextColor = bundle.getInt("selectedTextColor");
            mSelectedTextSize = bundle.getFloat("selectedTextSize");
            mSelectedDrawableResId = bundle.getInt("selectedDrawableRedId");
            mScrollEnable = bundle.getBoolean("scrollEnable");
            invalidate();
            setupLayoutParameter(mItemWidth, mItemHeight);
            if (mSlideView != null) {
                mSlideView.setTextColor(mSelectedTextColor);
                mSlideView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mSelectedTextSize);
                mSlideView.setBackgroundResource(mSelectedDrawableResId);
            }
        }
        super.onRestoreInstanceState(state);
    }

    private int dpToPx(int dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, getContext().getResources().getDisplayMetrics());
    }

    private int getTextWidth(String text) {
        return (int) mTextPaint.measureText(text);
    }

    private int getTextStartY() {
        Paint.FontMetricsInt fm = mTextPaint.getFontMetricsInt();
        return getHeight() / 2 - fm.descent + (fm.descent - fm.ascent) / 2;
    }
}
