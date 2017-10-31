package com.vdian.sample.wrapper.wrapper;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vdian.uikit.wrapper.WrapperFinder;
import com.vdian.uikit.wrapper.recycler.SwipeWrapper;

/**
 * Created by zhangliang on 16/11/3.
 */
public class DivSwipeWrapper extends SwipeWrapper {
    public DivSwipeWrapper(RecyclerView.Adapter adapter) {
        super(adapter);
    }

    @Override
    protected ViewGroup onReplaceViewGroup(ViewGroup parent, int viewType) {
        DivSwipeView swipe = new DivSwipeView(parent.getContext());
        swipe.addMenu(onCreateMenu(swipe, viewType));
        return swipe;
    }

    @Override
    protected View onCreateMenu(ViewGroup parent, int viewType) {
        TextView menu = new TextView(parent.getContext());
        menu.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int padding = (int) (33 * parent.getContext().getResources().getDisplayMetrics().density);
        menu.setPadding(padding, 0, padding, 0);
        menu.setBackgroundColor(Color.RED);
        menu.setGravity(Gravity.CENTER);
        menu.setTextColor(Color.WHITE);
        menu.setTextSize(16);
        menu.setText("Delete");
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "" + new WrapperFinder(-1).getPosition(v), Toast.LENGTH_SHORT).show();
            }
        });
        return menu;
    }

    private static class DivSwipeView extends SwipeView {
        private DivSwipeView(Context context) {
            super(context);
        }

        @Override
        public void display(View self, float value) {
            int offset = (int) toCoordinate(value);
            mLeft.scrollTo(offset, 0);
            mRight.scrollTo((offset - mRight.getWidth()) / 2, 0);
        }
    }
}
