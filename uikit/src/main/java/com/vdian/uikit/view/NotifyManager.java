package com.vdian.uikit.view;

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by zhangliang on 16/11/15.
 */
public abstract class NotifyManager<D> {
    private RecyclerView mView; //控件
    private RecyclerView.Adapter mAdapter; //适配器
    private Rect mRect = new Rect(); //位置
    private List<D> mBefore; //更新前数据
    private List<D> mAfter; // 更新后数据
    private DiffUtil.Callback mCallback; //更新处理器
    private Boolean isAttached; //控件是否添加
    private ScrollOrder mLazy; //懒滚动指令

    public NotifyManager(RecyclerView view, RecyclerView.Adapter adapter) {
        mView = view;
        mAdapter = adapter;
        mRect = new Rect();
        mBefore = new ArrayList<>();
        mAfter = new ArrayList<>();
        mCallback = new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mBefore.size();
            }

            @Override
            public int getNewListSize() {
                return mAfter.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return NotifyManager.this.sameItem(mBefore.get(oldItemPosition), mAfter.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return NotifyManager.this.sameData(mBefore.get(oldItemPosition), mAfter.get(newItemPosition));
            }
        };
        mView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if (Boolean.TRUE.equals(isAttached)) return;
                isAttached = Boolean.TRUE;
                if (mLazy != null) {
                    scrollToPositionWithOffset(mLazy.stop, mLazy.position, mLazy.offset);
                    mLazy.recycle();
                    mLazy = null;
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (Boolean.FALSE.equals(isAttached)) return;
                isAttached = Boolean.FALSE;
            }
        });
    }

    /**
     * 数据变化前
     *
     * @param before
     */
    public void onPreChanged(List<D> before) {
        mBefore.addAll(before);
    }

    /**
     * 数据变化后
     *
     * @param after
     */
    public void onChanged(List<D> after) {
        mAfter.addAll(after);
        if (mLazy != null) {
            DiffUtil.calculateDiff(mCallback).dispatchUpdatesTo(mAdapter); //更新处理
            mView.postInvalidate();
        } else {
            boolean horizontal = false;
            RecyclerView.LayoutManager manager = mView.getLayoutManager();
            if (manager instanceof LinearLayoutManager)
                horizontal = ((LinearLayoutManager) manager).getOrientation() == OrientationHelper.HORIZONTAL;
            else {
                if (manager instanceof StaggeredGridLayoutManager)
                    horizontal = ((StaggeredGridLayoutManager) manager).getOrientation() == OrientationHelper.HORIZONTAL;
            }
            int position = RecyclerView.NO_POSITION, offset = 0;
            for (int i = 0; i < mView.getChildCount(); i++) {
                View child = mView.getChildAt(i);
                if (child != null) {
                    int p = mView.getChildAdapterPosition(child);
                    if ((p != RecyclerView.NO_POSITION) && (position == RecyclerView.NO_POSITION || position > p)) {
                        position = p; //修复位置
                        mView.getDecoratedBoundsWithMargins(child, mRect);
                        offset = horizontal ? (mRect.left - mView.getPaddingLeft()) : (mRect.top - mView.getPaddingTop()); //修复偏移
                    }
                }
            }
            DiffUtil.calculateDiff(mCallback).dispatchUpdatesTo(mAdapter); //更新处理
            mView.postInvalidate();
            scrollToPositionWithOffset(false, position, offset);
        }
        mBefore.clear();
        mAfter.clear();
    }

    /**
     * 控件滚动指令
     *
     * @param stop
     * @param position
     * @param offset
     */
    public void scrollToPositionWithOffset(boolean stop, int position, int offset) {
        if (isAttached == null)
            isAttached = ViewCompat.isAttachedToWindow(mView) ? Boolean.TRUE : Boolean.FALSE; //判断添加情况
        if (Boolean.TRUE.equals(isAttached)) {
            if (stop) mView.stopScroll(); //停止滚动
            RecyclerView.Adapter adapter = mView.getAdapter();
            int size = (adapter == null) ? 0 : adapter.getItemCount();
            if (size <= 0 || position < 0) {
                position = 0; //置顶
                offset = 0;
            } else {
                if (position > size - 1) {
                    position = size - 1; //置底
                    offset = Integer.MIN_VALUE / 2;
                }
            }
            RecyclerView.LayoutManager manager = mView.getLayoutManager();
            if (manager instanceof LinearLayoutManager)
                ((LinearLayoutManager) manager).scrollToPositionWithOffset(position, offset);
            else {
                if (manager instanceof StaggeredGridLayoutManager)
                    ((StaggeredGridLayoutManager) manager).scrollToPositionWithOffset(position, offset);
            }
        } else {
            if (mLazy == null) {
                mLazy = ScrollOrder.obtain(); //暂存滚动指令
                mLazy.stop = stop;
                mLazy.position = position;
                mLazy.offset = offset;
            } else {
                if (stop) mLazy.stop = true; //合并滚动指令
                mLazy.position = position;
                mLazy.offset = offset;
            }
        }
    }

    protected abstract boolean sameItem(D d1, D d2);

    protected abstract boolean sameData(D d1, D d2);

    private static class ScrollOrder {
        private static Queue<ScrollOrder> cache = new LinkedList<>(); //数据缓存

        public boolean stop; //滚动停止
        public int position; //滚动位置
        public int offset; //滚动偏移

        public static ScrollOrder obtain() {
            return cache.size() > 0 ? cache.remove() : new ScrollOrder();
        }

        public void recycle() {
            cache.add(this);
        }
    }
}
