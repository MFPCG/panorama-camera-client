package pers.liufushihai.panocamclient.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import pers.liufushihai.panocamclient.R;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 客户端设置Fragment
 */

public class SettingFragment extends BaseFragment {

    LinearLayout ll;
    EditText edtIp;
    EditText editPort;

    @Override
    protected void loadData() {
        //加载数据
//        Toast.makeText(mContext, "SettingFragment",
//                Toast.LENGTH_SHORT).show();

        //从SharedPreference中读取历史连接的硬件端IP地址并加载
    }

    @Override
    protected View initView() {
        //Fragment布局初始化
        View view = View.inflate(mContext, R.layout.fragment_settings,null);
        View dialogView = View.inflate(mContext,R.layout.dialog_server_info,null);
        ll = view.findViewById(R.id.ll_connection_setting);
        edtIp = dialogView.findViewById(R.id.server_ip);
        editPort = dialogView.findViewById(R.id.server_port);

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext,"You click 连接设置",Toast.LENGTH_SHORT).show();
                //弹出窗口，让用户输入并保存
                showDialog();
            }
        });
        return view;
    }

    /**
     * 弹出窗口
     */
    private void showDialog(){
        android.support.v7.app.AlertDialog.Builder builder =
                new  android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("请输入相机端IP及端口号")
                .setNegativeButton("取消",null)
                .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //存储到SharePreference中

//                        Log.d("liufushihai", "onClick: " + String.valueOf(edtIp.getText().toString())
//                                            + '\t' + String.valueOf(editPort.getText().toString()));
                        //先做连接处理
//                        DevPicFragment.mTcpClientConnector
//                                .createConnect(edtIp.getText().toString(),
//                                        Integer.valueOf(editPort.getText().toString()));

                        DevPicFragment.mTcpClientConnector
                                .createConnect("192.168.1.64",
                                        8888);
                    }
                })
                .setView(R.layout.dialog_server_info)
                .show();
    }
}
