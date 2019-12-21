/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-12 11:45 AM
 * Cooperation: loopring.org 路印协议基金会
 */
package leaf.prod.app.presenter.wallet;

import java.io.IOException;
import java.math.BigInteger;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.web3j.crypto.Credentials;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;
import leaf.prod.app.activity.wallet.AuthorityWebActivity;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.walletsdk.SDK;
import leaf.prod.walletsdk.model.CancelOrder;
import leaf.prod.walletsdk.model.QRCodeType;
import leaf.prod.walletsdk.model.ScanWebQRCode;
import leaf.prod.walletsdk.model.SignStatus;
import leaf.prod.walletsdk.model.request.relayParam.NotifyScanParam;
import leaf.prod.walletsdk.model.request.relayParam.NotifyStatusParam;
import leaf.prod.walletsdk.service.LoopringService;
import leaf.prod.walletsdk.util.SignUtils;
import leaf.prod.walletsdk.util.WalletUtil;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AuthorityWebPresenter extends BasePresenter<AuthorityWebActivity> {

    private static LoopringService loopringService = new LoopringService();

    private static Gson gson = new Gson();

    private AlertDialog passwordDialog;

    private String value;

    private QRCodeType type;

    private Credentials credentials;

    private String owner;

    public AuthorityWebPresenter(AuthorityWebActivity view, Context context, String info, QRCodeType type) {
        super(view, context);
        this.type = type;
        this.owner = WalletUtil.getCurrentAddress(context);
        this.value = gson.fromJson(info, ScanWebQRCode.class).getValue();
    }

    public void showPasswordDialog() {
        if (passwordDialog == null) {
            final AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogTheme);
            View passwordView = LayoutInflater.from(context).inflate(R.layout.dialog_put_password, null);
            builder.setView(passwordView);
            final EditText passwordInput = passwordView.findViewById(R.id.password_input);
            passwordView.findViewById(R.id.cancel).setOnClickListener(v -> passwordDialog.dismiss());
            passwordView.findViewById(R.id.confirm).setOnClickListener(v -> {
                try {
                    credentials = WalletUtil.getCredential(context, passwordInput.getText().toString());
                    handle(credentials);
                } catch (Exception e) {
                    passwordInput.setText("");
                    RxToast.error(context.getResources().getString(R.string.authority_login_error));
                }
            });
            builder.setCancelable(true);
            passwordDialog = builder.create();
            passwordDialog.setCancelable(true);
            passwordDialog.setCanceledOnTouchOutside(true);
        }
        passwordDialog.show();
    }

    public void handle(Credentials credentials) {
        this.credentials = credentials;
        switch (type) {
            case LOGIN:
                handleLogin();
                break;
            case APPROVE:
                handleApprove();
                break;
            case CONVERT:
                handleConvert();
                break;
            case ORDER:
                handleOrder();
                break;
            case CANCEL_ORDER:
                handleCancel();
                break;
        }
    }

    private NotifyStatusParam.NotifyBody getStatus(SignStatus status) {
        return NotifyStatusParam.NotifyBody.builder()
                .hash(value)
                .status(status.name())
                .build();
    }

    private NotifyScanParam.SignParam getSignParam() {
        return SignUtils.genSignParam(credentials, owner);
    }

    private void handleApprove() {
    }

    private void handleConvert() {
        if (value != null) {
            NotifyStatusParam.NotifyBody received = getStatus(SignStatus.received);
            loopringService.notifyStatus(received, owner)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.getSignMessage(value))
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) rawTx -> {
                        signAndSendRawTx(rawTx);
                        return loopringService.notifyStatus(getStatus(SignStatus.accept), owner);
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        }
    }

    private void signAndSendRawTx(String rawTx) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(rawTx).getAsJsonObject().getAsJsonObject("tx");
            BigInteger gasPrice = Numeric.toBigInt(json.get("gasPrice").getAsString());
            BigInteger gasLimit = Numeric.toBigInt(json.get("gasLimit").getAsString());
            String to = json.get("to").getAsString();
            String data = json.get("data").getAsString();
            BigInteger value = Numeric.toBigInt(json.get("value").getAsString());
            RawTransactionManager transactionManager = new RawTransactionManager(SDK.getWeb3j(), credentials, SDK.CHAIN_ID);
            transactionManager.sendTransaction(gasPrice, gasLimit, to, data, value);
        } catch (IOException e) {
            if (passwordDialog != null) {
                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
            }
            RxToast.error(context.getResources().getString(R.string.authority_login_error));
        }
    }

    private void handleOrder() {
        if (value != null) {
            NotifyStatusParam.NotifyBody received = getStatus(SignStatus.received);
            loopringService.notifyStatus(received, owner)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.getSignMessage(value))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) rawTx -> {
                        signAndSubmitOrder(rawTx);
                        return loopringService.notifyStatus(getStatus(SignStatus.accept), owner);
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        }
    }

    private void signAndSubmitOrder(String order) {
        JsonParser parser = new JsonParser();
        JsonArray jsonArray = parser.parse(order).getAsJsonArray();
        for (JsonElement subJson : jsonArray) {
            String type = subJson.getAsJsonObject().get("type").getAsString();
            JsonObject data = subJson.getAsJsonObject().get("data").getAsJsonObject();
            if (type.equals("order")) {
                System.out.println(111);
            } else {
                System.out.println(111);
            }
        }
    }

    private void handleCancel() {
        if (value != null) {
            NotifyStatusParam.NotifyBody received = getStatus(SignStatus.received);
            loopringService.notifyStatus(received, owner)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.getSignMessage(value))
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) this::signAndCancel)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Func1<String, Observable<String>>) result -> loopringService.notifyStatus(getStatus(SignStatus.accept), owner))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        }
    }

    private Observable<String> signAndCancel(String cancel) {
        CancelOrder cancelOrder = gson.fromJson(cancel, CancelOrder.class);
        if (cancelOrder.isValid()) {
            return loopringService.cancelOrderFlex(cancelOrder, getSignParam());
        }
        return null;
    }

    public void handleLogin() {
        try {
            loopringService.notifyScanLogin(getSignParam(), owner, value)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<String>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (passwordDialog != null) {
                                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
                            }
                            RxToast.error(context.getResources().getString(R.string.authority_login_error));
                        }

                        @Override
                        public void onNext(String s) {
                            RxToast.success(context.getResources().getString(R.string.authority_login_success));
                            view.finish();
                            view.getOperation().forward(MainActivity.class);
                        }
                    });
        } catch (Exception e) {
            if (passwordDialog != null) {
                ((TextView) passwordDialog.findViewById(R.id.password_input)).setText("");
            }
            RxToast.error(context.getResources().getString(R.string.authority_login_error));
        }
    }
}
