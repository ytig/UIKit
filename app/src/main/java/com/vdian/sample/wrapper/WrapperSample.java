package com.vdian.sample.wrapper;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vdian.sample.BaseSample;
import com.vdian.sample.wrapper.wrapper.DivSwipeWrapper;
import com.vdian.sample.wrapper.wrapper.DividerWrapper;
import com.vdian.wrapper.recycler.AppendWrapper;

/**
 * Created by zhangliang on 16/11/3.
 */
public class WrapperSample extends BaseSample {
    public WrapperSample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        RecyclerView rv = new RecyclerView(mContext);
        rv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rv.setLayoutManager(new LinearLayoutManager(mContext));

        TextView header = new TextView(mContext);
        header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (90 * mContext.getResources().getDisplayMetrics().density)));
        header.setTextSize(16);
        header.setGravity(Gravity.CENTER);
        header.setBackgroundColor(Color.LTGRAY);
        header.setTextColor(Color.WHITE);
        header.setText("HEADER");
        TextView footer = new TextView(mContext);
        footer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (90 * mContext.getResources().getDisplayMetrics().density)));
        footer.setTextSize(16);
        footer.setGravity(Gravity.CENTER);
        footer.setBackgroundColor(Color.LTGRAY);
        footer.setTextColor(Color.WHITE);
        footer.setText("FOOTER");
        rv.setAdapter(new AppendWrapper(new DividerWrapper(new DivSwipeWrapper(new NewAdapter(20)))).addHeaderView(header).addFooterView(footer));
        return rv;
    }

    private static class NewAdapter extends RecyclerView.Adapter {
        private int mCount;

        private NewAdapter(int count) {
            mCount = count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (56 * parent.getContext().getResources().getDisplayMetrics().density)));
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(Color.rgb(238, 238, 238));
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
