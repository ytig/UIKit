package com.vdian.sample.alipay;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vdian.sample.BaseSample;
import com.vdian.sample.alipay.view.TitleView;
import com.vdian.uikit.view.extend.refresh.RefreshView;

/**
 * Created by zhangliang on 16/10/24.
 */
public class AlipaySample extends BaseSample {
    public AlipaySample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        final TitleView parent = new TitleView(mContext);
        parent.setTitleColor(Color.parseColor("#4285f4"));

        TextView title0 = new TextView(mContext);
        title0.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (50 * mContext.getResources().getDisplayMetrics().density)));
        title0.setGravity(Gravity.CENTER);
        title0.setTextColor(Color.WHITE);
        title0.setTextSize(18);
        title0.setText("ONE");
        parent.title0.addView(title0);
        TextView title1 = new TextView(mContext);
        title1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (50 * mContext.getResources().getDisplayMetrics().density)));
        title1.setGravity(Gravity.CENTER);
        title1.setTextColor(Color.WHITE);
        title1.setTextSize(18);
        title1.setText("TWO");
        parent.title1.addView(title1);
        TextView title2 = new TextView(mContext);
        title2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (108 * mContext.getResources().getDisplayMetrics().density)));
        title2.setGravity(Gravity.CENTER);
        title2.setTextColor(Color.WHITE);
        title2.setTextSize(20);
        title2.setText("THREE");
        parent.title2.addView(title2);

        final NewAdapter adapter = new NewAdapter(0);
        parent.recycler.setAdapter(parent.wrapper(adapter));
        parent.refresh.setRefreshListener(new RefreshView.RefreshTopListener() {
            @Override
            public void topRefresh() {
                parent.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.mCount = 10;
                        adapter.notifyDataSetChanged();
                        parent.refresh.callback(true, (adapter.mCount >= 10 * 3) ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
                    }
                }, (long) (1000L + 3000L * Math.random()));
            }
        }, new RefreshView.RefreshBottomListener() {
            @Override
            public void bottomRefresh() {
                parent.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.mCount += 10;
                        adapter.notifyDataSetChanged();
                        parent.refresh.callback(false, (adapter.mCount >= 10 * 3) ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
                    }
                }, 1000);
            }
        });
        return parent;
    }

    private static class NewAdapter extends RecyclerView.Adapter {
        private int mCount;

        private NewAdapter(int count) {
            mCount = count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (80 * parent.getContext().getResources().getDisplayMetrics().density)));
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);
            return new RecyclerView.ViewHolder(tv) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText("Position" + position);
        }

        @Override
        public int getItemCount() {
            return mCount;
        }
    }
}
