package pers.liufushihai.panocamclient.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import pers.liufushihai.panocamclient.R;
import pers.liufushihai.panocamclient.util.NetworkUtil;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 客户端设置Fragment
 */

public class SettingFragment extends BaseFragment {

    LinearLayout ll;
    TextView mTextView;

    TextView tv_local_status;

    LayoutInflater layoutInflater;
    View dialogView;

    android.support.v7.app.AlertDialog.Builder builder;


    @Override
    protected void loadData() {
        //加载数据
//        Toast.makeText(mContext, "SettingFragment",
//                Toast.LENGTH_SHORT).show();

        layoutInflater = LayoutInflater.from(mContext);
        dialogView = layoutInflater.inflate(R.layout.dialog_server_info,null);

        //从SharedPreference中读取历史连接的硬件端IP地址并加载
        {
            SharedPreferences pref = mContext.
                    getSharedPreferences("data",Context.MODE_PRIVATE);
            String ip = pref.getString("ip","null");
            int port = pref.getInt("port",0);
            mTextView.setText(ip + " : " + String.valueOf(port));
        }
    }

    @Override
    protected View initView() {
        //Fragment布局初始化
        View view = View.inflate(mContext, R.layout.fragment_settings,null);
        ll = view.findViewById(R.id.ll_connection_setting);
        mTextView = view.findViewById(R.id.tv_ip_port);
        TextView localIp = view.findViewById(R.id.tv_local_ip);
        tv_local_status = view.findViewById(R.id.tv_connect_status);
        tv_local_status.setText("未连接");
        localIp.setText(NetworkUtil.getIPAddress(true));

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext,"You click 连接设置",Toast.LENGTH_SHORT).show();
                //弹出窗口，让用户输入并保存
                //builder = null;
              //  showDialog();
                showDialog();
            }
        });

        return view;
    }

    /**
     * 弹出窗口
     */
    private void showDialog(){
//        android.support.v7.app.AlertDialog.Builder builder =
//                new  android.support.v7.app.AlertDialog.Builder(mContext);
        builder = new  android.support.v7.app.AlertDialog.Builder(mContext);
        builder.setTitle("请输入相机端IP及端口号")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("连接", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //存储到SharePreference中

                        EditText edtIp = dialogView.findViewById(R.id.server_ip);
                        EditText editPort = dialogView.findViewById(R.id.server_port);

                        final int port = Integer.parseInt(editPort.getText().toString());
                        final String ip = edtIp.getText().toString();

                        //保存到SharedPreference中去
                        {
                            SharedPreferences.Editor editor = mContext
                                    .getSharedPreferences("data", Context.MODE_PRIVATE)
                                    .edit();

                            editor.clear();
                            editor.putString("ip",ip);
                            editor.putInt("port", port);
                            editor.apply();
                        }

//                        Log.d("liufushihai", "onClick: " + String.valueOf(edtIp.getText().toString())
//                                + '\t' + String.valueOf(editPort.getText().toString()));

                        DevPicFragment.mTcpClientConnector
                                .createConnect(ip,
                                       port);

                        mTextView.setText(ip + " : " + String.valueOf(port));

                        /* 宿舍调试使用 */
//                        DevPicFragment.mTcpClientConnector
//                                .createConnect("192.168.31.40",8888);

                        tv_local_status.setText("已连接");
                    }
                })
                .setView(dialogView)
                .show();
    }


    private void showDialog2(){
//        new MaterialDialog.Builder(mContext)
//
//                .show();

        new MaterialDialog.Builder(mContext)
                .customView(dialogView,true)
                .show();
    }
}
