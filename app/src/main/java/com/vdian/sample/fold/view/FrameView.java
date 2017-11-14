package com.vdian.sample.fold.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.vdian.refresh.RefreshHintView;
import com.vdian.uikit.view.TransitionController;

/**
 * Created by zhangliang on 17/7/24.
 */
public class FrameView extends RefreshHintView {
    private Frame view;
    private FrameController animation;

    public FrameView(Context context) {
        super(context);
    }

    public FrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int type() {
        return VIEW_TYPE_TOP;
    }

    @Override
    protected long stay() {
        return 0;
    }

    @Override
    protected View build() {
        setWillNotDraw(false);
        view = new Frame(getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (90 * getResources().getDisplayMetrics().density)));
        animation = new FrameController(view, this);
        return view;
    }

    @Override
    protected void status(int from, int to) {
        animation.status(to == STATUS_REFRESH);
    }

    @Override
    protected void layout(int height) {
    }

    @Override
    protected void scroll(float offset) {
        setTranslationY(-(int) offset);
        float skip = 80 * getContext().getResources().getDisplayMetrics().density;
        animation.scroll(Math.min(Math.max((offset - skip) / (getHeight() - skip), 0), 1));
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        animation.computeScroll();
    }

    @Override
    public void draw(Canvas canvas) {
        int bottom = (int) mOffset;
        if (bottom < 0) bottom = 0;
        canvas.clipRect(0, 0, getWidth(), bottom);
        super.draw(canvas);
    }

    private static class Frame extends View implements FrameController.FrameListener {
        private float mValue;
        private ShaRinGan mPainter;

        public Frame(Context context) {
            super(context);
            mValue = -1;
            mPainter = new ShaRinGan(getContext());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int color = Color.parseColor("#fa7298");
            int x = getWidth() / 2;
            int y = (int) (34 * getContext().getResources().getDisplayMetrics().density);
            int r = (int) (8 * getContext().getResources().getDisplayMetrics().density);
            canvas.translate(x, y);
            if (mValue < 0) {
                if (mValue + 1 < 0.382f) {
                    float value = (mValue + 1) / 0.382f;
                    if (value < 0) value = 0;
                    else if (value > 1) value = 1;
                    float alpha = Math.min(value / 0.2f, 1);
                    float scale = 1.414f * value;
                    float rotate = 1 - value;
                    mPainter.draw(canvas, color, -1, 0, 0);
                    mPainter.draw(canvas, Color.argb((int) (255 * alpha), Color.red(color), Color.green(color), Color.blue(color)), r * scale, 3, 180 * rotate);
                } else {
                    float value = 1 + mValue / (1 - 0.382f);
                    if (value < 0) value = 0;
                    else if (value > 1) value = 1;
                    float scale = (float) (1 + 0.414f * Math.pow(1 - value, 0.828f) * Math.sin(1.5f * Math.PI * value + 0.5f * Math.PI));
                    mPainter.draw(canvas, color, -1, 0, 0);
                    mPainter.draw(canvas, color, r * scale, 3, 0);
                    mPainter.draw(canvas, color, 2 * r / scale, 0, 0);
                }
            } else {
                float rotate = mValue;
                mPainter.draw(canvas, color, -1, 0, 0);
                mPainter.draw(canvas, color, r, 3, (-120) * rotate);
                mPainter.draw(canvas, color, 2 * r, 0, 0);
            }
        }

        @Override
        public long duration(boolean warning) {
            return warning ? 555L : 333L;
        }

        @Override
        public void line(float value) {
            mValue = value - 1;
            invalidate();
        }

        @Override
        public void circle(float value) {
            mValue = value;
            invalidate();
        }

        private static class ShaRinGan {
            private float unit;
            private Path magatama;
            private Paint paint;

            public ShaRinGan(Context context) {
                unit = 2f * context.getResources().getDisplayMetrics().density;
                magatama = new Path();
                magatama.moveTo(-1f * unit, 0);
                magatama.quadTo(-1f * unit, -2f * unit, 0.5f * unit, -2.618f * unit);
                magatama.quadTo(-0.5f * unit, -1.25f * unit, 0.5f * unit, -0.8f * unit);
                magatama.close();
                magatama.addCircle(0, 0, unit, Path.Direction.CW);
                paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.rgb(0, 0, 0));
                paint.setStrokeWidth(0.75f * unit);
            }

            public void draw(Canvas canvas, int color, float radius, int division, float angle) {
                if (radius < 0) {
                    paint.setColor(color);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(0, 0, -radius * unit, paint);
                } else {
                    paint.setColor(Color.rgb(Color.red(color), Color.green(color), Color.blue(color)));
                    canvas.saveLayerAlpha(-(radius + 3 * unit), -(radius + 3 * unit), radius + 3 * unit, radius + 3 * unit, Color.alpha(color), Canvas.MATRIX_SAVE_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    canvas.drawCircle(0, 0, radius, paint);
                    paint.setStyle(Paint.Style.FILL);
                    for (int i = 0; i < division; i++) {
                        canvas.save(Canvas.MATRIX_SAVE_FLAG);
                        canvas.rotate(angle + i * 360 / division);
                        canvas.translate(0, -radius);
                        canvas.drawPath(magatama, paint);
                        canvas.restore();
                    }
                    canvas.restore();
                }
            }
        }
    }

    private static class FrameController {
        private boolean loop;
        private boolean refresh;
        private float offset;
        private FrameListener listener;
        private TransitionController line;
        private TransitionController circle;

        public FrameController(FrameListener l, View v) {
            loop = false;
            refresh = false;
            offset = 0;
            listener = l;
            line = new TransitionController(new TransitionController.TransitionListener() {
                @Override
                public float speed(float value, float target) {
                    return 1f / Math.abs(listener.duration(true));
                }

                @Override
                public void display(View self, float value) {
                    if (value != 1) listener.line(value);
                    else {
                        if (!refresh) listener.line(value);
                        else {
                            loop = true;
                            circle.setValue(0);
                            circle.setTarget(1);
                        }
                    }
                }
            }, v);
            circle = new TransitionController(new TransitionController.TransitionListener() {
                @Override
                public float speed(float value, float target) {
                    return 1f / Math.abs(listener.duration(false));
                }

                @Override
                public void display(View self, float value) {
                    if (value != 1) listener.circle(value);
                    else {
                        if (refresh) {
                            circle.setValue(0);
                            circle.setTarget(1);
                        } else {
                            loop = false;
                            line.setValue(1);
                            line.setTarget(offset);
                        }
                    }
                }
            }, v);
        }

        public void status(boolean r) {
            if (refresh == r) return;
            refresh = r;
            if (reset()) return;
            if (refresh) {
                if (!loop) {
                    if (line.getValue() != 1) line.setTarget(1);
                    else line.setValue(1);
                }
            } else {
                if (!loop) line.setTarget(offset);
            }
        }

        public void scroll(float o) {
            if (offset == o) return;
            offset = o;
            if (reset()) return;
            if (!refresh && !loop) {
                if (line.getValue() == line.getTarget() && listener.duration(true) < 0)
                    line.setValue(offset);
                else line.setTarget(offset);
            }
        }

        private boolean reset() {
            if (!refresh && offset == 0 && listener.duration(false) < 0) {
                loop = false;
                line.setValue(0);
                return true;
            }
            return false;
        }

        public void computeScroll() {
            if (!loop) line.computeScroll();
            else circle.computeScroll();
        }

        public interface FrameListener {
            long duration(boolean warning);

            void line(float value);

            void circle(float value);
        }
    }
}
