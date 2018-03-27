package pers.liufushihai.panocamclient.fragment;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import pers.liufushihai.panocamclient.R;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 客户端设置Fragment
 */

public class SettingFragment extends BaseFragment {

    @Override
    protected void loadData() {
        //加载数据
        Toast.makeText(mContext, "SettingFragment",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected View initView() {
        //Fragment布局初始化
        View view = View.inflate(mContext, R.layout.fragment_settings,null);
        return view;

    }
}
