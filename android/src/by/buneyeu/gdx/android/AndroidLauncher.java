package by.buneyeu.gdx.android;

import android.os.Bundle;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.MyAndroidApplication;
import com.badlogic.gdx.backends.android.MyAndroidGraphics;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import javax.microedition.khronos.egl.EGLContext;

import by.buneyeu.gdx.YourGameApplication;

public class AndroidLauncher extends MyAndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new YourGameApplication(), config);

		getGraphics().setOnSurfaceCreatedListener(new MyAndroidGraphics.OnSurfaceCreatedListener() {
			@Override
			public void onSurfaceCreated() {
				EGLContext eglContext = getGraphics().getSharedContext();
				AssetManager manager = new AssetManager();
				manager.setLoader(Texture.class, new AsyncTextureLoader(eglContext));
				manager.load("badlogic.jpg", Texture.class);
				// pass asset manager to your application adapter
			}
		});
	}
}
