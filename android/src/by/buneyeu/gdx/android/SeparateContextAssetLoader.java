package by.buneyeu.gdx.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

/**
 * Asset loader that loads its content (it can be {@link com.badlogic.gdx.graphics.Texture},
 * {@link com.badlogic.gdx.graphics.glutils.FrameBuffer}, or what you want) in a separate GL context.
 *
 * @author Andrei Buneyeu
 */
public abstract class SeparateContextAssetLoader<T extends Disposable> extends AsynchronousAssetLoader<T, AssetLoaderParameters<T>> {
    private static final String TAG = SeparateContextAssetLoader.class.getSimpleName();
    private GL10 gl;
    private EGLContext sharedContext;
    private EGLDisplay eglDisplay;
    private EGL10 egl;
    private T disposable;
    private EGLContext mEglContext;
    private EGLSurface mEglPBSurface;

    public SeparateContextAssetLoader(EGLContext eglContext) {
        super(new AbsoluteFileHandleResolver());
        this.sharedContext = eglContext;
    }

    private void endContext() {
        if (!egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_SURFACE,
                EGL10.EGL_NO_CONTEXT)) {
            Gdx.app.log(TAG, "Error! eglMakeCurrent null failed:" + egl.eglGetError());
        }
        if (!egl.eglDestroyContext(eglDisplay, mEglContext)) {
            Gdx.app.log(TAG, "Error! eglDestroyContext failed:" + egl.eglGetError());
        }
        mEglContext = null;
        if (!egl.eglDestroySurface(eglDisplay, mEglPBSurface)) {
            Gdx.app.log(TAG, "Error! eglDestroySurface failed:" + egl.eglGetError());
        }
        mEglPBSurface = null;
    }

    private void initContext() {
        Gdx.app.log(TAG, "Thread start! " + Thread.currentThread().getId());
        egl = (EGL10) EGLContext.getEGL();
        Gdx.app.log(TAG, "egl: " + egl + ", sharedContext.getEGL = " + sharedContext.getEGL());
        eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        Gdx.app.log(TAG, "eglDisplay " + eglDisplay + ", default = " + egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY));

        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        int[] configSpec = {
                EGL10.EGL_DEPTH_SIZE, 0,
                EGL10.EGL_NONE
        };
        egl.eglChooseConfig(eglDisplay, configSpec, configs, 1,
                num_config);
        EGLConfig mEglConfig = configs[0];

        int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE};
        mEglContext = egl.eglCreateContext(eglDisplay, mEglConfig, sharedContext, // EGL10.EGL_NO_CONTEXT,
                attrib_list);
        Gdx.app.log(TAG, "Loaded context: " + mEglContext);
        if (mEglContext == EGL10.EGL_NO_CONTEXT) {
            Gdx.app.log(TAG, "Error! eglCreateContext Failed!");
        }


        int attribListPbuffer[] = {
                // The NDK code would never draw to Pbuffer, so it's not neccessary to
                // match anything.
                EGL10.EGL_WIDTH, 32, EGL10.EGL_HEIGHT, 32, EGL10.EGL_NONE};

        mEglPBSurface = egl.eglCreatePbufferSurface(eglDisplay, mEglConfig,
                attribListPbuffer);
        if (mEglPBSurface == EGL10.EGL_NO_SURFACE) {
            Gdx.app.log(TAG, "Error! eglCreatePbufferSurface failed!");
        }

        if (!egl.eglMakeCurrent(eglDisplay, mEglPBSurface, mEglPBSurface, mEglContext)) {
            Gdx.app.log(TAG, "Error! eglMakeCurrent failed:" + egl.eglGetError());
        }

        gl = (GL10) mEglContext.getGL();
        Gdx.app.log(TAG, "mEglContext.getGL() = " + gl + ", sharedContext gl: " + sharedContext.getGL());

    }

    @Override
    public synchronized void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<T> parameter) {
        initContext();
        /*
        if task was removed from the assets manager load queue and loadSync wasn't called, assets manager
        don't know that there is an object that needs to be disposed.
         */
        if (disposable != null) {
            disposable.dispose();
            disposable = null;
        }
        this.disposable = loadTexture(file);
        endContext();
    }


    @Override
    public synchronized T loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<T> parameter) {
        T t = disposable;
        disposable = null;
        return t;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<T> parameter) {
        return null;
    }

    public abstract T loadTexture(FileHandle file);

}
