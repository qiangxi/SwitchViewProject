# SwitchViewProject
### 效果图：
##### 默认效果
![默认效果](https://github.com/qiangxi/SwitchViewProject/blob/master/image/%E9%BB%98%E8%AE%A4.gif?raw=true)
##### 自定义文本及字体颜色、大小
![效果1](https://github.com/qiangxi/SwitchViewProject/blob/master/image/%E6%95%88%E6%9E%9C3.png?raw=true)
##### 自定义插值器，锁定位置（锁定位置为2），滑动时不展示文本 
![效果2](https://github.com/qiangxi/SwitchViewProject/blob/master/image/%E6%95%88%E6%9E%9C2.gif?raw=true)

### 更新日志
 
#### 2017-07-11 
发布第一个版本V1.0.0。
 
### 集成方式
#### gradle：
 
#### maven：
 
### API说明
API | API调用时机 | 对应的xml属性|作用
---|---|---|---
setOnItemClickListener(OnItemClickListener listener) | view初始化后的任意时刻|无|设置item点击监听
setScrollEnable(boolean scrollEnable)|view初始化后的任意时刻|app:scrollEnable="boolean"|设置是否禁用滑动手势【true:不禁用；false：禁用；默认为true】
smoothScrollTo(int position)|需要动态滑动时调用|无|平滑移动到指定位置【注意与setDefaultSelectedPosition(int position)方法区分开来。】
setShowTextWhenScrolling(boolean isShowText)|view初始化后的任意时刻|app:showTextWhenScrolling="boolean"|滑块在滑动时是否在滑块上显示文本【true：显示；false：不显示；默认为true】
setInterpolator(TimeInterpolator interpolator)|view初始化后的任意时刻|无|设置滑动插值器
setLockPosition(int position)|view初始化后的任意时刻|app:lockedPosition="interger"|指定锁定位置【位置被锁定后，该位置不可点击，且滑块滑动到该位置松开后，会自动滑动到原位置；只能指定一个位置为锁定位置】
unlockPosition()|view初始化后的任意时刻|无|解锁被锁定的位置【解锁后，所有位置都自由了】
getLockPosition()|view初始化后的任意时刻|无|获取被锁定的位置【未设置锁定位置时返回-1】
getTotalItemCount()|view初始化后的任意时刻|无|获取item总数量
setNormalTextColor(int normalTextColor)|view初始化后的任意时刻|app:normalTextColor="color"|设置默认文本颜色【未设置则使用默认】
setNormalTextSize(float normalTextSize)|view初始化后的任意时刻|app:normalTextSize="dimension"|设置默认文本字体大小【未设置则使用默认】
setSelectedTextColor(int selectedTextColor)|view初始化后的任意时刻|app:selectedTextColor="color"|设置选中文本颜色【未设置则使用默认】
setSelectedTextSize(float selectedTextSize)|view初始化后的任意时刻|app:selectedTextSize="dimension"|设置选中文本字体大小【未设置则使用默认】
setSelectedDrawableResId(@DrawableRes int selectedDrawableResId)|view初始化后的任意时刻|app:selectedDrawableResId="integer"|设置选中的item的背景drawable【未设置则使用默认】
setSelectedBgMarginArray(int[] selectedBgMarginArray) |view初始化时调用|无|设置滑块距离四周的margin值【整型数组,位置对应关系：[left,top,right,bottom]，数组长度必须为4】
setDefaultSelectedPosition(int position)|view初始化时调用|app:defaultSelectedPosition="integer"|设置滑块初始位置【未设置则使用默认值，即0位置】
setTextArray(String[] textArray)|view初始化时调用|app:textArray="reference"|设置文本内容【使用xml属性时，按照如下方式使用：app:textArray="@array/customTextArray"】
getSlideView()|view初始化后的任意时刻|无|获取滑块view【目前是TextView，将来可能会继承TextView自定义一个SlideView】
setEnable(boolean enable)|view初始化后的任意时刻|无|是否禁用所有手势【true:不禁用；false:禁用；禁用之后，不在响应任何手势，包括点击和滑动】
isEnable()|view初始化后的任意时刻|无|是否已经禁用所有手势
 
### 基本使用方式
 
### 最后
如果该项目对你有帮助，还请不吝赏赐star或fork。也欢迎提出issue或pr，一起维护这个项目。 
