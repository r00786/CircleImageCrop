package circlecrop.rohit.image.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class PixelBuffer {
    private final int[] version;
    Bitmap mBitmap;
    EGL10 mEGL = ((EGL10) EGLContext.getEGL());
    EGLConfig mEGLConfig;
    EGLConfig[] mEGLConfigs;
    EGLContext mEGLContext;
    EGLDisplay mEGLDisplay = this.mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
    EGLSurface mEGLSurface;
    GL10 mGL;
    int mHeight;
    Renderer mRenderer;
    String mThreadOwner;
    int mWidth;

    public PixelBuffer(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        version = new int[2];
        int[] attribList = new int[]{12375, this.mWidth, 12374, this.mHeight, 12344};
        this.mEGL.eglInitialize(this.mEGLDisplay, version);
        this.mEGLConfig = chooseConfig();
        this.mEGLContext = this.mEGL.eglCreateContext(this.mEGLDisplay, this.mEGLConfig, EGL10.EGL_NO_CONTEXT, new int[]{12440, 2, 12344});
        this.mEGLSurface = this.mEGL.eglCreatePbufferSurface(this.mEGLDisplay, this.mEGLConfig, attribList);
        this.mEGL.eglMakeCurrent(this.mEGLDisplay, this.mEGLSurface, this.mEGLSurface, this.mEGLContext);
        this.mGL = (GL10) this.mEGLContext.getGL();
        this.mThreadOwner = Thread.currentThread().getName();
    }

    public void setRenderer(Renderer renderer) {
        this.mRenderer = renderer;
        if (Thread.currentThread().getName().equals(this.mThreadOwner)) {
            this.mRenderer.onSurfaceCreated(this.mGL, this.mEGLConfig);
            this.mRenderer.onSurfaceChanged(this.mGL, this.mWidth, this.mHeight);
            return;
        }
        Log.e("PixelBuffer", "setRenderer: This thread does not own the OpenGL context.");
    }

    public Bitmap getBitmap() {
        if (this.mRenderer == null) {
            Log.e("PixelBuffer", "getBitmap: Renderer was not set.");
            return null;
        } else if (Thread.currentThread().getName().equals(this.mThreadOwner)) {
            this.mRenderer.onDrawFrame(this.mGL);
            this.mRenderer.onDrawFrame(this.mGL);
            convertToBitmap();
            return this.mBitmap;
        } else {
            Log.e("PixelBuffer", "getBitmap: This thread does not own the OpenGL context.");
            return null;
        }
    }

    public void destroy() {
        this.mRenderer.onDrawFrame(this.mGL);
        this.mRenderer.onDrawFrame(this.mGL);
        this.mEGL.eglMakeCurrent(this.mEGLDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
        this.mEGL.eglDestroySurface(this.mEGLDisplay, this.mEGLSurface);
        this.mEGL.eglDestroyContext(this.mEGLDisplay, this.mEGLContext);
        this.mEGL.eglTerminate(this.mEGLDisplay);
    }

    private EGLConfig chooseConfig() {
        int[] attribList = new int[]{12325, 0, 12326, 0, 12324, 8, 12323, 8, 12322, 8, 12321, 8, 12352, 4, 12344};
        int[] numConfig = new int[1];
        this.mEGL.eglChooseConfig(this.mEGLDisplay, attribList, null, 0, numConfig);
        int configSize = numConfig[0];
        this.mEGLConfigs = new EGLConfig[configSize];
        this.mEGL.eglChooseConfig(this.mEGLDisplay, attribList, this.mEGLConfigs, configSize, numConfig);
        return this.mEGLConfigs[0];
    }

    private void convertToBitmap() {
        int[] iat = new int[(this.mWidth * this.mHeight)];
        IntBuffer ib = IntBuffer.allocate(this.mWidth * this.mHeight);
        this.mGL.glReadPixels(0, 0, this.mWidth, this.mHeight, 6408, 5121, ib);
        int[] ia = ib.array();
        for (int i = 0; i < this.mHeight; i++) {
            for (int j = 0; j < this.mWidth; j++) {
                iat[(((this.mHeight - i) - 1) * this.mWidth) + j] = ia[(this.mWidth * i) + j];
            }
        }
        this.mBitmap = Bitmap.createBitmap(this.mWidth, this.mHeight, Config.ARGB_8888);
        this.mBitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat));
    }
}
