package com.vdian.uikit.util.task;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by zhangliang on 16/8/1.
 */
@Deprecated
public abstract class Pool<T> implements Pipeline<T> {
    private boolean mutex = true;
    protected Queue<T> tasks = new LinkedList<>();
    protected int running = 0;
    protected Executor executor = new Executor();
    private boolean MUTEX = true;

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
    public void publish(T task) {
        if (task == null) return;
        while (true) {
            if (apply()) {
                tasks.add(task);
                if (running < 1) {
                    running++;
                    new Thread(executor).start();
                }
                release();
                break;
            }
        }
    }

    protected class Executor implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (apply()) {
                    T task = null;
                    if (tasks.size() > 0) task = tasks.remove();
                    else running--;
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
