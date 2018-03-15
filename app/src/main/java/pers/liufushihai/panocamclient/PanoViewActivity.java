package pers.liufushihai.panocamclient;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import pers.liufushihai.panocamclient.renderer.PanoRenderer;

public class PanoViewActivity extends AppCompatActivity {
    private static final String TAG = "PanoViewActivity";

    public static GLSurfaceView glSurfaceView;
    PanoRenderer panoRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        panoRenderer = new PanoRenderer(this);
        glSurfaceView = new GLSurfaceView(this);    //创建SurfaceView实例
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
                        getWindowManager().getDefaultDisplay().getHeight());
            }
        });
        Log.d(TAG, "onTouchEvent: " + event.getPointerCount());
        glSurfaceView.requestRender();
        return true;
    }
}