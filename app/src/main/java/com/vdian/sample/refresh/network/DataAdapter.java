package com.vdian.sample.refresh.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;

import com.vdian.uikit.view.extend.refresh.RefreshView;
import com.vdian.uikit.view.NotifyManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/11/14.
 */
public abstract class DataAdapter extends RecyclerView.Adapter implements RefreshView.RefreshTopListener, RefreshView.RefreshBottomListener {
    private RefreshView mRefresh;
    private NotifyManager<Data> mManager;
    protected List<Data> mDatas;

    public DataAdapter(RefreshView refresh) {
        mRefresh = refresh;
        mDatas = new ArrayList<>();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mManager = new NotifyManager<Data>(recyclerView, this) {
            @Override
            protected boolean sameItem(Data d1, Data d2) {
                return d1.equals(d2);
            }

            @Override
            protected boolean sameData(Data d1, Data d2) {
                return true;
            }
        };
    }

    @Override
    public void topRefresh() {
        Database.instanse.request(null, new Database.Callback() {
            @Override
            public void response(final List<Data> datas) {
                boolean end = datas.size() <= 0 ? true : (datas.get(datas.size() - 1).getId() <= 0);
                mManager.onPreChanged(mDatas);
                mDatas.clear();
                mDatas.addAll(datas);
                mManager.onChanged(mDatas);
                mRefresh.callback(true, end ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
            }

            @Override
            public void error() {
                mManager.onPreChanged(mDatas);
                mDatas.clear();
                mManager.onChanged(mDatas);
                mRefresh.callback(true, RefreshView.CALLBACK_ERROR);
            }
        }, wifi());
    }

    @Override
    public void bottomRefresh() {
        Database.instanse.request(mDatas.size() <= 0 ? null : mDatas.get(mDatas.size() - 1).getId(), new Database.Callback() {
            @Override
            public void response(final List<Data> datas) {
                boolean end = datas.size() <= 0 ? true : (datas.get(datas.size() - 1).getId() <= 0);
                mManager.onPreChanged(mDatas);
                mDatas.addAll(datas);
                mManager.onChanged(mDatas);
                mRefresh.callback(false, end ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
            }

            @Override
            public void error() {
                mRefresh.callback(false, RefreshView.CALLBACK_ERROR);
            }
        }, wifi());
    }

    private boolean wifi() {
        NetworkInfo activeNetInfo = ((ConnectivityManager) mRefresh.getContext().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI)
            return true;
        return false;
    }
}
