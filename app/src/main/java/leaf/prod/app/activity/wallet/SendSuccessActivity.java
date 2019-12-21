package leaf.prod.app.activity.wallet;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Bundle;
import androidx.constraintlayout.ConstraintLayout;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.util.StringUtils;

public class SendSuccessActivity extends BaseActivity {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @BindView(R.id.token_amount)
    TextView tokenAmount;

    @BindView(R.id.to_address)
    TextView toAddress;

    @BindView(R.id.time)
    TextView time;

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.send_addr_layout)
    ConstraintLayout sendAddrLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_send_success);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitleNoBar(getResources().getString(R.string.send_result));
    }

    @Override
    public void initView() {
        tokenAmount.setText(getIntent().getStringExtra("tokenAmount"));
        String address = getIntent().getStringExtra("address");
        if (StringUtils.isEmpty(address)) {
            sendAddrLayout.setVisibility(View.GONE);
        } else {
            toAddress.setText(getIntent().getStringExtra("address"));
            sendAddrLayout.setVisibility(View.VISIBLE);
        }
        time.setText(sdf.format(new Date()));
    }

    @Override
    public void initData() {
    }

    @OnClick({R.id.btn_return})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_return:
                getOperation().forward(MainActivity.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        getOperation().forward(MainActivity.class);
    }
}
