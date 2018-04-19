package pers.liufushihai.panocamclient.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pers.liufushihai.panocamclient.R;
import pers.liufushihai.panocamclient.activity.PanoViewActivity;
import pers.liufushihai.panocamclient.adapter.ImageAdapter;
import pers.liufushihai.panocamclient.bean.ImageBean;
import pers.liufushihai.panocamclient.constant.Constants;
import pers.liufushihai.panocamclient.network.TcpClientConnector;
import pers.liufushihai.panocamclient.util.FileUtils;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 客户端接收图像显示的Fragment
 */

public class DevPicFragment extends BaseFragment {
    private static final String TAG = "DevPicFragment";

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static List<ImageBean> imageBeanList = new ArrayList<>();
    public static TcpClientConnector mTcpClientConnector;
    private MaterialDialog progressDialog;

    /**
     * 初始化加载数据
     */
    @Override
    protected void loadData() {
        requestPermissions();
        File dir = new File(String.valueOf(Environment.
                getExternalStorageDirectory() + "/" + Constants.SAVING_FOLDER));
        FileUtils.resursionFileInFolder(dir,imageBeanList);
        initTcpClientConnector();
    }

    /**
     * DevPicFragment布局初始化
     * @return
     */
    @Override
    protected View initView() {
        View view = View.inflate(mContext,R.layout.fragment_dev_pic,null);
        mRecyclerView = view.findViewById(R.id.my_recycler_view);

        mLayoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ImageAdapter(mContext,imageBeanList);

        mRecyclerView.setAdapter(mAdapter);

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TcpClientConnector.isConnected == false){
                    Toast.makeText(mContext, getResources().getText(
                            R.string.dev_pic_info_please_to_connect),
                            Toast.LENGTH_SHORT).show();
                }else{
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                mTcpClientConnector.send(getResources().getString(
                                        R.string.shoot_command));
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    if(progressDialog == null){
                        progressDialog = new MaterialDialog.Builder(mContext)
                                .content(getResources().
                                        getText(R.string.dev_pic_info_receiving_data))
                                .progress(true, 0)
                                .cancelable(false)
                                .show();
                    }else{
                        progressDialog.show();
                    }
                }
            }
        });
        return view;
    }

    /**
     * 请求存储权限
     * Fragment中进行权限认证，直接调用requestPermissions即可
     */
    private void requestPermissions(){
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileUtils.initFileSaveHelper();
                    Log.d(TAG, "onRequestPermissionsResult: ");
                }else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    /* 授权取消处理 */
                }
                break;
            default:
                break;
        }
    }

    /**
     * 初始化TCP客户端实例
     */
    private void initTcpClientConnector(){
        mTcpClientConnector = TcpClientConnector.getInstance();
        mTcpClientConnector.setOnConnectListener(new TcpClientConnector.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                if(data.toString() == "RECV_DONE"){

                    if(progressDialog != null){
                        progressDialog.dismiss();
                    }

                    Intent intent = new Intent(mContext, PanoViewActivity.class);
                    intent.putExtra("string_uri",TcpClientConnector.currentFileName);
                    mContext.startActivity(intent);

                    imageBeanList.add(new ImageBean(String.valueOf(
                            Uri.parse(TcpClientConnector.currentFileName))));
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}
