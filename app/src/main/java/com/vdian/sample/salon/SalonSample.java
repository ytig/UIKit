package com.vdian.sample.salon;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.vdian.sample.BaseSample;
import com.vdian.sample.R;
import com.vdian.sample.salon.view.FrescoSalonView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 17/2/13.
 */
public class SalonSample extends BaseSample {
    private static int error = (int) (6 * Math.random());

    public SalonSample(Context context) {
        super(context);
        Fresco.initialize(mContext);
        FrescoSalonView.setUrlRule(new FrescoSalonView.UrlRule() {
            @Override
            public String toLowUrl(String url) {
                if (url.startsWith(",")) return "";
                if (url.endsWith(",")) return url.substring(0, url.length() - 1);
                String[] urls = url.split(",");
                if (urls.length == 2) return urls[0];
                return "";
            }

            @Override
            public String toHighUrl(String url) {
                if (url.endsWith(",")) return "";
                if (url.startsWith(",")) return url.substring(1, url.length());
                String[] urls = url.split(",");
                if (urls.length == 2) return urls[1];
                return url;
            }
        });
    }

    @Override
    public View init() {
        List<String> lows = new ArrayList<>();
        List<String> highs = new ArrayList<>();
        lows.add("res:///" + R.drawable.salon1);
        highs.add(error != 1 ? ("res:///" + R.drawable.salon2) : "");
        lows.add("res:///" + R.drawable.salon3);
        highs.add(error != 3 ? ("res:///" + R.drawable.salon4) : "");
        lows.add("res:///" + R.drawable.salon5);
        highs.add(error != 5 ? ("res:///" + R.drawable.salon6) : "");
        return init(lows, highs);
    }

    private View init(List<String> lows, List<String> highs) {
        RelativeLayout root = new RelativeLayout(mContext);
        final FrescoSalonView salon = new FrescoSalonView(root);
        final List<String> urls = new ArrayList<>();
        final List<View> views = new ArrayList<>();
        int line = 3;
        int padding = (int) (8 * mContext.getResources().getDisplayMetrics().density);
        int length = (mContext.getResources().getDisplayMetrics().widthPixels - (line + 1) * padding) / line;
        for (int i = 0; i < lows.size(); i++) {
            urls.add(lows.get(i) + "," + highs.get(i));
            SimpleDraweeView image = new SimpleDraweeView(mContext);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(length, length);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            image.setLayoutParams(lp);
            if (lows.size() < line)
                image.setTranslationX((i - (lows.size() - 1) / 2f) * (length + padding));
            else {
                image.setTranslationX((i % line - (line - 1) / 2f) * (length + padding));
                image.setTranslationY((i / line - ((lows.size() - 1) / line) / 2f) * (length + padding));
            }
            image.setImageURI(Uri.parse(lows.get(i)));
            image.setTag(Integer.valueOf(i));
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    salon.display((Integer) v.getTag(), urls, views);
                }
            });
            root.addView(image);
            views.add(image);
        }
        return root;
    }
}
