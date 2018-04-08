package pers.liufushihai.panocamclient.network;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;

import pers.liufushihai.panocamclient.util.TimeUtils;

/**
 * Date        : 2018/3/30
 * Author      : liufushihai
 * Description : Tcp客户端连接管理类
 */

public class TcpClientConnector {
    private static final String TAG = "TcpClientConnector";

    private static TcpClientConnector mTcpClientConnector;
    private Socket mClient;
    private ConnectListener mListener;
    private Thread mConnectThread;
    private FileOutputStream fout;
    private int recvBytes = 0;
    private final int FLAG_OF_STR_DATA = 1000;
    private final int FLAG_RECEIVE_IMAGE_DONE = 1;

    public static String currentFileName = "";
    public static boolean isConnected = false;

    public interface ConnectListener{
        void onReceiveData(String data);
    }

    public void setOnConnectListener(ConnectListener listener){
        this.mListener = listener;
    }

    public static TcpClientConnector getInstance(){            //单例模式
        if(mTcpClientConnector == null){
            mTcpClientConnector = new TcpClientConnector();
        }
        return mTcpClientConnector;
    }

    public void createConnect(final String mSerIP, final int mSerPort) {   //连接服务器端的线程
        if (mConnectThread == null) {
            mConnectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect(mSerIP, mSerPort);              //执行的下面的connect函数，及创建线程用来接收来自服务器端的图像数据
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "run: isConnected : " + isConnected);
                    }
                }
            });
            mConnectThread.start();
        }
    }

    Handler mHandler = new Handler() {                          //接收到字符串数据就放往此Handler进行处理
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FLAG_OF_STR_DATA:
                    if (mListener != null) {                                        //在这里：接收到文件名(字符串)
                        mListener.onReceiveData(msg.getData().getString("data"));   //实现了此接口的话，就可以处理接收到的数据
                    }
                    break;

                case FLAG_RECEIVE_IMAGE_DONE:
                    if(mListener != null){
                        mListener.onReceiveData(msg.getData().getString("str_recv"));
                    }
                    break;
            }
        }
    };

    /**
     * 该方法在子线程中进行
     * 与服务器端进行进行连接，连接成功后阻塞接收数据
     * @param mServIp
     * @param mServPort
     * @throws IOException
     */
    private void connect(String mServIp, int mServPort) throws IOException{
        //创建socket
        if(mClient == null){
            mClient = new Socket(mServIp, mServPort);
            /* 如果socket创建失败的话，就会抛出异常，下面变量值依旧为false,不过要等一段较长的时间就没有办法正常处理吗 */
            isConnected = true;
        }

        Log.d(TAG, "connect: isConnected : " + isConnected);

        //根据时间创建一个本地文件接收数据
        File root = Environment.getExternalStorageDirectory();
        File directory = new File(root,"PanoramaImages");
        if(!directory.exists()){
            directory.mkdirs();
        }

        //File file = new File(directory,new Date().getTime() + ".jpg");
        File file = new File(directory, TimeUtils.millis2String(new Date().getTime()) + ".jpg");
        fout = new FileOutputStream(file);

        currentFileName = String.valueOf(file.getAbsolutePath());
        Log.d(TAG, "connect: " + "currentFileName = " + currentFileName);


        //阻塞接收数据
        InputStream inputStream = mClient.getInputStream();
        byte[] buffer = new byte[50];
        int len = -1;

        /**
         * 拿到图像数据，不管什么方式
         */
        while ((len = inputStream.read(buffer)) != -1){

            recvBytes += len;       //计算接收数据字节数

            Log.d(TAG, "connect: " + "len = " + len);

            try {
                fout.write(buffer,0,len);       //往file写入接收的图像数据
                fout.flush();
                if(len < 50){
                    String str_recv_done = "RECV_DONE";
                    Message message = new Message();
                    message.what = FLAG_RECEIVE_IMAGE_DONE;
                    Bundle bundle = new Bundle();
                    bundle.putString("str_recv", str_recv_done);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }catch (IOException e){
                fout.close();
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据
     * @param data
     * @throws IOException
     */
    public void send(String data) throws IOException{
        if(mClient != null){
            OutputStream outputStream = mClient.getOutputStream();
            outputStream.write(data.getBytes());
        }
    }

    /**
     * 断开连接与相机端的连接
     * @throws IOException
     */
    public void disConnect() throws IOException{
        if(mClient != null){
            mClient.close();
            mClient = null;
        }
    }

    /**
     * 测试socket连接是否断开
     * @return
     */
    public boolean isKeepAlive(){
        boolean res = false;
        try {
            mClient.sendUrgentData(0XFF);
            res = true;
        }catch (IOException e){
            res = false;
            //e.printStackTrace();
        }
        return res;
    }
}
