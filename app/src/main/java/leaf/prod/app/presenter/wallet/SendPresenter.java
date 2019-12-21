package leaf.prod.app.presenter.wallet;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import androidx.appcompat.app.AlertDialog;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import com.xw.repo.BubbleSeekBar;

import leaf.prod.app.R;
import leaf.prod.app.activity.wallet.SendActivity;
import leaf.prod.app.activity.wallet.SendErrorActivity;
import leaf.prod.app.adapter.wallet.ContactSearchAdapter;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.utils.PasswordDialogUtil;
import leaf.prod.walletsdk.Transfer;
import leaf.prod.walletsdk.exception.IllegalCredentialException;
import leaf.prod.walletsdk.exception.InvalidKeystoreException;
import leaf.prod.walletsdk.exception.TransactionException;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.GasDataManager;
import leaf.prod.walletsdk.manager.LoginDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.manager.TransactionDataManager;
import leaf.prod.walletsdk.model.Contact;
import leaf.prod.walletsdk.model.response.relay.BalanceResult;
import leaf.prod.walletsdk.model.response.relay.Token;
import leaf.prod.walletsdk.util.CredentialsUtils;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.DpUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.walletsdk.util.SPUtils;
import leaf.prod.walletsdk.util.WalletUtil;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-18 4:21 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class SendPresenter extends BasePresenter<SendActivity> {

    private static String PASSWORD_TYPE = "SEND";

    private static String GAS_LIMIT_TYPE = "token_transfer";

    private int maxHeight;

    private int perHeight;

    private BalanceDataManager balanceManager;

    private TokenDataManager tokenDataManager;

    private GasDataManager gasDataManager;

    private MarketcapDataManager marketcapDataManager;

    private TransactionDataManager transactionDataManager;

    private LoginDataManager loginDataManager;

    private double gasFee = 0.0002; //基础邮费

    private String sendChoose;

    /**
     * 邮费选择弹窗相关组件
     */
    private AlertDialog feeDialog;

    private TextView tvAmount;

    private TextView tvWalletInfo;

    private ImageView cancel;

    private TextView recommendGas;

    private BubbleSeekBar gasSeekBar;

    private Double gasEthValue;

    /**
     * 确认转出弹窗相关组件
     */
    private AlertDialog confirmDialog;

    private TextView payAmount;

    private TextView toAddress;

    private TextView formAddress;

    private TextView tvGassFee;

    private Button confirm;

    private String address;

    private double amountSend; //输入转币金额

    private double amountTotal; //选中币的值

    /**
     * seekbar和edittext联动标志位
     */
    private boolean moneyAmountChange = false;

    public SendPresenter(SendActivity view, Context context) {
        super(view, context);
        balanceManager = BalanceDataManager.getInstance(context);
        tokenDataManager = TokenDataManager.getInstance(context);
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        gasDataManager = GasDataManager.getInstance(context);
        transactionDataManager = TransactionDataManager.getInstance(context);
        loginDataManager = LoginDataManager.getInstance(context);
        address = WalletUtil.getCurrentAddress(context);
        maxHeight = DpUtil.dp2Int(context, 180);
        perHeight = DpUtil.dp2Int(context, 50);
    }

    public void updateTransactionFeeUI() {
        gasDataManager.getGasObservable().subscribe(gasPrice -> {
            LyqbLogger.log("gas: " + gasPrice);
            gasDataManager.setRecommendGasPrice(gasPrice);
            if (sendChoose.equals("ETH")) {
                GAS_LIMIT_TYPE = "eth_transfer";
            } else {
                GAS_LIMIT_TYPE = "token_transfer";
            }
            gasEthValue = Double.parseDouble(gasDataManager.getGasAmountInETH(gasDataManager.getGasLimitByType(GAS_LIMIT_TYPE)
                    .toString(), gasDataManager.getGasPriceString()));
            // Avoid scientific notation
            DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            df.setMaximumFractionDigits(10);
            view.transactionFee.setText(new StringBuilder(df.format(gasEthValue)).append(" ETH ≈ ")
                    .append(CurrencyUtil.format(context, gasEthValue * marketcapDataManager.getPriceBySymbol("ETH"))));
        }, error -> Log.e("Send", error.getMessage()));
    }

    /**
     * 转出确认弹窗
     */
    public void showConfirmDialog() {
        if (confirmDialog == null) {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogTheme);//
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
                    PasswordDialogUtil.showPasswordDialog(context, PASSWORD_TYPE, view1 -> send(PasswordDialogUtil.getInputPassword()));
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
        payAmount.setText(view.moneyAmount.getText().toString() + " " + sendChoose);
        toAddress.setText(view.walletAddress.getText().toString());
        formAddress.setText(address);
        tvGassFee.setText(view.transactionFee.getText());
        confirmDialog.show();
    }

    /**
     * 油费选择弹窗
     */
    public void showFeeDialog() {
        showKeyboard(view.moneyAmount, false);
        if (feeDialog == null) {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogTheme);//
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
                    gasEthValue = Double.parseDouble(gasDataManager.getGasAmountInETH(String.valueOf(gasDataManager.getGasLimitByType(GAS_LIMIT_TYPE)), String
                            .valueOf(gasDataManager.getCustomizeGasPriceInWei())));
                    tvAmount.setText(new StringBuilder(NumberUtils.format1(gasEthValue, BalanceDataManager.getPrecision("ETH")))
                            .append(" ETH ≈ ")
                            .append(CurrencyUtil.format(view.getContext(), gasEthValue * marketcapDataManager.getPriceBySymbol("ETH"))));
                    tvWalletInfo.setText(new StringBuilder(view.getResources().getString(R.string.gas_limit)).append("(").append(gasDataManager.getGasLimitByType(GAS_LIMIT_TYPE))
                            .append(") x ").append(view.getResources().getString(R.string.gas_price)).append("(")
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
            feeDialog.setOnDismissListener(dialogInterface -> this.view.transactionFee.setText(tvAmount.getText()));
            Objects.requireNonNull(feeDialog.getWindow()).setGravity(Gravity.BOTTOM);
        }
        gasSeekBar.setProgress((float) gasDataManager.getGasPriceInGwei());
        tvAmount.setText(this.view.transactionFee.getText());
        tvWalletInfo.setText(new StringBuilder(view.getResources().getString(R.string.gas_limit)).append("(").append(gasDataManager.getGasLimitByType(GAS_LIMIT_TYPE))
                .append(") x ").append(view.getResources().getString(R.string.gas_price)).append("(")
                .append((int) gasDataManager.getGasPriceInGwei())
                .append(" Gwei)"));
        feeDialog.show();
    }

    public void dismissPasswordDialog() {
        PasswordDialogUtil.dismiss(PASSWORD_TYPE);
    }

    public void send(String password) {
        view.showProgress(view.getResources().getString(R.string.loading_default_messsage));
        new Thread(() -> {
            try {
                gasFee = gasDataManager.getGasAmountInETH(GAS_LIMIT_TYPE);
                if (gasFee > balanceManager.getAssetBySymbol("ETH").getValue()) {
                    // 油费不足
                    view.getOperation().addParameter("tokenAmount", gasFee + " ETH");
                    view.getOperation().forwardClearTop(SendErrorActivity.class);
                }
                Credentials credentials = WalletUtil.getCredential(context, password);
                BigInteger values = tokenDataManager.getWeiFromDouble(sendChoose, view.moneyAmount.getText()
                        .toString());
                //调用transaction方法
                String txHash;
                Transfer transfer = new Transfer(credentials);
                BigInteger gasLimit, gasPrice = gasDataManager.getCustomizeGasPriceInWei().toBigInteger();
                if (sendChoose.equals("ETH")) {
                    gasLimit = gasDataManager.getGasLimitByType("eth_transfer");
                    txHash = transfer.eth(gasPrice, gasLimit)
                            .send(credentials, address, view.walletAddress.getText().toString(), values);
                } else {
                    gasLimit = gasDataManager.getGasLimitByType("token_transfer");
                    String contract = tokenDataManager.getTokenBySymbol(sendChoose).getProtocol();
                    txHash = transfer.erc20(contract, gasPrice, gasLimit)
                            .transfer(credentials, contract, view.walletAddress.getText().toString(), values);
                }
                transactionDataManager.queryByHash(txHash);
                LyqbLogger.log(txHash);
                view.handlerCreate.sendEmptyMessage(SendActivity.SEND_SUCCESS);
            } catch (TransactionException e) {
                view.handlerCreate.sendEmptyMessage(SendActivity.SEND_FAILED);
                e.printStackTrace();
            } catch (InvalidKeystoreException | IllegalCredentialException e) {
                view.handlerCreate.sendEmptyMessage(SendActivity.ERROR_ONE);
                e.printStackTrace();
            } catch (JSONException | IOException e) {
                view.handlerCreate.sendEmptyMessage(SendActivity.ERROR_FOUR);
                e.printStackTrace();
            } catch (Exception e) {
                view.handlerCreate.sendEmptyMessage(SendActivity.ERROR_TWO);
                e.printStackTrace();
            }
        }).start();
    }

    public void checkInfo() {
        String amount = view.moneyAmount.getText().toString();
        String address = view.walletAddress.getText().toString().trim();
        if (TextUtils.isEmpty(address) || !CredentialsUtils.isValidAddress(address)) {
            view.addressToast.setText(view.getResources().getText(R.string.input_valid_address));
            view.addressToast.setTextColor(view.getResources().getColor(R.color.colorRed));
            view.addressToast.setVisibility(View.VISIBLE);
            view.addressToast.startAnimation(view.shakeAnimation);
            return;
        }
        if (TextUtils.isEmpty(amount) || (amountSend = Double.parseDouble(amount)) > amountTotal || amountSend == 0) {
            if (TextUtils.isEmpty(amount) || amountSend == 0) {
                view.amountToast.setText(view.getResources().getString(R.string.input_valid_amount));
            } else {
                view.amountToast.setText(view.getResources()
                        .getString(R.string.available_balance, view.sendWalletCount.getText()));
            }
            view.amountToast.setTextColor(view.getResources().getColor(R.color.colorRed));
            view.amountToast.setVisibility(View.VISIBLE);
            view.amountToast.startAnimation(view.shakeAnimation);
            return;
        }
        showConfirmDialog();
    }

    public void updateBySymbol(Intent data) {
        Intent intent = data == null ? view.getIntent() : data;
        sendChoose = intent.getStringExtra("symbol");
        if (sendChoose == null) {
            sendChoose = (String) SPUtils.get(context, "send_choose", "ETH");
        }
        updateTransactionFeeUI();
        BalanceResult.Asset asset = balanceManager.getAssetBySymbol(sendChoose);
        setWalletImage(sendChoose);
        view.sendWalletName.setText(sendChoose);
        view.walletName2.setText(sendChoose);
        amountTotal = asset.getValue();
        view.sendWalletCount.setText(asset.getValueShown() + " " + sendChoose);
    }

    public String getSendChoose() {
        return sendChoose;
    }

    private void setWalletImage(String symbol) {
        Token token = tokenDataManager.getTokenBySymbol(symbol);
        if (token.getImageResId() != 0) {
            view.walletSymbol.setVisibility(View.GONE);
            view.walletImage.setImageResource(token.getImageResId());
            view.walletImage.setVisibility(View.VISIBLE);
        } else {
            view.walletImage.setVisibility(View.GONE);
            view.walletSymbol.setText(symbol);
            view.walletSymbol.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 金额拖动条
     */
    public void initSeekbar() {
        view.moneyAmount.post(() -> view.moneyAmount.setText(""));
        view.seekBar.setProgress(0);
        view.seekBar.setCustomSectionTextArray((sectionCount, array) -> {
            array.clear();
            array.put(0, "0%");
            array.put(1, "25%");
            array.put(2, "50%");
            array.put(3, "75%");
            array.put(4, "100%");
            return array;
        });
        view.seekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                if (moneyAmountChange) {
                    moneyAmountChange = false;
                    return;
                }
                int precision = balanceManager.getPrecisionBySymbol(sendChoose);
                view.moneyAmount.setText(NumberUtils.format1(balanceManager.getAssetBySymbol(sendChoose)
                        .getValue() * progressFloat / 100, precision));
                view.amountToast.setText("≈" + CurrencyUtil.format(context, balanceManager.getAssetBySymbol(sendChoose)
                        .getLegalValue() * progressFloat / 100));
                Selection.setSelection(view.moneyAmount.getText(), view.moneyAmount.getText().length());
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
    public void initMoneyAmount() {
        view.amountToast.setText("");
        view.amountToast.setTextColor(view.getResources().getColor(R.color.colorNineText));
        view.moneyAmount.addTextChangedListener(new TextWatcher() {
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
                if (editable == null || editable.toString().isEmpty()) {
                    view.amountToast.setText("");
                    view.seekBar.setProgress(0);
                } else if (currentAmount > amountTotal) {
                    view.amountToast.setText(view.getResources()
                            .getString(R.string.available_balance, view.sendWalletCount.getText()));
                    view.amountToast.setTextColor(view.getResources().getColor(R.color.colorRed));
                    view.amountToast.startAnimation(view.shakeAnimation);
                    view.seekBar.setProgress(100);
                } else {
                    view.amountToast.setText("≈" + CurrencyUtil.format(context, currentAmount * marketcapDataManager
                            .getPriceBySymbol(sendChoose)));
                    view.amountToast.setTextColor(view.getResources().getColor(R.color.colorNineText));
                    view.seekBar.setProgress((float) (amountTotal != 0 ? currentAmount / amountTotal * 100 : 0));
                }
            }
        });
    }

    /**
     * 钱包地址实时验证
     */
    public void initWalletAddress() {
        String sendAddress = view.getIntent().getStringExtra("send_address");
        if (sendAddress != null && !sendAddress.isEmpty()) {
            view.walletAddress.setText(view.getIntent().getStringExtra("send_address"));
            showKeyboard(view.moneyAmount, true);
        }
        view.walletAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable == null || editable.toString().isEmpty()) {
                    view.addressToast.setTextColor(view.getResources().getColor(R.color.colorNineText));
                    view.addressToast.setText(view.getResources().getText(R.string.address_confirm));
                    view.addressToast.setVisibility(View.VISIBLE);
                } else if (!CredentialsUtils.isValidAddress(editable.toString().trim())) {
                    view.addressToast.setTextColor(view.getResources().getColor(R.color.colorRed));
                    view.addressToast.setText(view.getResources().getText(R.string.input_valid_address));
                    view.addressToast.startAnimation(view.shakeAnimation);
                    view.addressToast.setVisibility(View.VISIBLE);
                } else if (CredentialsUtils.isHexAddress(editable.toString().trim())) {
                    view.addressToast.setVisibility(View.INVISIBLE);
                } else {
                    view.addressToast.setTextColor(view.getResources().getColor(R.color.colorNineText));
                    view.addressToast.setText(CredentialsUtils.getENSAddress(editable.toString().trim()));
                    view.addressToast.setVisibility(View.VISIBLE);
                }
                setSearchContacts(editable.toString());
            }
        });
    }

    /**
     * 设置联系人检索提示
     *
     * @param content
     */
    public void setSearchContacts(String content) {
        List<Contact> contacts = loginDataManager.searchContacts(content);
        if (contacts.size() == 0) {
            view.llSearchContents.setVisibility(View.GONE);
        } else {
            ((ContactSearchAdapter) view.rvSearchContacts.getAdapter()).setNewData(contacts);
            view.llSearchContents.setVisibility(View.VISIBLE);
            if (contacts.size() * perHeight > maxHeight) {
                view.rvSearchContacts.getLayoutParams().height = maxHeight;
                view.llSearchContents.getLayoutParams().height = maxHeight + DpUtil.dp2Int(context, 2);
            } else {
                view.llSearchContents.getLayoutParams().height = contacts.size() * perHeight + DpUtil.dp2Int(context, 2);
                if (view.llSearchContents.getLayoutParams().height < DpUtil.dp2Int(context, 80)) {
                    view.llSearchContents.getLayoutParams().height = DpUtil.dp2Int(context, 80);
                }
            }
            ((ContactSearchAdapter) view.rvSearchContacts.getAdapter()).setOnItemClickListener((adapter, view, position) -> {
                this.view.walletAddress.setText(contacts.get(position).getAddress());
                this.view.llSearchContents.setVisibility(View.GONE);
            });
        }
    }

    public boolean clickInView(Point point, View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        if (point.x >= location[0] && point.x <= location[0] + view.getWidth() &&
                point.y >= location[1] && point.y <= location[1] + view.getHeight())
            return true;
        return false;
    }

    private void showKeyboard(View view, boolean show) {
        this.view.getWindow().getDecorView().postDelayed(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) this.view.getSystemService(Context.INPUT_METHOD_SERVICE);
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
