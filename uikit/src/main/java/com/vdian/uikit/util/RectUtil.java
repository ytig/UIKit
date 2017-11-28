package com.vdian.uikit.util;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhangliang on 16/9/19.
 */
public class RectUtil {
    private static boolean vary = false;
    private static int[] location = new int[2];
    private static Queue<Rect> cache = new LinkedList<>();

    public static void vary() {
        vary = true;
    }

    public static Rect measure(View view) {
        return measure(view, new Rect());
    }

    public static Rect measure(View view, Rect rect) {
        view.getLocationOnScreen(location);
        float width = view.getWidth(), height = view.getHeight();
        if (vary) {
            do {
                width *= view.getScaleX();
                height *= view.getScaleY();
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            } while (view != null);
        }
        rect.set(location[0], location[1], location[0] + (int) width, location[1] + (int) height);
        return rect;
    }

    public static Rect obtain() {
        return cache.size() > 0 ? cache.remove() : new Rect();
    }

    public static void recycle(Rect rect) {
        cache.add(rect);
    }
}
