package com.vdian.wrapper.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/10/20.
 */
public class AppendWrapper extends BaseWrapper {
    private List<View> mHeader = new ArrayList<>();
    private List<View> mFooter = new ArrayList<>();

    public AppendWrapper(RecyclerView.Adapter adapter) {
        super(adapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                AppendWrapper.this.onChanged();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                AppendWrapper.this.onItemRangeChanged(positionStart, itemCount);
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                AppendWrapper.this.onItemRangeChanged(positionStart, itemCount, payload);
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                AppendWrapper.this.onItemRangeInserted(positionStart, itemCount);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                AppendWrapper.this.onItemRangeRemoved(positionStart, itemCount);
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                AppendWrapper.this.onItemMoved(fromPosition, toPosition);
            }
        });
        setHasStableIds(mAdapter.hasStableIds());
    }

    public AppendWrapper addHeaderView(View view) {
        if (!hasObservers()) mHeader.add(view);
        return this;
    }

    public AppendWrapper addFooterView(View view) {
        if (!hasObservers()) mFooter.add(view);
        return this;
    }

    private void onChanged() {
        notifyDataSetChanged();
    }

    private void onItemRangeChanged(int positionStart, int itemCount) {
        notifyItemRangeChanged(positionStart + mHeader.size(), itemCount);
    }

    private void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        notifyItemRangeChanged(positionStart + mHeader.size(), itemCount, payload);
    }

    private void onItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart + mHeader.size(), itemCount);
    }

    private void onItemRangeRemoved(int positionStart, int itemCount) {
        notifyItemRangeRemoved(positionStart + mHeader.size(), itemCount);
    }

    private void onItemMoved(int fromPosition, int toPosition) {
        notifyItemMoved(fromPosition + mHeader.size(), toPosition + mHeader.size());
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
        if (!(holder instanceof MyHolder)) mAdapter.onViewRecycled(holder);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        if (!(holder instanceof MyHolder)) return mAdapter.onFailedToRecycleView(holder);
        return false;
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        if (!(holder instanceof MyHolder)) mAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        if (!(holder instanceof MyHolder)) mAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < mHeader.size()) return new MyHolder(mHeader.get(viewType));
        if (viewType < mHeader.size() + mFooter.size())
            return new MyHolder(mFooter.get(viewType - mHeader.size()));
        return mAdapter.onCreateViewHolder(parent, viewType - mHeader.size() - mFooter.size());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (!(holder instanceof MyHolder))
            mAdapter.onBindViewHolder(holder, position - mHeader.size());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if (!(holder instanceof MyHolder))
            mAdapter.onBindViewHolder(holder, position - mHeader.size(), payloads);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getItemCount() + mHeader.size() + mFooter.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeader.size()) return position;
        int over = position - mAdapter.getItemCount() - mHeader.size();
        if (over >= 0) return over + mHeader.size();
        return mAdapter.getItemViewType(position - mHeader.size()) + mHeader.size() + mFooter.size();
    }

    @Override
    public long getItemId(int position) {
        if (!hasStableIds()) return RecyclerView.NO_ID;
        if (position < mHeader.size()) return position;
        int over = position - mAdapter.getItemCount() - mHeader.size();
        if (over >= 0) return over + mHeader.size();
        return mAdapter.getItemId(position - mHeader.size()) + mHeader.size() + mFooter.size();
    }

    @Override
    public int toChildPosition(int position) {
        return super.toChildPosition(position - mHeader.size());
    }

    private static class MyHolder extends RecyclerView.ViewHolder {
        private MyHolder(View itemView) {
            super(itemView);
        }
    }
}
