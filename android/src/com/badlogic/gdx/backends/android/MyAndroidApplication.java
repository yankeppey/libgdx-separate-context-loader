package com.badlogic.gdx.backends.android;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.utils.GdxRuntimeException;

import java.lang.reflect.Method;

/**
 * {@link AndroidApplication} implementation that uses {@link MyAndroidGraphics} implementation inside
 * (with access to {@link javax.microedition.khronos.egl.EGLContext}).
 * Your activity should extend this class. You want to use something like this in your {@link android.app.Activity#onCreate(Bundle)}:
 * <pre>
 *     {@code
 *          getGraphics().setOnSurfaceCreatedListener(new MyAndroidGraphics.OnSurfaceCreatedListener() {
 *              @Override
 *              public void onSurfaceCreated() {
 *                  EGLContext eglContext = getGraphics().getSharedContext();
 *                  AssetManager manager = new AssetManager();
 *                  manager.setLogger(Consts.ASSETS_MANAGER_LOGGER);
 *                  manager.setLoader(FrameBuffer.class, new AsyncTextureLoader(MyAndroidApplication.this, eglContext));
 *                  ///...
 *              }
 *          });
 *     }
 * </pre>
 *
 * @author mzechner
 * @author Andrei Buneyeu
 */
public class MyAndroidApplication extends AndroidApplication {

    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
        init(listener, config, true);
        return graphics.getView();
    }

    // Almost everything here is copied from the original {@link AndroidApplication}, except of
    // graphics init
    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView) {
        if (this.getVersion() < MINIMUM_SDK) {
            throw new GdxRuntimeException("LibGDX requires Android API Level " + MINIMUM_SDK + " or later.");
        }
        graphics = new MyAndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy()
                : config.resolutionStrategy);
        input = AndroidInputFactory.newAndroidInput(this, this, graphics.view, config);
        audio = new AndroidAudio(this, config);
        this.getFilesDir(); // workaround for Android bug #10515463
        files = new AndroidFiles(this.getAssets(), this.getFilesDir().getAbsolutePath());
        net = new AndroidNet(this);
        this.listener = listener;
        this.handler = new Handler();
        this.useImmersiveMode = config.useImmersiveMode;
        this.hideStatusBar = config.hideStatusBar;

        // Add a specialized audio lifecycle listener
        addLifecycleListener(new LifecycleListener() {

            @Override
            public void resume() {
                // No need to resume audio here
            }

            @Override
            public void pause() {
                audio.pause();
            }

            @Override
            public void dispose() {
                audio.dispose();
            }
        });

        Gdx.app = this;
        Gdx.input = this.getInput();
        Gdx.audio = this.getAudio();
        Gdx.files = this.getFiles();
        Gdx.graphics = this.getGraphics();
        Gdx.net = this.getNet();

        if (!isForView) {
            try {
                requestWindowFeature(Window.FEATURE_NO_TITLE);
            } catch (Exception ex) {
                log("AndroidApplication", "Content already displayed, cannot request FEATURE_NO_TITLE", ex);
            }
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            setContentView(graphics.getView(), createLayoutParams());
        }

        createWakeLock(config.useWakelock);
        hideStatusBar(this.hideStatusBar);
        useImmersiveMode(this.useImmersiveMode);
        if (this.useImmersiveMode && getVersion() >= Build.VERSION_CODES.KITKAT) {
            try {
                Class<?> vlistener = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
                Object o = vlistener.newInstance();
                Method method = vlistener.getDeclaredMethod("createListener", AndroidApplicationBase.class);
                method.invoke(o, this);
            } catch (Exception e) {
                log("AndroidApplication", "Failed to create AndroidVisibilityListener", e);
            }
        }
    }

    @Override
    public MyAndroidGraphics getGraphics() {
        return (MyAndroidGraphics) graphics;
    }

}
