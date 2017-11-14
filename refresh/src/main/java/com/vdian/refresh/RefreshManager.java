package com.vdian.refresh;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.vdian.uikit.util.task.Task;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhangliang on 16/10/11.
 */
public abstract class RefreshManager implements RefreshView.RefreshTopListener, RefreshView.RefreshBottomListener {
    private RefreshView mView; //加载控件
    private List mDatas; //显示数据
    private String mCacheName; //缓存名称
    private NotifyPatch mPatch; //数据更新补丁
    private boolean isLoadMode = true; //是否为网络状态
    private int loadTag = 0; //会话标记
    private int pageNumber = 1; //加载页号
    private boolean isLoading = true; //是否在等待数据到达
    private boolean isEnd = true; //当前加载已到达底部
    private int cachePageNumber = 1; //缓存加载页号
    private boolean cacheIsLoading = true; //是否在等待缓存到达
    private boolean cacheIsEnd = true; //当前缓存已到达底部
    private boolean hasPageSuccess = true; //重置后有无数据到达
    private List<RefreshListener> mListeners = new ArrayList<>(); //数据加载监听

    public RefreshManager(RefreshView view, List datas, String cacheName) {
        mView = view;
        mDatas = datas;
        mCacheName = cacheName;
        mPatch = new NotifyPatch() {
            @Override
            public void onPreChanged(List before) {
            }

            @Override
            public void onChanged(List after) {
                RefreshCompat.AdapterNotify.notifyData(mView.getNestedChild()); //默认更新方法
            }
        };
        view.setRefreshListener(this, this);
    }

    /**
     * 续载会话
     */
    public void resume() {
        if (isLoadMode) {
            if (isEnd) {
                isLoading = false;
                isEnd = false;
                mView.resume();
                mView.postInvalidate(); //触发加载更多
            }
        }
    }

    /**
     * 存在缓存区
     *
     * @return
     */
    public boolean hasCache() {
        return mCacheName != null;
    }

    /**
     * 变更缓存区
     *
     * @param name
     */
    public void setCache(String name) {
        mCacheName = name;
    }

    /**
     * 清除缓存
     *
     * @param name
     */
    public void clearCache(String name) {
        clearCache(mView.getContext().getApplicationContext(), name);
    }

    /**
     * 清除缓存
     *
     * @param context
     * @param name
     */
    public static void clearCache(Context context, String name) {
        new Cache(context, name).publish();
    }

    @Override
    public void topRefresh() {
        isLoadMode = true;
        loadTag++;
        pageNumber = 1;
        isLoading = true;
        isEnd = false;
        cachePageNumber = 1;
        cacheIsLoading = true;
        cacheIsEnd = false;
        hasPageSuccess = false;
        cacheRequest(loadTag, cachePageNumber);
        for (RefreshListener listener : mListeners) listener.onHttpRequest(pageNumber);
        httpRequest(loadTag, pageNumber);
    }

    @Override
    public void bottomRefresh() {
        if (isLoadMode) {
            if (!isLoading && !isEnd) {
                isLoading = true;
                for (RefreshListener listener : mListeners) listener.onHttpRequest(pageNumber);
                httpRequest(loadTag, pageNumber);
            }
        } else {
            if (!cacheIsLoading && !cacheIsEnd) {
                cacheIsLoading = true;
                cacheRequest(loadTag, cachePageNumber);
            }
        }
    }

    protected abstract List parseArray(String json); //转换数据

    protected abstract void httpRequest(int tag, int page); //网络请求

    /**
     * 网络请求成功回调
     *
     * @param tag
     * @param json
     * @param end
     * @return
     */
    public boolean httpResponse(int tag, String json, boolean end) {
        if (tag != loadTag || !isLoadMode) return false;
        hasPageSuccess = true;
        List list = (json == null) ? null : parseArray(json);
        if (list == null || list.size() == 0) {
            isEnd = true;
            if (pageNumber == 1) {
                mPatch.onPreChanged(mDatas);
                mDatas.clear(); //清除数据
                mPatch.onChanged(mDatas);
                if (mCacheName != null) clearCache(mCacheName); //清空缓存
            }
        } else {
            if (end) isEnd = true;
            mPatch.onPreChanged(mDatas);
            if (pageNumber == 1) mDatas.clear(); //清除数据
            mDatas.addAll(list);
            mPatch.onChanged(mDatas);
            if (mCacheName != null)
                new Cache(mView.getContext().getApplicationContext(), mCacheName, pageNumber, json).publish(); //缓存
        }
        mView.callback(pageNumber == 1, isEnd ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
        pageNumber++;
        isLoading = false;
        for (RefreshListener listener : mListeners)
            listener.onHttpResponse(pageNumber - 1, (list == null || list.size() == 0), isEnd);
        return true;
    }

    /**
     * 网络请求失败回调
     *
     * @param tag
     * @return
     */
    public boolean httpError(int tag) {
        if (tag != loadTag || !isLoadMode) return false;
        if (pageNumber == 1) {
            isLoadMode = false; //进入查看本地缓存模式
            if (mCacheName != null) {
                if (!cacheIsLoading) {
                    mView.callback(true, cacheIsEnd ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL); //停止顶部加载
                    mView.postInvalidate(); //手动触发加载缓存
                }
            } else {
                mPatch.onPreChanged(mDatas);
                mDatas.clear(); //清除数据
                mPatch.onChanged(mDatas);
                mView.callback(true, RefreshView.CALLBACK_ERROR);
            }
        } else mView.callback(false, RefreshView.CALLBACK_ERROR);
        isLoading = false;
        for (RefreshListener listener : mListeners) listener.onHttpError(pageNumber);
        return true;
    }

    private void cacheRequest(int tag, int page) {
        if (mCacheName == null) return;
        for (RefreshListener listener : mListeners) listener.onCacheRequest(cachePageNumber);
        new Cache(this, mCacheName, tag, page).publish();
    }

    private void cacheCallback(int tag, String json, boolean end) {
        if (tag != loadTag) return;
        if (cachePageNumber == 1) {
            if (hasPageSuccess) return;
        } else {
            if (isLoadMode) return;
        }
        List list = (json == null) ? null : parseArray(json);
        if (list == null || list.size() == 0) {
            cacheIsEnd = true;
            if (cachePageNumber == 1) {
                mPatch.onPreChanged(mDatas);
                mDatas.clear(); //清除数据
                mPatch.onChanged(mDatas);
            }
        } else {
            if (end) cacheIsEnd = true;
            mPatch.onPreChanged(mDatas);
            if (cachePageNumber == 1) mDatas.clear(); //清除数据
            mDatas.addAll(list);
            mPatch.onChanged(mDatas);
        }
        if (!isLoadMode)
            mView.callback(cachePageNumber == 1, cacheIsEnd ? RefreshView.CALLBACK_END : RefreshView.CALLBACK_NORMAL);
        cachePageNumber++;
        cacheIsLoading = false;
        for (RefreshListener listener : mListeners)
            listener.onCacheCallback(cachePageNumber - 1, (list == null || list.size() == 0), cacheIsEnd);
    }

    private static class Cache extends Task {
        private Read read;
        private Write write;
        private Clear clear;

        private Cache(RefreshManager manager, String name, int tag, int page) {
            read = new Read();
            read.reference = new WeakReference<>(manager);
            read.name = name;
            read.tag = tag;
            read.page = page;
        }

        private Cache(Context context, String name, int page, String json) {
            write = new Write();
            write.reference = new WeakReference<>(context);
            write.name = name;
            write.page = page;
            write.json = json;
        }

        private Cache(Context context, String name) {
            clear = new Clear();
            clear.reference = new WeakReference<>(context);
            clear.name = name;
        }

        @Override
        protected void execute() {
            if (read != null) read.excute();
            if (write != null) write.excute();
            if (clear != null) clear.excute();
        }

        private static class Read {
            private WeakReference<RefreshManager> reference;
            private String name;
            private int tag;
            private int page;
            private String json;
            private boolean end;

            private void excute() {
                RefreshManager manager = reference.get();
                if (manager != null) {
                    SharedPreferences sp = manager.mView.getContext().getSharedPreferences(name, Activity.MODE_PRIVATE);
                    int length = sp.getInt("cachenumber", 0);
                    json = page > length ? null : sp.getString("cachedata " + page, null);
                    end = (page >= length);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RefreshManager manager = reference.get();
                            if (manager != null) manager.cacheCallback(tag, json, end); //在主线程中回调
                        }
                    });
                }
            }
        }

        private static class Write {
            private WeakReference<Context> reference;
            private String name;
            private int page;
            private String json;

            private void excute() {
                Context context = reference.get();
                if (context != null) {
                    SharedPreferences sp = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
                    if (page == 1)
                        sp.edit().clear().putString("cachedata " + page, json).putInt("cachenumber", page).commit();
                    else {
                        if (page == sp.getInt("cachenumber", 0) + 1)
                            sp.edit().putString("cachedata " + page, json).putInt("cachenumber", page).commit();
                    }
                }
            }
        }

        private static class Clear {
            private WeakReference<Context> reference;
            private String name;

            private void excute() {
                Context context = reference.get();
                if (context != null) {
                    SharedPreferences sp = context.getSharedPreferences(name, Activity.MODE_PRIVATE);
                    sp.edit().clear().commit();
                }
            }
        }
    }

    /**
     * 设置通知补丁
     *
     * @param patch
     */
    public void setPatch(NotifyPatch patch) {
        mPatch = patch;
    }

    public interface NotifyPatch {
        void onPreChanged(List before);

        void onChanged(List after);
    }

    /**
     * 添加加载监听
     *
     * @param listener
     */
    public void addListener(RefreshListener listener) {
        mListeners.add(listener);
    }

    public interface RefreshListener {
        void onHttpRequest(int page);

        void onHttpResponse(int page, boolean empty, boolean end);

        void onHttpError(int page);

        void onCacheRequest(int page);

        void onCacheCallback(int page, boolean empty, boolean end);
    }
}
