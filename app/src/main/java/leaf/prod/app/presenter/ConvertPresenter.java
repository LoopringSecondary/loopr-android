/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-14 3:30 PM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.web3j.crypto.Credentials;
import com.vondear.rxtool.view.RxToast;
import com.xw.repo.BubbleSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.ConvertActivity;
import leaf.prod.app.activity.SendErrorActivity;
import leaf.prod.app.activity.SendSuccessActivity;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.GasDataManager;
import leaf.prod.walletsdk.manager.MarketcapDataManager;
import leaf.prod.walletsdk.util.CurrencyUtil;
import leaf.prod.walletsdk.util.NumberUtils;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import leaf.prod.walletsdk.Transfer;
import leaf.prod.walletsdk.exception.IllegalCredentialException;
import leaf.prod.walletsdk.exception.InvalidKeystoreException;
import leaf.prod.walletsdk.model.response.data.BalanceResult;
import leaf.prod.walletsdk.util.UnitConverter;

public class ConvertPresenter extends BasePresenter<ConvertActivity> {

    @BindView(R.id.first_token_img1)
    ImageView firstTokenImg1;

    @BindView(R.id.first_token_img2)
    ImageView firstTokenImg2;

    @BindView(R.id.second_token_img1)
    ImageView secondTokenImg1;

    @BindView(R.id.second_token_img2)
    ImageView secondTokenImg2;

    @BindView(R.id.first_token_tx)
    TextView firstTokenTx;

    @BindView(R.id.second_token_tx)
    TextView secondTokenTx;

    @BindView(R.id.transaction_fee)
    TextView transactionFee;

    @BindView(R.id.hint_text)
    TextView hint;

    @BindView(R.id.first_val)
    TextView firstVal;

    @BindView(R.id.second_val)
    TextView secondVal;

    @BindView(R.id.tip)
    TextView tipTxt;

    private String firstToken = "ETH";

    private AlertDialog feeDialog;

    private AlertDialog passwordDialog;

    private TextView tvAmount;

    private TextView tvWalletInfo;

    private ImageView cancel;

    private TextView recommendGas;

    private BubbleSeekBar gasSeekBar;

    private Double gasEthValue;

    private GasDataManager gasDataManager;

    private MarketcapDataManager marketcapDataManager;

    private BalanceDataManager balanceDataManager;

    private Animation shakeAnimation;

    public ConvertPresenter(ConvertActivity view, Context context) {
        super(view, context);
        ButterKnife.bind(this, view);
        gasDataManager = GasDataManager.getInstance(context);
        marketcapDataManager = MarketcapDataManager.getInstance(context);
        balanceDataManager = BalanceDataManager.getInstance(context);
        shakeAnimation = AnimationUtils.loadAnimation(context, R.anim.shake_x);
        setHint(0);
    }

    /**
     * token切换
     */
    public void switchToken() {
        if (firstToken.equals("ETH")) {
            firstTokenImg1.animate().alpha(0f).setDuration(300);
            firstTokenImg2.animate().alpha(1f).setDuration(300);
            secondTokenImg1.animate().alpha(0f).setDuration(300);
            secondTokenImg2.animate().alpha(1f).setDuration(300);
            firstTokenImg2.bringToFront();
            secondTokenImg2.bringToFront();
            firstTokenTx.setText("WETH");
            secondTokenTx.setText("ETH");
            tipTxt.setVisibility(View.INVISIBLE);
            firstToken = "WETH";
        } else {
            firstTokenImg1.animate().alpha(1f).setDuration(300);
            firstTokenImg2.animate().alpha(0f).setDuration(300);
            secondTokenImg1.animate().alpha(1f).setDuration(300);
            secondTokenImg2.animate().alpha(0f).setDuration(300);
            firstTokenImg1.bringToFront();
            secondTokenImg2.bringToFront();
            firstTokenTx.setText("ETH");
            secondTokenTx.setText("WETH");
            tipTxt.setVisibility(View.VISIBLE);
            firstToken = "ETH";
        }
        firstVal.setText("0");
        secondVal.setText("0");
        setHint(0);
    }

    /**
     * 油费选择弹窗
     */
    public void showFeeDialog() {
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
                            .doubleValue(), 1))).build();
            gasSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
                @Override
                public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                    gasDataManager.setCustomizeGasPriceInGWei((double) progressFloat);
                    gasEthValue = Double.parseDouble(gasDataManager.getGasAmountInETH(String.valueOf(gasDataManager.getGasLimitByType("token_transfer")), String
                            .valueOf(gasDataManager.getCustomizeGasPriceInWei())));
                    tvAmount.setText(new StringBuilder(gasEthValue.toString()).append(" ETH ≈ ")
                            .append(CurrencyUtil.format(view.getContext(), gasEthValue * marketcapDataManager.getPriceBySymbol("ETH"))));
                    tvWalletInfo.setText(new StringBuilder("Gas limit(").append(gasDataManager.getGasLimitByType("token_transfer"))
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
            feeDialog.setOnDismissListener(dialogInterface -> transactionFee.setText(tvAmount.getText()));
            Objects.requireNonNull(feeDialog.getWindow()).setGravity(Gravity.BOTTOM);
        }
        gasSeekBar.setProgress((float) gasDataManager.getGasPriceInGwei());
        tvAmount.setText(transactionFee.getText());
        tvWalletInfo.setText(new StringBuilder("Gas limit(").append(gasDataManager.getGasLimitByType("token_transfer"))
                .append(") * Gas Price(")
                .append((int) gasDataManager.getGasPriceInGwei())
                .append(" Gwei)"));
        feeDialog.show();
    }

    /**
     * 密码弹框
     */
    public void showPasswordDialog() {
        if (passwordDialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.DialogTheme);//
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_put_password, null);
            builder.setView(view);
            final EditText passwordInput = view.findViewById(R.id.password_input);
            view.findViewById(R.id.cancel).setOnClickListener(v -> passwordDialog.dismiss());
            view.findViewById(R.id.confirm).setOnClickListener(v -> {
                if (TextUtils.isEmpty(passwordInput.getText().toString())) {
                    ToastUtils.toast(view.getResources().getString(R.string.put_password));
                } else {
                    convert(passwordInput.getText().toString());
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

    /**
     * 设置最大交换额度
     */
    public void setMax() {
        balanceDataManager.getAssetBySymbol(firstToken);
        firstVal.setText(getMaxShow());
        secondVal.setText(firstVal.getText());
    }

    /**
     * 获得最大值
     *
     * @return
     */
    public double getMaxValue() {
        BalanceResult.Asset firstAsset = balanceDataManager.getAssetBySymbol(firstToken);
        return firstToken.equals("ETH") ? firstAsset.getValue() - 0.01 : firstAsset.getValue();
    }

    public String getMaxShow() {
        if (firstToken.equals("ETH")) {
            int precision = balanceDataManager.getPrecisionBySymbol("ETH");
            return NumberUtils.format1(getMaxValue(), precision);
        } else {
            return balanceDataManager.getAssetBySymbol(firstToken).getValueShown();
        }
    }

    /**
     * 交换
     */
    public void convert(String password) {
        view.showProgress(view.getResources().getString(R.string.loading_default_messsage));
        new Thread(() -> {
            try {
                BigInteger gasLimit = gasDataManager.getGasLimitByType(firstToken.equals("ETH") ? "deposit" : "withdraw");
                BigInteger gasPrice = gasDataManager.getCustomizeGasPriceInWei().toBigInteger();
                BigInteger values = UnitConverter.ethToWei(firstVal.getText().toString()); //转账金额
                Credentials credentials = WalletUtil.getCredential(context, password);
                Transfer transfer = new Transfer(credentials);
                if (firstToken.equals("ETH")) {
                    transfer.eth(gasPrice, gasLimit)
                            .deposit(credentials, WalletUtil.getCurrentAddress(context), values);
                } else {
                    transfer.eth(gasPrice, gasLimit)
                            .withDraw(credentials, WalletUtil.getCurrentAddress(context), values);
                }
                handlerCreate.sendEmptyMessage(0);
            } catch (InvalidKeystoreException | IOException | JSONException | IllegalCredentialException e) {
                handlerCreate.sendEmptyMessage(1);
            } catch (Exception e) {
                handlerCreate.sendEmptyMessage(2);
            }
        }).start();
    }

    public void setHint(int flag) {
        switch (flag) {
            case 0:
                hint.setText(view.getResources()
                        .getString(R.string.available_balance, " " + getMaxShow() + " " + firstToken));
                hint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                break;
            case 1:
                hint.setText(view.getResources()
                        .getString(R.string.available_balance, " " + getMaxShow() + " " + firstToken));
                hint.setTextColor(view.getResources().getColor(R.color.colorRed));
                hint.startAnimation(shakeAnimation);
                break;
            case 2:
                hint.setText(view.getResources().getString(R.string.input_valid_amount));
                hint.setTextColor(view.getResources().getColor(R.color.colorRed));
                hint.startAnimation(shakeAnimation);
                break;
            case 3:
                hint.setText(CurrencyUtil.format(context, marketcapDataManager.getPriceBySymbol(firstToken) *
                        Double.parseDouble(firstVal.getText().toString())));
                hint.setTextColor(view.getResources().getColor(R.color.colorNineText));
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            view.hideProgress();
            switch (msg.what) {
                case 0:
                    // 成功
                    view.getOperation().addParameter("tokenAmount", "-" + firstVal.getText() + " " + firstToken);
                    view.getOperation().forwardClearTop(SendSuccessActivity.class);
                    if (passwordDialog != null && passwordDialog.isShowing()) {
                        passwordDialog.dismiss();
                    }
                    break;
                case 1:
                    // 密码错误
                    RxToast.error(view.getResources().getString(R.string.keystore_psw_error));
                    break;
                case 2:
                    // 转账错误
                    view.getOperation().addParameter("error", view.getResources().getString(R.string.transfer_error));
                    view.getOperation().forwardClearTop(SendErrorActivity.class);
                    break;
            }
        }
    };
}
