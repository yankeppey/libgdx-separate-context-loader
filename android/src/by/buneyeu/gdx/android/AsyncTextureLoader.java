package by.buneyeu.gdx.android;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import javax.microedition.khronos.egl.EGLContext;

/**
 * A simple implementation of {@link SeparateContextAssetLoader} that loads {@link Texture} in a
 * separate gl context.
 *
 * @author Andrei Buneyeu
 */
public class AsyncTextureLoader extends SeparateContextAssetLoader<Texture> {

    public AsyncTextureLoader(EGLContext eglContext) {
        super(eglContext);
    }

    @Override
    public Texture loadTexture(FileHandle file) {
        return new Texture(file);
    }

}
