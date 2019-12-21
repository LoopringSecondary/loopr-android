package leaf.prod.app.activity.setting;

import java.util.List;

import android.os.Bundle;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.setupWallet.GenerateWalletActivity;
import leaf.prod.app.activity.setupWallet.ImportWalletActivity;
import leaf.prod.app.adapter.setting.ManageWalletListAdapter;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.walletsdk.util.WalletUtil;

public class ManageWalletActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.btn_import)
    Button btnImport;

    @BindView(R.id.btn_generate)
    Button btnGenerate;

    private ManageWalletListAdapter mAdapter;

    private List<WalletEntity> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_manage_wallet);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.set_manage_wallet));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new ManageWalletListAdapter(R.layout.adapter_item_manage_wallet, list);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            getOperation().addParameter("selectedWallet", list.get(position));
            getOperation().forward(WalletSafeActivity.class);
        });
    }

    @OnClick({R.id.btn_import, R.id.btn_generate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_import:
                getOperation().forward(ImportWalletActivity.class);
                break;
            case R.id.btn_generate:
                getOperation().forward(GenerateWalletActivity.class);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        list = WalletUtil.getWalletList(ManageWalletActivity.this);
        mAdapter.setNewData(list);
    }

    @Override
    protected void initPresenter() {
    }
}
