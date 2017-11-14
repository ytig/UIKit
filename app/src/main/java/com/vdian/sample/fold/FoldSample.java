package com.vdian.sample.fold;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vdian.refresh.RefreshView;
import com.vdian.sample.BaseSample;
import com.vdian.sample.fold.view.FoldView;
import com.vdian.sample.fold.view.FrameView;
import com.vdian.sample.fold.view.RootView;

/**
 * Created by zhangliang on 17/7/7.
 */
public class FoldSample extends BaseSample {
    public FoldSample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        FoldView fold = new FoldView(mContext);

        RootView title = new RootView(mContext);
        title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (56 * mContext.getResources().getDisplayMetrics().density)));
        fold.addView(title);
        TextView ti = new TextView(mContext);
        ti.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ti.setBackgroundColor(Color.parseColor("#fa7298"));
        ti.setGravity(Gravity.CENTER);
        ti.setTextColor(Color.WHITE);
        ti.setTextSize(16);
        ti.setText("TITLE");
        ti.setClickable(true);
        title.addView(ti);
        title.fake(false);

        NewViewPager pager = new NewViewPager(mContext);
        pager.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fold.addView(pager);
        PagerTabStrip indicator = new PagerTabStrip(mContext);
        ViewPager.LayoutParams lp = new ViewPager.LayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = (int) (34 * mContext.getResources().getDisplayMetrics().density);
        lp.gravity = Gravity.TOP;
        indicator.setLayoutParams(lp);
        indicator.setTextColor(Color.WHITE);
        indicator.setTabIndicatorColor(Color.WHITE);
        indicator.setBackgroundColor(Color.parseColor("#fa7298"));
        indicator.setDrawFullUnderline(true);
        pager.addView(indicator);

        RootView tail = new RootView(mContext) {
            @Override
            public boolean move(float dx, float dy) {
                return super.move(-dx * 56 / 40, -dy * 56 / 40);
            }
        };
        RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (40 * mContext.getResources().getDisplayMetrics().density));
        lp1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        tail.setLayoutParams(lp1);
        fold.addView(tail);
        TextView ta = new TextView(mContext);
        ta.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ta.setBackgroundColor(Color.parseColor("#fa7298"));
        ta.setGravity(Gravity.CENTER);
        ta.setTextColor(Color.WHITE);
        ta.setTextSize(16);
        ta.setText("TAIL");
        ta.setClickable(true);
        tail.addView(ta);
        tail.fake(false);

        new FoldView.FoldListener.Tools.VerticalSetBatch(fold, title, pager, tail).topExpand(false).autoFold(true).stopFling(false);
        return fold;
    }

    private static class NewViewPager extends ViewPager {
        private SparseArray<View> mViews;

        private NewViewPager(Context context) {
            super(context);
            mViews = new SparseArray<>();
            setAdapter(new PagerAdapter() {
                @Override
                public int getCount() {
                    return 10;
                }

                @Override
                public boolean isViewFromObject(View view, Object object) {
                    return (view == object);
                }

                @Override
                public Object instantiateItem(ViewGroup container, int position) {
                    View view = getView(position);
                    container.addView(view);
                    return view;
                }

                @Override
                public void destroyItem(ViewGroup container, int position, Object object) {
                    container.removeView((View) object);
                }

                @Override
                public CharSequence getPageTitle(int position) {
                    return "Page" + position;
                }
            });
        }

        private View getView(int position) {
            View view = mViews.get(position);
            if (view == null) {
                view = new NewFoldView(getContext(), position - 1, position);
                mViews.put(position, view);
            }
            return view;
        }
    }

    private static class NewFoldView extends FoldView {
        private NewAdapter mAdapter;

        private NewFoldView(Context context, int index, int size) {
            super(context);
            if (index < 0) {
                final RootView root = new RootView(getContext()) {
                    private float abs = 0;

                    @Override
                    public void down() {
                        abs = 0;
                        super.down();
                    }

                    @Override
                    public boolean move(float dx, float dy) {
                        float limit = 25 * getContext().getResources().getDisplayMetrics().density;
                        if (abs < limit) {
                            abs += Math.abs(dy);
                            if (abs >= limit) requestDisallowInterceptTouchEvent(true);
                        }
                        return super.move(dx, dy);
                    }
                };
                root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                addView(root);

                mAdapter = new NewAdapter(0);
                RecyclerView nested = new RecyclerView(getContext());
                nested.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                nested.setLayoutManager(new LinearLayoutManager(getContext()));
                nested.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
                nested.setItemAnimator(null);
                nested.setAdapter(mAdapter);
                root.addView(nested);

                FrameView frame = new FrameView(getContext());
                frame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                root.addView(frame);

                root.auto(5);
                root.setRefreshListener(new RefreshView.RefreshTopListener() {
                    @Override
                    public void topRefresh() {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.mCount = 10;
                                mAdapter.notifyDataSetChanged();
                                root.callback(true, (mAdapter.mCount >= 10 * 3) ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
                            }
                        }, (long) (1000L + 3000L * Math.random()));
                    }
                }, new RefreshView.RefreshBottomListener() {
                    @Override
                    public void bottomRefresh() {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.mCount += 10;
                                mAdapter.notifyDataSetChanged();
                                root.callback(false, (mAdapter.mCount >= 10 * 3) ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
                            }
                        }, 1000);
                    }
                });
            } else {
                RootView title = new RootView(getContext());
                title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                int left = 2, top = 1 + (index == size - 1 ? 1 : 0), right = 2, bottom = 1 + (index == 0 ? 1 : 0);
                title.setPadding((int) (left * getContext().getResources().getDisplayMetrics().density), (int) (top * getContext().getResources().getDisplayMetrics().density), (int) (right * getContext().getResources().getDisplayMetrics().density), (int) (bottom * getContext().getResources().getDisplayMetrics().density));
                addView(title);
                TextView ti = new TextView(getContext());
                ti.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (40 * getContext().getResources().getDisplayMetrics().density)));
                ti.setBackgroundColor(Color.parseColor("#fa7298"));
                ti.setGravity(Gravity.CENTER);
                ti.setTextColor(Color.WHITE);
                ti.setTextSize(14);
                ti.setText("Title" + (size - 1 - index));
                ti.setClickable(true);
                title.addView(ti);
                title.fake(false);

                NewFoldView fold = new NewFoldView(getContext(), index - 1, size);
                fold.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                addView(fold);

                new FoldListener.Tools.VerticalSetBatch(this, title, fold).topExpand(false).autoFold(true).stopFling(false);
            }
        }

        private static class NewAdapter extends RecyclerView.Adapter {
            private int mCount;

            private NewAdapter(int count) {
                mCount = count;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView tv = new TextView(parent.getContext());
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (66 * parent.getContext().getResources().getDisplayMetrics().density)));
                tv.setTextSize(14);
                tv.setGravity(Gravity.CENTER);
                return new RecyclerView.ViewHolder(tv) {
                };
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((TextView) holder.itemView).setText("Item" + position);
            }

            @Override
            public int getItemCount() {
                return mCount;
            }
        }
    }
}
