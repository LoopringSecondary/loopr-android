package leaf.prod.app.fragment;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.web3j.crypto.Credentials;
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
import leaf.prod.walletsdk.model.eventbusData.MnemonicData;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.walletsdk.util.FileUtils;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.walletsdk.util.MD5Utils;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import leaf.prod.app.views.wheelPicker.picker.OptionPicker;
import leaf.prod.walletsdk.exception.InvalidPrivateKeyException;
import leaf.prod.walletsdk.exception.KeystoreCreateException;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.CredentialsUtils;
import leaf.prod.walletsdk.util.KeystoreUtils;
import leaf.prod.walletsdk.util.MnemonicUtils;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 *
 */
public class ImportMnemonicFragment extends BaseFragment {

    public final static int MNEMONIC_SUCCESS = 1;

    public final static int CREATE_SUCCESS = 2;

    public final static int ERROR_ONE = 3;

    public final static int ERROR_TWO = 4;

    public final static int ERROR_THREE = 5;

    public final static int ERROR_FOUR = 6;

    public final static int ERROR_FIVE = 7;

    Unbinder unbinder;

    @BindView(R.id.et_mnemonic)
    MaterialEditText etMnemonic;

    @BindView(R.id.wallet_type)
    TextView walletType;

    @BindView(R.id.wallet_list)
    RelativeLayout walletList;

    @BindView(R.id.et_password)
    MaterialEditText etPassword;

    @BindView(R.id.btn_next)
    Button btnNext;

    @BindView(R.id.et_dpath)
    MaterialEditText etDpath;

    @BindView(R.id.rl_dpath)
    RelativeLayout rlDpath;

    private String dpath;

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
                    WalletEntity newWallet = new WalletEntity("", filename, address, etMnemonic.getText()
                            .toString(), MD5Utils.md5(etPassword.getText()
                            .toString()), dpath, walletType.getText().toString(), ImportWalletType.MNEMONIC);
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
                    RxToast.error(getResources().getString(R.string.add_wallet_error));
                    hideProgress();
                    break;
                case ERROR_TWO:
                    break;
                case ERROR_THREE:
                case ERROR_FOUR:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.local_file_error));
                    break;
                case ERROR_FIVE:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.mnemonic_invalid));
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
        layout = inflater.inflate(R.layout.fragment_import_mnemonic, container, false);
        unbinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
    public void onMessageEvent(MnemonicData event) {
        /**
         * 将扫描的结果存到输入框中
         */
        etMnemonic.setText(event.getMnemonic());
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick({R.id.wallet_list, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.wallet_list:
                walletChoose();
                break;
            case R.id.btn_next:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    if (TextUtils.isEmpty(etMnemonic.getText().toString())) {
                        ToastUtils.toast(getResources().getString(R.string.input_mnemonic));
                        return;
                    }
                    if (TextUtils.isEmpty(walletType.getText().toString())) {
                        ToastUtils.toast(getResources().getString(R.string.wallet_type));
                        return;
                    }
                    if (walletType.getText().toString().equals("其它")) {
                        if (TextUtils.isEmpty(etDpath.getText().toString())) {
                            ToastUtils.toast(getResources().getString(R.string.input_dpath));
                            return;
                        }
                        dpath = etDpath.getText().toString();
                    }
                    LyqbLogger.log(etMnemonic.getText().toString() + "   " + dpath + "   " + etPassword.getText()
                            .toString());
                    unlockWallet();
                }
                break;
        }
    }

    /**
     * 生成钱包
     */
    private void unlockWallet() {
        showProgress("加载中...");
        new Thread(() -> {
            Credentials credentials;
            try {
                String psw = !"imToken".equals(walletType.getText().toString()) ? etPassword.getText().toString() : "";
                credentials = MnemonicUtils.calculateCredentialsFromMnemonic(etMnemonic.getText()
                        .toString().trim(), dpath, psw);
                String privateKeyHexString = CredentialsUtils.toPrivateKeyHexString(credentials.getEcKeyPair()
                        .getPrivateKey());
                filename = KeystoreUtils.createFromPrivateKey(privateKeyHexString, psw, FileUtils.getKeyStoreLocation(getContext()));
                //                SPUtils.put(getContext(), "filename", filename);
                handlerCreate.sendEmptyMessage(MNEMONIC_SUCCESS);
            } catch (KeystoreCreateException | InvalidPrivateKeyException e) {
                handlerCreate.sendEmptyMessage(ERROR_ONE);
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                handlerCreate.sendEmptyMessage(ERROR_FIVE);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 选择钱包类型
     */
    public void walletChoose() {
        OptionPicker picker = new OptionPicker((Activity) getContext(), new String[]{"Loopring Wallet", "imToken", "MetaMask", "TREZOR (ETH)", "Digital Bitbox", "Exodus", "Jaxx", "Ledger (ETH)", "TREZOR (ETC)", "Ledger (ETC)", "SingularDTV", "Network: Testnets", "Network: Expanse", "Network: Ubiq", "Network: Ellaism", "other"});
        picker.setOffset(1);
        picker.setSelectedIndex(0);
        picker.setTextSize(18);
        picker.setOnOptionPickListener((position, option) -> {
            LyqbLogger.log(position + "  ");
            rlDpath.setVisibility(View.GONE);
            if (position <= 6) {
                dpath = "m/44'/60'/0'/0";
            } else if (position <= 7) {
                dpath = "m/44'/60'/0'";
            } else if (position <= 8) {
                dpath = "m/44'/61'/0'/0";
            } else if (position <= 9) {
                dpath = "m/44'/60'/160720'/0";
            } else if (position <= 10) {
                dpath = "m/0'/0'/0";
            } else if (position <= 11) {
                dpath = "m/44'/1'/0'/0";
            } else if (position <= 12) {
                dpath = "m/44'/40'/0'/0";
            } else if (position <= 13) {
                dpath = "m/44'/108'/0'/0";
            } else if (position <= 14) {
                dpath = "m/44'/163'/0'/0";
            } else if (position <= 15) {
                dpath = "";
                rlDpath.setVisibility(View.VISIBLE);
            }
            walletType.setText(option);
        });
        picker.show();
    }
}
