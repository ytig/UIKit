package com.vdian.uikit.wrapper.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by zhangliang on 16/11/3.
 */
public abstract class ReplaceWrapper extends BaseWrapper {
    public ReplaceWrapper(RecyclerView.Adapter adapter) {
        super(adapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                ReplaceWrapper.this.onChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                ReplaceWrapper.this.onItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                ReplaceWrapper.this.onItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                ReplaceWrapper.this.onItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                ReplaceWrapper.this.onItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                ReplaceWrapper.this.onItemMoved(fromPosition, toPosition);
            }
        });
        setHasStableIds(mAdapter.hasStableIds());
    }

    private void onChanged() {
        notifyDataSetChanged();
    }

    private void onItemRangeChanged(int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart, itemCount);
    }

    private void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        notifyItemRangeChanged(positionStart, itemCount, payload);
    }

    private void onItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart, itemCount);
    }

    private void onItemRangeRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    private void onItemMoved(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mAdapter.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        if (holder instanceof MyHolder) mAdapter.onViewRecycled(((MyHolder) holder).mHolder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        if (holder instanceof MyHolder)
            return mAdapter.onFailedToRecycleView(((MyHolder) holder).mHolder);
        return false;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof MyHolder)
            mAdapter.onViewAttachedToWindow(((MyHolder) holder).mHolder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (holder instanceof MyHolder)
            mAdapter.onViewDetachedFromWindow(((MyHolder) holder).mHolder);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parent = onReplaceViewGroup(parent, viewType);
        RecyclerView.ViewHolder holder = mAdapter.onCreateViewHolder(parent, viewType);
        if (parent instanceof ReplaceEvent) ((ReplaceEvent) parent).create(holder.itemView);
        return new MyHolder(parent, holder);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MyHolder) {
            View view = holder.itemView;
            if (view instanceof ReplaceEvent) ((ReplaceEvent) view).bind();
            mAdapter.onBindViewHolder(((MyHolder) holder).mHolder, position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if (holder instanceof MyHolder) {
            View view = holder.itemView;
            if (view instanceof ReplaceEvent) ((ReplaceEvent) view).bind();
            mAdapter.onBindViewHolder(((MyHolder) holder).mHolder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mAdapter.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return mAdapter.getItemId(position);
    }

    protected abstract ViewGroup onReplaceViewGroup(ViewGroup parent, int viewType);

    private static class MyHolder extends RecyclerView.ViewHolder {
        private RecyclerView.ViewHolder mHolder;

        private MyHolder(View itemView, RecyclerView.ViewHolder holder) {
            super(itemView);
            mHolder = holder;
        }
    }

    public interface ReplaceEvent {
        void create(View view);

        void bind();
    }
}
