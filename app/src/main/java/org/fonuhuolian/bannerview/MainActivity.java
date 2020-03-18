package org.fonuhuolian.bannerview;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.fonuhuolian.tbannerview.TBannerImageLoader;
import org.fonuhuolian.tbannerview.TBannerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private TBannerView bannerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        List<String> urls = new ArrayList<>();
        urls.add("https://raw.githubusercontent.com/fonuhuolian/XBottomBar/master/screenshot/a.png");
        urls.add("https://raw.githubusercontent.com/fonuhuolian/XTextView/master/screenshots/a.png");
        urls.add("https://raw.githubusercontent.com/fonuhuolian/XTextView/master/screenshots/b.png");


        bannerLayout = findViewById(R.id.banner);
        bannerLayout.setTBannerImageLoader(new TBannerImageLoader() {
            @Override
            public void displayImage(Context context, String path, ImageView imageView) {
                Picasso.get().load(path).into(imageView);
            }
        });
        bannerLayout.setViewUrls(urls);
    }

    public void click(View view) {
        List<String> urls = new ArrayList<>();
        urls.add("https://raw.githubusercontent.com/fonuhuolian/XBottomBar/master/screenshot/a.png");
        urls.add("https://raw.githubusercontent.com/fonuhuolian/XTextView/master/screenshots/a.png");
        urls.add("https://raw.githubusercontent.com/fonuhuolian/XTextView/master/screenshots/b.png");
        bannerLayout.setViewUrls(urls);
    }
}
