package com.tomcat360.lyqb.activity;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.tomcat360.lyqb.R;
import com.tomcat360.lyqb.adapter.ViewPageAdapter;
import com.tomcat360.lyqb.fragment.KeystoreFragment;
import com.tomcat360.lyqb.fragment.QRCodeFragment;
import com.tomcat360.lyqb.views.TitleView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExportKeystoreDetailActivity extends BaseActivity {

    @BindView(R.id.title)
    TitleView title;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    private List<Fragment> mFragments;
    private String[] mTitles = new String[2];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_export_keystore_detail);
        ButterKnife.bind(this);
        super.onCreate(savedInstanceState);
        mSwipeBackLayout.setEnableGesture(false);
    }

    @Override
    public void initTitle() {
        title.setBTitle(getResources().getString(R.string.export_keystore));
        title.clickLeftGoBack(getWContext());
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        mTitles[0] = getResources().getString(R.string.keystore);
        mTitles[1] = getResources().getString(R.string.qr_code);

        mFragments = new ArrayList<>();
        mFragments.add(new KeystoreFragment());
        mFragments.add(new QRCodeFragment());

        viewPager.setAdapter(new ViewPageAdapter(getSupportFragmentManager(), mFragments, mTitles));

        tabLayout.setupWithViewPager(viewPager);
    }
}