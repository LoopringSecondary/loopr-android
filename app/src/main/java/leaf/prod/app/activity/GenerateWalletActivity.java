package leaf.prod.app.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import com.google.common.base.Joiner;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.adapter.MnemonicWordAdapter;
import leaf.prod.app.adapter.MnemonicWordHintAdapter;
import leaf.prod.walletsdk.model.ImportWalletType;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.app.utils.AppManager;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.utils.DialogUtil;
import leaf.prod.walletsdk.util.FileUtils;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.walletsdk.util.MD5Utils;
import leaf.prod.app.utils.MyViewUtils;
import leaf.prod.app.utils.PasswordUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import leaf.prod.app.views.SpacesItemDecoration;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.exception.InvalidPrivateKeyException;
import leaf.prod.walletsdk.exception.KeystoreCreateException;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.CredentialsUtils;
import leaf.prod.walletsdk.util.KeystoreUtils;
import leaf.prod.walletsdk.util.MnemonicUtils;
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

    @BindView(R.id.confirm_mnemonic_word)
    TextView confirmMnemonicWord;

    @BindView(R.id.confirm_mnemonic_word_info)
    TextView confirmMnemonicWordInfo;

    @BindView(R.id.recycler_mnemonic_hint)
    RecyclerView recyclerMnemonicHint;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.generate_partthree)
    LinearLayout generatePartthree;

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

    private MnemonicWordAdapter mAdapter;  //助记词选择adapter

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
                    new Thread(() -> {
                        LyqbLogger.log(address);
                        loopringService.notifyCreateWallet(address)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<String>() {
                                    @Override
                                    public void onCompleted() {
                                        WalletUtil.addWallet(GenerateWalletActivity.this, new WalletEntity(walletName.getText()
                                                .toString(), filename, "0x" + address, mnemonic, MD5Utils.md5(password.getText()
                                                .toString()), "", "", ImportWalletType.KEY_STORE));
                                        hideProgress();
                                        DialogUtil.showWalletCreateResultDialog(GenerateWalletActivity.this, v -> {
                                            DialogUtil.dialog.dismiss();
                                            finish();
                                            AppManager.getAppManager().finishAllActivity();
                                            //                                                    startActivity(new Intent(GenerateWalletActivity.this,MainActivity.class));
                                            getOperation().forwardClearTop(MainActivity.class);
                                        });
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        RxToast.error("创建失败，请重试");
                                        hideProgress();
                                    }

                                    @Override
                                    public void onNext(String s) {
                                    }
                                });
                    }).start();
                    break;
                case ERROR_ONE:
                    hideProgress();
                    RxToast.error("本地文件读取失败，请重试");
                    break;
                case ERROR_TWO:
                    hideProgress();
                    RxToast.error("本地文件JSON解析失败，请重试");
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
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        GridLayoutManager layoutManagerHint = new GridLayoutManager(this, 3);
        recyclerMnemonicHint.setLayoutManager(layoutManagerHint);  //助记词提示列表
        mHintAdapter = new MnemonicWordHintAdapter(R.layout.adapter_item_mnemonic_word_hint, null);
        recyclerMnemonicHint.addItemDecoration(new SpacesItemDecoration(8));
        recyclerMnemonicHint.setAdapter(mHintAdapter);
        recyclerView.setLayoutManager(layoutManager);   //助记词选择列表
        mAdapter = new MnemonicWordAdapter(R.layout.adapter_item_mnemonic_word, null);
        recyclerView.addItemDecoration(new SpacesItemDecoration(8));
        recyclerView.setAdapter(mAdapter);
        final Joiner joiner = Joiner.on(" ");
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            //选择助记词，进行验证
            CheckBox checkBox = view.findViewById(R.id.mnemonic_word);
            if (checkBox.isChecked()) {
                checkBox.setChecked(false);
                mneCheckedList.remove(listRandomMnemonic.get(position));
                confirmMnemonicWordInfo.setText(joiner.join(mneCheckedList));
            } else {
                checkBox.setChecked(true);
                mneCheckedList.add(listRandomMnemonic.get(position));
                confirmMnemonicWordInfo.setText(joiner.join(mneCheckedList));
            }
        });
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
                    if (password.length() < 6) {
                        setPasswordHint(getResources().getColor(R.color.colorRed), getResources().getString(R.string.good), true, View.VISIBLE);
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
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    if (confirmStatus.equals("one")) {
                        confirmStatus = "two";
                        rlMnemonic.setVisibility(View.GONE);
                        rlWord.setVisibility(View.GONE);
                        generatePartthree.setVisibility(View.VISIBLE);
                        LyqbLogger.log(listMnemonic.toString());
                        Collections.shuffle(listRandomMnemonic); //打乱助记词
                        mAdapter.setNewData(listRandomMnemonic);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        matchMnemonic();
                    }
                }
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
        passwordHint.setVisibility(View.VISIBLE);
        if (animate) {
            passwordHint.setAnimation(shakeAnimation);
        }
    }

    /**
     * 生成钱包
     */
    private void startThread() {
        showProgress("加载中...");
        new Thread(() -> {
            Credentials credentials = MnemonicUtils.calculateCredentialsFromMnemonic(mnemonic, "m/44'/60'/0'/0", "");
            String privateKeyHexString = CredentialsUtils.toPrivateKeyHexString(credentials.getEcKeyPair()
                    .getPrivateKey());
            String pas = repeatPassword.getText().toString();
            try {
                filename = KeystoreUtils.createFromPrivateKey(privateKeyHexString, pas, FileUtils.getKeyStoreLocation(GenerateWalletActivity.this));
                LyqbLogger.log(filename);
                //                SPUtils.put(GenerateWalletActivity.this, "filename", filename);
                handlerCreate.sendEmptyMessage(MNEMONIC_SUCCESS);
            } catch (InvalidPrivateKeyException | KeystoreCreateException e) {
                handlerCreate.sendEmptyMessage(ERROR_THREE);
                e.printStackTrace();
            }
            //                WalletDetail walletDetail = createWallet();//生成助记词
            //                filename = walletDetail.getFilename();
            //                LyqbLogger.log(filename);
            //                SPUtils.put(GenerateWalletActivity.this, "filename", filename);
            //                handlerCreate.sendEmptyMessage(MNEMONIC_SUCCESS);
        }).start();
    }

    /**
     * 助记词匹配
     */
    private void matchMnemonic() {
        String str = Joiner.on(" ").join(mneCheckedList);
        if (str.trim().equals(mnemonic)) {
            startThread();
        } else {
            ToastUtils.toast("助记词不匹配");
        }
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
     * 创建钱包
     */
    //    private WalletDetail createWallet() {
    //        WalletDetail walletDetail = null;
    //        try {
    //            String pas = repeatPassword.getText().toString();
    //            walletDetail = WalletHelper.createFromMnemonic(mnemonic, null, pas, FileUtils.getKeyStoreLocation(this));
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //        return walletDetail;
    //    }

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
}
