package circlecrop.rohit.image.gpuimage;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;



import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Queue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import circlecrop.rohit.image.gpuimage.util.TextureRotationUtil;


@TargetApi(11)
public class GPUImageRenderer implements Renderer {
    static final float[] CUBE = new float[]{-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};
    private float baseScaleRatioHeight = 1.0f;
    private float baseScaleRatioWidth = 1.0f;
    private float mBackgroundBlue = 0.0f;
    private float mBackgroundGreen = 0.0f;
    private float mBackgroundRed = 0.0f;
    private float mCircleRadius = 0.0f;
    private GPUImageFilter mFilter;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private int mGLTextureId = -1;
    private int mImageHeight;
    private int mImageWidth;
    private int mOutputHeight;
    private int mOutputWidth;
    private Rotation mRotation = Rotation.NORMAL;
    private float mRotationAngle;
    private final Queue<Runnable> mRunOnDraw = new LinkedList();
    private final Queue<Runnable> mRunOnDrawEnd = new LinkedList();
    private float mScaleRatio = 1.0f;
    private GPUImage.ScaleType mScaleType = GPUImage.ScaleType.CENTER_CROP;
    public final Object mSurfaceChangedWaiter = new Object();
    private SurfaceTexture mSurfaceTexture = null;
    private float[] mTransformCenterCords = new float[]{0.5f, 0.5f};
    private float[] transformCenter = new float[2];
    private Matrix transformMatrix = new Matrix();

    class C34462 implements Runnable {
        C34462() {
        }

        public void run() {
            GLES20.glDeleteTextures(1, new int[]{GPUImageRenderer.this.mGLTextureId}, 0);
            GPUImageRenderer.this.mGLTextureId = -1;
        }
    }

    public GPUImageRenderer(GPUImageFilter filter) {
        this.mFilter = filter;
        this.mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mGLCubeBuffer.put(CUBE).position(0);
        this.mGLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(this.mBackgroundRed, this.mBackgroundGreen, this.mBackgroundBlue, 1.0f);
        GLES20.glDisable(2929);
        this.mFilter.init();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mOutputWidth = width;
        this.mOutputHeight = height;
        GLES20.glViewport(0, 0, width, height);
        GLES20.glUseProgram(this.mFilter.getProgram());
        this.mFilter.onOutputSizeChanged(width, height);
        initilizeTransformMatrix();
        adjustImageTransform();
        synchronized (this.mSurfaceChangedWaiter) {
            this.mSurfaceChangedWaiter.notifyAll();
        }
    }

    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(16640);
        runAll(this.mRunOnDraw);
        this.mFilter.onDraw(this.mGLTextureId, this.mGLCubeBuffer, this.mGLTextureBuffer);
        runAll(this.mRunOnDrawEnd);
        if (this.mSurfaceTexture != null) {
            this.mSurfaceTexture.updateTexImage();
        }
    }

    private void runAll(Queue<Runnable> queue) {
        synchronized (queue) {
            while (!queue.isEmpty()) {
                ((Runnable) queue.poll()).run();
            }
        }
    }

    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(new Runnable() {
            public void run() {
                GPUImageFilter oldFilter = GPUImageRenderer.this.mFilter;
                GPUImageRenderer.this.mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                GPUImageRenderer.this.mFilter.init();
                GLES20.glUseProgram(GPUImageRenderer.this.mFilter.getProgram());
                GPUImageRenderer.this.mFilter.onOutputSizeChanged(GPUImageRenderer.this.mOutputWidth, GPUImageRenderer.this.mOutputHeight);
            }
        });
    }

    public void deleteImage() {
        runOnDraw(new C34462());
    }

    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap != null) {
            runOnDraw(new Runnable() {
                public void run() {
                    Bitmap resizedBitmap = null;
                    if (bitmap.getWidth() % 2 == 1) {
                        resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(), Config.ARGB_8888);
                        Canvas can = new Canvas(resizedBitmap);
                        can.drawARGB(0, 0, 0, 0);
                        can.drawBitmap(bitmap, 0.0f, 0.0f, null);
                    }
                    GPUImageRenderer.this.mGLTextureId = OpenGlUtils.loadTexture(resizedBitmap != null ? resizedBitmap : bitmap, GPUImageRenderer.this.mGLTextureId, recycle);
                    if (resizedBitmap != null) {
                        resizedBitmap.recycle();
                    }
                    GPUImageRenderer.this.mImageWidth = bitmap.getWidth();
                    GPUImageRenderer.this.mImageHeight = bitmap.getHeight();
                    GPUImageRenderer.this.initilizeTransformMatrix();
                    GPUImageRenderer.this.adjustImageTransform();
                }
            });
        }
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        this.mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return this.mOutputWidth;
    }

    protected int getFrameHeight() {
        return this.mOutputHeight;
    }

    private void initilizeTransformMatrix() {
        if (this.mCircleRadius == 0.0f) {
            this.mCircleRadius = ((float) Math.min(this.mOutputWidth, this.mOutputHeight)) / 2.0f;
        }
        float ratio1 = ((float) this.mImageWidth) / ((float) this.mOutputWidth);
        float ratio2 = ((float) this.mImageHeight) / ((float) this.mOutputHeight);
        if (this.mImageWidth >= this.mImageHeight) {
            this.baseScaleRatioHeight = ((float) this.mOutputHeight) / (this.mCircleRadius * 2.0f);
            this.baseScaleRatioWidth = (this.baseScaleRatioHeight * ratio2) / ratio1;
        } else {
            this.baseScaleRatioWidth = ((float) this.mOutputWidth) / (this.mCircleRadius * 2.0f);
            this.baseScaleRatioHeight = (this.baseScaleRatioWidth * ratio1) / ratio2;
        }
        this.mRotationAngle = 0.0f;
        this.mRotation = Rotation.NORMAL;
        this.mScaleRatio = 1.0f;
        this.transformMatrix = new Matrix();
        this.transformMatrix.setScale(this.baseScaleRatioWidth, this.baseScaleRatioHeight, 0.5f, 0.5f);
    }

    private boolean adjustImageTransform() {
        float[] cube = CUBE;
        float[] textureCords = new float[8];
        float[] transformCenter = new float[2];
        this.transformMatrix.mapPoints(textureCords, TextureRotationUtil.TEXTURE_NO_ROTATION);
        this.transformMatrix.mapPoints(transformCenter, this.mTransformCenterCords);
        float imageWidthInPixel = (((float) this.mOutputWidth) / this.baseScaleRatioWidth) / this.mScaleRatio;
        float imageHeightInPixel = (((float) this.mOutputHeight) / this.baseScaleRatioHeight) / this.mScaleRatio;
        if (transformCenter[0] < this.mCircleRadius / imageWidthInPixel) {
            this.transformMatrix.postTranslate((this.mCircleRadius / imageWidthInPixel) - transformCenter[0], 0.0f);
        } else if (1.0f - transformCenter[0] < this.mCircleRadius / imageWidthInPixel) {
            this.transformMatrix.postTranslate((1.0f - transformCenter[0]) - (this.mCircleRadius / imageWidthInPixel), 0.0f);
        }
        if (transformCenter[1] < this.mCircleRadius / imageHeightInPixel) {
            this.transformMatrix.postTranslate(0.0f, (this.mCircleRadius / imageHeightInPixel) - transformCenter[1]);
        } else if (1.0f - transformCenter[1] < this.mCircleRadius / imageHeightInPixel) {
            this.transformMatrix.postTranslate(0.0f, (1.0f - transformCenter[1]) - (this.mCircleRadius / imageHeightInPixel));
        }
        this.transformMatrix.mapPoints(textureCords, TextureRotationUtil.TEXTURE_NO_ROTATION);
        this.transformMatrix.mapPoints(transformCenter, this.mTransformCenterCords);
        this.transformCenter = transformCenter;
        this.mGLCubeBuffer.clear();
        this.mGLCubeBuffer.put(cube).position(0);
        this.mGLTextureBuffer.clear();
        this.mGLTextureBuffer.put(textureCords).position(0);
        return true;
    }

    public void setScaleFactor(float scaleFactor) {
        float newScaleRatio = this.mScaleRatio / scaleFactor;
        if (((double) newScaleRatio) >= 0.1d && newScaleRatio <= 1.0f) {
            this.transformMatrix.postScale(1.0f / scaleFactor, 1.0f / scaleFactor, this.transformCenter[0], this.transformCenter[1]);
            this.mScaleRatio = newScaleRatio;
            adjustImageTransform();
        }
    }

    public void setTranslate(float x, float y) {
        float[] transformedTranslate = new float[]{(x / ((float) this.mOutputWidth)) * this.mScaleRatio, (y / ((float) this.mOutputHeight)) * this.mScaleRatio};
        Matrix matrix = new Matrix();
        matrix.postRotate(((float) this.mRotation.asInt()) + this.mRotationAngle);
        matrix.mapPoints(transformedTranslate);
        this.transformMatrix.postTranslate(transformedTranslate[0], transformedTranslate[1]);
        adjustImageTransform();
    }

    public PointF getCropTopLeft() {
        return getCenterCoordinatesWithOffset(-this.mCircleRadius, -this.mCircleRadius);
    }

    public PointF getCropTopRight() {
        return getCenterCoordinatesWithOffset(this.mCircleRadius, -this.mCircleRadius);
    }

    public PointF getCropBottomLeft() {
        return getCenterCoordinatesWithOffset(-this.mCircleRadius, this.mCircleRadius);
    }

    public PointF getCropBottomRight() {
        return getCenterCoordinatesWithOffset(this.mCircleRadius, this.mCircleRadius);
    }

    private PointF getCenterCoordinatesWithOffset(float offsetX, float offsetY) {
        float[] coords = new float[]{this.mTransformCenterCords[0] + (offsetX / ((float) this.mOutputWidth)), this.mTransformCenterCords[1] + (offsetY / ((float) this.mOutputHeight))};
        this.transformMatrix.mapPoints(coords);
        return new PointF(coords[0], coords[1]);
    }

    public void setCropRectangle(PointF topLeft, PointF topRight, PointF bottomLeft, PointF bottomRight) {
        Matrix pointsOriginal = new Matrix();
        pointsOriginal.setValues(new float[]{this.mTransformCenterCords[0] - (this.mCircleRadius / ((float) this.mOutputWidth)), this.mTransformCenterCords[0] + (this.mCircleRadius / ((float) this.mOutputWidth)), this.mTransformCenterCords[0] - (this.mCircleRadius / ((float) this.mOutputWidth)), this.mTransformCenterCords[1] - (this.mCircleRadius / ((float) this.mOutputHeight)), this.mTransformCenterCords[1] - (this.mCircleRadius / ((float) this.mOutputHeight)), this.mTransformCenterCords[1] + (this.mCircleRadius / ((float) this.mOutputHeight)), 1.0f, 1.0f, 1.0f});
        pointsOriginal.invert(pointsOriginal);
        Matrix pointsAfterTransform = new Matrix();
        pointsAfterTransform.setValues(new float[]{topLeft.x, topRight.x, bottomLeft.x, topLeft.y, topRight.y, bottomLeft.y, 1.0f, 1.0f, 1.0f});
        this.transformMatrix.setConcat(pointsAfterTransform, pointsOriginal);
        float[] p1 = new float[]{0.0f, 0.0f};
        float[] p2 = new float[]{1.0f, 0.0f};
        this.transformMatrix.mapPoints(p1);
        this.transformMatrix.mapPoints(p2);
        float diffX = ((p2[1] - p1[1]) * ((float) this.mOutputHeight)) / this.baseScaleRatioHeight;
        float diffY = ((p2[0] - p1[0]) * ((float) this.mOutputWidth)) / this.baseScaleRatioWidth;
        float angle = (float) Math.toDegrees(Math.atan2((double) diffX, (double) diffY));
        this.mRotationAngle = ((225.0f + angle) % 90.0f) - 45.0f;
        switch (((((int) angle) + 405) / 90) % 4) {
            case 1:
                this.mRotation = Rotation.ROTATION_90;
                break;
            case 2:
                this.mRotation = Rotation.ROTATION_180;
                break;
            case 3:
                this.mRotation = Rotation.ROTATION_270;
                break;
            default:
                this.mRotation = Rotation.NORMAL;
                break;
        }
        this.mScaleRatio = ((float) Math.sqrt((double) ((diffX * diffX) + (diffY * diffY)))) / ((float) this.mOutputWidth);
        adjustImageTransform();
    }

    public void setTransformOffsetLimit(float leftOffset, float rightOffset, float topOffset, float bottomOffset, float outputWidth, float outputHeight) {
        this.mTransformCenterCords[0] = (((leftOffset - rightOffset) * 0.5f) / outputWidth) + 0.5f;
        this.mTransformCenterCords[1] = (((topOffset - bottomOffset) * 0.5f) / outputHeight) + 0.5f;
        this.mCircleRadius = ((outputWidth - leftOffset) - rightOffset) / 2.0f;
    }

    public void setRotationAngle(float rotationAngle) {
        Matrix matrix = new Matrix();
        matrix.setScale(1.0f, ((float) this.mImageHeight) / ((float) this.mImageWidth), this.transformCenter[0], this.transformCenter[1]);
        matrix.postRotate(rotationAngle - this.mRotationAngle, this.transformCenter[0], this.transformCenter[1]);
        matrix.postScale(1.0f, ((float) this.mImageWidth) / ((float) this.mImageHeight), this.transformCenter[0], this.transformCenter[1]);
        this.transformMatrix.postConcat(matrix);
        adjustImageTransform();
        this.mRotationAngle = rotationAngle;
    }

    public float getRotationAngle() {
        return this.mRotationAngle;
    }

    public void rotate(boolean clockwise) {
        setRotation(clockwise ? this.mRotation.clockwiseNext() : this.mRotation.counterClockwiseNext());
    }

    public void setRotation(Rotation rotation) {
        Matrix matrix = new Matrix();
        matrix.setScale(1.0f, ((float) this.mImageHeight) / ((float) this.mImageWidth), this.transformCenter[0], this.transformCenter[1]);
        matrix.postRotate((float) (rotation.asInt() - this.mRotation.asInt()), this.transformCenter[0], this.transformCenter[1]);
        matrix.postScale(1.0f, ((float) this.mImageWidth) / ((float) this.mImageHeight), this.transformCenter[0], this.transformCenter[1]);
        this.transformMatrix.postConcat(matrix);
        adjustImageTransform();
        this.mRotation = rotation;
    }

    protected void runOnDraw(Runnable runnable) {
        synchronized (this.mRunOnDraw) {
            this.mRunOnDraw.add(runnable);
        }
    }

    protected void runOnDrawEnd(Runnable runnable) {
        synchronized (this.mRunOnDrawEnd) {
            this.mRunOnDrawEnd.add(runnable);
        }
    }

}
