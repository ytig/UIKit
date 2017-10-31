package com.vdian.sample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.vdian.sample.alipay.AlipaySample;
import com.vdian.sample.fold.FoldSample;
import com.vdian.sample.notice.NoticeSample;
import com.vdian.sample.refresh.RefreshSample;
import com.vdian.sample.salon.SalonSample;
import com.vdian.sample.swap.SwapSample;
import com.vdian.sample.table.TableSample;
import com.vdian.sample.wrapper.WrapperSample;

public class MainActivity extends AppCompatActivity {
    private static Sample[] SAMPLES = new Sample[]{
            new Sample(RefreshSample.class),
            new Sample(AlipaySample.class),
            new Sample(FoldSample.class),
            new Sample(WrapperSample.class),
            new Sample(SwapSample.class),
            new Sample(NoticeSample.class),
            new Sample(TableSample.class),
            new Sample(SalonSample.class)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            SharedPreferences sp = getSharedPreferences("sample", MODE_PRIVATE);
            int index = getIntent().getIntExtra("index", -1);
            if (index == -1) index = sp.getInt("index", 0);
            else sp.edit().putInt("index", index).commit();
            setContentView(SAMPLES[(index < 0 || index >= SAMPLES.length) ? 0 : index].getView(this));
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (int i = 0; i < SAMPLES.length; i++) menu.add(SAMPLES[i].getMenu());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String menu = (String) item.getTitle();
        for (int i = 0; i < SAMPLES.length; i++) {
            if (SAMPLES[i].getMenu().equals(menu)) {
                startActivity(new Intent(this, getClass()).putExtra("index", Integer.valueOf(i)));
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected static class Sample {
        private Class<?> mClass;
        private String mMenu;

        protected Sample(Class<?> c) {
            this(c, getMenu(c));
        }

        protected Sample(Class<?> c, String m) {
            mClass = c;
            mMenu = m;
        }

        protected View getView(Context context) throws Exception {
            return ((BaseSample) mClass.getConstructor(Context.class).newInstance(context)).init();
        }

        protected String getMenu() {
            return mMenu;
        }

        private static String getMenu(Class<?> c) {
            String tmp = c.getName();
            for (int i = tmp.length() - 1; i >= 0; i--) {
                if (tmp.charAt(i) == '.') {
                    tmp = tmp.substring(i + 1, tmp.length());
                    String end = "Sample";
                    while (tmp.endsWith(end)) tmp = tmp.substring(0, tmp.length() - end.length());
                    return tmp;
                }
            }
            return tmp;
        }
    }
}
