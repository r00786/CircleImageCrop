package circlecrop.rohit.image.gpuimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PointF;
import android.net.Uri;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

import java.io.File;
import java.nio.IntBuffer;
import java.util.concurrent.Semaphore;


public class ImageForeGround extends FrameLayout {
    private GestureDetector mDragDetector;
    private boolean mEditMode;
    private GPUImageFilter mFilter;
    public Size mForceSize = null;
    private GLSurfaceView mGLSurfaceView;
    private GPUImage mGPUImage;
    private float mRatio = 0.0f;
    private ScaleGestureDetector mScaleDetector;

    class C34481 implements OnGlobalLayoutListener {
        final /* synthetic */ ImageForeGround this$0=null;
        final /* synthetic */ Semaphore val$waiter=null;

        public void onGlobalLayout() {
            if (VERSION.SDK_INT < 16) {
                this.this$0.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            } else {
                this.this$0.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
            this.val$waiter.release();
        }
    }

    private class DragListener extends SimpleOnGestureListener {
        private DragListener() {
        }

        public boolean onDown(MotionEvent e) {
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (ImageForeGround.this.mEditMode) {
                ImageForeGround.this.setTransform(distanceX, distanceY);
            }
            return true;
        }
    }

    private class GPUImageGLSurfaceView extends GLSurfaceView {
        public GPUImageGLSurfaceView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (ImageForeGround.this.mForceSize != null) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(ImageForeGround.this.mForceSize.width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(ImageForeGround.this.mForceSize.height, MeasureSpec.EXACTLY));
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    private class ScaleListener extends SimpleOnScaleGestureListener {
        private ScaleListener() {
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (mEditMode) {
               setScaleFactor(detector.getScaleFactor());
            }
            return true;
        }
    }

    public static class Size {
        int height;
        int width;
    }

    public ImageForeGround(Context context) {
        super(context);
        init(context, null);
    }

    public ImageForeGround(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mGLSurfaceView = new GPUImageGLSurfaceView(context, attrs);
        this.mGLSurfaceView.setPreserveEGLContextOnPause(true);
        addView(this.mGLSurfaceView);
        this.mGPUImage = new GPUImage(getContext());
        this.mGPUImage.setGLSurfaceView(this.mGLSurfaceView);
        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.mDragDetector = new GestureDetector(context, new DragListener());
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mRatio != 0.0f) {
            int newWidth;
            int newHeight;
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (((float) width) / this.mRatio < ((float) height)) {
                newWidth = width;
                newHeight = Math.round(((float) width) / this.mRatio);
            } else {
                newHeight = height;
                newWidth = Math.round(((float) height) * this.mRatio);
            }
            super.onMeasure(MeasureSpec.makeMeasureSpec(newWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY));
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public GPUImage getGPUImage() {
        return this.mGPUImage;
    }

    public void setRatio(float ratio) {
        this.mRatio = ratio;
        this.mGLSurfaceView.requestLayout();
        this.mGPUImage.deleteImage();
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        this.mGPUImage.setScaleType(scaleType);
    }

    public void setRotation(Rotation rotation) {
        this.mGPUImage.setRotation(rotation);
        requestRender();
    }

    public void rotate(boolean clockwise) {
        this.mGPUImage.rotate(clockwise);
        requestRender();
    }

    public void setRotationAngle(float angle) {
        this.mGPUImage.setRotationAngle(angle);
        requestRender();
    }

    public float getRotationAngle() {
        return this.mGPUImage.getRotationAngle();
    }

    public void setCropRectangle(PointF topLeft, PointF topRight, PointF bottomLeft, PointF bottomRight) {
        this.mGPUImage.setCropRectangle(topLeft, topRight, bottomLeft, bottomRight);
        requestRender();
    }

    public void setEditMode(boolean editMode) {
        this.mEditMode = editMode;
    }

    public void setFilter(GPUImageFilter filter) {
        this.mFilter = filter;
        this.mGPUImage.setFilter(filter);
        requestRender();
    }

    public GPUImageFilter getFilter() {
        return this.mFilter;
    }

    public void setImage(Bitmap bitmap) {
        this.mGPUImage.setImage(bitmap);
    }

    public void setImage(Bitmap bitmap, Runnable renderCallback) {
        setImage(bitmap);
        this.mGPUImage.runOnGLThread(renderCallback);
    }

    public void setImage(Uri uri) {
        this.mGPUImage.setImage(uri);
    }

    public void setImage(File file) {
        this.mGPUImage.setImage(file);
    }

    public void setScaleFactor(float scaleFactor) {
        this.mGPUImage.setScaleFactor(scaleFactor);
    }

    public void setTransform(float x, float y) {
        this.mGPUImage.setTransform(x, y);
    }

    public void setTransformOffsetLimit(float leftOffset, float rightOffset, float topOffset, float bottomOffset, float width, float height) {
        this.mGPUImage.setTransformOffsetLimit(leftOffset, rightOffset, topOffset, bottomOffset, width, height);
    }

    public PointF getCropTopLeft() {
        return this.mGPUImage.getCropTopLeft();
    }

    public PointF getCropTopRight() {
        return this.mGPUImage.getCropTopRight();
    }

    public PointF getCropBottomLeft() {
        return this.mGPUImage.getCropBottomLeft();
    }

    public PointF getCropBottomRight() {
        return this.mGPUImage.getCropBottomRight();
    }

    public void requestRender() {
        this.mGLSurfaceView.requestRender();
    }

    public Bitmap captureCroppedWithPadding(int paddingLeft, int paddingTop) throws InterruptedException {
        final Semaphore waiter = new Semaphore(0);
        final int width = this.mGLSurfaceView.getMeasuredWidth() - (paddingLeft * 2);
        final int height = width;
        final int cropStartX = paddingLeft;
        final int cropStartY = getHeight() - (paddingTop + height);
        final int[] pixelMirroredArray = new int[(width * height)];
        this.mGPUImage.runOnGLThread(new Runnable() {
            public void run() {
                IntBuffer pixelBuffer = IntBuffer.allocate(width * height);
                GLES20.glReadPixels(cropStartX, cropStartY, width, height, 6408, 5121, pixelBuffer);
                int[] pixelArray = pixelBuffer.array();
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        pixelMirroredArray[(((height - i) - 1) * width) + j] = pixelArray[(width * i) + j];
                    }
                }
                waiter.release();
            }
        });
        requestRender();
        Log.e("GPUImageView", "Semaphore acquire");
        waiter.acquire();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(pixelMirroredArray));
        return bitmap;
    }

    public void onPause() {
        this.mGLSurfaceView.onPause();
    }

    public void onResume() {
        this.mGLSurfaceView.onResume();
    }

    public boolean onTouchEvent(MotionEvent event) {
        return this.mDragDetector.onTouchEvent(event) || this.mScaleDetector.onTouchEvent(event);
    }
}
