package com.vdian.sample.table;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vdian.sample.BaseSample;
import com.vdian.sample.table.view.TableView;
import com.vdian.wrapper.recycler.AppendWrapper;

/**
 * Created by zhangliang on 16/12/27.
 */
public class TableSample extends BaseSample {
    public TableSample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        TableView table = new TableView(mContext);
        TextView header = new TextView(mContext);
        header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (200 * mContext.getResources().getDisplayMetrics().density)));
        header.setTextSize(16);
        header.setGravity(Gravity.CENTER);
        header.setBackgroundColor(Color.DKGRAY);
        header.setTextColor(Color.WHITE);
        header.setText("HEADER");
        TextView footer = new TextView(mContext);
        footer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (200 * mContext.getResources().getDisplayMetrics().density)));
        footer.setTextSize(16);
        footer.setGravity(Gravity.CENTER);
        footer.setBackgroundColor(Color.DKGRAY);
        footer.setTextColor(Color.WHITE);
        footer.setText("FOOTER");
        table.setAdapter(new AppendWrapper((new NewAdapter(20))).addHeaderView(header).addFooterView(footer));
        return table;
    }

    private static class NewAdapter extends RecyclerView.Adapter implements TableView.TableAdapter {
        private int mCount;

        private NewAdapter(int count) {
            mCount = count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case 0:
                    TextView section = new TextView(parent.getContext());
                    section.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (24 * parent.getContext().getResources().getDisplayMetrics().density)));
                    section.setTextSize(14);
                    section.setGravity(Gravity.CENTER);
                    section.setBackgroundColor(Color.LTGRAY);
                    return new RecyclerView.ViewHolder(section) {
                    };
                case 1:
                    TextView cell = new TextView(parent.getContext());
                    cell.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (80 * parent.getContext().getResources().getDisplayMetrics().density)));
                    cell.setTextSize(16);
                    cell.setGravity(Gravity.CENTER);
                    return new RecyclerView.ViewHolder(cell) {
                    };
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (getItemViewType(position)) {
                case 0:
                    ((TextView) holder.itemView).setText("Section");
                    break;
                case 1:
                    ((TextView) holder.itemView).setText("Cell");
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mCount;
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 4 == 0) return 0;
            else return 1;
        }

        @Override
        public boolean isSection(int position) {
            return getItemViewType(position) == 0;
        }
    }
}
