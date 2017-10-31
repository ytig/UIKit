package com.vdian.uikit.wrapper;

/**
 * Created by zhangliang on 16/12/14.
 */
public interface Wrapper {
    int NO_POSITION = -1;

    Object getChild();

    int toChildPosition(int position);
}
