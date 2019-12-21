package leaf.prod.app.activity.wallet;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
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
import leaf.prod.walletsdk.model.NoDataType;
import leaf.prod.walletsdk.model.response.relay.Token;
import leaf.prod.walletsdk.util.SPUtils;

public class SendListChooseActivity extends BaseActivity {

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

    private List<Token> list;

    private List<Token> listSearch = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_send_list);
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
                    SPUtils.put(SendListChooseActivity.this, "send_choose", symbol);
                    Intent intent = new Intent();
                    intent.putExtra("symbol", symbol);
                    setResult(1, intent);
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
        list = BalanceDataManager.getInstance(this).getBalanceTokens();
        if (list.isEmpty()) {
            emptyAdapter = new NoDataAdapter(R.layout.adapter_item_no_data, null, NoDataType.asset);
            recyclerView.setAdapter(emptyAdapter);
            emptyAdapter.refresh();
            title.hideRightImageButton();
        } else {
            mAdapter = new TokenChooseAdapter(R.layout.adapter_item_token_choose, list);
            recyclerView.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener((adapter, view, position) -> {
                Intent intent = new Intent();
                intent.putExtra("symbol", list.get(position).getSymbol());
                setResult(1, intent);
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
                mAdapter.setNewData(list);
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
}
