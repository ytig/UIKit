package com.vdian.sample.table.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.vdian.uikit.util.RectUtil;
import com.vdian.uikit.view.ViewGroupMonitor;
import com.vdian.wrapper.Wrapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhangliang on 17/1/11.
 */
public class TableView extends CompatView implements ViewGroupMonitor.ChildListener {
    private Recycler core;
    private RecyclerView recycler;
    private RecyclerView.AdapterDataObserver observer;

    public TableView(Context context) {
        super(context);
        init();
    }

    public TableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        delegate(true);
        overshoot(true);
        core = new Recycler();
        recycler = new RecyclerView(getContext());
        recycler.setItemAnimator(null);
        recycler.setOverScrollMode(OVER_SCROLL_NEVER);
        recycler.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        addView(recycler);
        observer = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                changed();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                changed();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                changed();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                changed();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                changed();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                changed();
            }

            private void changed() {
                core.update();
                postInvalidate();
            }
        };
        ViewGroupMonitor.globalMonitor(recycler, this);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        RecyclerView.Adapter old = recycler.getAdapter();
        if (old != null) {
            core.reset();
            old.unregisterAdapterDataObserver(observer);
        }
        recycler.setAdapter(adapter);
        if (adapter != null) {
            core.update();
            adapter.registerAdapterDataObserver(observer);
        }
    }

    @Override
    public List<ViewGroupMonitor.Child> handle(List<ViewGroupMonitor.Child> children) {
        core.update(children);
        return children;
    }

    private boolean isSection(int position) {
        RecyclerView.Adapter adapter = recycler.getAdapter();
        while (adapter != null && position >= 0 && position < adapter.getItemCount()) {
            if (adapter instanceof TableAdapter) {
                if (((TableAdapter) adapter).isSection(position)) return true;
            }
            if (adapter instanceof Wrapper) {
                Object child = ((Wrapper) adapter).getChild();
                if (child instanceof RecyclerView.Adapter) {
                    position = ((Wrapper) adapter).toChildPosition(position);
                    adapter = (RecyclerView.Adapter) child;
                } else adapter = null;
            } else adapter = null;
        }
        return false;
    }

    private class Recycler {
        private List<Section> sections = new ArrayList<>();
        private Rect rect = new Rect();

        protected void reset() {
            for (int i = sections.size() - 1; i >= 0; i--)
                removeView(sections.remove(i).holder.itemView);
        }

        protected void update() {
            for (int i = sections.size() - 1; i >= 0; i--)
                removeView(sections.remove(i).holder.itemView);
            for (int position = 0; position < recycler.getAdapter().getItemCount(); position++) {
                if (isSection(position)) {
                    Section section = new Section(recycler.getAdapter().getItemViewType(position));
                    section.bind(position);
                    sections.add(section);
                    addView(section.holder.itemView);
                }
            }
        }

        protected void update(List<ViewGroupMonitor.Child> children) {
            Collections.sort(children);
            RectUtil.measure(TableView.this, rect);
            int lastTop = Integer.MAX_VALUE;
            for (int i = sections.size() - 1; i >= 0; i--) {
                Section section = sections.get(i);
                boolean show = false;
                int top = Integer.MAX_VALUE;
                int index = -1;
                for (ViewGroupMonitor.Child child : children) {
                    if (section.position == child.position) {
                        top = child.rect.top - rect.top;
                        index = child.index;
                        break;
                    }
                    if (section.position < child.position) top = 0;
                }
                if (top <= 0) {
                    top = 0;
                    int height = section.height();
                    if (top + height > lastTop) top = lastTop - height;
                }
                if (lastTop > 0 && top <= 0) show = true;
                lastTop = top;
                if (show) {
                    if (section.holder.itemView.getVisibility() != View.VISIBLE)
                        section.holder.itemView.setVisibility(View.VISIBLE);
                    section.holder.itemView.setTranslationY(top + getScrollY());
                    View child = recycler.getChildAt(index);
                    if (child != null && child.getVisibility() != View.INVISIBLE)
                        child.setVisibility(View.INVISIBLE);
                } else {
                    if (section.holder.itemView.getVisibility() != View.INVISIBLE)
                        section.holder.itemView.setVisibility(View.INVISIBLE);
                    cancelOnClick(section.holder.itemView);
                    View child = recycler.getChildAt(index);
                    if (child != null && child.getVisibility() != View.VISIBLE)
                        child.setVisibility(View.VISIBLE);
                }
            }
        }

        private class Section {
            protected RecyclerView.ViewHolder holder;
            protected int type;
            protected int position;

            protected Section(int type) {
                this.holder = recycler.getAdapter().createViewHolder(TableView.this, type);
                this.type = type;
            }

            protected void bind(int position) {
                this.position = position;
                recycler.getAdapter().bindViewHolder(this.holder, this.position);
            }

            protected int height() {
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp.height >= 0) return lp.height;
                holder.itemView.measure(MeasureSpec.makeMeasureSpec(TableView.this.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                return holder.itemView.getMeasuredHeight();
            }
        }
    }

    public interface TableAdapter {
        boolean isSection(int position);
    }
}
