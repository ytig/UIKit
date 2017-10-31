package com.vdian.uikit.util.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhangliang on 16/8/1.
 */
public abstract class Loop<T> extends Thread implements Pipeline<T> {
    private boolean mutex = true;
    protected Queue<T> tasks = new LinkedList<>();
    protected Executor executor;
    private boolean MUTEX = true;

    public Loop() {
        start();
    }

    public synchronized boolean apply() {
        if (!mutex) return false;
        mutex = false;
        return true;
    }

    public synchronized boolean release() {
        if (mutex) return false;
        mutex = true;
        return true;
    }

    public synchronized boolean APPLY() {
        if (!MUTEX) return false;
        MUTEX = false;
        return true;
    }

    public synchronized boolean RELEASE() {
        if (MUTEX) return false;
        MUTEX = true;
        return true;
    }

    @Override
    public void run() {
        Looper.prepare();
        while (true) {
            if (apply()) {
                executor = new Executor();
                if (tasks.size() > 0) executor.sendEmptyMessage(0);
                release();
                break;
            }
        }
        Looper.loop();
    }

    @Override
    public void publish(T task) {
        if (task == null) return;
        while (true) {
            if (apply()) {
                tasks.add(task);
                if (executor != null) executor.sendEmptyMessage(0);
                release();
                break;
            }
        }
    }

    protected class Executor extends Handler {
        @Override
        public void handleMessage(Message msg) {
            while (true) {
                if (apply()) {
                    T task = null;
                    if (tasks.size() > 0) task = tasks.remove();
                    release();
                    if (task != null) {
                        while (true) {
                            if (APPLY()) {
                                execute(task);
                                RELEASE();
                                break;
                            }
                        }
                    } else break;
                }
            }
        }
    }
}
