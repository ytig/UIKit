package com.vdian.sample.salon.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.vdian.salon.SalonMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by zhangliang on 17/2/21.
 */
public class FrescoSalonView extends SalonMaster {
    public interface UrlRule {
        String toLowUrl(String url);

        String toHighUrl(String url);
    }

    private static UrlRule rule = new UrlRule() {
        @Override
        public String toLowUrl(String url) {
            return null;
        }

        @Override
        public String toHighUrl(String url) {
            return url;
        }
    };

    public static void setUrlRule(UrlRule urlRule) {
        rule = urlRule;
    }

    private static class SleepUtil {
        private static HashMap<String, Long> maps = new HashMap<>();

        public static long sleep(String hash) {
            Long time = maps.get(hash);
            if (time == null) {
                time = (long) (5000L * Math.random()) + AnimationUtils.currentAnimationTimeMillis();
                maps.put(hash, time);
            }
            return time - AnimationUtils.currentAnimationTimeMillis();
        }
    }

    public FrescoSalonView(ViewGroup root) {
        super(root);
        setPopupAdapter(new SalonMaster.PopupAdapter() {
            @Override
            protected int[] onCreate(PopupInfo info) {
                if (((SalonImage) info.image).getRatio() <= 0) return new int[]{1};
                return new int[]{0, 1, 2};
            }

            @Override
            protected String onBind(int type, PopupInfo info) {
                switch (type) {
                    case 0:
                        return "发送给朋友";
                    case 1:
                        return "保存图片";
                    case 2:
                        return "收藏";
                }
                return null;
            }

            @Override
            protected void onClick(int type, PopupInfo info) {
                Toast.makeText(getContext(), info.index + "-" + type, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected View onCreateImage(Context context) {
        return new FrescoImageView(context);
    }

    @Override
    protected float getRatio(String url) {
        List<String> urls = new ArrayList<>();
        String low = rule.toLowUrl(url);
        if (low != null && !low.equals("")) urls.add(low);
        String high = rule.toHighUrl(url);
        if (high != null && !high.equals("")) urls.add(high);
        for (int i = 0; i < urls.size(); i++) {
            DataSource<CloseableReference<CloseableImage>> source = Fresco.getImagePipeline().fetchImageFromBitmapCache(ImageRequest.fromUri(Uri.parse(urls.get(i))), null);
            try {
                CloseableReference<CloseableImage> reference = source.getResult();
                if (reference != null) {
                    try {
                        CloseableImage image = reference.get();
                        return ((float) image.getWidth()) / image.getHeight();
                    } finally {
                        CloseableReference.closeSafely(reference);
                    }
                }
            } finally {
                source.close();
            }
        }
        return 0;
    }

    private static class FrescoImageView extends SimpleDraweeView implements SalonImage {
        private float mRatio = RATIO_LOAD;
        private ControllerListener listener;

        public FrescoImageView(Context context) {
            super(context);
            getHierarchy().setFadeDuration(0);
            getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
            listener = new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    if (imageInfo != null)
                        mRatio = ((float) imageInfo.getWidth()) / imageInfo.getHeight();
                }

                @Override
                public void onFailure(String id, Throwable throwable) {
                    super.onFailure(id, throwable);
                    mRatio = RATIO_FAIL;
                }
            };
        }

        @Override
        public void loadUrl(String url) {
            mRatio = RATIO_LOAD;
            if (url == null || url.equals("")) setImageURI("");
            else {
                String low = rule.toLowUrl(url);
                if (low == null) low = "";
                String high = rule.toHighUrl(url);
                if (high == null || high.equals("")) {
                    high = low;
                    low = "";
                }
                final long delay = SleepUtil.sleep(high);
                setController(Fresco.newDraweeControllerBuilder().setLowResImageRequest(ImageRequest.fromUri(Uri.parse(low))).setRetainImageOnFailure(true).setImageRequest(ImageRequestBuilder.newBuilderWithSource(Uri.parse(high)).setPostprocessor(new BasePostprocessor() {
                    @Override
                    public void process(Bitmap bitmap) {
                        super.process(bitmap);
                        if (delay > 0) {
                            try {
                                Thread.sleep(delay);
                            } catch (Exception e) {
                            }
                        }
                    }
                }).setResizeOptions(new ResizeOptions(800, 800)).build()).setControllerListener(listener).build());
            }
        }

        @Override
        public float getRatio() {
            return mRatio;
        }
    }
}
