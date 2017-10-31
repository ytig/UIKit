package com.vdian.uikit.util.task;

/**
 * Created by zhangliang on 16/8/1.
 */
public interface Pipeline<T> {
    void publish(T task);

    void execute(T task);
}