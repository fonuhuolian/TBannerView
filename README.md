# TBannerView
轮播图(某大神写的拿过来改了改，忘记是哪位大神了，感谢!!!)

> 添加依赖

`root build.gradle `
```
allprojects {
    repositories {
        ...
        maven {
            url 'https://jitpack.io'
        }
    }
}
```
`module build.gradle `
```
implementation 'com.github.fonuhuolian:TBannerView:1.0.3'
```

> 混淆
```
-dontwarn org.fonuhuolian.tbannerview.**
-keep class org.fonuhuolian.tbannerview.**{*;}
```

> xml

```
<!--autoPlayDuration 触发自动滚动的时间-->
<!--indicatorMargin 指示器的margin-->
<!--indicator_position 指示器的位置-->
<!--indicatorSpace 指示器之间的距离-->
<!--scrollDuration 图片自动滚动持续的时间-->
<!--isAutoPlay 默认自动轮播-->
<!--indicator_shape 指示器的样式
    rect 正方形
    oval 圆形
    支持以下属性：
        selectedIndicatorColor、unSelectedIndicatorColor
        selectedIndicatorWidth、selectedIndicatorHeight
        unSelectedIndicatorWidth、unSelectedIndicatorHeight
    imageView 自定义图片
    支持以下属性：
        selectedIndicatorSrc、unSelectedIndicatorSrc-->
<org.fonuhuolian.tbannerview.TBannerView
    android:id="@+id/banner"
    android:layout_width="match_parent"
    android:layout_height="168dp"
    android:layout_marginTop="10dp"
    app:autoPlayDuration="4000"
    app:indicatorMargin="5dp"
    app:indicator_position="centerBottom"
    app:indicator_shape="imageView"
    app:indicatorSpace="2dp"
    app:scrollDuration="600" />
```

> 代码

```
List<String> urls = new ArrayList<>();

TBannerView banner = findViewById(R.id.banner);
// 设置图片加载器
banner.setTBannerImageLoader(new TBannerImageLoader() {
    @Override
    public void displayImage(Context context, String path, ImageView imageView) {
        Picasso.get().load(path).into(imageView);
    }
});
banner.setViewUrls(urls);
```

