package pers.liufushihai.panocamclient.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
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
import pers.liufushihai.panocamclient.activity.MainActivity;
import pers.liufushihai.panocamclient.activity.PanoViewActivity;
import pers.liufushihai.panocamclient.adapter.ImageAdapter;
import pers.liufushihai.panocamclient.bean.ImageBean;
import pers.liufushihai.panocamclient.network.TcpClientConnector;
import pers.liufushihai.panocamclient.util.FileHandleHelper;

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

    @Override
    protected void loadData() {
        //加载数据
        requestPermissions();
        File dir = new File(String.valueOf(Environment.getExternalStorageDirectory() + "/PanoramaImages"));
        FileHandleHelper.resursionFile(dir,imageBeanList);
        //printImageListUri(imageBeanList);
        initTcpClientConnector();
    }

    @Override
    protected View initView() {
        //Fragment布局初始化
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
                    Toast.makeText(mContext,"未与相机端连接,请前往设置页设置连接",Toast.LENGTH_SHORT).show();
                }else{
                    new Thread(new Runnable() {             //创建一个子线程来发送数据
                        @Override
                        public void run() {
                            try{
                                mTcpClientConnector.send("Shoot");
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }).start();

                    progressDialog = new MaterialDialog.Builder(mContext)
                            .title("数据传输中")
                            .content("正在接收数据")
                            .progress(true, 0)
                            .cancelable(false)
                            .show();
                }
            }
        });

        return view;
    }

    /**
     * 请求存储权限
     */
    private void requestPermissions(){
        /* 在Fragment中使用Activity中使用ActivityCompat使用6.0权限认证是不行的，而且onRequestPermissionsResult无法回调*/
//        ActivityCompat.requestPermissions(getActivity(),new String[]{
//                Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

        /* 直接使用requestPermissions就能回调到当前重写的onRequestPermissionsResult */
        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    FileHandleHelper.initFileSaveHelper();
                    Log.d(TAG, "onRequestPermissionsResult: ");
                }
                break;
            default:
                break;
        }
    }

    /**
     * 打印列表当前的object类
     * @param imgList
     */
    private void printImageListUri(List<ImageBean> imgList){
        int count = 0;
        for(ImageBean imgbean : imgList){
            Log.d(TAG, "printImageListUri: " + imgbean.getUri());
            count++;
        }
    }

    private void initTcpClientConnector(){
        mTcpClientConnector = TcpClientConnector.getInstance();
        mTcpClientConnector.setOnConnectListener(new TcpClientConnector.ConnectListener() {
            @Override
            public void onReceiveData(String data) {
                if(data.toString() == "RECV_DONE"){
//                    Toast.makeText(mContext, "接收图片完成！",
//                            Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(mContext, PanoViewActivity.class);
                    intent.putExtra("string_uri",TcpClientConnector.currentFileName);
                    mContext.startActivity(intent);

                    progressDialog.dismiss();

                    imageBeanList.add(new ImageBean(String.valueOf(
                            Uri.parse(TcpClientConnector.currentFileName))));
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

}
