package com.vdian.sample.notice.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.vdian.uikit.view.extend.refresh.RefreshView;
import com.vdian.uikit.view.ViewGroupMonitor;
import com.vdian.uikit.wrapper.recycler.AppendWrapper;

import java.util.Collections;
import java.util.List;

/**
 * Created by zhangliang on 16/11/23.
 */
public class NoticeCenter extends RefreshView implements NoticeParent.NoticePatch, ViewGroupMonitor.ChildListener {
    private int line = 0;
    private int offset = 0;
    private int type = -1;
    private RecyclerView.ViewHolder holder;
    private RecyclerView recycler;
    private Boolean header;

    public NoticeCenter(Context context) {
        super(context);
        init();
    }

    public NoticeCenter(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NoticeCenter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        permit(false, false);
        setWillNotDraw(false);
        recycler = new ReformView(getContext()) {
            @Override
            protected int correct() {
                Adapter adapter = recycler.getAdapter();
                if (adapter == null) return 0;
                int correct = recycler.getHeight();
                for (int i = 1; i < adapter.getItemCount(); i++) {
                    RelativeLayout parent = new RelativeLayout(recycler.getContext());
                    RecyclerView.ViewHolder holder = adapter.createViewHolder(parent, adapter.getItemViewType(i));
                    adapter.bindViewHolder(holder, -i - 1);
                    parent.addView(holder.itemView);
                    new RelativeLayout(recycler.getContext()).addView(parent);
                    parent.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    correct -= parent.getMeasuredHeight();
                    if (correct < 0) {
                        correct = 0;
                        break;
                    }
                }
                return correct;
            }
        };
        recycler.setOverScrollMode(OVER_SCROLL_NEVER);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        addView(recycler);
        ViewGroupMonitor.localMonitor(recycler, this);
    }

    public void init(View header, RecyclerView.Adapter adapter) {
        recycler.setAdapter(new AppendWrapper(adapter).addHeaderView(header));
        adjust();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                adjust();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                adjust();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                adjust();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                adjust();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                adjust();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                adjust();
            }
        });
    }

    private void adjust() {
        int type = recycler.getAdapter().getItemCount() <= 1 ? -1 : recycler.getAdapter().getItemViewType(1);
        if (this.type != type) {
            this.type = type;
            if (holder != null) removeView(holder.itemView);
            if (type == -1) holder = null;
            else {
                holder = recycler.getAdapter().createViewHolder(this, type);
                addView(holder.itemView);
            }
        }
        if (holder != null) recycler.getAdapter().onBindViewHolder(holder, 1);
    }

    @Override
    public boolean judgeIntercept(float downX, float downY) {
        return isEdge(false, recycler);
    }

    @Override
    public void centerLayout(int top, int bottom) {
        LayoutParams lp1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp1.setMargins(0, 0, 0, bottom);
        setLayoutParams(lp1);
        LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp2.setMargins(0, top, 0, 0);
        recycler.setLayoutParams(lp2);
    }

    @Override
    public void centerDisplay(float value, float parallax) {
        line = (int) (recycler.getBottom() * value) - recycler.getTop();
        offset = line - (int) parallax;
        if (offset > 0) offset = 0;
        else line = (int) parallax;
        invalidate();
    }

    @Override
    public void resetNotice() {
        recycler.scrollToPosition(0);
    }

    @Override
    public List<ViewGroupMonitor.Child> handle(List<ViewGroupMonitor.Child> children) {
        boolean visable = false;
        int top = 0;
        int last = (holder == null ? 0 : holder.itemView.getHeight());
        Collections.sort(children);
        for (int i = children.size() - 1; i >= 0; i--) {
            ViewGroupMonitor.Child child = children.get(i);
            View view = recycler.getChildAt(child.index);
            int offset = child.rect.bottom - line;
            if (offset < 0) offset = 0;
            if (child.position == 0) offset += last;
            if (view.getScrollY() != offset) {
                view.scrollTo(0, offset);
                view.invalidate();
            }
            if (child.position >= 1) {
                visable = true;
                if (child.position == 1) {
                    top = child.rect.top - offset;
                    last = offset;
                    if (last > child.rect.height()) last = child.rect.height();
                }
            }
        }
        if (holder != null) {
            if (!visable)
                holder.itemView.setTranslationY(recycler.getTop() + line - holder.itemView.getHeight());
            else holder.itemView.setTranslationY(recycler.getTop() + (top < 0 ? 0 : top));
        }
        Boolean header = children.size() > 0 ? (children.get(0).position == 0) : null;
        if ((this.header != null ? !this.header : false) && (header != null ? header : false) && !touching() && !isEdge(true, recycler))
            recycler.smoothScrollBy(0, children.get(0).rect.bottom);
        this.header = header;
        return children;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.translate(0, offset);
        super.draw(canvas);
    }

    private static boolean isEdge(boolean isTop, RecyclerView view) {
        if (view.getAdapter() == null) return true;
        if (view.getAdapter().getItemCount() == 0) return true;
        if (isTop) {
            for (int i = 0; i < view.getChildCount(); i++) {
                View top = view.getChildAt(i);
                if (top != null) {
                    if (view.getChildAdapterPosition(top) == 0) {
                        if (top.getTop() >= view.getPaddingTop()) return true;
                    }
                }
            }
        } else {
            for (int i = view.getChildCount() - 1; i >= 0; i--) {
                View bottom = view.getChildAt(i);
                if (bottom != null) {
                    if (view.getChildAdapterPosition(bottom) == view.getAdapter().getItemCount() - 1) {
                        if (bottom.getBottom() + view.getPaddingBottom() <= view.getHeight())
                            return true;
                    }
                }
            }
        }
        return false;
    }
}
