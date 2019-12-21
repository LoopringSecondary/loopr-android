package leaf.prod.app.activity.setupWallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.os.Bundle;
import androidx.appcompat.widget.GridLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.base.Joiner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.setupwallet.MnemonicWordAdapter;
import leaf.prod.app.presenter.setupwallet.ConfirmMnemonicPresenter;
import leaf.prod.app.utils.ButtonClickUtil;
import leaf.prod.app.views.SpacesItemDecoration;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.util.StringUtils;

public class ConfirmMnemonicActivity extends BaseActivity {

    @BindView(R.id.title)
    public TitleView title;

    @BindView(R.id.btn_confirm)
    public Button btnConfirm;

    @BindView(R.id.btn_skip)
    public Button btnSkip;

    @BindView(R.id.generate_partthree)
    public LinearLayout llGeneratePartthree;

    @BindView(R.id.recycler_view)
    public RecyclerView recyclerView;

    @BindView(R.id.confirm_mnemonic_word_info)
    public TextView confirmMnemonicWordInfo;

    List<String> mneCheckedList = new LinkedList<>();//选中的助记词

    private List<String> listMnemonic = new ArrayList<>();

    private MnemonicWordAdapter mAdapter;

    private String mnemonic;

    private ConfirmMnemonicPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_generate_partthree);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        llGeneratePartthree.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initPresenter() {
        presenter = new ConfirmMnemonicPresenter(this, this);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.confirm_mnemonic_word));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
        if (!getIntent().getBooleanExtra("skip", true)) {
            btnSkip.setVisibility(View.GONE);
        }
    }

    @Override
    public void initData() {
        mnemonic = getIntent().getStringExtra("mnemonic");
        if (StringUtils.isEmpty(mnemonic))
            return;
        String[] arrayMne = mnemonic.split(" ");
        listMnemonic.clear();
        listMnemonic.addAll(Arrays.asList(arrayMne));
        Collections.shuffle(listMnemonic);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);   //助记词选择列表
        mAdapter = new MnemonicWordAdapter(R.layout.adapter_item_mnemonic_word, listMnemonic);
        recyclerView.addItemDecoration(new SpacesItemDecoration(8, 8, 2, 2));
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            CheckBox checkBox = view.findViewById(R.id.mnemonic_word);
            if (checkBox.isChecked()) {
                checkBox.setChecked(false);
                mneCheckedList.remove(listMnemonic.get(position));
                confirmMnemonicWordInfo.setText(Joiner.on(" ").join(mneCheckedList));
            } else {
                checkBox.setChecked(true);
                mneCheckedList.add(listMnemonic.get(position));
                confirmMnemonicWordInfo.setText(Joiner.on(" ").join(mneCheckedList));
            }
        });
    }

    @OnClick({R.id.btn_confirm, R.id.btn_skip})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_skip:
                presenter.setResult();
                break;
            case R.id.btn_confirm:
                if (!(ButtonClickUtil.isFastDoubleClick(1))) {
                    presenter.matchMnemonic(mneCheckedList, mnemonic);
                }
                break;
        }
    }
}
