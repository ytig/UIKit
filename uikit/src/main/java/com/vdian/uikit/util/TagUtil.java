package com.vdian.uikit.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.view.View;
import android.view.Window;

import java.util.HashMap;

/**
 * Created by zhangliang on 16/9/12.
 */
public class TagUtil {
    private static final boolean COVER_TAG = false; //是否覆盖

    public static boolean put(Object host, String key, Object value) {
        View view = find(host);
        if (view != null) {
            Object tag = view.getTag();
            if (tag instanceof Map) {
                ((Map) tag).put(key, value);
                return true;
            }
            if (tag == null || COVER_TAG) {
                view.setTag(new Map(key, value));
                return true;
            }
        }
        return false;
    }

    public static Object get(Object host, String key) {
        return get(host, key, null);
    }

    public static Object get(Object host, String key, Object defaultValue) {
        View view = find(host);
        if (view != null) {
            Object tag = view.getTag();
            if (tag instanceof Map) {
                Object value = ((Map) tag).get(key);
                if (value != null) return value;
            }
        }
        return defaultValue;
    }

    private static View find(Object object) {
        if (object instanceof View) return (View) object;
        if (object instanceof Window) return find((Window) object);
        if (object instanceof Activity) return find((Activity) object);
        if (object instanceof Dialog) return find((Dialog) object);
        if (object instanceof Fragment) return find((Fragment) object);
        if (object instanceof android.support.v4.app.Fragment)
            return find((android.support.v4.app.Fragment) object);
        return null;
    }

    private static View find(Window window) {
        return window.getDecorView();
    }

    private static View find(Activity activity) {
        Window window = activity.getWindow();
        return window == null ? null : find(window);
    }

    private static View find(Dialog dialog) {
        Window window = dialog.getWindow();
        return window == null ? null : find(window);
    }

    private static View find(Fragment fragment) {
        if (fragment instanceof DialogFragment) {
            Dialog dialog = ((DialogFragment) fragment).getDialog();
            return dialog == null ? null : find(dialog);
        } else {
            Activity activity = fragment.getActivity();
            return activity == null ? null : find(activity);
        }
    }

    private static View find(android.support.v4.app.Fragment fragment) {
        if (fragment instanceof android.support.v4.app.DialogFragment) {
            Dialog dialog = ((android.support.v4.app.DialogFragment) fragment).getDialog();
            return dialog == null ? null : find(dialog);
        } else {
            Activity activity = fragment.getActivity();
            return activity == null ? null : find(activity);
        }
    }

    private static class Map extends HashMap<String, Object> {
        private Map(String key, Object value) {
            put(key, value);
        }
    }
}
