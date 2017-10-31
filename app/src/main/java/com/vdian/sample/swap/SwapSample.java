package com.vdian.sample.swap;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vdian.sample.BaseSample;
import com.vdian.sample.swap.view.SwapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/12/21.
 */
public class SwapSample extends BaseSample {
    public SwapSample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        SwapView swap = new SwapView(mContext);
        NewAdapter adapter = new NewAdapter(20);
        swap.setAdapter(adapter);
        swap.setSwapListener(adapter);
        return swap;
    }

    private static class NewAdapter extends RecyclerView.Adapter implements SwapView.SwapListener {
        private List<Integer> datas;

        private NewAdapter(int count) {
            datas = new ArrayList<>();
            for (int i = 0; i < count; i++) datas.add(i);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RelativeLayout rel = new RelativeLayout(parent.getContext());
            rel.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((SwapView) v.getParent().getParent()).startSwap(v);
                    return true;
                }
            });
            rel.setPadding(0, 0, 0, (int) (15 * parent.getResources().getDisplayMetrics().density));

            TextView tv = new TextView(parent.getContext());
            tv.setWidth(parent.getResources().getDisplayMetrics().widthPixels);
            tv.setTextSize(14);
            tv.setGravity(Gravity.CENTER);
            tv.setBackgroundColor(Color.WHITE);
            rel.addView(tv);
            return new RecyclerView.ViewHolder(rel) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView tv = (TextView) ((ViewGroup) holder.itemView).getChildAt(0);
            int height = (datas.get(position) % 3 + 1) * 40;
            tv.setHeight((int) (height * holder.itemView.getContext().getResources().getDisplayMetrics().density));
            tv.setText("Item" + datas.get(position));
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public void swap(int from, int to) {
            Integer data = datas.remove(from);
            datas.add(to, data);
            notifyDataSetChanged();
        }
    }
}
