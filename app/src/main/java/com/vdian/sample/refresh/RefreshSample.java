package com.vdian.sample.refresh;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vdian.sample.BaseSample;
import com.vdian.sample.refresh.network.DataAdapter;
import com.vdian.sample.refresh.view.RefreshAutoView;
import com.vdian.sample.refresh.view.RefreshBottomView;
import com.vdian.sample.refresh.view.RefreshTopView;
import com.vdian.uikit.view.extend.refresh.RefreshView;
import com.vdian.uikit.wrapper.recycler.AppendWrapper;

/**
 * Created by zhangliang on 16/10/19.
 */
public class RefreshSample extends BaseSample {
    public RefreshSample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        if (Math.random() < 0.5f) return normal();
        else return auto();
    }

    private View normal() {
        RefreshView refresh = new RefreshView(mContext);

        RecyclerView nested = new RecyclerView(mContext);
        nested.setItemAnimator(null);
        nested.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        nested.setLayoutManager(new LinearLayoutManager(mContext));
        refresh.addView(nested);

        RefreshTopView top = new RefreshTopView(mContext);
        top.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        refresh.addView(top);

        RefreshBottomView bottom = new RefreshBottomView(mContext);
        bottom.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        refresh.addView(bottom);

        NewAdapter adapter = new NewAdapter(refresh);
        nested.setAdapter(adapter);
        refresh.setRefreshListener(adapter, adapter);
        return refresh;
    }

    private View auto() {
        RefreshView refresh = new RefreshView(mContext);
        refresh.auto(0);
        refresh.permit(true, false);

        RecyclerView nested = new RecyclerView(mContext);
        nested.setItemAnimator(null);
        nested.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        nested.setLayoutManager(new LinearLayoutManager(mContext));
        refresh.addView(nested);

        RefreshTopView top = new RefreshTopView(mContext);
        top.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        refresh.addView(top);

        RefreshAutoView auto = new RefreshAutoView(mContext) {
            @Override
            protected View build() {
                super.build();
                getView().setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((RefreshView) getParent()).refresh(false);
//                        ((RefreshView) getParent()).refresh(mStatus == STATUS_BAN);
                    }
                });
                return null;
            }
        };
        auto.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        refresh.addView(auto);

        NewAdapter adapter = new NewAdapter(refresh);
        TextView footer = new TextView(mContext);
        footer.setWidth(1);
        footer.setHeight(1);
        nested.setAdapter(new AppendWrapper(adapter).addFooterView(auto.getView()).addFooterView(footer));
        refresh.setRefreshListener(adapter, adapter);
        return refresh;
    }

    private static class NewAdapter extends DataAdapter {
        private NewAdapter(RefreshView refresh) {
            super(refresh);
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
            ((TextView) holder.itemView).setText("ID" + mDatas.get(position).getId());
        }

        @Override
        public int getItemCount() {
            return mDatas.size();
        }
    }
}
