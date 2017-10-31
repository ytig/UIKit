package com.vdian.uikit.util.task;

import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;

/**
 * Created by zhangliang on 16/8/1.
 */
public abstract class Task {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static HashMap<String, Pipeline> pipelines = new HashMap<>();

    public static void runOnUiThread(Runnable action) {
        handler.post(action);
    }

    public void publish() {
        String key = getClass().getName();
        Pipeline pipeline = pipelines.get(key);
        if (pipeline == null) {
            pipeline = new MyPipeline();
            pipelines.put(key, pipeline);
        }
        pipeline.publish(this);
    }

    protected abstract void execute();

    private static class MyPipeline extends Loop<Task> {
        @Override
        public void execute(Task task) {
            task.execute();
        }
    }
}
