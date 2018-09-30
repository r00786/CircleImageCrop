package circlecrop.rohit.image.gpuimage;

import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class GPUImage {
    private final Context mContext;
    private Bitmap mCurrentBitmap;
    private GPUImageFilter mFilter;
    private GLSurfaceView mGlSurfaceView;
    private final GPUImageRenderer mRenderer;
    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    class C34391 implements Runnable {
        C34391() {
        }

        public void run() {
            synchronized (GPUImage.this.mFilter) {
                GPUImage.this.mFilter.destroy();
                GPUImage.this.mFilter.notify();
            }
        }
    }

    private abstract class LoadImageTask extends AsyncTask<Void, Void, Bitmap> {
        private final GPUImage mGPUImage;
        private int mOutputHeight;
        private int mOutputWidth;

        protected abstract Bitmap decode(Options options);

        protected abstract int getImageOrientation() throws IOException;

        public LoadImageTask(GPUImage gpuImage) {
            this.mGPUImage = gpuImage;
        }

        protected Bitmap doInBackground(Void... params) {
            if (GPUImage.this.mRenderer != null && GPUImage.this.mRenderer.getFrameWidth() == 0) {
                try {
                    synchronized (GPUImage.this.mRenderer.mSurfaceChangedWaiter) {
                        GPUImage.this.mRenderer.mSurfaceChangedWaiter.wait(3000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.mOutputWidth = GPUImage.this.getOutputWidth();
            this.mOutputHeight = GPUImage.this.getOutputHeight();
            return loadResizedImage();
        }

        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            this.mGPUImage.deleteImage();
            this.mGPUImage.setImage(bitmap);
        }

        private Bitmap loadResizedImage() {
            Options options = new Options();
            options.inJustDecodeBounds = true;
            decode(options);
            int scale = 1;
            while (true) {
                boolean z;
                if (options.outWidth / scale > this.mOutputWidth) {
                    z = true;
                } else {
                    z = false;
                }
                if (!checkSize(z, options.outHeight / scale > this.mOutputHeight)) {
                    break;
                }
                scale++;
            }
            scale--;
            if (scale < 1) {
                scale = 1;
            }
            options = new Options();
            options.inSampleSize = scale;
            options.inPreferredConfig = Config.RGB_565;
            options.inPurgeable = true;
            options.inTempStorage = new byte[32768];
            Bitmap bitmap = decode(options);
            if (bitmap == null) {
                return null;
            }
            return scaleBitmap(rotateImage(bitmap));
        }

        private Bitmap scaleBitmap(Bitmap bitmap) {
            int[] newSize = getScaleSize(bitmap.getWidth(), bitmap.getHeight());
            Bitmap workBitmap = Bitmap.createScaledBitmap(bitmap, newSize[0], newSize[1], true);
            if (workBitmap != bitmap) {
                bitmap.recycle();
                bitmap = workBitmap;
                System.gc();
            }
            if (GPUImage.this.mScaleType != ScaleType.CENTER_CROP) {
                return bitmap;
            }
            int diffWidth = newSize[0] - this.mOutputWidth;
            int diffHeight = newSize[1] - this.mOutputHeight;
            workBitmap = Bitmap.createBitmap(bitmap, diffWidth / 2, diffHeight / 2, newSize[0] - diffWidth, newSize[1] - diffHeight);
            if (workBitmap == bitmap) {
                return bitmap;
            }
            bitmap.recycle();
            return workBitmap;
        }

        private int[] getScaleSize(int width, int height) {
            float newHeight;
            float newWidth;
            float withRatio = ((float) width) / ((float) this.mOutputWidth);
            float heightRatio = ((float) height) / ((float) this.mOutputHeight);
            boolean adjustWidth = GPUImage.this.mScaleType == ScaleType.CENTER_CROP ? withRatio > heightRatio : withRatio < heightRatio;
            if (adjustWidth) {
                newHeight = (float) this.mOutputHeight;
                newWidth = (newHeight / ((float) height)) * ((float) width);
            } else {
                newWidth = (float) this.mOutputWidth;
                newHeight = (newWidth / ((float) width)) * ((float) height);
            }
            return new int[]{Math.round(newWidth), Math.round(newHeight)};
        }

        private boolean checkSize(boolean widthBigger, boolean heightBigger) {
            boolean z = false;
            if (GPUImage.this.mScaleType != ScaleType.CENTER_CROP) {
                if (widthBigger || heightBigger) {
                    z = true;
                }
                return z;
            } else if (widthBigger && heightBigger) {
                return true;
            } else {
                return false;
            }
        }

        private Bitmap rotateImage(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            Bitmap rotatedBitmap = bitmap;
            try {
                int orientation = getImageOrientation();
                if (orientation == 0) {
                    return rotatedBitmap;
                }
                Matrix matrix = new Matrix();
                matrix.postRotate((float) orientation);
                rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                return rotatedBitmap;
            } catch (IOException e) {
                e.printStackTrace();
                return rotatedBitmap;
            }
        }
    }

    private class LoadImageFileTask extends LoadImageTask {
        private final File mImageFile;

        public LoadImageFileTask(GPUImage gpuImage, File file) {
            super(gpuImage);
            this.mImageFile = file;
        }

        protected Bitmap decode(Options options) {
            return BitmapFactory.decodeFile(this.mImageFile.getAbsolutePath(), options);
        }

        protected int getImageOrientation() throws IOException {
            switch (new ExifInterface(this.mImageFile.getAbsolutePath()).getAttributeInt("Orientation", 1)) {
                case 3:
                    return 180;
                case 6:
                    return 90;
                case 8:
                    return 270;
                default:
                    return 0;
            }
        }
    }

    private class LoadImageUriTask extends LoadImageTask {
        private final Uri mUri;

        public LoadImageUriTask(GPUImage gpuImage, Uri uri) {
            super(gpuImage);
            this.mUri = uri;
        }

        protected Bitmap decode(Options options) {
            try {
                InputStream inputStream;
                if (this.mUri.getScheme().startsWith("http") || this.mUri.getScheme().startsWith("https")) {
                    inputStream = new URL(this.mUri.toString()).openStream();
                } else {
                    inputStream = GPUImage.this.mContext.getContentResolver().openInputStream(this.mUri);
                }
                return BitmapFactory.decodeStream(inputStream, null, options);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected int getImageOrientation() throws IOException {
            Cursor cursor = GPUImage.this.mContext.getContentResolver().query(this.mUri, new String[]{"orientation"}, null, null, null);
            if (cursor == null || cursor.getCount() != 1) {
                return 0;
            }
            cursor.moveToFirst();
            int orientation = cursor.getInt(0);
            cursor.close();
            return orientation;
        }
    }

    public enum ScaleType {
        CENTER_INSIDE,
        CENTER_CROP
    }

    public GPUImage(Context context) {
        if (supportsOpenGLES2(context)) {
            this.mContext = context;
            this.mFilter = new GPUImageFilter();
            this.mRenderer = new GPUImageRenderer(this.mFilter);
            return;
        }
        throw new IllegalStateException("OpenGL ES 2.0 is not supported on this phone.");
    }

    private boolean supportsOpenGLES2(Context context) {
        return ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo().reqGlEsVersion >= 131072;
    }

    public void setGLSurfaceView(GLSurfaceView view) {
        this.mGlSurfaceView = view;
        this.mGlSurfaceView.setEGLContextClientVersion(2);
        this.mGlSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        this.mGlSurfaceView.getHolder().setFormat(1);
        this.mGlSurfaceView.setRenderer(this.mRenderer);
        this.mGlSurfaceView.setRenderMode(0);
        this.mGlSurfaceView.requestRender();
    }

    public void requestRender() {
        if (this.mGlSurfaceView != null) {
            this.mGlSurfaceView.requestRender();
        }
    }

    public void setFilter(GPUImageFilter filter) {
        this.mFilter = filter;
        this.mRenderer.setFilter(this.mFilter);
        requestRender();
    }

    public void setImage(Bitmap bitmap) {
        this.mCurrentBitmap = bitmap;
        this.mRenderer.setImageBitmap(bitmap, false);
        requestRender();
    }

    public void setScaleType(ScaleType scaleType) {
        this.mScaleType = scaleType;
        this.mRenderer.setScaleType(scaleType);
        this.mRenderer.deleteImage();
        this.mCurrentBitmap = null;
        requestRender();
    }

    public void setRotationAngle(float rotationAngle) {
        this.mRenderer.setRotationAngle(rotationAngle);
        requestRender();
    }

    public float getRotationAngle() {
        return this.mRenderer.getRotationAngle();
    }

    public void rotate(boolean clockwise) {
        this.mRenderer.rotate(clockwise);
        requestRender();
    }

    public void setRotation(Rotation rotation) {
        this.mRenderer.setRotation(rotation);
        requestRender();
    }

    public void setScaleFactor(float scaleFactor) {
        this.mRenderer.setScaleFactor(scaleFactor);
        requestRender();
    }

    public void setTransform(float x, float y) {
        this.mRenderer.setTranslate(x, y);
        requestRender();
    }

    public void setTransformOffsetLimit(float leftOffset, float rightOffset, float topOffset, float bottomOffset, float outputWidth, float outputHeight) {
        this.mRenderer.setTransformOffsetLimit(leftOffset, rightOffset, topOffset, bottomOffset, outputWidth, outputHeight);
    }

    public PointF getCropTopLeft() {
        return this.mRenderer.getCropTopLeft();
    }

    public PointF getCropTopRight() {
        return this.mRenderer.getCropTopRight();
    }

    public PointF getCropBottomLeft() {
        return this.mRenderer.getCropBottomLeft();
    }

    public PointF getCropBottomRight() {
        return this.mRenderer.getCropBottomRight();
    }

    public void setCropRectangle(PointF topLeft, PointF topRight, PointF bottomLeft, PointF bottomRight) {
        this.mRenderer.setCropRectangle(topLeft, topRight, bottomLeft, bottomRight);
    }

    public void deleteImage() {
        this.mRenderer.deleteImage();
        this.mCurrentBitmap = null;
        requestRender();
    }

    public void setImage(Uri uri) {
        new LoadImageUriTask(this, uri).execute(new Void[0]);
    }

    public void setImage(File file) {
        new LoadImageFileTask(this, file).execute(new Void[0]);
    }

    public Bitmap getBitmapWithFilterApplied() {
        return getBitmapWithFilterApplied(this.mCurrentBitmap);
    }

    public Bitmap getBitmapWithFilterApplied(Bitmap bitmap) {
        if (this.mGlSurfaceView != null) {
            this.mRenderer.deleteImage();
            this.mRenderer.runOnDraw(new C34391());
            synchronized (this.mFilter) {
                requestRender();
                try {
                    this.mFilter.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        GPUImageRenderer renderer = new GPUImageRenderer(this.mFilter);
        renderer.setRotation(Rotation.NORMAL);
        renderer.setScaleType(this.mScaleType);
        PixelBuffer buffer = new PixelBuffer(bitmap.getWidth(), bitmap.getHeight());
        buffer.setRenderer(renderer);
        renderer.setImageBitmap(bitmap, false);
        Bitmap result = buffer.getBitmap();
        this.mFilter.destroy();
        renderer.deleteImage();
        buffer.destroy();
        this.mRenderer.setFilter(this.mFilter);
        if (this.mCurrentBitmap != null) {
            this.mRenderer.setImageBitmap(this.mCurrentBitmap, false);
        }
        requestRender();
        return result;
    }

    void runOnGLThread(Runnable runnable) {
        this.mRenderer.runOnDrawEnd(runnable);
    }

    private int getOutputWidth() {
        if (this.mRenderer != null && this.mRenderer.getFrameWidth() != 0) {
            return this.mRenderer.getFrameWidth();
        }
        if (this.mCurrentBitmap != null) {
            return this.mCurrentBitmap.getWidth();
        }
        return ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    }

    private int getOutputHeight() {
        if (this.mRenderer != null && this.mRenderer.getFrameHeight() != 0) {
            return this.mRenderer.getFrameHeight();
        }
        if (this.mCurrentBitmap != null) {
            return this.mCurrentBitmap.getHeight();
        }
        return ((WindowManager) this.mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
    }
}
