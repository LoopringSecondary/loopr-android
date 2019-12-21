package leaf.prod.app.presenter.trade;

import android.content.Context;
import androidx.core.app.Fragment;
import androidx.core.app.FragmentManager;
import androidx.core.app.FragmentTransaction;

import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.trade.P2PActivity;
import leaf.prod.app.fragment.trade.P2PRecordsFragment;
import leaf.prod.app.fragment.trade.P2PTradeFragment;
import leaf.prod.app.presenter.BasePresenter;

/**
 * Created with IntelliJ IDEA.
 * User: laiyanyan
 * Time: 2018-11-29 2:42 PM
 * Cooperation: loopring.org 路印协议基金会
 */
public class P2PPresenter extends BasePresenter<P2PActivity> {

    private Fragment tradeFragment, recordsFragment;

    public P2PPresenter(P2PActivity view, Context context) {
        super(view, context);
        ButterKnife.bind(this, view);
    }

    public void setTabSelect(int index) {
        FragmentManager manager = view.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        switch (index) {
            case 0:
                tradeFragment = manager.findFragmentByTag("trade_item");
                hideTab(transaction);
                if (tradeFragment == null) {
                    tradeFragment = new P2PTradeFragment();
                    transaction.add(R.id.main_frame, tradeFragment, "trade_item");
                } else {
                    transaction.show(tradeFragment);
                }
                break;
            case 1:
                recordsFragment = manager.findFragmentByTag("records_item");
                hideTab(transaction);
                if (recordsFragment == null) {
                    recordsFragment = new P2PRecordsFragment();
                    transaction.add(R.id.main_frame, recordsFragment, "records_item");
                } else {
                    transaction.show(recordsFragment);
                }
                break;
        }
        transaction.commitAllowingStateLoss();
    }

    private void hideTab(FragmentTransaction transaction) {
        if (tradeFragment != null) {
            transaction.hide(tradeFragment);
        }
        if (recordsFragment != null) {
            transaction.hide(recordsFragment);
        }
    }
}
