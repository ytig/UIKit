package com.vdian.sample.notice;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vdian.sample.BaseSample;
import com.vdian.sample.R;
import com.vdian.sample.notice.view.NoticeCenter;
import com.vdian.sample.notice.view.NoticeParent;

/**
 * Created by zhangliang on 16/11/22.
 */
public class NoticeSample extends BaseSample {
    public NoticeSample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        NoticeParent parent = new NoticeParent(mContext);
        TextView top = new TextView(mContext);
        top.setWidth(mContext.getResources().getDisplayMetrics().widthPixels);
        top.setHeight((int) (128 * mContext.getResources().getDisplayMetrics().density));
        top.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
        top.setTextColor(Color.WHITE);
        top.setTextSize(24);
        top.setGravity(Gravity.CENTER);
        top.setText("Search");
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        parent.top.addView(top);

        final NoticeCenter center = new NoticeCenter(mContext);
        RelativeLayout header = new RelativeLayout(parent.getContext());
        header.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (128 * parent.getContext().getResources().getDisplayMetrics().density)));
        TextView tools = new TextView(mContext);
        tools.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tools.setBackgroundColor(Color.WHITE);
        tools.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        tools.setTextSize(24);
        tools.setGravity(Gravity.CENTER);
        tools.setText("Tools");
        header.addView(tools);
        View divider = new View(parent.getContext());
        int height = (int) (0.8f * parent.getContext().getResources().getDisplayMetrics().density);
        if (height < 1) height = 1;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(Color.LTGRAY);
        header.addView(divider);
        center.init(header, new NewAdapter(12));
        parent.center.addView(center);
        parent.setNoticePatch(center);

        TextView bottom = new TextView(mContext);
        bottom.setWidth(mContext.getResources().getDisplayMetrics().widthPixels);
        bottom.setHeight((int) (88 * mContext.getResources().getDisplayMetrics().density));
        bottom.setTextColor(Color.WHITE);
        bottom.setTextSize(24);
        bottom.setGravity(Gravity.CENTER);
        bottom.setText("Clear");
        parent.bottom.addView(bottom);

        RelativeLayout rel = new RelativeLayout(mContext);
        TextView hint = new TextView(mContext);
        hint.setWidth(mContext.getResources().getDisplayMetrics().widthPixels);
        hint.setHeight((int) (20 * mContext.getResources().getDisplayMetrics().density));
        hint.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setText("Notice");
        rel.addView(hint);
        rel.addView(parent);
        return rel;
    }

    private static class NewAdapter extends RecyclerView.Adapter {
        private int mCount;

        private NewAdapter(int count) {
            mCount = count;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RelativeLayout rel = new RelativeLayout(parent.getContext());
            rel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (64 * parent.getContext().getResources().getDisplayMetrics().density)));
            TextView tv = new TextView(parent.getContext());
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tv.setPadding((int) (80 * parent.getContext().getResources().getDisplayMetrics().density), 0, 0, 0);
            tv.setBackgroundColor(Color.WHITE);
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER_VERTICAL);
            rel.addView(tv);
            View divider = new View(parent.getContext());
            int height = (int) (0.8f * parent.getContext().getResources().getDisplayMetrics().density);
            if (height < 1) height = 1;
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            lp.setMargins((int) (80 * parent.getContext().getResources().getDisplayMetrics().density), 0, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            divider.setLayoutParams(lp);
            divider.setBackgroundColor(Color.LTGRAY);
            rel.addView(divider);
            return new RecyclerView.ViewHolder(rel) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            String text = "";
            switch (position % 12) {
                case 0:
                    text = "January - Jan.";
                    break;
                case 1:
                    text = "February - Feb.";
                    break;
                case 2:
                    text = "March - Mar.";
                    break;
                case 3:
                    text = "April - Apr.";
                    break;
                case 4:
                    text = "May - May.";
                    break;
                case 5:
                    text = "June - Jun.";
                    break;
                case 6:
                    text = "July - Jul.";
                    break;
                case 7:
                    text = "August - Aug.";
                    break;
                case 8:
                    text = "September - Sep. / Sept.";
                    break;
                case 9:
                    text = "October - Oct.";
                    break;
                case 10:
                    text = "November - Nov.";
                    break;
                case 11:
                    text = "December - Dec.";
                    break;
            }
            ((TextView) ((ViewGroup) holder.itemView).getChildAt(0)).setText(text);
        }

        @Override
        public int getItemCount() {
            return mCount;
        }
    }
}
