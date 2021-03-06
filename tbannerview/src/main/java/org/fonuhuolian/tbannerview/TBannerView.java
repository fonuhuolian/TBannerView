package org.fonuhuolian.tbannerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by dongjunkun on 2015/8/9.
 */
public class TBannerView extends RelativeLayout {

    PageSelectedListener listener;

    private ViewPager pager;
    //指示器容器
    private LinearLayout indicatorContainer;

    private Drawable unSelectedDrawable;
    private Drawable selectedDrawable;

    // 临时保存
    private Drawable unSelectedDrawableTemp;
    private Drawable selectedDrawableTemp;

    private int WHAT_AUTO_PLAY = 1000;

    private boolean isAutoPlay = true;

    private int itemCount;

    private int selectedIndicatorColor = 0xffff0000;
    private int unSelectedIndicatorColor = 0x88888888;

    private Shape indicatorShape = Shape.oval;
    private int selectedIndicatorHeight = 6;
    private int selectedIndicatorWidth = 6;
    private int unSelectedIndicatorHeight = 6;
    private int unSelectedIndicatorWidth = 6;

    private int imageViewPaddingLeft = 0;
    private int imageViewPaddingRight = 0;
    private int imageViewPaddingTop = 0;
    private int imageViewPaddingBottom = 0;
    private int imageViewPadding = 0;

    private Position indicatorPosition = Position.centerBottom;
    private int autoPlayDuration = 4000;
    private int scrollDuration = 900;

    private int indicatorSpace = 3;
    private int indicatorMargin = 10;

    private int currentPosition;

    private TBannerImageLoader TBannerImageLoader;
    private TBannerViewsLoader tBannerViewsLoader;
    private int gravity;
    private float fraction;


    private Context context;

    private enum Shape {
        rect, oval, imageView
    }

    public void setListener(PageSelectedListener listener) {
        this.listener = listener;
    }

    private enum Position {
        centerBottom,
        rightBottom,
        leftBottom,
        centerTop,
        rightTop,
        leftTop
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoPlay();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoPlay();
    }

    private OnBannerItemClickListener onBannerItemClickListener;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT_AUTO_PLAY) {
                if (pager != null && isAutoPlay) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1, true);
                    handler.sendEmptyMessageDelayed(WHAT_AUTO_PLAY, autoPlayDuration);
                }
            }
            return false;
        }
    });

    public TBannerView(Context context) {
        this(context, null);
    }

    public TBannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TBannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyle) {

        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.TBannerView, defStyle, 0);
        selectedIndicatorColor = array.getColor(R.styleable.TBannerView_selectedIndicatorColor, selectedIndicatorColor);
        unSelectedIndicatorColor = array.getColor(R.styleable.TBannerView_unSelectedIndicatorColor, unSelectedIndicatorColor);

        gravity = array.getInt(R.styleable.TBannerView_bannerGravity, 0);
        fraction = array.getFraction(R.styleable.TBannerView_bannerInScreenPercent, 1, 1, 1);

        int shape = array.getInt(R.styleable.TBannerView_indicator_shape, Shape.oval.ordinal());
        for (Shape shape1 : Shape.values()) {
            if (shape1.ordinal() == shape) {
                indicatorShape = shape1;
                break;
            }
        }
        selectedIndicatorHeight = (int) array.getDimension(R.styleable.TBannerView_selectedIndicatorHeight, selectedIndicatorHeight);
        selectedIndicatorWidth = (int) array.getDimension(R.styleable.TBannerView_selectedIndicatorWidth, selectedIndicatorWidth);
        unSelectedIndicatorHeight = (int) array.getDimension(R.styleable.TBannerView_unSelectedIndicatorHeight, unSelectedIndicatorHeight);
        unSelectedIndicatorWidth = (int) array.getDimension(R.styleable.TBannerView_unSelectedIndicatorWidth, unSelectedIndicatorWidth);

        imageViewPadding = (int) array.getDimension(R.styleable.TBannerView_imageViewPadding, imageViewPadding);
        imageViewPaddingTop = (int) array.getDimension(R.styleable.TBannerView_imageViewPaddingTop, imageViewPaddingTop);
        imageViewPaddingBottom = (int) array.getDimension(R.styleable.TBannerView_imageViewPaddingBottom, imageViewPaddingBottom);
        imageViewPaddingLeft = (int) array.getDimension(R.styleable.TBannerView_imageViewPaddingLeft, imageViewPaddingLeft);
        imageViewPaddingRight = (int) array.getDimension(R.styleable.TBannerView_imageViewPaddingRight, imageViewPaddingRight);

        int position = array.getInt(R.styleable.TBannerView_indicator_position, Position.centerBottom.ordinal());
        for (Position position1 : Position.values()) {
            if (position == position1.ordinal()) {
                indicatorPosition = position1;
            }
        }
        indicatorSpace = (int) array.getDimension(R.styleable.TBannerView_indicatorSpace, indicatorSpace);
        indicatorMargin = (int) array.getDimension(R.styleable.TBannerView_indicatorMargin, indicatorMargin);
        autoPlayDuration = array.getInt(R.styleable.TBannerView_autoPlayDuration, autoPlayDuration);
        scrollDuration = array.getInt(R.styleable.TBannerView_scrollDuration, scrollDuration);
        isAutoPlay = array.getBoolean(R.styleable.TBannerView_isAutoPlay, isAutoPlay);
        unSelectedDrawableTemp = getResources().getDrawable(array.getResourceId(R.styleable.TBannerView_unSelectedIndicatorSrc, R.drawable.x_banner_unselect));
        selectedDrawableTemp = getResources().getDrawable(array.getResourceId(R.styleable.TBannerView_selectedIndicatorSrc, R.drawable.x_banner_select));
        array.recycle();

        //绘制未选中状态图形
        LayerDrawable unSelectedLayerDrawable;
        LayerDrawable selectedLayerDrawable;
        GradientDrawable unSelectedGradientDrawable;
        unSelectedGradientDrawable = new GradientDrawable();

        //绘制选中状态图形
        GradientDrawable selectedGradientDrawable;
        selectedGradientDrawable = new GradientDrawable();
        switch (indicatorShape) {
            case rect:
                unSelectedGradientDrawable.setShape(GradientDrawable.RECTANGLE);
                selectedGradientDrawable.setShape(GradientDrawable.RECTANGLE);

                unSelectedGradientDrawable.setColor(unSelectedIndicatorColor);
                unSelectedGradientDrawable.setSize(unSelectedIndicatorWidth, unSelectedIndicatorHeight);
                unSelectedLayerDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
                unSelectedDrawable = unSelectedLayerDrawable;

                selectedGradientDrawable.setColor(selectedIndicatorColor);
                selectedGradientDrawable.setSize(selectedIndicatorWidth, selectedIndicatorHeight);
                selectedLayerDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
                selectedDrawable = selectedLayerDrawable;
                break;
            case oval:
                unSelectedGradientDrawable.setShape(GradientDrawable.OVAL);
                selectedGradientDrawable.setShape(GradientDrawable.OVAL);

                unSelectedGradientDrawable.setColor(unSelectedIndicatorColor);
                unSelectedGradientDrawable.setSize(unSelectedIndicatorWidth, unSelectedIndicatorHeight);
                unSelectedLayerDrawable = new LayerDrawable(new Drawable[]{unSelectedGradientDrawable});
                unSelectedDrawable = unSelectedLayerDrawable;

                selectedGradientDrawable.setColor(selectedIndicatorColor);
                selectedGradientDrawable.setSize(selectedIndicatorWidth, selectedIndicatorHeight);
                selectedLayerDrawable = new LayerDrawable(new Drawable[]{selectedGradientDrawable});
                selectedDrawable = selectedLayerDrawable;
                break;
            case imageView:
                // 未选中 指示器图片 选中的 指示器图片
                unSelectedDrawable = unSelectedDrawableTemp;
                selectedDrawable = selectedDrawableTemp;
                break;
        }


    }


    //添加网络图片路径
    public void setViewUrls(List<String> urls) {
        List<View> views = new ArrayList<>();
        itemCount = urls.size();
        //主要是解决当item为小于3个的时候滑动有问题，这里将其拼凑成3个以上
        if (itemCount < 1) {//当item个数0
            throw new IllegalStateException("item count not equal zero");
        } else if (itemCount < 2) { //当item个数为1
            views.add(getViews(urls.get(0), 0));
            views.add(getViews(urls.get(0), 0));
            views.add(getViews(urls.get(0), 0));
        } else if (itemCount < 3) {//当item个数为2
            views.add(getViews(urls.get(0), 0));
            views.add(getViews(urls.get(1), 1));
            views.add(getViews(urls.get(0), 0));
            views.add(getViews(urls.get(1), 1));
        } else {
            for (int i = 0; i < urls.size(); i++) {
                views.add(getViews(urls.get(i), i));
            }
        }
        setViews(views);
    }

    //添加自定义布局
    public void setCustomViews(List<Integer> v, List data) {

        List<View> views = new ArrayList<>();
        itemCount = v.size();
        //主要是解决当item为小于3个的时候滑动有问题，这里将其拼凑成3个以上
        if (itemCount < 1) {//当item个数0
            throw new IllegalStateException("item count not equal zero");
        } else if (itemCount < 2) { //当item个数为1
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(0), 0, data));
        } else if (itemCount < 3) {//当item个数为2
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(1), 1, data));
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(1), 1, data));
        } else {
            for (int i = 0; i < v.size(); i++) {
                views.add(getViews(v.get(i), i, data));
            }
        }
        setViews(views);
    }

    //添加自定义布局
    public void setCustomViews(int res, List data) {

        List<Integer> v = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            v.add(res);
        }
        List<View> views = new ArrayList<>();
        itemCount = v.size();
        //主要是解决当item为小于3个的时候滑动有问题，这里将其拼凑成3个以上
        if (itemCount < 1) {//当item个数0
            throw new IllegalStateException("item count not equal zero");
        } else if (itemCount < 2) { //当item个数为1
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(0), 0, data));
        } else if (itemCount < 3) {//当item个数为2
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(1), 1, data));
            views.add(getViews(v.get(0), 0, data));
            views.add(getViews(v.get(1), 1, data));
        } else {
            for (int i = 0; i < v.size(); i++) {
                views.add(getViews(v.get(i), i, data));
            }
        }
        setViews(views);
    }

    @NonNull
    private View getViews(final Integer id, final int position, List data) {

        final View inflate = LayoutInflater.from(context).inflate(id, null);

        if (imageViewPadding != 0) {
            inflate.setPadding(imageViewPadding, imageViewPadding, imageViewPadding, imageViewPadding);
        } else {
            inflate.setPadding(imageViewPaddingLeft, imageViewPaddingTop, imageViewPaddingRight, imageViewPaddingBottom);
        }

        inflate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBannerItemClickListener != null) {
                    onBannerItemClickListener.onItemClick(position, inflate);
                }
            }
        });
        tBannerViewsLoader.displayView(getContext(), position, inflate, data.get(position));
        return inflate;
    }

    @NonNull
    private View getViews(String url, final int position) {
        final ImageView imageView = new ImageView(getContext());
        if (imageViewPadding != 0) {
            imageView.setPadding(imageViewPadding, imageViewPadding, imageViewPadding, imageViewPadding);
        } else {
            imageView.setPadding(imageViewPaddingLeft, imageViewPaddingTop, imageViewPaddingRight, imageViewPaddingBottom);
        }

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onBannerItemClickListener != null) {
                    onBannerItemClickListener.onItemClick(position, imageView);
                }
            }
        });
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        TBannerImageLoader.displayImage(getContext(), url, imageView);
        return imageView;
    }


    public void setTBannerImageLoader(TBannerImageLoader TBannerImageLoader) {
        this.TBannerImageLoader = TBannerImageLoader;
    }


    public void setTBannerViewsLoader(TBannerViewsLoader tBannerViewsLoader) {
        this.tBannerViewsLoader = tBannerViewsLoader;
    }

    public ViewPager getPager() {
        if (pager != null) {
            return pager;
        }
        return null;
    }

    //添加任意View视图
    private void setViews(final List<View> views) {

        this.setClipChildren(false);

        // 先清空所有控件
        removeAllViews();

        //初始化pager
        pager = new ViewPager(getContext());
        //添加viewpager到SliderLayout
        addView(pager);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        RelativeLayout.LayoutParams paramss = (LayoutParams) pager.getLayoutParams();
        paramss.width = (int) (metrics.widthPixels * fraction); // 宽度设置成屏幕宽度的86%，这里根据自己喜好设置
        // 高度不变
        paramss.addRule(gravity == 0 ? RelativeLayout.ALIGN_LEFT : RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        pager.setLayoutParams(paramss);
        pager.setClipChildren(false);
        this.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return pager.dispatchTouchEvent(motionEvent);
            }
        });
        setSliderTransformDuration(scrollDuration);
        //初始化indicatorContainer
        indicatorContainer = new LinearLayout(getContext());
        indicatorContainer.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        switch (indicatorPosition) {
            case centerBottom:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case centerTop:
                params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case leftBottom:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case leftTop:
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
            case rightBottom:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                break;
            case rightTop:
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                break;
        }
        //设置margin
        params.setMargins(indicatorMargin, indicatorMargin, indicatorMargin, indicatorMargin);
        //添加指示器容器布局到SliderLayout
        addView(indicatorContainer, params);

        //初始化指示器，并添加到指示器容器布局
        for (int i = 0; i < itemCount; i++) {
            ImageView indicator = new ImageView(getContext());
            indicator.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            indicator.setPadding(indicatorSpace, indicatorSpace, indicatorSpace, indicatorSpace);
            indicator.setImageDrawable(unSelectedDrawable);
            indicatorContainer.addView(indicator);
        }
        LoopPagerAdapter pagerAdapter = new LoopPagerAdapter(views);
        pager.setAdapter(pagerAdapter);
        //设置当前item到Integer.MAX_VALUE中间的一个值，看起来像无论是往前滑还是往后滑都是ok的
        //如果不设置，用户往左边滑动的时候已经划不动了
        int targetItemPosition = Integer.MAX_VALUE / 2 - Integer.MAX_VALUE / 2 % itemCount;
        currentPosition = targetItemPosition;
        pager.setCurrentItem(targetItemPosition);
        switchIndicator(targetItemPosition % itemCount);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                switchIndicator(position % itemCount);

                if (listener != null)
                    listener.onPageSelected(position);
            }
        });
        if (isAutoPlay) {
            startAutoPlay();
        }

    }

    public void setSliderTransformDuration(int duration) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(pager.getContext(), null, duration);
            mScroller.set(pager, scroller);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    /**
     * 开始自动轮播
     */
    private void startAutoPlay() {
        stopAutoPlay(); // 避免重复消息
        if (isAutoPlay) {
            handler.sendEmptyMessageDelayed(WHAT_AUTO_PLAY, autoPlayDuration);
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);

        if (visibility == VISIBLE) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }
    }


    /**
     * 停止自动轮播
     */
    private void stopAutoPlay() {
        if (pager != null) {
            pager.setCurrentItem(pager.getCurrentItem(), false);
        }
        if (isAutoPlay) {
            handler.removeMessages(WHAT_AUTO_PLAY);
            if (pager != null) {
                pager.setCurrentItem(pager.getCurrentItem(), false);
            }
        }
    }

    /**
     * @param autoPlay 是否自动轮播
     */
    public void setAutoPlay(boolean autoPlay) {
        isAutoPlay = autoPlay;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                stopAutoPlay();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startAutoPlay();
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 切换指示器状态
     *
     * @param currentPosition 当前位置
     */
    private void switchIndicator(int currentPosition) {
        for (int i = 0; i < indicatorContainer.getChildCount(); i++) {
            ((ImageView) indicatorContainer.getChildAt(i)).setImageDrawable(i == currentPosition ? selectedDrawable : unSelectedDrawable);
        }
    }


    public void setOnBannerItemClickListener(OnBannerItemClickListener onBannerItemClickListener) {
        this.onBannerItemClickListener = onBannerItemClickListener;
    }

    public interface OnBannerItemClickListener {
        void onItemClick(int position, View view);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    private static class SavedState extends BaseSavedState {
        int currentPosition;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    private class LoopPagerAdapter extends PagerAdapter {
        private List<View> views;

        LoopPagerAdapter(List<View> views) {
            this.views = views;
        }

        @Override
        public int getCount() {
            //Integer.MAX_VALUE = 2147483647
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (views.size() > 0) {
                //position % view.size()是指虚拟的position会在[0，view.size()）之间循环
                View view = views.get(position % views.size());
                if (container.equals(view.getParent())) {
                    container.removeView(view);
                }
                container.addView(view);
                return view;
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }
    }

    public class FixedSpeedScroller extends Scroller {

        private int mDuration = 1000;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator, int duration) {
            this(context, interpolator);
            mDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

    public interface PageSelectedListener {

        void onPageSelected(int position);
    }
}


