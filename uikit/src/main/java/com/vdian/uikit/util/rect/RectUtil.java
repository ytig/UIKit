package com.vdian.uikit.util.rect;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by zhangliang on 16/9/19.
 */
public class RectUtil {
    private static int[] location = new int[2];

    public static Rect measure(View view) {
        Rect rect = new Rect();
        measure(view, rect);
        return rect;
    }

    public static void measure(View view, Rect rect) {
        view.getLocationOnScreen(location);
        float width = view.getWidth(), height = view.getHeight();
        do {
            width *= view.getScaleX();
            height *= view.getScaleY();
            ViewParent parent = view.getParent();
            view = parent instanceof View ? (View) parent : null;
        } while (view != null);
        rect.set(location[0], location[1], location[0] + (int) width, location[1] + (int) height);
    }
}
