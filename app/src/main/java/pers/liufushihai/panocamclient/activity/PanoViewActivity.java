package pers.liufushihai.panocamclient.activity;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import pers.liufushihai.panocamclient.renderer.PanoRenderer;
import pers.liufushihai.panocamclient.util.LoggerConfig;

public class PanoViewActivity extends AppCompatActivity {
    private static final String TAG = "PanoViewActivity";

    public static GLSurfaceView glSurfaceView;
    private PanoRenderer panoRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String getUri = getIntent().getStringExtra("string_uri");

        if(LoggerConfig.ON){
            Log.d(TAG, "onCreate: " + getUri);
        }

        panoRenderer = new PanoRenderer(this,getWindowManager(),getUri);
        glSurfaceView = new GLSurfaceView(this);                                  //创建SurfaceView实例
        glSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));  //设置glSurfaceView的布局
        setContentView(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(panoRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);   //设置渲染模式
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                panoRenderer.handleMotionEvent(event,
                        getWindowManager().getDefaultDisplay().getHeight());    //通过计算单指滑动距离来计算摄像头偏移角度
            }
        });
        if(LoggerConfig.ON){
            Log.d(TAG, "onTouchEvent: "
                    + "phone height : " + getWindowManager().getDefaultDisplay().getHeight() + '\t'
                    + "phone width : " + getWindowManager().getDefaultDisplay().getWidth());
        }
        glSurfaceView.requestRender();
        return true;
    }
}