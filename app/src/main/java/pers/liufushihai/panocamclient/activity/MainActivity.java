package pers.liufushihai.panocamclient.activity;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import pers.liufushihai.panocamclient.R;
import pers.liufushihai.panocamclient.fragment.BaseFragment;
import pers.liufushihai.panocamclient.fragment.FragmentFactory;
import pers.liufushihai.panocamclient.network.TcpClientConnector;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TabLayout mTab;
    private ViewPager mViewPager;
    ShortPagerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initView(){
        mTab = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.viewpager);
    }

    private void initData() {
        FragmentManager fm = getSupportFragmentManager();
        adapter = new ShortPagerAdapter(fm);
        mViewPager.setAdapter(adapter);
        mTab.setupWithViewPager(mViewPager,true);
    }

    public class ShortPagerAdapter extends FragmentPagerAdapter {
        public String[] mTitle;

        public ShortPagerAdapter(FragmentManager fm){
            super(fm);
            mTitle = getResources().
                    getStringArray(R.array.tab_short_title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitle[position];
        }

        @Override
        public Fragment getItem(int position) {
            BaseFragment fragment =
                    FragmentFactory.createFragment(position);
            return fragment;
        }

        @Override
        public int getCount() {
            return mTitle.length;
        }
    }
}
