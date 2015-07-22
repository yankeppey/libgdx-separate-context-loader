package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * Instance of this class is used inside {@link MyAndroidApplication} instead of {@link AndroidGraphics}
 * Provides callbacks of surface creation and access to {@link EGLContext} created by {@link AndroidGraphics}.
 *
 * Created by Andrei Buneyeu on 4/9/2015.
 */
public class MyAndroidGraphics extends AndroidGraphics {
    private static final String TAG = MyAndroidGraphics.class.getSimpleName();
    private OnSurfaceCreatedListener onSurfaceCreatedListener;

    public MyAndroidGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config, ResolutionStrategy resolutionStrategy, boolean focusableView) {
        super(application, config, resolutionStrategy, focusableView);
    }

    public MyAndroidGraphics(AndroidApplicationBase application, AndroidApplicationConfiguration config, ResolutionStrategy resolutionStrategy) {
        super(application, config, resolutionStrategy);
    }

    public interface OnSurfaceCreatedListener {
        void onSurfaceCreated();
    }

    public void setOnSurfaceCreatedListener(OnSurfaceCreatedListener listener) {
        this.onSurfaceCreatedListener = listener;
    }

    public EGLContext getSharedContext() {
        return eglContext;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        super.onSurfaceCreated(gl, config);
        if (onSurfaceCreatedListener != null) {
            onSurfaceCreatedListener.onSurfaceCreated();
        }
    }

}
