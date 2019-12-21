package leaf.prod.app.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import leaf.prod.app.R;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-04 10:17 AM
 * Cooperation: loopring.org 路印协议基金会
 */
public class PasswordDialogUtil {

    private static Map<String, AlertDialog> dialogMap = new HashMap<>();

    private static Map<String, Context> contextMap = new HashMap<>();

    private static String passwordTag;

    public static void showPasswordDialog(Context context, String tag, View.OnClickListener listener) {
        Context existContext = contextMap.get(passwordTag = tag);
        if (existContext == null || ((Activity) existContext).isFinishing()) {
            final androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context, R.style.DialogTheme);
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_put_password, null);
            builder.setView(view);
            view.findViewById(R.id.password_input);
            view.findViewById(R.id.cancel).setOnClickListener(v -> {
                ((EditText) view.findViewById(R.id.password_input)).setText("");
                showKeyboard(false);
                dialogMap.get(passwordTag).dismiss();
            });
            view.findViewById(R.id.confirm).setOnClickListener(listener);
            builder.setCancelable(true);
            AlertDialog passwordDialog = builder.create();
            passwordDialog.setCancelable(false);
            passwordDialog.setCanceledOnTouchOutside(false);
            dialogMap.put(passwordTag, passwordDialog);
            contextMap.put(passwordTag, context);
        } else {
            ((EditText) dialogMap.get(tag).findViewById(R.id.password_input)).setText("");
        }
        dialogMap.get(passwordTag).show();
    }

    public static String getInputPassword() {
        EditText passwordInput = dialogMap.get(passwordTag) != null ? dialogMap.get(passwordTag)
                .findViewById(R.id.password_input) : null;
        return passwordInput != null ? passwordInput.getText().toString() : "";
    }

    public static void clearPassword() {
        EditText passwordInput = dialogMap.get(passwordTag) != null ? dialogMap.get(passwordTag)
                .findViewById(R.id.password_input) : null;
        if (passwordInput != null) {
            ((EditText) dialogMap.get(passwordTag).findViewById(R.id.password_input)).setText("");
        }
    }

    public static void dismiss(String tag) {
        if (dialogMap.get(tag) != null) {
            dialogMap.get(tag).dismiss();
            dialogMap.remove(tag);
            contextMap.remove(tag);
        }
    }

    private static void showKeyboard(boolean show) {
        EditText passwordInput = dialogMap.get(passwordTag) != null ? dialogMap.get(passwordTag)
                .findViewById(R.id.password_input) : null;
        if (passwordInput != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) passwordInput.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                passwordInput.requestFocus();
                if (show)
                    inputMethodManager.showSoftInput(passwordInput, 0);
                else
                    inputMethodManager.hideSoftInputFromWindow(passwordInput.getWindowToken(), 0);
            }
        }
    }
}

