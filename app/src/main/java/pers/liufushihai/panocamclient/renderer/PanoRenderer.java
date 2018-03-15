package pers.liufushihai.panocamclient.renderer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import pers.liufushihai.panocamclient.PanoViewActivity;
import pers.liufushihai.panocamclient.R;
import pers.liufushihai.panocamclient.util.LoggerConfig;
import pers.liufushihai.panocamclient.util.TextResourceReader;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_REPEAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Date        : 2018/3/12
 * Author      : liufushihai
 * Description : 全景图像显示渲染类
 * Reference   : https://www.jianshu.com/p/394606f1ca90
 */

public class PanoRenderer implements GLSurfaceView.Renderer{
    private static final String TAG = "PanoRenderer";

    private FloatBuffer verticalsBuffer;

    /* 球体表面的切分的小矩形的绘制两个三角形，6个顶点 */

    private int CAP = 1;                        //绘制球体时，每次增加的角度
    /* 球体上切分的小片矩形的顶点数据的存放数组，每个顶点有3个向量x,y,z */
    private float[] verticals = new float[(180 / CAP) * (360 / CAP) * 6 * 3];

    private FloatBuffer mUvTexVertexBuffer;

    private final float[] UV_TEX_VERTEX = new float[(180 / CAP) * (360 / CAP) * 6 * 2];

    private int mProgramId;
    private int mPositionHandle;
    private int mTexCoordHandle;
    private int mMatrixHandle;
    private int mTexSamplerHandle;
    private int[] mTexNames;

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mCameraMatrix = new float[16];
    private final float[] mMVPMatrix = new float[16];

    private int mWidth;
    private int mHeight;

    /* 增设相关加载glsl相关变量 */
    private final Context context;

    /* 缩放上下限 */
    private static final float MAX_SCALE_VALUE = 3.0f;
    private static final float MIN_SCALE_VALUE = 0.8f;

    /* 摄像机位置 */
    private float mAngleX = 0;// 摄像机所在的x坐标
    private float mAngleY = 0;// 摄像机所在的y坐标
    private float mAngleZ = 3;// 摄像机所在的z坐标

    /* 手势相关变量 */
    private float startRawX;
    private float startRawY;

    private double xFlingAngle;
    private double xFlingAngleTemp;

    private double yFlingAngle;
    private double yFlingAngleTemp;

    private int fingerCount = 0;
    private float oldDistance = 0f;
    private float newDistance = 0f;

    /* 缩放操作间距值，用于判断操作是缩小或者放大 */
    private static final float SCALE_DISTANCE_VALUE = 250f;

    /* 球体半径 */
    private float sphereRadius = MAX_SCALE_VALUE;

    /**
     * 1.确定绘制的球体的半径(大小)
     * 2.本地环境内存的分配
     * @param context
     */
    public PanoRenderer(Context context) {
        this.context = context;
        programDataInit();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        mWidth = width;
        mHeight = height;
        mProgramId = GLES20.glCreateProgram();

        /* 读入着色器代码 */
        String vertexShaderSource =
                TextResourceReader.readTextFileFromResource(context,R.raw.pano_vertex_shader);
        String fragmentShaderSource =
                TextResourceReader.readTextFileFromResource(context,R.raw.pano_fragment_shader);

        /* 创建一个着色器对象 */
        int vertexShaderId = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        /* 上传着色器源代码到着色器对象上 */
        glShaderSource(vertexShaderId,vertexShaderSource);
        /* 编译着色器对象 */
        glCompileShader(vertexShaderId);

        int fragmentShaderId = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShaderId,fragmentShaderSource);
        glCompileShader(fragmentShaderId);

        /* 将顶点着色器与片段着色器附着到程序对象上 */
        glAttachShader(mProgramId,vertexShaderId);
        glAttachShader(mProgramId,fragmentShaderId);

        /* 连接着色器程序 */
        glLinkProgram(mProgramId);

        /* 获取属性位置 */
        mPositionHandle = glGetAttribLocation(mProgramId, "vPosition");
        mTexCoordHandle = glGetAttribLocation(mProgramId,"a_texCoord");
        mMatrixHandle = glGetUniformLocation(mProgramId,"uMVPMatrix");
        mTexSamplerHandle = glGetUniformLocation(mProgramId,"s_texture");

        mTexNames = new int[1];

        /* 产生纹理对象 */
        glGenTextures(1,mTexNames,0);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.local_test3);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,mTexNames[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_REPEAT);

        GLUtils.texImage2D(GL_TEXTURE_2D,0,bitmap,0);
        bitmap.recycle();

        float ratio = (float) height / width;

        Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio,0.8f,7);
    }

    /**
     * 不断清屏，提高效率
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        glClearColor(0f,0f,0f,1f);

        programDataInit();

        //调整摄像机焦点位置，使画面滚动
        Matrix.setLookAtM(mCameraMatrix, 0, mAngleX, mAngleY, mAngleZ, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgramId);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, verticalsBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, mUvTexVertexBuffer);
        GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniform1i(mTexSamplerHandle, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (180 / CAP) * (360 / CAP) * 6);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    /**
     * MotionEvent总处理函数
     */
    public void handleMotionEvent(final MotionEvent event, int windowHeight){
        switch (event.getAction() & MotionEvent.ACTION_MASK){       //第一次有手指触摸在屏幕上
            case MotionEvent.ACTION_DOWN:
                startRawX = event.getRawX();
                startRawY = event.getRawY();
                fingerCount = 1;
                break;
            case MotionEvent.ACTION_POINTER_UP:                   //抬起时仍然有手指在屏幕上
                --fingerCount;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:                //按下时已有手指在屏幕上
                ++fingerCount;
                oldDistance = distance(event);
                break;
            case MotionEvent.ACTION_MOVE:                       //手指滑动屏幕
                float distanceX = startRawX - event.getRawX();
                float distanceY = startRawY - event.getRawY();

                //这里的0.1f是为了不让摄像机移动的过快
                distanceY = 0.1f * (distanceY) / windowHeight;

                yFlingAngleTemp = distanceY * 180 / (Math.PI * 3);

                if(yFlingAngleTemp + yFlingAngle > Math.PI / 2){
                    yFlingAngleTemp = Math.PI / 2 - yFlingAngle;
                }
                if(yFlingAngleTemp + yFlingAngle < -Math.PI / 2){
                    yFlingAngleTemp = -Math.PI / 2 - yFlingAngle;
                }

                distanceX = 0.1f * (-distanceX) / windowHeight;
                xFlingAngleTemp = distanceX * 180 / (Math.PI * 3);

                mAngleX = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp)
                        * Math.sin(xFlingAngle + xFlingAngleTemp));
                mAngleY = (float) (3 * Math.sin(yFlingAngle + yFlingAngleTemp));
                mAngleZ = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp)
                        * Math.cos(xFlingAngle + xFlingAngleTemp));

                if(fingerCount >= 2){
                    newDistance = distance(event);
                    if((newDistance > (oldDistance + SCALE_DISTANCE_VALUE))                  //放大
                            || (newDistance < (oldDistance - SCALE_DISTANCE_VALUE))){       //缩小
                        sphereRadius *= getScaleRatio(newDistance,oldDistance);
                        oldDistance = newDistance;              //将当前距离值改为上一个距离值

                        //  programDataInit();             //重新清空数据
                        PanoViewActivity.glSurfaceView.requestRender();  //请求重新渲染，会调用onDrawframe()
                    }
                }
                break;
            case MotionEvent.ACTION_UP:                 //最后一个手指离开屏幕
                xFlingAngle += xFlingAngleTemp;
                yFlingAngle += yFlingAngleTemp;
                fingerCount = 0;
                break;
        }

        if(LoggerConfig.ON){
            Log.d(TAG, "handleMotionEvent: "
                    + "指点按压数量：" + fingerCount + '\t'
                    + "oldDistance: " + oldDistance + '\t'
                    + "newDistance: " + newDistance + '\n');
        }
    }

    /**
     * 计算缩放比例因子
     * @return
     */
    private float getScaleRatio(float newDistance, float oldDistance){
        if(LoggerConfig.ON){
            Log.d(TAG, "getScaleRatio: " + (newDistance / oldDistance));
        }
        return (newDistance / oldDistance);
    }

    /**
     * 计算两个指尖的距离
     * 用于后续缩放比的计算
     * @param event
     * @return
     */
    private float distance(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        if(LoggerConfig.ON){
            Log.d(TAG, "distance: " + (float)Math.sqrt(x * x + y * y));
        }

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * OpenGL ES 程序的全局数据初始化
     */
    private void programDataInit(){

        /* 先对数组清空 */
//        if(verticalsBuffer.hasArray())
//        {
//            verticalsBuffer.clear();
//            mUvTexVertexBuffer.clear();
//        }

        float x = 0;
        float y = 0;
        float z = 0;

        float r = sphereRadius;//球体半径

        if(sphereRadius >= MAX_SCALE_VALUE){
            r = MAX_SCALE_VALUE;
        }else if(sphereRadius <= MIN_SCALE_VALUE){
            r = MIN_SCALE_VALUE;
        }

        int index = 0;
        int index1 = 0;
        double d = CAP * Math.PI / 180;//每次递增的弧度
        for (int i = 0; i < 180; i += CAP) {
            double d1 = i * Math.PI / 180;
            for (int j = 0; j < 360; j += CAP) {
                //获得球体上切分的超小片矩形的顶点坐标（两个三角形组成，所以有六点顶点）
                double d2 = j * Math.PI / 180;
                verticals[index++] = (float) (x + r * Math.sin(d1 + d) * Math.cos(d2 + d));
                verticals[index++] = (float) (y + r * Math.cos(d1 + d));
                verticals[index++] = (float) (z + r * Math.sin(d1 + d) * Math.sin(d2 + d));
                //获得球体上切分的超小片三角形的纹理坐标
                UV_TEX_VERTEX[index1++] = (j + CAP) * 1f / 360;
                UV_TEX_VERTEX[index1++] = (i + CAP) * 1f / 180;

                verticals[index++] = (float) (x + r * Math.sin(d1) * Math.cos(d2));
                verticals[index++] = (float) (y + r * Math.cos(d1));
                verticals[index++] = (float) (z + r * Math.sin(d1) * Math.sin(d2));

                UV_TEX_VERTEX[index1++] = j * 1f / 360;
                UV_TEX_VERTEX[index1++] = i * 1f / 180;

                verticals[index++] = (float) (x + r * Math.sin(d1) * Math.cos(d2 + d));
                verticals[index++] = (float) (y + r * Math.cos(d1));
                verticals[index++] = (float) (z + r * Math.sin(d1) * Math.sin(d2 + d));

                UV_TEX_VERTEX[index1++] = (j + CAP) * 1f / 360;
                UV_TEX_VERTEX[index1++] = i * 1f / 180;

                verticals[index++] = (float) (x + r * Math.sin(d1 + d) * Math.cos(d2 + d));
                verticals[index++] = (float) (y + r * Math.cos(d1 + d));
                verticals[index++] = (float) (z + r * Math.sin(d1 + d) * Math.sin(d2 + d));

                UV_TEX_VERTEX[index1++] = (j + CAP) * 1f / 360;
                UV_TEX_VERTEX[index1++] = (i + CAP) * 1f / 180;

                verticals[index++] = (float) (x + r * Math.sin(d1 + d) * Math.cos(d2));
                verticals[index++] = (float) (y + r * Math.cos(d1 + d));
                verticals[index++] = (float) (z + r * Math.sin(d1 + d) * Math.sin(d2));

                UV_TEX_VERTEX[index1++] = j * 1f / 360;
                UV_TEX_VERTEX[index1++] = (i + CAP) * 1f / 180;

                verticals[index++] = (float) (x + r * Math.sin(d1) * Math.cos(d2));
                verticals[index++] = (float) (y + r * Math.cos(d1));
                verticals[index++] = (float) (z + r * Math.sin(d1) * Math.sin(d2));

                UV_TEX_VERTEX[index1++] = j * 1f / 360;
                UV_TEX_VERTEX[index1++] = i * 1f / 180;

            }
        }
        verticalsBuffer = ByteBuffer.allocateDirect(verticals.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(verticals);
        verticalsBuffer.position(0);

        mUvTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(UV_TEX_VERTEX);
        mUvTexVertexBuffer.position(0);
    }
}
