package leaf.prod.app.presenter.trade;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.vondear.rxtool.view.RxToast;

import leaf.prod.app.R;
import leaf.prod.app.activity.trade.FlutterP2PRecordDetailActivity;
import leaf.prod.app.presenter.BasePresenter;
import leaf.prod.app.utils.QRCodeUitl;
import leaf.prod.app.utils.ShareUtil;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-12-07 11:57 AM
 * Cooperation: loopring.org 路印协议基金会
 */
public class P2PRecordDetailPresenter extends BasePresenter<FlutterP2PRecordDetailActivity> {

    private AlertDialog shareDialog;

    private View dialogView;

    public P2PRecordDetailPresenter(FlutterP2PRecordDetailActivity view, Context context) {
        super(view, context);
    }

    public void showShareDialog(String qrCode) {
        if (shareDialog == null) {
            final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context, R.style.DialogTheme);
            dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_p2p_share, null);
            Bitmap bitmap = QRCodeUitl.createQRCodeBitmap(qrCode, 300);
            ((ImageView) dialogView.findViewById(R.id.iv_qr_code)).setImageBitmap(bitmap);
            view.qrCodeImage.setImageBitmap(bitmap);
            dialogView.findViewById(R.id.tv_save).setOnClickListener(v -> {
                if (ShareUtil.saveChart(ShareUtil.getBitmap(view.shareView), view.shareView.getMeasuredHeight(), view.shareView
                        .getMeasuredWidth())) {
                    RxToast.success(view.getResources().getString(R.string.save_pic_success));
                }
            });
            dialogView.findViewById(R.id.btn_share).setOnClickListener(v -> {
                shareDialog.hide();
                ShareUtil.uShareImage(view, view.getResources()
                        .getString(R.string.share_order), ShareUtil.getBitmap(view.shareView));
            });
            builder.setView(dialogView);
            builder.setCancelable(true);
            shareDialog = builder.create();
            shareDialog.setCancelable(true);
            shareDialog.setCanceledOnTouchOutside(true);
            shareDialog.getWindow().setGravity(Gravity.CENTER);
        }
        shareDialog.show();
    }
}
