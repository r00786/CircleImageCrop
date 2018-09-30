package circlecrop.rohit.image.gpuimage;

import android.graphics.PointF;


public class LiFilterFactory {
    public static final int OPTION_ENABLE_TEXT = 128;
    private LiFilterFactory() {
    }

    public static GPUImageBCSFilter getFilterOne() {
        GPUImageBCSFilter gpuImageBCSFilter = new GPUImageBCSFilter();
        gpuImageBCSFilter.setContrast(1.02f);
        gpuImageBCSFilter.setRgbCompositeControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(47, 32), getPointFBasedIntegers(71, 64), getPointFBasedIntegers(200, 214), getPointFBasedIntegers(255, 255)});
        return gpuImageBCSFilter;
    }

    public static GPUImageBCSFilter getFilterTwo() {
        GPUImageBCSFilter gpuImageBCSFilter = new GPUImageBCSFilter();
        gpuImageBCSFilter.setSaturation(0.7f);
        gpuImageBCSFilter.setRgbCompositeControlPoints(new PointF[]{getPointFBasedIntegers(0, 40), getPointFBasedIntegers(60, 54), getPointFBasedIntegers(190, 206), getPointFBasedIntegers(255, 220)});
        return gpuImageBCSFilter;
    }

    public static GPUImageBCSFilter getFilterThree() {
        GPUImageBCSFilter gpuImageBCSFilter = new GPUImageBCSFilter();
        gpuImageBCSFilter.setRgbCompositeControlPoints(new PointF[]{getPointFBasedIntegers(0, 40), getPointFBasedIntegers(64, 54), getPointFBasedIntegers(127, 127), getPointFBasedIntegers(192, 207), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setRedControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(127, 137), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setGreenControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(127, 123), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setBlueControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(63, 61), getPointFBasedIntegers(255, 217)});
        return gpuImageBCSFilter;
    }

    public static GPUImageBCSFilter getFilterFour() {
        GPUImageBCSFilter gpuImageBCSFilter = new GPUImageBCSFilter();
        gpuImageBCSFilter.setSaturation(0.0f);
        gpuImageBCSFilter.setRgbCompositeControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(64, 39), getPointFBasedIntegers(126, 146), getPointFBasedIntegers(255, 255)});
        return gpuImageBCSFilter;
    }

    public static GPUImageBCSFilter getFilterFive() {
        GPUImageBCSFilter gpuImageBCSFilter = new GPUImageBCSFilter();
        gpuImageBCSFilter.setRedControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(137, 114), getPointFBasedIntegers(195, 204), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setGreenControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(72, 57), getPointFBasedIntegers(127, 126), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setBlueControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(52, 54), getPointFBasedIntegers(OPTION_ENABLE_TEXT, 127), getPointFBasedIntegers(187, 199), getPointFBasedIntegers(255, 255)});
        return gpuImageBCSFilter;
    }

    public static GPUImageBCSFilter getFilterSix() {
        GPUImageBCSFilter gpuImageBCSFilter = new GPUImageBCSFilter();
        gpuImageBCSFilter.setSaturation(0.86f);
        gpuImageBCSFilter.setRgbCompositeControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(58, 57), getPointFBasedIntegers(89, 90), getPointFBasedIntegers(123, 130), getPointFBasedIntegers(169, 197), getPointFBasedIntegers(206, 233), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setRedControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(124, 129), getPointFBasedIntegers(185, 197), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setGreenControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(125, 132), getPointFBasedIntegers(198, 192), getPointFBasedIntegers(255, 255)});
        gpuImageBCSFilter.setBlueControlPoints(new PointF[]{getPointFBasedIntegers(0, 0), getPointFBasedIntegers(124, 137), getPointFBasedIntegers(184, 194), getPointFBasedIntegers(255, 255)});
        return gpuImageBCSFilter;
    }

    private static PointF getPointFBasedIntegers(int x, int y) {
        return new PointF(((float) x) / 255.0f, ((float) y) / 255.0f);
    }
}
