package com.vdian.wrapper;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewParent;
import android.widget.AdapterView;

/**
 * Created by zhangliang on 16/12/14.
 */
public class WrapperFinder {
    private Object mTarget; //定位层对象
    private String mName; //定位层类名
    private int mIndex; //定位层序号

    private WrapperFinder(Object target, Class clazz, int index) {
        mTarget = target;
        mName = (clazz == null) ? null : clazz.getName();
        mIndex = index;
    }

    public WrapperFinder(Object target) {
        this(target, null, 0);
    }

    public WrapperFinder(Class clazz, int index) {
        this(null, clazz, index);
    }

    public WrapperFinder(Class clazz) {
        this(null, clazz, 0);
    }

    public WrapperFinder(int index) {
        this(null, null, index);
    }

    public int getPosition(View view) {
        int position = Wrapper.NO_POSITION;
        Object adapter = null;
        while (view != null) {
            ViewParent parent = view.getParent();
            if (parent instanceof RecyclerView) {
                position = ((RecyclerView) parent).getChildAdapterPosition(view);
                adapter = ((RecyclerView) parent).getAdapter();
                if (position == RecyclerView.NO_POSITION || adapter == null)
                    position = Wrapper.NO_POSITION;
                break;
            }
            if (parent instanceof AdapterView) {
                position = ((AdapterView) parent).getPositionForView(view);
                adapter = ((AdapterView) parent).getAdapter();
                if (position == AdapterView.INVALID_POSITION || adapter == null)
                    position = Wrapper.NO_POSITION;
                break;
            }
            view = (parent instanceof View) ? (View) parent : null;
        }
        int index = (mTarget == null) ? mIndex : 0;
        while (position != Wrapper.NO_POSITION && adapter != null) {
            if (mTarget != null) {
                if (mTarget == adapter) break;
            } else {
                if (mName == null || mName.equals(adapter.getClass().getName())) {
                    if (index == 0) break;
                    else index--;
                }
            }
            if (adapter instanceof Wrapper) {
                position = ((Wrapper) adapter).toChildPosition(position);
                adapter = ((Wrapper) adapter).getChild();
            } else {
                if (index >= 0) position = Wrapper.NO_POSITION;
                else adapter = null;
            }
        }
        return position;
    }
}
