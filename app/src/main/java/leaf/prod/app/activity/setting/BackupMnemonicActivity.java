package leaf.prod.app.activity.setting;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.GridLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vondear.rxtool.view.RxToast;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.activity.setupWallet.ConfirmMnemonicActivity;
import leaf.prod.app.activity.wallet.MainActivity;
import leaf.prod.app.adapter.setupwallet.MnemonicWordAdapter;
import leaf.prod.app.views.SpacesItemDecoration;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.walletsdk.util.WalletUtil;

public class BackupMnemonicActivity extends BaseActivity {

    public final static int MNEMONIC_SUCCESS = 1;

    public final static int ERROR_ONE = 2;

    public final static int ERROR_TWO = 3;

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.mnemonic_hint)
    TextView mnemonicHint;

    @BindView(R.id.rl_mnemonic)
    RelativeLayout rlMnemonic;

    @BindView(R.id.recycler_mnemonic_hint)
    RecyclerView recyclerMnemonicHint;

    @BindView(R.id.rl_word)
    RelativeLayout rlWord;

    @BindView(R.id.btn_next)
    Button btnNext;

    private MnemonicWordAdapter mHintAdapter; //助记词提示adapter

    private List<String> listMnemonic = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    Handler handlerCreate = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MNEMONIC_SUCCESS:
                    mHintAdapter.setNewData(listMnemonic);
                    break;
                case ERROR_ONE:
                case ERROR_TWO:
                    hideProgress();
                    RxToast.error(getResources().getString(R.string.local_file_error));
                    break;
            }
        }
    };

    private WalletEntity selectedWallet;

    private String mnemonic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_backup_mnemonic);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.backup_mnemonic));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {
        //        mnemonic = EncryptUtil.decryptAES(getIntent().getStringExtra("mnemonic"), getIntent().getStringExtra("password"));
        selectedWallet = (WalletEntity) getIntent().getSerializableExtra("selectedWallet");
        mnemonic = WalletUtil.decryptMnemonic(selectedWallet.getMnemonic(), getIntent().getStringExtra("password"), selectedWallet
                .getSalt(), selectedWallet.getIv());
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(BackupMnemonicActivity.this, ConfirmMnemonicActivity.class);
            intent.putExtra("mnemonic", mnemonic);
            intent.putExtra("skip", false);
            startActivityForResult(intent, 1);
        });
    }

    @Override
    public void initData() {
        String[] arrayMne = mnemonic.split(" ");
        listMnemonic.clear();
        for (int i = 0; i < arrayMne.length; i++) {
            listMnemonic.add(arrayMne[i]);
        }
        GridLayoutManager layoutManagerHint = new GridLayoutManager(this, 3);
        recyclerMnemonicHint.setLayoutManager(layoutManagerHint);  //助记词提示列表
        mHintAdapter = new MnemonicWordAdapter(R.layout.adapter_item_mnemonic_word_hint, listMnemonic);
        recyclerMnemonicHint.addItemDecoration(new SpacesItemDecoration(8, 8, 2, 2));
        recyclerMnemonicHint.setAdapter(mHintAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data) {
            Bundle bundle = data.getExtras();
            if (bundle == null) {
                return;
            }
            Boolean result = bundle.getBoolean("result");
            if (result) {
                finish();
                getOperation().forward(MainActivity.class);
            }
        }
    }
}
