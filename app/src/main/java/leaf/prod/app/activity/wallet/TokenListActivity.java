package leaf.prod.app.activity.wallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.LinearLayoutManager;
import androidx.appcompat.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.kyleduo.switchbutton.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.wallet.TokenListAdapter;
import leaf.prod.app.utils.LyqbLogger;
import leaf.prod.app.views.RecyclerViewBugLayoutManager;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.WalletEntity;
import leaf.prod.walletsdk.model.response.relay.Token;
import leaf.prod.walletsdk.util.WalletUtil;

public class TokenListActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.et_search)
    EditText etSearch;

    @BindView(R.id.cancel_text)
    TextView cancelText;

    @BindView(R.id.ll_search)
    LinearLayout llSearch;

    List<String> chooseToken;

    private TokenListAdapter mAdapter;

    private List<Token> list;

    private List<Token> listSearch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_token_list);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.tokens));
        title.clickLeftGoBack(getWContext());
        title.setMiddleImageButton(R.mipmap.icon_plus, button -> getOperation().forward(AddCustomTokenActivity.class));
        title.setRightImageButton(R.mipmap.icon_search, button -> llSearch.setVisibility(View.VISIBLE));
        title.setLeftButton(button -> {
            WalletEntity wallet = WalletUtil.getCurrentWallet(TokenListActivity.this);
            wallet.setChooseTokenList(chooseToken);
            WalletUtil.updateWallet(TokenListActivity.this, wallet);
            finish();
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listSearch.clear();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getSymbol().contains(s.toString().toUpperCase())) {
                        listSearch.add(list.get(i));
                    }
                }
                mAdapter.setNewData(listSearch);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        chooseToken = WalletUtil.getChooseTokens(this);
        LyqbLogger.log(chooseToken.toString());
        RecyclerViewBugLayoutManager layoutManager = new RecyclerViewBugLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        list = TokenDataManager.getInstance(this).getTokens();
        mAdapter = new TokenListAdapter(R.layout.adapter_item_token_list, list, chooseToken);
        recyclerView.setAdapter(mAdapter);
        /**
         *代币选中状态
         * */
        recyclerView.setClickable(false);
        recyclerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                SwitchButton aSwitch = view.findViewById(R.id.s_v);
                String symbol = aSwitch.getTag().toString().toUpperCase();
                if (aSwitch.isChecked()) {
                    if (!Arrays.asList("ETH", "WETH", "LRC").contains(symbol)) {
                        aSwitch.toggle();
                        chooseToken.remove(symbol);
                    }
                } else {
                    if (!Arrays.asList("ETH", "WETH", "LRC").contains(symbol)) {
                        aSwitch.toggle();
                        chooseToken.add(symbol);
                    }
                }
                mAdapter.setChooseToken(chooseToken);
            }
        });
    }

    @OnClick({R.id.cancel_text})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_text:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                llSearch.setVisibility(View.GONE);
                etSearch.setText("");
                mAdapter.setNewData(list);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
}
