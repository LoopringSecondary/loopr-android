package leaf.prod.app.activity.setupWallet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.GridLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.app.adapter.setupwallet.MnemonicWordHintAdapter;
import leaf.prod.app.utils.AppManager;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.utils.DialogUtil;
import leaf.prod.app.utils.MyViewUtils;
import leaf.prod.app.views.SpacesItemDecoration;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.exception.InvalidPrivateKeyException;
import leaf.prod.walletsdk.exception.KeystoreCreateException;
import leaf.prod.walletsdk.model.ImportWalletType;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.CredentialsUtils;
import leaf.prod.walletsdk.util.EncryptUtil;
import leaf.prod.walletsdk.util.FileUtils;
import leaf.prod.walletsdk.util.KeystoreUtils;
import leaf.prod.walletsdk.util.MnemonicUtils;
import leaf.prod.walletsdk.util.PasswordUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;

public class GenerateWalletActivity extends BaseActivity {

    public final static int MNEMONIC_SUCCESS = 1;

    public final static int CREATE_SUCCESS = 2;

    public final static int ERROR_ONE = 3;

    public final static int ERROR_TWO = 4;

    public final static int ERROR_THREE = 5;

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.wallet_name)
    MaterialEditText walletName;

    @BindView(R.id.password_hint)
    TextView passwordHint;

    @BindView(R.id.password)
    MaterialEditText password;

    @BindView(R.id.repeat_password)
    MaterialEditText repeatPassword;

    @BindView(R.id.btn_next)
    Button btnNext;
    //    @BindView(R.id.password_match)
    //    TextView passwordMatch;

    @BindView(R.id.generate_partone)
    LinearLayout generatePartone;

    @BindView(R.id.generate_parttwo)
    LinearLayout generateParttwo;

    @BindView(R.id.mnemonic_hint)
    TextView mnemonicHint;

    @BindView(R.id.rl_mnemonic)
    RelativeLayout rlMnemonic;

    @BindView(R.id.rl_word)
    RelativeLayout rlWord;
    //    @BindView(R.id.confirm_mnemonic_word)
    //    TextView confirmMnemonicWord;
    //    @BindView(R.id.confirm_mnemonic_word_info)
    //    TextView confirmMnemonicWordInfo;

    @BindView(R.id.recycler_mnemonic_hint)
    RecyclerView recyclerMnemonicHint;
    //    @BindView(R.id.recycler_view)
    //    RecyclerView recyclerView;
    //
    //    @BindView(R.id.generate_partthree)
    //    LinearLayout generatePartthree;

    @BindView(R.id.btn_confirm)
    Button btnConfirm;

    @BindView(R.id.btn_skip)
    Button btnSkip;

    @BindView(R.id.password_level)
    LinearLayout pswLevel;

    @BindView(R.id.level1)
    View level1;

    @BindView(R.id.level2)
    View level2;

    @BindView(R.id.level3)
    View level3;

    @BindView(R.id.level_text)
    TextView levelText;

    List<String> mneCheckedList = new LinkedList<>();//选中的助记词

    List<String> listMnemonic = new ArrayList<>();  //正确顺序的助记词列表

    List<String> listRandomMnemonic = new ArrayList<>();  //打乱顺序的助记词列表

    private MnemonicWordHintAdapter mHintAdapter; //助记词提示adapter
    //    private MnemonicWordAdapter mAdapter;  //助记词选择adapter

    private String mnemonic;  //助记词

    private String nextStatus = "start"; //点击next时根据不同状态显示不同页面

    private String confirmStatus = "one";//点击confirm时根据不同状态显示不同页面

    private String address;//钱包地址

    private String filename;//钱包keystore名称

    private LoopringService loopringService = new LoopringService();

    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MNEMONIC_SUCCESS:
                    getAddress();
                    break;
                case CREATE_SUCCESS:  //获取keystore中的address成功后，调用解锁钱包方法（unlockWallet）
                    new Thread(() ->
                            loopringService.notifyCreateWallet(address)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Subscriber<String>() {
                                        @Override
                                        public void onCompleted() {
                                            String salt = EncryptUtil.getSecureRandom(), iv = EncryptUtil.getSecureRandom();
                                            WalletUtil.addWallet(GenerateWalletActivity.this, WalletEntity.builder()
                                                    .walletname(walletName.getText().toString())
                                                    .filename(filename)
                                                    .address("0x" + address)
                                                    .pas(EncryptUtil.encryptSHA3(password.getText().toString()))
                                                    .salt(salt)
                                                    .iv(iv)
                                                    .mnemonic(WalletUtil.encryptMnemonic(mnemonic, password.getText()
                                                            .toString(), salt, iv))
                                                    .chooseTokenList(Arrays.asList("ETH", "WETH", "LRC"))
                                                    .walletType(ImportWalletType.ALL).build());
                                            hideProgress();
                                            DialogUtil.showWalletCreateResultDialog(GenerateWalletActivity.this, v -> {
                                                DialogUtil.dialog.dismiss();
                                                finish();
                                                AppManager.getAppManager().finishAllActivity();
                                                getOperation().forwardClearTop(MainActivity.class);
                                            });
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            RxToast.error(getResources().getString(R.string.add_wallet_error));
                                            hideProgress();
                                        }

                                        @Override
                                        public void onNext(String s) {
                                        }
                                    })
                    ).start();
                    break;
                case ERROR_ONE:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.local_file_error));
                    break;
                case ERROR_TWO:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.local_file_error));
                    break;
            }
        }
    };

    private Animation shakeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_generate_wallet);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        MyViewUtils.hideSoftInput(this, walletName);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.generate_wallet));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null || editable.toString().length() == 0) {
                    pswLevel.setVisibility(View.INVISIBLE);
                    setPasswordHint(getResources().getColor(R.color.colorNineText), getResources().getString(R.string.good), false, View.VISIBLE);
                } else {
                    int level = PasswordUtils.checkPasswordLevel(password.getText().toString());
                    switch (level) {
                        case 1:
                            level1.setBackgroundColor(getResources().getColor(R.color.colorRed));
                            level2.setBackgroundColor(getResources().getColor(R.color.colorNineText));
                            level3.setBackgroundColor(getResources().getColor(R.color.colorNineText));
                            levelText.setText(getResources().getString(R.string.weak));
                            break;
                        case 2:
                            level1.setBackgroundColor(getResources().getColor(R.color.colorCenter));
                            level2.setBackgroundColor(getResources().getColor(R.color.colorCenter));
                            level3.setBackgroundColor(getResources().getColor(R.color.colorNineText));
                            levelText.setText(getResources().getString(R.string.middle));
                            break;
                        case 3:
                            level1.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                            level2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                            level3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                            levelText.setText(getResources().getString(R.string.strong));
                            break;
                    }
                    pswLevel.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void initData() {
        GridLayoutManager layoutManagerHint = new GridLayoutManager(this, 3);
        recyclerMnemonicHint.setLayoutManager(layoutManagerHint);  //助记词提示列表
        mHintAdapter = new MnemonicWordHintAdapter(R.layout.adapter_item_mnemonic_word_hint, null);
        recyclerMnemonicHint.addItemDecoration(new SpacesItemDecoration(8, 8, 2, 2));
        recyclerMnemonicHint.setAdapter(mHintAdapter);
        shakeAnimation = AnimationUtils.loadAnimation(GenerateWalletActivity.this, R.anim.shake_x);
    }

    @OnClick({R.id.wallet_name, R.id.btn_next, R.id.btn_confirm, R.id.btn_skip})
    public void onViewClicked(View view) {
        pswLevel.setVisibility(View.INVISIBLE);
        switch (view.getId()) {
            case R.id.wallet_name:
                break;
            case R.id.btn_next:
                if (nextStatus.equals("start")) {
                    if (TextUtils.isEmpty(walletName.getText().toString().trim())) {
                        setPasswordHint(getResources().getColor(R.color.colorRed), getResources().getString(R.string.wallet_name_hint), true, View.VISIBLE);
                    } else if (WalletUtil.isWalletExisted(this, walletName.getText().toString().trim())) {
                        setPasswordHint(getResources().getColor(R.color.colorRed), getResources().getString(R.string.wallet_name_existed), true, View.VISIBLE);
                    } else {
                        nextStatus = "password";
                        SPUtils.put(this, "walletname", walletName.getText().toString());
                        setPasswordHint(getResources().getColor(R.color.colorNineText), getResources().getString(R.string.good), false, View.VISIBLE);
                        passwordHint.setTextColor(getResources().getColor(R.color.colorNineText));
                        walletName.setVisibility(View.GONE);
                        password.setVisibility(View.VISIBLE);
                    }
                } else if (nextStatus.equals("password")) {
                    if (PasswordUtils.checkPasswordLevel(password.getText().toString()) != 3) {
                        setPasswordHint(getResources().getColor(R.color.colorRed), getResources().getString(R.string.password_weak), true, View.VISIBLE);
                    } else {
                        nextStatus = "match";
                        passwordHint.setVisibility(View.GONE);
                        password.setVisibility(View.GONE);
                        repeatPassword.setVisibility(View.VISIBLE);
                    }
                } else if (nextStatus.equals("match")) {
                    if (repeatPassword.getText().toString().equals(password.getText().toString())) {
                        if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                            mnemonic = MnemonicUtils.randomMnemonic();
                            String[] arrayMne = mnemonic.split(" ");
                            listMnemonic.clear();
                            for (int i = 0; i < arrayMne.length; i++) {
                                listMnemonic.add(arrayMne[i]);
                                listRandomMnemonic.add(arrayMne[i]);
                            }
                            mHintAdapter.setNewData(listMnemonic);
                            mHintAdapter.notifyDataSetChanged();
                            generatePartone.setVisibility(View.GONE);
                            generateParttwo.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                            MyViewUtils.hideSoftInput(this, repeatPassword);
                        }
                    } else {
                        setPasswordHint(getResources().getColor(R.color.colorRed), getResources().getString(R.string.password_match_toast), true, View.VISIBLE);
                        passwordHint.startAnimation(shakeAnimation);
                        MyViewUtils.hideSoftInput(this, repeatPassword);
                    }
                }
                break;
            case R.id.btn_confirm:
                Intent intent = new Intent(this, ConfirmMnemonicActivity.class);
                intent.putExtra("mnemonic", mnemonic);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_skip:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    startThread();
                }
                break;
        }
    }

    private void setPasswordHint(int color, String content, boolean animate, int visible) {
        passwordHint.setTextColor(color);
        passwordHint.setText(content);
        passwordHint.setVisibility(visible);
        if (animate) {
            passwordHint.startAnimation(shakeAnimation);
        }
    }

    /**
     * 生成钱包
     */
    private void startThread() {
        showProgress(getString(R.string.loading_default_messsage));
        new Thread(() -> {
            // TODO: need test for generate with password
            String pas = repeatPassword.getText().toString();
            Credentials credentials = MnemonicUtils.calculateCredentialsFromMnemonic(mnemonic, "m/44'/60'/0'/0", pas);
            String privateKeyHexString = CredentialsUtils.toPrivateKeyHexString(credentials.getEcKeyPair()
                    .getPrivateKey());
            try {
                filename = KeystoreUtils.createFromPrivateKey(privateKeyHexString, pas, FileUtils.getKeyStoreLocation(GenerateWalletActivity.this));
                handlerCreate.sendEmptyMessage(MNEMONIC_SUCCESS);
            } catch (InvalidPrivateKeyException | KeystoreCreateException e) {
                handlerCreate.sendEmptyMessage(ERROR_THREE);
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 判断两个集合是否相等
     */
    public boolean compare(List<String> a, List<String> b) {
        if (a.size() != b.size())
            return false;
        Collections.sort(a);
        Collections.sort(b);
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i)))
                return false;
        }
        return true;
    }

    /**
     * 获取本地keystore中的address
     */
    public void getAddress() {
        new Thread() {
            @Override
            public void run() {
                Message msg = handlerCreate.obtainMessage();
                try {
                    address = FileUtils.getFileFromSD(GenerateWalletActivity.this, filename);
                } catch (IOException e) {
                    handlerCreate.sendEmptyMessage(ERROR_ONE);
                    e.printStackTrace();
                } catch (JSONException e) {
                    handlerCreate.sendEmptyMessage(ERROR_TWO);
                    e.printStackTrace();
                }
                msg.obj = address;
                msg.what = CREATE_SUCCESS;
                handlerCreate.sendMessage(msg);
            }
        }.start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            Boolean result = bundle.getBoolean("result");
            if (result) {
                startThread();
            }
        }
    }
}
