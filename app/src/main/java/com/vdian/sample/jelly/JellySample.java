package com.vdian.sample.jelly;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vdian.sample.BaseSample;
import com.vdian.sample.R;
import com.vdian.sample.jelly.view.JellyView;

/**
 * Created by zhangliang on 17/12/1.
 */
public class JellySample extends BaseSample {
    public JellySample(Context context) {
        super(context);
    }

    @Override
    public View init() {
        RelativeLayout content = new RelativeLayout(mContext);
        content.setClipChildren(false);
        content.setGravity(Gravity.CENTER);
        content.addView(new MyJellyView(mContext), new ViewGroup.LayoutParams((int) (44 * mContext.getResources().getDisplayMetrics().density), (int) (44 * mContext.getResources().getDisplayMetrics().density)));
        return content;
    }

    private static class MyJellyView extends JellyView {
        public MyJellyView(Context context) {
            super(context);
            setJellyColor(getContext().getResources().getColor(R.color.colorPrimary));
            setGravity(Gravity.CENTER);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            final View view = new View(getContext()) {
                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);
                    int width = getWidth(), height = getHeight();
                    canvas.drawCircle(width / 2, height / 2, Math.min(width, height) * 0.2f / 2, paint);
                }
            };
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "click", Toast.LENGTH_SHORT).show();
                }
            });
            addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            setJellyListener(new JellyListener() {
                @Override
                public void jelly(double angle, double scalar) {
                    scalar *= 0.618f;
                    view.setTranslationX((float) (scalar * Math.cos(angle)));
                    view.setTranslationY((float) (scalar * Math.sin(angle)));
                    view.invalidate();
                }
            });
        }
    }
}
