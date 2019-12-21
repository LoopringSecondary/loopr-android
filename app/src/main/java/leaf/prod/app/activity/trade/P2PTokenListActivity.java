package leaf.prod.app.activity.trade;

import java.util.ArrayList;
import java.util.Iterator;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import leaf.prod.app.R;
import leaf.prod.app.activity.BaseActivity;
import leaf.prod.app.adapter.NoDataAdapter;
import leaf.prod.app.adapter.wallet.TokenChooseAdapter;
import leaf.prod.app.views.TitleView;
import leaf.prod.walletsdk.manager.BalanceDataManager;
import leaf.prod.walletsdk.manager.P2POrderDataManager;
import leaf.prod.walletsdk.manager.TokenDataManager;
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.TradeType;
import leaf.prod.walletsdk.model.response.relay.BalanceResult;
import leaf.prod.walletsdk.model.response.relay.Token;

public class P2PTokenListActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;

    @BindView(R.id.et_search)
    EditText etSearch;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.cancel_text)
    TextView cancelText;

    @BindView(R.id.ll_search)
    LinearLayout llSearch;

    private TokenChooseAdapter mAdapter;

    private NoDataAdapter emptyAdapter;

    private List<Token> list = new ArrayList<>();

    private List<Token> listSearch = new ArrayList<>();

    private P2POrderDataManager p2pOrderManager;

    private BalanceDataManager balanceDataManager;

    private TradeType type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_p2p_token_list);
        ButterKnife.bind(this);
        p2pOrderManager = P2POrderDataManager.getInstance(this);
        balanceDataManager = BalanceDataManager.getInstance(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    protected void initPresenter() {
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.tokens));
        title.clickLeftGoBack(getWContext());
        title.setRightImageButton(R.mipmap.icon_search, button -> llSearch.setVisibility(View.VISIBLE));
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
                mAdapter.setOnItemClickListener((adapter, view, position) -> {
                    String symbol = listSearch.get(position).getSymbol();
                    if (type == TradeType.buy) {
                        p2pOrderManager.changeToTokenB(symbol);
                    } else if (type == TradeType.sell) {
                        p2pOrderManager.changeToTokenS(symbol);
                    }
                    finish();
                });
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        list.addAll(TokenDataManager.getInstance(this).getERC20Tokens());
        String ignoreSymbol = getIntent().getStringExtra("ignoreSymbol");
        type = TradeType.valueOf(getIntent().getStringExtra("tokenType"));
        for (Token token : list) {
            if (token.getSymbol().equals(ignoreSymbol)) {
                list.remove(token);
                break;
            }
        }
        // sell列表只保留有余额的token
        if (type == TradeType.sell) {
            Iterator<Token> iterator = list.iterator();
            while (iterator.hasNext()) {
                Token token = iterator.next();
                BalanceResult.Asset asset = balanceDataManager.getAssetBySymbol(token.getSymbol());
                if (asset == null || asset.getValue() == 0) {
                    iterator.remove();
                }
            }
        }
        if (list.isEmpty()) {
            emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.asset);
            recyclerView.setAdapter(emptyAdapter);
            emptyAdapter.refresh();
            title.hideRightImageButton();
        } else {
            mAdapter = new TokenChooseAdapter(R.layout.adapter_item_token_choose, list);
            recyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener((adapter, view, position) -> {
                String symbol = list.get(position).getSymbol();
                if (type == TradeType.buy) {
                    p2pOrderManager.changeToTokenB(symbol);
                } else if (type == TradeType.sell) {
                    p2pOrderManager.changeToTokenS(symbol);
                }
                finish();
            });
        }
    }

    @OnClick({R.id.cancel_text})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cancel_text:
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus()
                        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                llSearch.setVisibility(View.GONE);
                etSearch.setText("");
                break;
        }
    }
}
