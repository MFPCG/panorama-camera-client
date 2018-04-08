package pers.liufushihai.panocamclient.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import pers.liufushihai.panocamclient.R;
import pers.liufushihai.panocamclient.constant.Constants;
import pers.liufushihai.panocamclient.network.TcpClientConnector;
import pers.liufushihai.panocamclient.util.NetworkUtils;
import pers.liufushihai.panocamclient.util.RegexUtils;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 客户端设置Fragment
 */

public class SettingFragment extends BaseFragment {

    private LinearLayout llConnSetting;       //"连接设置"项布局
    private TextView tvIpAndPort;
    private TextView tvConnStatus;
    private LayoutInflater layoutInflater;
    private AlertDialog dialog;
    private View dialogView;
    private String tmpIp;
    private int tmpPort;

    @Override
    protected void loadData() {
        //加载数据

        layoutInflater = LayoutInflater.from(mContext);
        dialogView = layoutInflater.inflate(R.layout.dialog_server_info, null);

        //从SharedPreference中读取历史连接的硬件端IP地址并加载
        {
            SharedPreferences pref = mContext.
                    getSharedPreferences("data", Context.MODE_PRIVATE);
            tmpIp = pref.getString("ip", "null");
            tmpPort = pref.getInt("port", 0);
            tvIpAndPort.setText(tmpIp + " : " + String.valueOf(tmpPort));
        }
    }

    /**
     * SettingFragment布局初始化
     * @return
     */
    @Override
    protected View initView() {
        final View view = View.inflate(mContext, R.layout.fragment_settings, null);
        llConnSetting = view.findViewById(R.id.ll_connection_settings_display);
        tvIpAndPort = view.findViewById(R.id.tv_ip_port);
        TextView localIp = view.findViewById(R.id.tv_local_ip);
        tvConnStatus = view.findViewById(R.id.tv_connect_status);
        tvConnStatus.setText(getResources().getText(R.string.conn_status_disconnect));
        localIp.setText(NetworkUtils.getIPAddress(true));

        llConnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext,"You click 连接设置",Toast.LENGTH_SHORT).show();
                //弹出窗口，让用户输入并保存
                showInputDialog(v);
            }
        });
        return view;
    }

    /**
     * Ip及Port信息输入窗口弹窗
     */
    private void showInputDialog(final View view) {
        if (dialog == null) {
            dialog = new AlertDialog.Builder(mContext)
                    .setTitle(getResources().getText(R.string.hint_info_dialog_title))
                    .setPositiveButton(getResources().getText(R.string.btn_text_connect), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((ViewGroup) view.getParent()).removeView(dialogView);
                            dialog.dismiss();

                            /* 获取窗体布局中的控件实例 */
                            EditText edtIp = dialogView.findViewById(R.id.server_ip);
                            EditText editPort = dialogView.findViewById(R.id.server_port);

                            int port = 8888;
                            String ip = edtIp.getText().toString();

                            /* 端口合法性判断 */
                            try {
                                port = Integer.parseInt(editPort.getText().toString());
                            } catch (NumberFormatException e) {
                                Toast.makeText(mContext,
                                        getResources().getText(R.string.hint_info_check_port),
                                        Toast.LENGTH_SHORT).show();
                            }

                            /* IP地址合法性判断 */
                            if (RegexUtils.isIP(ip)) {
                                DevPicFragment.mTcpClientConnector
                                        .createConnect(ip, port);

                                //保存到SharedPreference中去
                                SharedPreferences.Editor editor = mContext
                                        .getSharedPreferences("data", Context.MODE_PRIVATE)
                                        .edit();

                                editor.clear();
                                editor.putString("ip", ip);
                                editor.putInt("port", port);
                                editor.apply();

                                tvIpAndPort.setText(ip + " : " + String.valueOf(port));
                                tvConnStatus.setText(
                                        getResources().getText(R.string.conn_status_connected));
                            } else {
                                Toast.makeText(mContext,
                                        getResources().getText(R.string.hint_info_check_ip),
                                        Toast.LENGTH_SHORT).show();
                            }

                             /* 调试环境 */
//                            DevPicFragment.mTcpClientConnector
//                                    .createConnect(Constants.DEBUG_IP,
//                                            Integer.parseInt(Constants.DEBUG_PORT));

                            /* 判断socket连接是否可用并且是否连接上相机端,判断需要一定的时间 */
//                            if(TcpClientConnector.isConnected == true){
//                                tvConnStatus.setText(
//                                        getResources().getText(R.string.conn_status_connected));
//                            }else{
//                                tvConnStatus.setText(
//                                        getResources().getText(R.string.conn_status_disconnect));
//                            }

                            /* 程序走到此步时，可能Socket尚未创建成功 */
//                            if(DevPicFragment.mTcpClientConnector.isKeepAlive() == false){
//                                tvConnStatus.setText(
//                                        getResources().getText(R.string.conn_status_disconnect));
//                            }else{
//                                tvConnStatus.setText(
//                                        getResources().getText(R.string.conn_status_connected));
//                            }
                        }
                    })
                    .setNegativeButton(getResources().getText(R.string.btn_text_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((ViewGroup) view.getParent()).removeView(dialogView);
                            dialog.dismiss();
                        }
                    })
                    .setView(dialogView)
                    .create();
        }
        dialog.show();
    }
}
