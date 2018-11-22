package leaf.prod.app.fragment;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import leaf.prod.app.R;
import leaf.prod.app.activity.SetWalletNameActivity;
import leaf.prod.walletsdk.model.ImportWalletType;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.walletsdk.model.eventbusData.PrivateKeyData;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.walletsdk.util.FileUtils;
import leaf.prod.walletsdk.util.MD5Utils;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import leaf.prod.walletsdk.exception.InvalidPrivateKeyException;
import leaf.prod.walletsdk.exception.KeystoreCreateException;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.KeystoreUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 */
public class ImportPrivateKeyFragment extends BaseFragment {

    public final static int MNEMONIC_SUCCESS = 1;

    public final static int CREATE_SUCCESS = 2;

    public final static int ERROR_ONE = 3;

    public final static int ERROR_TWO = 4;

    public final static int ERROR_THREE = 5;

    public final static int ERROR_FOUR = 6;

    Unbinder unbinder;

    @BindView(R.id.et_password)
    MaterialEditText etPassword;

    @BindView(R.id.et_repeat_password)
    MaterialEditText etRepeatPassword;

    @BindView(R.id.btn_next)
    Button btnNext;

    @BindView(R.id.et_private_key)
    MaterialEditText etPrivateKey;

    private String address;//钱包地址

    private String filename;//钱包名称

    private LoopringService loopringService = new LoopringService();

    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MNEMONIC_SUCCESS:
                    hideProgress();
                    getAddress();
                    break;
                case CREATE_SUCCESS:  //获取keystore中的address成功后，调用解锁钱包方法（unlockWallet）
                    WalletEntity newWallet = new WalletEntity("", filename, address, "", MD5Utils.md5(etPassword.getText()
                            .toString()), "", "", ImportWalletType.PRIVATE_KEY);
                    loopringService.notifyCreateWallet(address)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<String>() {
                                @Override
                                public void onCompleted() {
                                    hideProgress();
                                    if (WalletUtil.isWalletExisted(getContext(), newWallet)) {
                                        RxToast.error(getResources().getString(R.string.wallet_existed));
                                    } else {
                                        getOperation().addParameter("newWallet", newWallet);
                                        getOperation().forward(SetWalletNameActivity.class);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastUtils.toast(getResources().getString(R.string.add_wallet_error));
                                    hideProgress();
                                }

                                @Override
                                public void onNext(String s) {
                                }
                            });
                    break;
                case ERROR_ONE:
                    ToastUtils.toast("私钥错误");
                    hideProgress();
                    break;
                case ERROR_TWO:
                    ToastUtils.toast(getResources().getString(R.string.add_wallet_error));
                    hideProgress();
                    break;
                case ERROR_THREE:
                    hideProgress();
                    ToastUtils.toast(getResources().getString(R.string.local_file_error));
                    break;
                case ERROR_FOUR:
                    hideProgress();
                    ToastUtils.toast(getResources().getString(R.string.local_file_error));
                    break;
            }
        }
    };

    public void getAddress() {
        new Thread() {
            @Override
            public void run() {
                Message msg = handlerCreate.obtainMessage();
                try {
                    address = FileUtils.getFileFromSD(getContext(), filename);
                    msg.obj = address;
                    msg.what = CREATE_SUCCESS;
                    handlerCreate.sendMessage(msg);
                } catch (IOException e) {
                    handlerCreate.sendEmptyMessage(ERROR_THREE);
                    e.printStackTrace();
                } catch (JSONException e) {
                    handlerCreate.sendEmptyMessage(ERROR_FOUR);
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 布局导入
        layout = inflater.inflate(R.layout.fragment_import_private_key, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(PrivateKeyData event) {
        /**
         * 将扫描的结果存到输入框中
         */
        etPrivateKey.setText(event.getPrivateKey());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_next)
    public void onViewClicked() {
        if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
            if (TextUtils.isEmpty(etPrivateKey.getText().toString())) {
                ToastUtils.toast("请输入私钥");
                return;
            }
            if (TextUtils.isEmpty(etPassword.getText().toString())) {
                ToastUtils.toast("请输入密码");
                return;
            }
            if (etPassword.getText().toString().length() < 6) {
                ToastUtils.toast("请输入6位以上密码");
                return;
            }
            if (TextUtils.isEmpty(etRepeatPassword.getText().toString())) {
                ToastUtils.toast("请再次输入密码");
                return;
            }
            if (!etRepeatPassword.getText().toString().equals(etPassword.getText().toString())) {
                ToastUtils.toast("两次输入密码不一致");
                return;
            }
            unlockWallet();
        }
    }

    /**
     * 生成钱包
     */
    private void unlockWallet() {
        showProgress("加载中...");
        new Thread(() -> {
            try {
                filename = KeystoreUtils.createFromPrivateKey(etPrivateKey.getText()
                        .toString().trim(), etPassword.getText()
                        .toString(), FileUtils.getKeyStoreLocation(getContext()));
                //                SPUtils.put(getContext(), "filename", filename);
                handlerCreate.sendEmptyMessage(MNEMONIC_SUCCESS);
            } catch (KeystoreCreateException e) {
                handlerCreate.sendEmptyMessage(ERROR_TWO);
                e.printStackTrace();
            } catch (InvalidPrivateKeyException e) {
                handlerCreate.sendEmptyMessage(ERROR_ONE);
                e.printStackTrace();
            }
        }).start();
    }
}
