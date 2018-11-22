package leaf.prod.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import leaf.prod.app.R;
import leaf.prod.app.adapter.MnemonicWordAdapter;
import leaf.prod.app.utils.ToastUtils;
import leaf.prod.app.views.SpacesItemDecoration;
import leaf.prod.app.views.TitleView;

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
                    hideProgress();
                    ToastUtils.toast(getResources().getString(R.string.local_file_error));
                    break;
                case ERROR_TWO:
                    hideProgress();
                    ToastUtils.toast(getResources().getString(R.string.local_file_error));
                    break;
            }
        }
    };

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
        mnemonic = getIntent().getStringExtra("mnemonic");
    }

    @Override
    public void initData() {
        String[] arrayMne = mnemonic.split(" ");
        listMnemonic.clear();
        for (int i = 0; i < arrayMne.length; i++) {
            listMnemonic.add(arrayMne[i]);
        }
        //        new Thread(new Runnable() {
        //            @Override
        //            public void run() {
        //                try {
        //                    String[] arrayMne = FileUtils.getFile(BackupMnemonicActivity.this,"mnemonic.txt");
        //                    listMnemonic.clear();
        //                    for (int i = 0; i < arrayMne.length; i++) {
        //                        listMnemonic.add(arrayMne[i]);
        //                    }
        //                    handlerCreate.sendEmptyMessage(MNEMONIC_SUCCESS);
        //                } catch (IOException e) {
        //                    handlerCreate.sendEmptyMessage(ERROR_ONE);
        //                    e.printStackTrace();
        //                } catch (JSONException e) {
        //                    handlerCreate.sendEmptyMessage(ERROR_TWO);
        //                    e.printStackTrace();
        //                }
        //            }
        //        }).start();
        GridLayoutManager layoutManagerHint = new GridLayoutManager(this, 3);
        recyclerMnemonicHint.setLayoutManager(layoutManagerHint);  //助记词提示列表
        mHintAdapter = new MnemonicWordAdapter(R.layout.adapter_item_mnemonic_word_hint, listMnemonic);
        recyclerMnemonicHint.addItemDecoration(new SpacesItemDecoration(8, 8, 2, 2));
        recyclerMnemonicHint.setAdapter(mHintAdapter);
    }
}
