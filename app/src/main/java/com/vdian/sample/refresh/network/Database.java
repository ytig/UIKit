package com.vdian.sample.refresh.network;

import android.os.Handler;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/11/14.
 */
public class Database {
    public static Database instanse = new Database();
    private static int count = 10;
    private static int initial = 10;
    private static long rate = 1000;

    private long time = -1;
    private Handler handler = new Handler();
    private List<Data> database = new ArrayList<>();

    public void request(final Integer minId, final Callback callback, boolean success) {
        if (success) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    update();
                    List<Data> datas = new ArrayList<>();
                    int start = 0;
                    for (int i = 0; i < database.size(); i++) {
                        if (database.get(i).getId() < ((minId == null) ? Integer.MAX_VALUE : minId))
                            break;
                        start = i + 1;
                    }
                    for (int i = start; i < start + count; i++) {
                        if (i >= database.size()) break;
                        datas.add(new Data(database.get(i)));
                    }
                    callback.response(datas);
                }
            }, 1000);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    update();
                    callback.error();
                }
            }, 3000);
        }
    }

    private void update() {
        long now = AnimationUtils.currentAnimationTimeMillis();
        int count;
        if (time == -1) {
            time = now;
            count = initial;
        } else count = (int) ((now - time) / rate);
        if (count > 0) {
            for (; count > 0; count--)
                database.add(0, new Data(database.size() <= 0 ? 0 : (database.get(0).getId() + 1)));
            time = now;
        }
    }

    public interface Callback {
        void response(List<Data> datas);

        void error();
    }
}
