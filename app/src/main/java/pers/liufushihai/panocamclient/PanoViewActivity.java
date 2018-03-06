package pers.liufushihai.panocamclient;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PanoViewActivity extends AppCompatActivity {

    private static final String TAG = "PanoViewActivity";

    GLSurfaceView glSurfaceView;
    public float mAngleX = 0;// 摄像机所在的x坐标
    public float mAngleY = 0;// 摄像机所在的y坐标
    public float mAngleZ = 3;// 摄像机所在的z坐标

    public int finger_num = 0;
    public float oldDist;
    public float sphere_r = 3.0f;

    public static String VL = "uniform mat4 uMVPMatrix;" +      //Vertex Shader程序
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" +
            "}";
    public static String FL = "precision mediump float;" +      //Fragment Shader程序
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";

    private String recvImagesFilePath = Environment.getExternalStorageDirectory() + "/MT_PanoCamera/Images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: ");

        glSurfaceView = new GLSurfaceView(this);    //创建SurfaceView实例
        glSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));  //设置glSurfaceView的布局
        setContentView(glSurfaceView);

        glSurfaceView.setEGLContextClientVersion(2);

        glSurfaceView.setRenderer(new RenderListener());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);   //设置渲染模式
    }

    float startRawX;
    float startRawY;

    double xFlingAngle;
    double xFlingAngleTemp;

    double yFlingAngle;
    double yFlingAngleTemp;

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        Log.d(TAG, "onTouchEvent: ");
        switch (me.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                startRawX = me.getRawX();
                startRawY = me.getRawY();
                finger_num = 1;
                break;
            case MotionEvent.ACTION_UP:
                xFlingAngle += xFlingAngleTemp;
                yFlingAngle += yFlingAngleTemp;
                finger_num = 0;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                --finger_num;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                ++finger_num;
                oldDist = spacing(me);
                break;
            case MotionEvent.ACTION_MOVE:
                float distanceX = startRawX - me.getRawX();
                float distanceY = startRawY - me.getRawY();

                //这里的0.1f是为了不让摄像机移动的过快
                distanceY = 0.1f * (distanceY) / getWindowManager().getDefaultDisplay().getHeight();

                yFlingAngleTemp = distanceY * 180 / (Math.PI * 3);

                if (yFlingAngleTemp + yFlingAngle > Math.PI / 2) {
                    yFlingAngleTemp = Math.PI / 2 - yFlingAngle;
                }
                if (yFlingAngleTemp + yFlingAngle < -Math.PI / 2) {
                    yFlingAngleTemp = -Math.PI / 2 - yFlingAngle;
                }

                distanceX = 0.1f * (-distanceX) / getWindowManager().getDefaultDisplay().getWidth();
                xFlingAngleTemp = distanceX * 180 / (Math.PI * 3);


                mAngleX = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp) * Math.sin(xFlingAngle + xFlingAngleTemp));
                mAngleY = (float) (3 * Math.sin(yFlingAngle + yFlingAngleTemp));
                mAngleZ = (float) (3 * Math.cos(yFlingAngle + yFlingAngleTemp) * Math.cos(xFlingAngle + xFlingAngleTemp));
                glSurfaceView.requestRender();          //操作完后及时渲染并绘制

                if (finger_num >= 2) {
                    float newDist = spacing(me);
                    if (newDist > (oldDist + 250f)) {
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    } else if (newDist < (oldDist - 250f)) {
                        zoom(newDist / oldDist);
                        oldDist = newDist;
                    }
                }
                break;
        }
        return true;
    }

    private void zoom(float radio) {
        //球体半径乘以缩放比例
        //可以设置步长，让图像的缩放更加平滑
        sphere_r *= radio;
        glSurfaceView.requestRender();
        Log.d(TAG, "zoom: ");
    }

    private float spacing(MotionEvent motionEvent) {
        float x = motionEvent.getX(0) - motionEvent.getX(1);
        float y = motionEvent.getY(0) - motionEvent.getY(1);
        Log.d(TAG, "spacing: ");
        return (float) Math.sqrt(x * x + y * y);
    }


    class RenderListener implements GLSurfaceView.Renderer {

        FloatBuffer verticalsBuffer;

        int CAP = 6;//绘制球体时，每次增加的角度
        float[] verticals = new float[(180 / CAP) * (360 / CAP) * 6 * 3];

        //private final FloatBuffer mUvTexVertexBuffer;
        private FloatBuffer mUvTexVertexBuffer;

        private final float[] UV_TEX_VERTEX = new float[(180 / CAP) * (360 / CAP) * 6 * 2];

        private int mProgram;
        private int mPositionHandle;
        private int mTexCoordHandle;
        private int mMatrixHandle;
        private int mTexSamplerHandle;
        int[] mTexNames;

        private final float[] mProjectionMatrix = new float[16];
        private final float[] mCameraMatrix = new float[16];
        private final float[] mMVPMatrix = new float[16];

        private int mWidth;
        private int mHeight;

        //RenderListener()中进行球体的绘制，更改完半径后都要进行一次球体的绘制
        public RenderListener() {
            Log.d(TAG, "RenderListener: ");
        }

        /**
         * 在Surface被创建时调用
         *
         * @param gl
         * @param config
         */
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(TAG, "onSurfaceCreated: ");
        }

        /**
         * 在Surface尺寸变化时调用，主要是横竖屏切换的时候
         *
         * @param gl
         * @param width
         * @param height
         */
        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged: ");

            mWidth = width;
            mHeight = height;
            mProgram = GLES20.glCreateProgram();

            int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
            GLES20.glShaderSource(vertexShader, VL);
            GLES20.glCompileShader(vertexShader);

            int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
            GLES20.glShaderSource(fragmentShader, FL);
            GLES20.glCompileShader(fragmentShader);

            GLES20.glAttachShader(mProgram, vertexShader);
            GLES20.glAttachShader(mProgram, fragmentShader);

            GLES20.glLinkProgram(mProgram);

            mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texCoord");
            mMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");

            mTexNames = new int[1];
            GLES20.glGenTextures(1, mTexNames, 0);
            //这里的全景图需要长宽的比例使2：1，不然南北两级会出现严重变形，影响观看
            //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.result);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.local_test2);
            //Bitmap bitmap = BitmapFactory.decodeFile(recvImagesFilePath+"/recvImage2.jpg");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexNames[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
            float ratio = (float) height / width;
            //透视投影的投影线是不平行的，他们相交于视点。通过透视可以产生现实世界中"近大远小"的效果
            Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 0.8f, 7);
            //Matrix.perspectiveM(mProjectionMatrix,0,(float)width/(float)height,ratio,3,0);
            //参数为：offset,left ,right,bottom,top,near,far
            //Matrix.frustumM(mProjectionMatrix, 0, -1, 1, -ratio, ratio, 3, 7);
        }

        /**
         * 绘制一帧图像的时候被调用
         *
         * @param gl
         */
        @Override
        public void onDrawFrame(GL10 gl) {

            float x = 0;
            float y = 0;
            float z = 0;

            //float r = 1.5f;//球体半径
            float r = sphere_r;//球体半径

            if (sphere_r >= 3.0f) {        //控制缩放上限
                r = 3.0f;
            }

            if (sphere_r <= 0.8f) {      //控制缩放下限
                r = 0.8f;
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

            //调整摄像机焦点位置，使画面滚动
            Matrix.setLookAtM(mCameraMatrix, 0, mAngleX, mAngleY, mAngleZ, 0, 0, 0, 0, 1, 0);
            //Matrix.setLookAtM(mCameraMatrix, 0, mAngleX, mAngleY, mAngleZ, 0, 0, 0, 0, 50, 0);

            Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mCameraMatrix, 0);

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(mProgram);
            GLES20.glEnableVertexAttribArray(mPositionHandle);
            GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false,
                    12, verticalsBuffer);
            GLES20.glEnableVertexAttribArray(mTexCoordHandle);
            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0,
                    mUvTexVertexBuffer);
            GLES20.glUniformMatrix4fv(mMatrixHandle, 1, false, mMVPMatrix, 0);
            GLES20.glUniform1i(mTexSamplerHandle, 0);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, (180 / CAP) * (360 / CAP) * 6);

            GLES20.glDisableVertexAttribArray(mPositionHandle);

        }
    }
}