package leaf.prod.app.activity;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vondear.rxtool.view.RxToast;
import com.xw.repo.BubbleSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.GasDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.manager.TransactionDataManager;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.Erc20TransactionManager;
import leaf.prod.walletsdk.EthTransactionManager;
import leaf.prod.walletsdk.Transfer;
import leaf.prod.walletsdk.exception.IllegalCredentialException;
import leaf.prod.walletsdk.exception.InvalidKeystoreException;
import leaf.prod.walletsdk.exception.TransactionException;
import leaf.prod.walletsdk.model.QRCodeType;
import leaf.prod.walletsdk.model.response.data.BalanceResult;
import leaf.prod.walletsdk.model.response.data.Token;
import leaf.prod.walletsdk.service.LoopringService;

public class SendActivity extends BaseActivity {

    public final static int SEND_SUCCESS = 3;

    public final static int SEND_FAILED = 4;

    public final static int ERROR_ONE = 5;

    public final static int ERROR_TWO = 6;

    public final static int ERROR_THREE = 7;

    public final static int ERROR_FOUR = 8;

    private static int REQUEST_CODE = 1;  //二维码扫一扫code

    private static int TOKEN_CODE = 2;  //选择币种code

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.wallet_image)
    ImageView walletImage;

    @BindView(R.id.wallet_symbol)
    TextView walletSymbol;

    @BindView(R.id.send_wallet_name)
    TextView sendWalletName;

    @BindView(R.id.send_wallet_count)
    TextView sendWalletCount;

    @BindView(R.id.ll_manager_wallet)
    LinearLayout llManagerWallet;

    @BindView(R.id.wallet_address)
    MaterialEditText walletAddress;

    @BindView(R.id.iv_scan)
    ImageView ivScan;

    @BindView(R.id.address_toast)
    TextView addressToast;

    @BindView(R.id.money_amount)
    MaterialEditText moneyAmount;

    @BindView(R.id.wallet_name2)
    TextView walletName2;

    @BindView(R.id.amount_toast)
    TextView amountToast;

    @BindView(R.id.seekBar)
    BubbleSeekBar seekBar;

    @BindView(R.id.transacition_fee)
    TextView transacitionFee;

    @BindView(R.id.btn_send)
    Button btnSend;

    @BindView(R.id.ll_show_fee)
    LinearLayout llShowFee;

    private String errorMes;  //异常错误信息

    private double amountTotal; //选中币的值

    private double amountSend; //输入转币金额

    private double gasFee = 0.0002; //基础邮费

    private String address; //钱包地址

    private BigInteger nonce;

    private String sendChoose;

    private String gasLimitType = "token_transfer";

    private LoopringService loopringService = new LoopringService();

    private BalanceDataManager balanceManager;

    private TokenDataManager tokenDataManager;

    private GasDataManager gasDataManager;

    private MarketcapDataManager marketcapDataManager;

    private Erc20TransactionManager erc20TransactionManager;

    private EthTransactionManager ethTransactionManager;

    /**
     * 邮费选择弹窗相关组件
     */
    private AlertDialog feeDialog;

    private TextView tvAmount;

    private TextView tvWalletInfo;

    private ImageView cancel;

    private TextView recommendGas;

    private BubbleSeekBar gasSeekBar;

    /**
     * 确认转出弹窗相关组件
     */
    private AlertDialog confirmDialog;

    private TextView payAmount;

    private TextView toAddress;

    private TextView formAddress;

    private TextView tvGassFee;

    private Button confirm;

    private Double gasEthValue;

    /**
     * 输入密码dialog
     */
    private AlertDialog passwordDialog;

    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_SUCCESS:
                    hideProgress();
                    getOperation().addParameter("tokenAmount", "-" + moneyAmount.getText() + " " + sendChoose);
                    getOperation().addParameter("address", walletAddress.getText().toString());
                    getOperation().forwardClearTop(SendSuccessActivity.class);
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        passwordDialog.dismiss();
                    }
                    break;
                case ERROR_THREE:
                case ERROR_FOUR:
                case ERROR_ONE:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.keystore_psw_error));
                    break;
                case SEND_FAILED:
                case ERROR_TWO:
                    hideProgress();
                    getOperation().addParameter("error", getResources().getString(R.string.transfer_error));
                    getOperation().forwardClearTop(SendErrorActivity.class);
                    break;
            }
        }
    };

    /**
     * seekbar和edittext联动标志位
     */
    private boolean moneyAmountChange = false;

    private Animation shakeAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_send);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
        LyqbLogger.log(WalletUtil.getCurrentFileName(this));
    }

    @Override
    protected void initPresenter() {
        balanceManager = BalanceDataManager.getInstance(this);
        tokenDataManager = TokenDataManager.getInstance(this);
        marketcapDataManager = MarketcapDataManager.getInstance(this);
        gasDataManager = GasDataManager.getInstance(this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.send));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
        initSeekbar();
        initMoneyAmount();
    }

    @Override
    public void initData() {
        updateBySymbol(null);
        address = WalletUtil.getCurrentAddress(this);
        updateTransactionFeeUI();
        shakeAnimation = AnimationUtils.loadAnimation(SendActivity.this, R.anim.shake_x);
    }

    @Override
    public void onResume() {
        super.onResume();
        initWalletAddress();
    }

    @Override
    public void onRestart() {
        super.onRestart();
    }

    @OnClick({R.id.ll_manager_wallet, R.id.iv_scan, R.id.btn_send, R.id.ll_show_fee})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_manager_wallet:
                getOperation().forwardForResult(SendListChooseActivity.class, TOKEN_CODE);
                break;
            case R.id.iv_scan:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    Intent intent = new Intent(this, ActivityScanerCode.class);
                    intent.putExtra("restrict", QRCodeType.TRANSFER.name());
                    startActivityForResult(intent, REQUEST_CODE);
                }
                break;
            case R.id.ll_show_fee:
                showFeeDialog(this);
                break;
            case R.id.btn_send:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) { //防止一秒内多次点击
                    checkInfo();
                }
                break;
        }
    }

    private void checkInfo() {
        String amount = moneyAmount.getText().toString();
        if (TextUtils.isEmpty(walletAddress.getText().toString()) || !WalletUtils.isValidAddress(walletAddress.getText()
                .toString()
                .trim())) {
            addressToast.setText(getResources().getText(R.string.input_valid_address));
            addressToast.setTextColor(getResources().getColor(R.color.colorRed));
            addressToast.setVisibility(View.VISIBLE);
            addressToast.startAnimation(shakeAnimation);
            return;
        }
        if (TextUtils.isEmpty(amount) || (amountSend = Double.parseDouble(amount)) > amountTotal || amountSend == 0) {
            if (TextUtils.isEmpty(amount) || amountSend == 0) {
                amountToast.setText(getResources().getString(R.string.input_valid_amount));
            } else {
                amountToast.setText(getResources().getString(R.string.available_balance, sendWalletCount.getText()));
            }
            amountToast.setTextColor(getResources().getColor(R.color.colorRed));
            amountToast.setVisibility(View.VISIBLE);
            amountToast.startAnimation(shakeAnimation);
            return;
        }
        showConfirmDialog(this);
    }

    public void showConfirmDialog(Context context) {
        if (confirmDialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.DialogTheme);//
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_send_confirm, null);
            builder.setView(view);
            payAmount = view.findViewById(R.id.pay_amount);
            toAddress = view.findViewById(R.id.to_address);
            formAddress = view.findViewById(R.id.form_address);
            tvGassFee = view.findViewById(R.id.gass_fee);
            confirm = view.findViewById(R.id.btn_confirm);
            confirm.setOnClickListener(v -> {
                confirmDialog.dismiss();
                if (WalletUtil.needPassword(context)) {
                    showPasswordDialog();
                } else {
                    send("");
                }
            });
            builder.setCancelable(true);
            confirmDialog = null;
            confirmDialog = builder.create();
            confirmDialog.setCancelable(true);
            confirmDialog.setCanceledOnTouchOutside(true);
        }
        payAmount.setText(moneyAmount.getText().toString() + " " + sendChoose);
        toAddress.setText(walletAddress.getText().toString());
        formAddress.setText(address);
        tvGassFee.setText(transacitionFee.getText());
        confirmDialog.show();
    }

    public void showPasswordDialog() {
        if (passwordDialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this, R.style.DialogTheme);//
            View view = LayoutInflater.from(this).inflate(R.layout.dialog_put_password, null);
            builder.setView(view);
            final EditText passwordInput = view.findViewById(R.id.password_input);
            view.findViewById(R.id.cancel).setOnClickListener(v -> passwordDialog.dismiss());
            view.findViewById(R.id.confirm).setOnClickListener(v -> {
                if (TextUtils.isEmpty(passwordInput.getText().toString())) {
                    ToastUtils.toast(view.getResources().getString(R.string.put_password));
                } else {
                    send(passwordInput.getText().toString());
                }
            });
            builder.setCancelable(true);
            passwordDialog = null;
            passwordDialog = builder.create();
            passwordDialog.setCancelable(true);
            passwordDialog.setCanceledOnTouchOutside(true);
        }
        passwordDialog.show();
    }

    private void send(String password) {
        showProgress(getResources().getString(R.string.loading_default_messsage));
        new Thread(() -> {
            try {
                gasFee = gasDataManager.getGasAmountInETH(gasLimitType);
                if (gasFee > balanceManager.getAssetBySymbol("ETH").getValue()) {
                    // 油费不足
                    getOperation().addParameter("tokenAmount", gasFee + " ETH");
                    getOperation().forwardClearTop(SendErrorActivity.class);
                }
                Credentials credentials = WalletUtil.getCredential(this, password);
                //                WalletEntity walletEntity = WalletUtil.getCurrentWallet(this);
                //                if (walletEntity != null && walletEntity.getWalletType() != null && walletEntity.getWalletType() == ImportWalletType.MNEMONIC) {
                //                    LyqbLogger.log(walletEntity.toString());
                //                    credentials = MnemonicUtils.calculateCredentialsFromMnemonic(walletEntity.getMnemonic(), walletEntity
                //                            .getdPath(), password);
                //                } else {
                //                    String keystore = FileUtils.getKeystoreFromSD(SendActivity.this);
                //                    credentials = KeystoreUtils.unlock(password, keystore);
                //                }
                //                BigInteger values = UnitConverter.ethToWei(moneyAmount.getText().toString()); //转账金额
                BigInteger values = tokenDataManager.getWeiFromDouble(sendChoose, moneyAmount.getText().toString());
                //调用transaction方法
                String txHash;
                Transfer transfer = new Transfer(credentials);
                BigInteger gasLimit, gasPrice = gasDataManager.getCustomizeGasPriceInWei().toBigInteger();
                if (sendChoose.equals("ETH")) {
                    gasLimit = gasDataManager.getGasLimitByType("eth_transfer");
                    txHash = transfer.eth(gasPrice, gasLimit)
                            .send(credentials, address, walletAddress.getText().toString(), values);
                } else {
                    gasLimit = gasDataManager.getGasLimitByType("token_transfer");
                    txHash = transfer.erc20(tokenDataManager.getTokenBySymbol(sendChoose)
                            .getProtocol(), gasPrice, gasLimit)
                            .transfer(credentials, tokenDataManager.getTokenBySymbol(sendChoose)
                                    .getProtocol(), walletAddress.getText().toString(), values);
                }
                TransactionDataManager manager = TransactionDataManager.getInstance(SendActivity.this);
                manager.queryByHash(txHash);
                LyqbLogger.log(txHash);
                handlerCreate.sendEmptyMessage(SEND_SUCCESS);
            } catch (TransactionException e) {
                errorMes = e.getMessage();
                handlerCreate.sendEmptyMessage(SEND_FAILED);
                e.printStackTrace();
            } catch (InvalidKeystoreException | IllegalCredentialException e) {
                handlerCreate.sendEmptyMessage(ERROR_ONE);
                e.printStackTrace();
            } catch (JSONException | IOException e) {
                handlerCreate.sendEmptyMessage(ERROR_FOUR);
                e.printStackTrace();
            } catch (Exception e) {
                errorMes = e.getMessage();
                handlerCreate.sendEmptyMessage(ERROR_TWO);
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //            处理扫描结果（在界面上显示）
            if (null != data) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                String result = bundle.getString("result");
                LyqbLogger.log(result);
                walletAddress.setText(result);
            }
        } else if (requestCode == TOKEN_CODE) {
            if (resultCode == 1) {
                updateBySymbol(data);
            }
        }
    }

    /**
     * 油费选择弹窗
     *
     * @param context
     */
    public void showFeeDialog(Context context) {
        showKeyboard(moneyAmount, false);
        if (feeDialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.DialogTheme);//
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_fee, null);
            builder.setView(view);
            tvAmount = view.findViewById(R.id.tv_amount);
            tvWalletInfo = view.findViewById(R.id.tv_wallet_info);
            cancel = view.findViewById(R.id.cancel);
            recommendGas = view.findViewById(R.id.recommend_gas);
            gasSeekBar = view.findViewById(R.id.gasSeekBar);
            gasSeekBar.getConfigBuilder()
                    .min(1)
                    .max(Float.parseFloat(NumberUtils.format1(gasDataManager.getRecommendGasPriceInGWei()
                            .multiply(new BigDecimal(2))
                            .doubleValue(), 1)))
                    .build();
            gasSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                    gasDataManager.setCustomizeGasPriceInGWei((double) progressFloat);
                    gasEthValue = Double.parseDouble(gasDataManager.getGasAmountInETH(String.valueOf(gasDataManager.getGasLimitByType(gasLimitType)), String
                            .valueOf(gasDataManager.getCustomizeGasPriceInWei())));
                    tvAmount.setText(new StringBuilder(gasEthValue.toString()).append(" ETH ≈ ")
                            .append(CurrencyUtil.format(view.getContext(), gasEthValue * marketcapDataManager.getPriceBySymbol("ETH"))));
                    tvWalletInfo.setText(new StringBuilder("Gas limit(").append(gasDataManager.getGasLimitByType(gasLimitType))
                            .append(") * Gas Price(")
                            .append((int) gasDataManager.getGasPriceInGwei())
                            .append(" Gwei)"));
                }

                @Override
                public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                }

                @Override
                public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                }
            });
            recommendGas.setOnClickListener(view1 -> gasSeekBar.post(() -> gasSeekBar.setProgress(gasDataManager.getRecommendGasPriceInGWei()
                    .intValue())));
            cancel.setOnClickListener(v -> feeDialog.dismiss());
            builder.setCancelable(true);
            feeDialog = builder.create();
            feeDialog.setCancelable(true);
            feeDialog.setCanceledOnTouchOutside(true);
            feeDialog.setOnDismissListener(dialogInterface -> transacitionFee.setText(tvAmount.getText()));
            Objects.requireNonNull(feeDialog.getWindow()).setGravity(Gravity.BOTTOM);
        }
        gasSeekBar.setProgress((float) gasDataManager.getGasPriceInGwei());
        tvAmount.setText(transacitionFee.getText());
        tvWalletInfo.setText(new StringBuilder("Gas limit(").append(gasDataManager.getGasLimitByType(gasLimitType))
                .append(") * Gas Price(")
                .append((int) gasDataManager.getGasPriceInGwei())
                .append(" Gwei)"));
        feeDialog.show();
    }

    private void updateBySymbol(Intent data) {
        Intent intent = data == null ? getIntent() : data;
        sendChoose = intent.getStringExtra("symbol");
        if (sendChoose == null) {
            sendChoose = (String) SPUtils.get(this, "send_choose", "ETH");
        }
        updateTransactionFeeUI();
        BalanceResult.Asset asset = balanceManager.getAssetBySymbol(sendChoose);
        setWalletImage(sendChoose);
        sendWalletName.setText(sendChoose);
        walletName2.setText(sendChoose);
        amountTotal = asset.getValue();
        sendWalletCount.setText(asset.getValueShown() + " " + sendChoose);
    }

    // Update the transaction fee and related UI.
    private void updateTransactionFeeUI() {
        gasDataManager.getGasObservable().subscribe(gasPrice -> {
            LyqbLogger.log("gas: " + gasPrice);
            gasDataManager.setRecommendGasPrice(gasPrice);
            if (sendChoose.equals("ETH")) {
                gasLimitType = "eth_transfer";
            } else {
                gasLimitType = "token_transfer";
            }
            gasEthValue = Double.parseDouble(gasDataManager.getGasAmountInETH(gasDataManager.getGasLimitByType(gasLimitType)
                    .toString(), gasDataManager.getGasPriceString()));

            // Avoid scientific notation
            DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            df.setMaximumFractionDigits(10);

            transacitionFee.setText(new StringBuilder(df.format(gasEthValue)).append(" ETH ≈ ")
                    .append(CurrencyUtil.format(this, gasEthValue * marketcapDataManager.getPriceBySymbol("ETH"))));
        }, error -> Log.e("Send", error.getMessage()));
    }

    private void setWalletImage(String symbol) {
        Token token = tokenDataManager.getTokenBySymbol(symbol);
        if (token.getImageResId() != 0) {
            walletSymbol.setVisibility(View.GONE);
            walletImage.setImageResource(token.getImageResId());
            walletImage.setVisibility(View.VISIBLE);
        } else {
            walletImage.setVisibility(View.GONE);
            walletSymbol.setText(symbol);
            walletSymbol.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 金额拖动条
     */
    private void initSeekbar() {
        moneyAmount.post(() -> moneyAmount.setText(""));
        seekBar.setProgress(0);
        seekBar.setCustomSectionTextArray((sectionCount, array) -> {
            array.clear();
            array.put(0, "0%");
            array.put(1, "25%");
            array.put(2, "50%");
            array.put(3, "75%");
            array.put(4, "100%");
            return array;
        });
        seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                if (moneyAmountChange) {
                    moneyAmountChange = false;
                    return;
                }
                int precision = balanceManager.getPrecisionBySymbol(sendChoose);
                moneyAmount.setText(NumberUtils.format1(balanceManager.getAssetBySymbol(sendChoose)
                        .getValue() * progressFloat / 100, precision));
                amountToast.setText(CurrencyUtil.format(getApplicationContext(), balanceManager.getAssetBySymbol(sendChoose)
                        .getLegalValue() * progressFloat / 100));
                Selection.setSelection(moneyAmount.getText(), moneyAmount.getText().length());
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
            }
        });
    }

    /**
     * 输入金额实时验证
     */
    private void initMoneyAmount() {
        amountToast.setText(CurrencyUtil.format(SendActivity.this, 0));
        amountToast.setTextColor(getResources().getColor(R.color.colorNineText));
        moneyAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                double currentAmount = (editable != null && !editable.toString()
                        .isEmpty()) ? Double.parseDouble(editable.toString()
                        .equals(".") ? "0" : editable.toString()) : 0;
                moneyAmountChange = true;
                if (currentAmount > amountTotal) {
                    amountToast.setText(getResources().getString(R.string.available_balance, sendWalletCount.getText()));
                    amountToast.setTextColor(getResources().getColor(R.color.colorRed));
                    amountToast.startAnimation(shakeAnimation);
                    seekBar.setProgress(100);
                } else {
                    amountToast.setText(CurrencyUtil.format(getApplicationContext(), currentAmount * marketcapDataManager
                            .getPriceBySymbol(sendChoose)));
                    amountToast.setTextColor(getResources().getColor(R.color.colorNineText));
                    seekBar.setProgress((float) (amountTotal != 0 ? currentAmount / amountTotal * 100 : 0));
                }
            }
        });
    }

    /**
     * 钱包地址实时验证
     */
    private void initWalletAddress() {
        String sendAddress = getIntent().getStringExtra("send_address");
        if (sendAddress != null && !sendAddress.isEmpty()) {
            walletAddress.setText(getIntent().getStringExtra("send_address"));
            showKeyboard(moneyAmount, true);
        }
        walletAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null || editable.toString().isEmpty()) {
                    addressToast.setTextColor(getResources().getColor(R.color.colorNineText));
                    addressToast.setText(getResources().getText(R.string.address_confirm));
                    addressToast.setVisibility(View.VISIBLE);
                } else if (!WalletUtils.isValidAddress(editable.toString().trim())) {
                    addressToast.setTextColor(getResources().getColor(R.color.colorRed));
                    addressToast.setText(getResources().getText(R.string.input_valid_address));
                    addressToast.startAnimation(shakeAnimation);
                    addressToast.setVisibility(View.VISIBLE);
                } else {
                    addressToast.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void showKeyboard(View view, boolean show) {
        getWindow().getDecorView().postDelayed(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                view.requestFocus();
                if (show)
                    inputMethodManager.showSoftInput(view, 0);
                else
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }, 100);
    }
}
