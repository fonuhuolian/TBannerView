package org.fonuhuolian.tbannerview;

import android.content.Context;
import android.widget.ImageView;

import java.io.Serializable;

public interface TBannerImageLoader extends Serializable {

    void displayImage(Context context, String path, ImageView imageView);
}
