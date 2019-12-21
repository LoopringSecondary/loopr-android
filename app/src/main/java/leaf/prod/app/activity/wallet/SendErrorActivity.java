package leaf.prod.app.activity.wallet;

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

public class SendErrorActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.gas_fee)
    TextView gasFee;

    @BindView(R.id.gas_layout)
    ConstraintLayout gasLayout;

    @BindView(R.id.send_error_text)
    TextView sendErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_send_error);
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
    }

    @Override
    public void initData() {
        if (!StringUtils.isEmpty(getIntent().getStringExtra("tokenAmount"))) {
            gasFee.setText(getIntent().getStringExtra("tokenAmount"));
            gasLayout.setVisibility(View.VISIBLE);
        } else if (!StringUtils.isEmpty(getIntent().getStringExtra("error"))) {
            sendErrorText.setText(getIntent().getStringExtra("error"));
            gasLayout.setVisibility(View.INVISIBLE);
        } else {
            gasLayout.setVisibility(View.INVISIBLE);
        }
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
