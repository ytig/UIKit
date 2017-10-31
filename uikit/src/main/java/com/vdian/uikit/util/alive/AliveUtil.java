package com.vdian.uikit.util.alive;

import android.app.Activity;
import android.app.Fragment;

/**
 * Created by zhangliang on 16/11/16.
 */
public class AliveUtil {
    public static boolean isAlive(Object host) {
        Activity activity = find(host);
        if (activity != null) {
            if (!activity.isFinishing()) return true;
        }
        return false;
    }

    private static Activity find(Object host) {
        if (host instanceof Activity) return (Activity) host;
        if (host instanceof Fragment) return find((Fragment) host);
        if (host instanceof android.support.v4.app.Fragment)
            return find((android.support.v4.app.Fragment) host);
        return null;
    }

    private static Activity find(Fragment fragment) {
        return fragment.getActivity();
    }

    private static Activity find(android.support.v4.app.Fragment fragment) {
        return fragment.getActivity();
    }
}
