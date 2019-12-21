package leaf.prod.app.adapter.infomation;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Typeface;
import androidx.constraintlayout.ConstraintLayout;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.vondear.rxtool.view.RxToast;

import at.blogc.android.views.ExpandableTextView;
import butterknife.BindView;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.utils.ShareUtil;
import leaf.prod.walletsdk.model.response.crawler.IndexResult;
import leaf.prod.walletsdk.model.response.crawler.News;
import leaf.prod.walletsdk.service.CrawlerService;
import leaf.prod.walletsdk.util.SPUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-22 4:25 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class NewsFlashItem extends NewsBodyItem {

    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private static SimpleDateFormat sdf2 = new SimpleDateFormat("MM-dd HH:mm");

    @BindView(R.id.tv_time)
    public TextView tvTime;

    @BindView(R.id.tv_title)
    public TextView tvTitle;

    @BindView(R.id.cl_content)
    public ExpandableTextView clContent;

    @BindView(R.id.tv_share)
    public TextView tvShare;

    @BindView(R.id.tv_bear)
    public TextView tvBear;

    @BindView(R.id.tv_bull)
    public TextView tvBull;

    @BindView(R.id.tv_source)
    public TextView tvSource;

    @BindView(R.id.iv_bull)
    public ImageView ivBull;

    @BindView(R.id.iv_bull_active)
    public ImageView ivBullActive;

    @BindView(R.id.iv_bear)
    public ImageView ivBear;

    @BindView(R.id.iv_bear_active)
    public ImageView ivBearActive;

    @BindView(R.id.iv_share)
    public ImageView ivShare;

    @BindView(R.id.cl_share)
    public ConstraintLayout clShare;

    @BindView(R.id.cl_bear)
    public ConstraintLayout clBear;

    @BindView(R.id.cl_bull)
    public ConstraintLayout clBull;

    private static CrawlerService crawlerService;

    private boolean expand = false;

    private News data;

    public NewsFlashItem(View itemView, Activity activity) {
        super(itemView, activity);
        clContent.setExpandInterpolator(new OvershootInterpolator());
        clContent.setCollapseInterpolator(new OvershootInterpolator());
        clContent.setOnClickListener(view -> {
            if (!expand && !clContent.isExpanded() || expand && clContent.isExpanded()) {
                expandView(expand = !expand);
            }
        });
        if (crawlerService == null) {
            crawlerService = new CrawlerService();
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setContent(News data) {
        if (data == null)
            return;
        try {
            this.data = data;
            if (data.getTitle().startsWith("孟岩")) {
                LyqbLogger.log(data.toString());
            }
            tvTime.setText(sdf2.format(sdf1.parse(data.getPublishTime())));
            tvSource.setText(activity.getResources().getString(R.string.news_source) + ":" + data.getSource());
            tvTitle.setText(data.getTitle());
            clContent.setText(data.getContent());
            tvShare.setText(activity.getResources()
                    .getString(R.string.news_share) + " " + (data.getForwardNum() > 0 ? data.getForwardNum() : ""));
            String result = (String) SPUtils.get(activity, "news_" + data.getUuid(), "");
            hideBullView(data.getBullIndex());
            hideBearView(data.getBearIndex());
            if (result.equalsIgnoreCase("bull")) {
                if (data.getBullIndex() == 0) {
                    SPUtils.put(activity, "news_" + data.getUuid(), "");
                } else {
                    showBullView(data.getBullIndex());
                }
            }
            if (result.equalsIgnoreCase("bear")) {
                if (data.getBearIndex() == 0) {
                    SPUtils.put(activity, "news_" + data.getUuid(), "");
                } else {
                    showBearView(data.getBearIndex());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void expandView(boolean flag) {
        clContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, flag ? clContent.getTextSize() + 8 : clContent.getTextSize() - 8);
        clContent.toggle();
        ViewGroup.LayoutParams lp = innerLayout.getLayoutParams();
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        innerLayout.setLayoutParams(lp);
    }

    @OnClick({R.id.cl_bull, R.id.cl_bear, R.id.cl_share})
    public void onViewClicked(View view) {
        if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
            switch (view.getId()) {
                case R.id.cl_bull:
                    setBull();
                    break;
                case R.id.cl_bear:
                    setBear();
                    break;
                case R.id.cl_share:
                    ShareUtil.uShareUrl(activity, data.getTitle(), data.getUrl(), " ", new UMShareListener() {
                        @Override
                        public void onStart(SHARE_MEDIA platform) {
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResult(SHARE_MEDIA platform) {
                            RxToast.success(activity.getResources().getString(R.string.share_success));
                            crawlerService.confirmForward(data.getUuid())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<IndexResult>() {
                                        @Override
                                        public void onCompleted() {
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            LyqbLogger.log(e.getMessage());
                                        }

                                        @Override
                                        public void onNext(IndexResult indexResult) {
                                            tvShare.setText(activity.getString(R.string.news_share) + " " + indexResult.getForwardNum());
                                        }
                                    });
                        }

                        @Override
                        public void onError(SHARE_MEDIA platform, Throwable t) {
                            if (t.getMessage().contains("2008")) {//错误码
                                RxToast.error(activity.getResources().getString(R.string.share_failed_no_app));
                            } else {
                                RxToast.error(activity.getResources().getString(R.string.share_failed, t.getMessage()));
                            }
                        }

                        @Override
                        public void onCancel(SHARE_MEDIA platform) {
                        }
                    });
                    break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setBull() {
        String result = (String) SPUtils.get(activity, "news_" + data.getUuid(), "");
        if (result.isEmpty()) {
            crawlerService.confirmBull(data.getUuid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<IndexResult>() {
                        @Override
                        public void onCompleted() {
                            unsubscribe();
                        }

                        @Override
                        public void onError(Throwable e) {
                            RxToast.error(activity.getResources().getString(R.string.service_error));
                            unsubscribe();
                        }

                        @Override
                        public void onNext(IndexResult indexResult) {
                            SPUtils.put(activity, "news_" + data.getUuid(), "bull");
                            showBullView(indexResult.getBullIndex());
                            unsubscribe();
                        }
                    });
        } else {
            if (result.equals("bull")) {
                crawlerService.cancelBull(data.getUuid()).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<IndexResult>() {
                            @Override
                            public void onCompleted() {
                                unsubscribe();
                            }

                            @Override
                            public void onError(Throwable e) {
                                RxToast.error(activity.getResources().getString(R.string.service_error));
                                unsubscribe();
                            }

                            @Override
                            public void onNext(IndexResult indexResult) {
                                SPUtils.put(activity, "news_" + data.getUuid(), "");
                                hideBullView(indexResult.getBullIndex());
                                unsubscribe();
                            }
                        });
            } else {
                crawlerService.confirmBull(data.getUuid())
                        .flatMap((Func1<IndexResult, Observable<IndexResult>>) indexResult -> crawlerService.cancelBear(indexResult
                                .getUuid()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<IndexResult>() {
                            @Override
                            public void onCompleted() {
                                unsubscribe();
                            }

                            @Override
                            public void onError(Throwable e) {
                                RxToast.error(activity.getResources().getString(R.string.service_error));
                                unsubscribe();
                            }

                            @Override
                            public void onNext(IndexResult indexResult) {
                                SPUtils.put(activity, "news_" + data.getUuid(), "bull");
                                showBullView(indexResult.getBullIndex());
                                hideBearView(indexResult.getBearIndex());
                                unsubscribe();
                            }
                        });
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setBear() {
        String result = (String) SPUtils.get(activity, "news_" + data.getUuid(), "");
        if (result.isEmpty()) {
            crawlerService.confirmBear(data.getUuid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<IndexResult>() {
                        @Override
                        public void onCompleted() {
                            unsubscribe();
                        }

                        @Override
                        public void onError(Throwable e) {
                            RxToast.error(activity.getResources().getString(R.string.service_error));
                            unsubscribe();
                        }

                        @Override
                        public void onNext(IndexResult indexResult) {
                            SPUtils.put(activity, "news_" + data.getUuid(), "bear");
                            showBearView(indexResult.getBearIndex());
                            unsubscribe();
                        }
                    });
        } else {
            if (result.equals("bear")) {
                crawlerService.cancelBear(data.getUuid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<IndexResult>() {
                            @Override
                            public void onCompleted() {
                                unsubscribe();
                            }

                            @Override
                            public void onError(Throwable e) {
                                RxToast.error(activity.getResources().getString(R.string.service_error));
                                unsubscribe();
                            }

                            @Override
                            public void onNext(IndexResult indexResult) {
                                SPUtils.put(activity, "news_" + data.getUuid(), "");
                                hideBearView(indexResult.getBearIndex());
                                unsubscribe();
                            }
                        });
            } else {
                crawlerService.confirmBear(data.getUuid())
                        .flatMap((Func1<IndexResult, Observable<IndexResult>>) indexResult -> crawlerService.cancelBull(data
                                .getUuid()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<IndexResult>() {

                            @Override
                            public void onCompleted() {
                                unsubscribe();
                            }

                            @Override
                            public void onError(Throwable e) {
                                RxToast.error(activity.getResources().getString(R.string.service_error));
                                unsubscribe();
                            }

                            @Override
                            public void onNext(IndexResult indexResult) {
                                SPUtils.put(activity, "news_" + data.getUuid(), "bear");
                                showBearView(indexResult.getBearIndex());
                                hideBullView(indexResult.getBullIndex());
                                unsubscribe();
                            }
                        });
            }
        }
    }

    private void showBullView(int index) {
        tvBull.setText(activity.getResources().getString(R.string.news_bull) + " " + (index > 0 ? index : ""));
        tvBull.setTypeface(null, Typeface.BOLD);
        tvBull.setTextColor(activity.getResources().getColor(R.color.colorGreen));
        ivBull.setVisibility(View.GONE);
        ivBullActive.setVisibility(View.VISIBLE);
    }

    private void hideBullView(int index) {
        tvBull.setText(activity.getResources().getString(R.string.news_bull) + " " + (index > 0 ? index : ""));
        tvBull.setTypeface(null, Typeface.NORMAL);
        tvBull.setTextColor(activity.getResources().getColor(R.color.colorNineText));
        ivBull.setVisibility(View.VISIBLE);
        ivBullActive.setVisibility(View.GONE);
    }

    private void showBearView(int index) {
        tvBear.setText(activity.getResources().getString(R.string.news_bear) + " " + (index > 0 ? index : ""));
        tvBear.setTypeface(null, Typeface.BOLD);
        tvBear.setTextColor(activity.getResources().getColor(R.color.colorRed));
        ivBear.setVisibility(View.GONE);
        ivBearActive.setVisibility(View.VISIBLE);
    }

    private void hideBearView(int index) {
        tvBear.setText(activity.getResources().getString(R.string.news_bear) + " " + (index > 0 ? index : ""));
        tvBear.setTypeface(null, Typeface.NORMAL);
        tvBear.setTextColor(activity.getResources().getColor(R.color.colorNineText));
        ivBear.setVisibility(View.VISIBLE);
        ivBearActive.setVisibility(View.GONE);
    }
}
