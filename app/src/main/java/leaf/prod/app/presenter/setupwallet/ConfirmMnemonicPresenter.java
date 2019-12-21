package leaf.prod.app.presenter.setupwallet;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;

import com.google.common.base.Joiner;
import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;
import leaf.prod.app.activity.setupWallet.ConfirmMnemonicActivity;
import leaf.prod.app.presenter.BasePresenter;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-11 7:45 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class ConfirmMnemonicPresenter extends BasePresenter<ConfirmMnemonicActivity> {

    public ConfirmMnemonicPresenter(ConfirmMnemonicActivity view, Context context) {
        super(view, context);
    }

    /**
     * 助记词匹配
     */
    public void matchMnemonic(List<String> mneCheckedList, String mnemonic) {
        String str = Joiner.on(" ").join(mneCheckedList);
        if (str.trim().equals(mnemonic)) {
            AlertDialog.Builder updateDialog = new AlertDialog.Builder(context);
            updateDialog.setPositiveButton(context.getResources()
                    .getString(R.string.confirm), (dialogInterface, i0) -> {
                dialogInterface.dismiss();
                setResult();
            });
            updateDialog.setMessage(view.getResources().getString(R.string.mnemonic_backup_success));
            updateDialog.show();
        } else {
            RxToast.error(view.getResources().getString(R.string.mnemonic_not_match));
        }
    }

    public void setResult() {
        Intent intent = new Intent();
        intent.putExtra("result", true);
        view.setResult(Activity.RESULT_OK, intent);
        view.finish();
    }
}
