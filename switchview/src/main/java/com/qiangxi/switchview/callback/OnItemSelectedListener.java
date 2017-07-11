package com.qiangxi.switchview.callback;

/**
 * @author qiang_xi
 *         item选中监听
 */
public interface OnItemSelectedListener {

    /**
     * @param currentSelectedPosition 当前选中位置
     * @param lastSelectedPosition    上次选中位置
     */
    void onItemSelected(int currentSelectedPosition, int lastSelectedPosition);
}
