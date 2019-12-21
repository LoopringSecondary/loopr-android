package leaf.prod.app.utils;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import leaf.prod.app.R;

public class DialogUtil {

    public static AlertDialog dialog;

    private Context mContext;

    public DialogUtil(Context context) {
        mContext = context;
    }

    /**
     * 创建钱包结果dialog
     *
     * @param context
     * @param listener
     */
    public static void showWalletCreateResultDialog(Context context, View.OnClickListener listener) {
        final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogTheme);//
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_wallet_create_success, null);
        builder.setView(view);
        TextView create_success = (TextView) view.findViewById(R.id.wallet_create_success);
        TextView ok = (TextView) view.findViewById(R.id.got_it);
        ok.setOnClickListener(listener);
        builder.setCancelable(false);
        dialog = null;
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
