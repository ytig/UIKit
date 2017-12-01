package com.vdian.sample.jelly;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.vdian.sample.BaseSample;
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
            setJellyColor(Color.rgb(253, 136, 36));
            setGravity(Gravity.CENTER);
            final View view = new View(getContext()) {
                private Painter painter;

                @Override
                protected void onDraw(Canvas canvas) {
                    super.onDraw(canvas);
                    if (painter == null) painter = new Painter();
                    painter.draw(this, canvas);
                }

                class Painter {
                    private Path path;
                    private Matrix matrix;
                    private Paint paint;

                    public Painter() {
                        path = new Path();
                        matrix = new Matrix();
                        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        paint.setColor(Color.rgb(251, 13, 27));
                    }

                    public void draw(View view, Canvas canvas) {
                        int width = view.getWidth(), height = view.getHeight(), unit = Math.min(width, height) / 2;
                        unit *= (1 - 0.618f);
                        path.reset();
                        path.moveTo(0, -unit);
                        matrix.reset();
                        matrix.setRotate(144);
                        for (int i = 5 - 1; i >= 0; i--) {
                            path.transform(matrix);
                            if (i != 0) path.lineTo(0, -unit);
                            else path.close();
                        }
                        matrix.reset();
                        matrix.setTranslate(width / 2, height / 2);
                        path.transform(matrix);
                        canvas.drawPath(path, paint);
                    }
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
