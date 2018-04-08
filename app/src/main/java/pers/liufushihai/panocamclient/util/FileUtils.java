package pers.liufushihai.panocamclient.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import pers.liufushihai.panocamclient.bean.ImageBean;

/**
 * Date        : 2018/3/27
 * Author      : liufushihai
 * Description : 文件保存工具类
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    private static String mSaveFolderName;      //存放图像文件的文件夹名
    private static File root;                   //手机内存根目录
    private static File directory;              //根据文件夹名创建的文件夹

    public static void initFileSaveHelper(){
        mSaveFolderName = "PanoramaImages";
        root = Environment.getExternalStorageDirectory();
        directory = new File(root,mSaveFolderName);

        if(!directory.exists()){
            directory.mkdirs();           //可创建多级目录，mkdir()只能创建一级目录
        }
    }

    /**
     * 以当前时间为保存文件名
     * @param bitmap
     */
    public static void saveBitmapWithTime(Bitmap bitmap){
        File file = new File(directory,new Date().getTime() + ".jpg");
        saveFile(bitmap,file);
    }

    /**
     * 以指定字符串名为保存文件名
     * @param bitmap
     * @param str
     */
    public static void saveBitmapWithString(Bitmap bitmap, String str){
        File file = new File(directory,str + ".jpg");
        saveFile(bitmap,file);
    }

    /**
     * 保存File文件
     * @param file
     */
    private static void saveFile(Bitmap bitmap, File file){
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,41,fos);
            fos.flush();

            Uri uri = Uri.fromFile(file);
            Log.d(TAG, "saveFile: uri " + uri.toString());
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            //关闭所有流
            if(fos != null){
                try {
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归方式遍历根目录下存放接收文件的文件夹，
     * 并将Uri存入到相应参数中
     * @param dir
     * @param images
     */
    public static void resursionFileInFolder(File dir, List<ImageBean> images){
        //得到某个文件夹下所有的文件
        File[] files = dir.listFiles();
        //文件为空
        if(files == null){
            return;
        }

        for(File file : files){
            //如果是文件夹
            if(file.isDirectory()){
                //则递归
                resursionFileInFolder(file,images);
            }else{
                //如果不是文件夹，则是文件
                if(file.getName().endsWith(".jpg")){
                    //往图片集合中添加图片的路径
                    images.add(new ImageBean(file.getAbsolutePath()));
                }
            }
        }

    }
}
