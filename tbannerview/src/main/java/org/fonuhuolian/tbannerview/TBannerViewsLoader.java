package org.fonuhuolian.tbannerview;

import android.content.Context;
import android.view.View;

import java.io.Serializable;

public interface TBannerViewsLoader extends Serializable {

    void displayView(Context context, int positon, View view);
}
