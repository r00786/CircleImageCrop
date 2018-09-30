package circlecrop.rohit.image.gpuimage;

import android.graphics.Point;
import android.graphics.PointF;
import android.opengl.GLES20;


import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;

public class GPUImageBCSFilter extends GPUImageFilter {
    private PointF[] mBlueControlPoints;
    private ArrayList<Float> mBlueCurve;
    private float mBrightness = 0.0f;
    private int mBrightnessLocation;
    private float mContrast = 1.0f;
    private int mContrastLocation;
    private PointF[] mGreenControlPoints;
    private ArrayList<Float> mGreenCurve;
    private PointF[] mRedControlPoints;
    private ArrayList<Float> mRedCurve;
    private PointF[] mRgbCompositeControlPoints;
    private ArrayList<Float> mRgbCompositeCurve;
    private float mSaturation = 1.0f;
    private int mSaturationLocation;
    private int[] mToneCurveTexture = new int[]{-1};
    private int mToneCurveTextureUniformLocation;
    private double k;
    private double[] dArr;

    class C34401 implements Runnable {
        C34401() {
        }

        public void run() {
            GLES20.glActiveTexture(33987);
            GLES20.glBindTexture(3553, GPUImageBCSFilter.this.mToneCurveTexture[0]);
            if (GPUImageBCSFilter.this.mRedCurve.size() >= LIConstants.OPTION_ENABLE_METADATA && GPUImageBCSFilter.this.mGreenCurve.size() >= LIConstants.OPTION_ENABLE_METADATA && GPUImageBCSFilter.this.mBlueCurve.size() >= LIConstants.OPTION_ENABLE_METADATA && GPUImageBCSFilter.this.mRgbCompositeCurve.size() >= LIConstants.OPTION_ENABLE_METADATA) {
                byte[] toneCurveByteArray = new byte[1024];
                for (int currentCurveIndex = 0; currentCurveIndex < LIConstants.OPTION_ENABLE_METADATA; currentCurveIndex++) {
                    toneCurveByteArray[(currentCurveIndex * 4) + 2] = (byte) (((int) Math.min(Math.max(((Float) GPUImageBCSFilter.this.mRgbCompositeCurve.get(currentCurveIndex)).floatValue() + (((float) currentCurveIndex) + ((Float) GPUImageBCSFilter.this.mBlueCurve.get(currentCurveIndex)).floatValue()), 0.0f), 255.0f)) & 255);
                    toneCurveByteArray[(currentCurveIndex * 4) + 1] = (byte) (((int) Math.min(Math.max(((Float) GPUImageBCSFilter.this.mRgbCompositeCurve.get(currentCurveIndex)).floatValue() + (((float) currentCurveIndex) + ((Float) GPUImageBCSFilter.this.mGreenCurve.get(currentCurveIndex)).floatValue()), 0.0f), 255.0f)) & 255);
                    toneCurveByteArray[currentCurveIndex * 4] = (byte) (((int) Math.min(Math.max(((Float) GPUImageBCSFilter.this.mRgbCompositeCurve.get(currentCurveIndex)).floatValue() + (((float) currentCurveIndex) + ((Float) GPUImageBCSFilter.this.mRedCurve.get(currentCurveIndex)).floatValue()), 0.0f), 255.0f)) & 255);
                    toneCurveByteArray[(currentCurveIndex * 4) + 3] = (byte) -1;
                }
                GLES20.glTexImage2D(3553, 0, 6408, LIConstants.OPTION_ENABLE_METADATA, 1, 0, 6408, 5121, ByteBuffer.wrap(toneCurveByteArray));
            }
        }
    }

    class C34412 implements Comparator<PointF> {
        C34412() {
        }

        public int compare(PointF point1, PointF point2) {
            if (point1.x < point2.x) {
                return -1;
            }
            if (point1.x > point2.x) {
                return 1;
            }
            return 0;
        }
    }

    public GPUImageBCSFilter() {
        super("attribute vec4 position;\nattribute vec4 inputTextureCoordinate;\n \nvarying vec2 textureCoordinate;\n \nvoid main()\n{\n    gl_Position = position;\n    textureCoordinate = inputTextureCoordinate.xy;\n}", "varying highp vec2 textureCoordinate;\n \n uniform sampler2D inputImageTexture;\n uniform lowp float brightness;\n uniform lowp float contrast;\n uniform lowp float saturation;\n uniform sampler2D toneCurveTexture;\n \n // Values from \"Graphics Shaders: Theory and Practice\" by Bailey and Cunningham\n const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);\n \n void main()\n {\n     if (any(lessThan(textureCoordinate, vec2(0.0,0.0))) || any(greaterThan(textureCoordinate, vec2(1.0,1.0)))) {\n         // apply clamp to border \n         gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);\n     } else {\n         lowp vec4 textureColor = texture2D(inputImageTexture, textureCoordinate);\n         \n         // apply tone curve \n         lowp float redCurveValue = texture2D(toneCurveTexture, vec2(textureColor.r, 0.0)).r;\n         lowp float greenCurveValue = texture2D(toneCurveTexture, vec2(textureColor.g, 0.0)).g;\n         lowp float blueCurveValue = texture2D(toneCurveTexture, vec2(textureColor.b, 0.0)).b;\n         textureColor = vec4(redCurveValue, greenCurveValue, blueCurveValue, textureColor.a);\n         \n         // apply brightness, contrast and saturation \n         textureColor = vec4((textureColor.rgb + vec3(brightness)), textureColor.w);\n         textureColor = vec4(((textureColor.rgb - vec3(0.5)) * contrast + vec3(0.5)), textureColor.w);\n         lowp float luminance = dot(textureColor.rgb, luminanceWeighting);\n         lowp vec3 greyScaleColor = vec3(luminance);\n         \n         gl_FragColor = vec4(mix(greyScaleColor, textureColor.rgb, saturation), textureColor.w);\n     }\n }");
        PointF[] defaultCurvePoints = new PointF[]{new PointF(0.0f, 0.0f), new PointF(0.5f, 0.5f), new PointF(1.0f, 1.0f)};
        this.mRgbCompositeControlPoints = defaultCurvePoints;
        this.mRedControlPoints = defaultCurvePoints;
        this.mGreenControlPoints = defaultCurvePoints;
        this.mBlueControlPoints = defaultCurvePoints;
    }

    public void onInit() {
        super.onInit();
        this.mBrightnessLocation = GLES20.glGetUniformLocation(getProgram(), "brightness");
        this.mContrastLocation = GLES20.glGetUniformLocation(getProgram(), "contrast");
        this.mSaturationLocation = GLES20.glGetUniformLocation(getProgram(), "saturation");
        this.mToneCurveTextureUniformLocation = GLES20.glGetUniformLocation(getProgram(), "toneCurveTexture");
        GLES20.glActiveTexture(33987);
        GLES20.glGenTextures(1, this.mToneCurveTexture, 0);
        GLES20.glBindTexture(3553, this.mToneCurveTexture[0]);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        GLES20.glTexParameteri(3553, 10242, 33071);
        GLES20.glTexParameteri(3553, 10243, 33071);
    }

    public void onInitialized() {
        super.onInitialized();
        setBrightness(this.mBrightness);
        setContrast(this.mContrast);
        setSaturation(this.mSaturation);
        setRgbCompositeControlPoints(this.mRgbCompositeControlPoints);
        setRedControlPoints(this.mRedControlPoints);
        setGreenControlPoints(this.mGreenControlPoints);
        setBlueControlPoints(this.mBlueControlPoints);
    }

    public void setBrightness(float brightness) {
        this.mBrightness = brightness;
        setFloat(this.mBrightnessLocation, this.mBrightness);
    }

    public void setContrast(float contrast) {
        this.mContrast = contrast;
        setFloat(this.mContrastLocation, this.mContrast);
    }

    public void setSaturation(float saturation) {
        this.mSaturation = saturation;
        setFloat(this.mSaturationLocation, this.mSaturation);
    }

    public void resetBCS() {
        setBrightness(0.0f);
        setContrast(1.0f);
        setSaturation(1.0f);
    }

    protected void onDrawArraysPre() {
        if (this.mToneCurveTexture[0] != -1) {
            GLES20.glActiveTexture(33987);
            GLES20.glBindTexture(3553, this.mToneCurveTexture[0]);
            GLES20.glUniform1i(this.mToneCurveTextureUniformLocation, 3);
        }
    }

    public void setRgbCompositeControlPoints(PointF[] points) {
        this.mRgbCompositeControlPoints = points;
        this.mRgbCompositeCurve = createSplineCurve(this.mRgbCompositeControlPoints);
        updateToneCurveTexture();
    }

    public void setRedControlPoints(PointF[] points) {
        this.mRedControlPoints = points;
        this.mRedCurve = createSplineCurve(this.mRedControlPoints);
        updateToneCurveTexture();
    }

    public void setGreenControlPoints(PointF[] points) {
        this.mGreenControlPoints = points;
        this.mGreenCurve = createSplineCurve(this.mGreenControlPoints);
        updateToneCurveTexture();
    }

    public void setBlueControlPoints(PointF[] points) {
        this.mBlueControlPoints = points;
        this.mBlueCurve = createSplineCurve(this.mBlueControlPoints);
        updateToneCurveTexture();
    }

    private void updateToneCurveTexture() {
        runOnDraw(new C34401());
    }

    private ArrayList<Float> createSplineCurve(PointF[] points) {
        if (points == null || points.length <= 0) {
            return null;
        }
        int i;
        PointF[] pointsSorted = (PointF[]) points.clone();
        Arrays.sort(pointsSorted, new C34412());
        Point[] convertedPoints = new Point[pointsSorted.length];
        for (i = 0; i < points.length; i++) {
            PointF point = pointsSorted[i];
            convertedPoints[i] = new Point((int) (point.x * 255.0f), (int) (point.y * 255.0f));
        }
        ArrayList<Point> splinePoints = createSplineCurve2(convertedPoints);
        Point firstSplinePoint = (Point) splinePoints.get(0);
        if (firstSplinePoint.x > 0) {
            for (i = firstSplinePoint.x; i >= 0; i--) {
                splinePoints.add(0, new Point(i, 0));
            }
        }
        Point lastSplinePoint = (Point) splinePoints.get(splinePoints.size() - 1);
        if (lastSplinePoint.x < 255) {
            for (i = lastSplinePoint.x + 1; i <= 255; i++) {
                splinePoints.add(new Point(i, 255));
            }
        }
        ArrayList<Float> preparedSplinePoints = new ArrayList(splinePoints.size());
        Iterator it = splinePoints.iterator();
        while (it.hasNext()) {
            Point newPoint = (Point) it.next();
            Point origPoint = new Point(newPoint.x, newPoint.x);
            float distance = (float) Math.sqrt(Math.pow((double) (origPoint.x - newPoint.x), 2.0d) + Math.pow((double) (origPoint.y - newPoint.y), 2.0d));
            if (origPoint.y > newPoint.y) {
                distance = -distance;
            }
            preparedSplinePoints.add(Float.valueOf(distance));
        }
        return preparedSplinePoints;
    }

    private ArrayList<Point> createSplineCurve2(Point[] points) {
        ArrayList<Double> sdA = createSecondDerivative(points);
        int n = sdA.size();
        if (n < 1) {
            return null;
        }
        int i;
        double[] sd = new double[n];
        for (i = 0; i < n; i++) {
            sd[i] = ((Double) sdA.get(i)).doubleValue();
        }
        ArrayList<Point> output = new ArrayList(n + 1);
        for (i = 0; i < n - 1; i++) {
            Point cur = points[i];
            Point next = points[i + 1];
            for (int x = cur.x; x < next.x; x++) {
                double t = ((double) (x - cur.x)) / ((double) (next.x - cur.x));
                double a = 1.0d - t;
                double b = t;
                double h = (double) (next.x - cur.x);
                double y = ((((double) cur.y) * a) + (((double) next.y) * b)) + (((h * h) / 6.0d) * (((((a * a) * a) - a) * sd[i]) + ((((b * b) * b) - b) * sd[i + 1])));
                if (y > 255.0d) {
                    y = 255.0d;
                } else if (y < 0.0d) {
                    y = 0.0d;
                }
                output.add(new Point(x, (int) Math.round(y)));
            }
        }
        if (output.size() != 255) {
            return output;
        }
        output.add(points[points.length - 1]);
        return output;
    }

    private ArrayList<Double> createSecondDerivative(Point[] points) {
        int n = points.length;
        if (n <= 1) {
            return null;
        }
        int i;
        double[][] matrix = (double[][]) Array.newInstance(Double.TYPE, new int[]{n, 3});
        double[] result = new double[n];
        matrix[0][1] = 1.0d;
        matrix[0][0] = 0.0d;
        matrix[0][2] = 0.0d;
        for (i = 1; i < n - 1; i++) {
            Point P1 = points[i - 1];
            Point P2 = points[i];
            Point P3 = points[i + 1];
            matrix[i][0] = ((double) (P2.x - P1.x)) / 6.0d;
            matrix[i][1] = ((double) (P3.x - P1.x)) / 3.0d;
            matrix[i][2] = ((double) (P3.x - P2.x)) / 6.0d;
            result[i] = (((double) (P3.y - P2.y)) / ((double) (P3.x - P2.x))) - (((double) (P2.y - P1.y)) / ((double) (P2.x - P1.x)));
        }
        result[0] = 0.0d;
        result[n - 1] = 0.0d;
        matrix[n - 1][1] = 1.0d;
        matrix[n - 1][0] = 0.0d;
        matrix[n - 1][2] = 0.0d;
        for (i = 1; i < n; i++) {
            double k = matrix[i][0] / matrix[i - 1][1];
            double[] dArr = matrix[i];
            dArr[1] = dArr[1] - (matrix[i - 1][2] * k);
            matrix[i][0] = 0.0d;
            result[i] = result[i] - (result[i - 1] * k);
        }
        for (i = n - 2; i >= 0; i--) {
            k = matrix[i][2] / matrix[i + 1][1];
            dArr = matrix[i];
            dArr[1] = dArr[1] - (matrix[i + 1][0] * k);
            matrix[i][2] = 0.0d;
            result[i] = result[i] - (result[i + 1] * k);
        }
        ArrayList<Double> output = new ArrayList(n);
        for (i = 0; i < n; i++) {
            output.add(Double.valueOf(result[i] / matrix[i][1]));
        }
        return output;
    }
}
