package circlecrop.rohit.image.gpuimage;

import android.graphics.PointF;

public class LiGPUImageFilter extends GPUImageFilterGroup {
    private GPUImageBCSFilter bcsFilter = new GPUImageBCSFilter();
    private int brightness;
    private int contrast;
    private int filterTag;
    private ImageForeGround imageForeGround;
    private int saturation;
    private int vignette;
    private GPUImageVignetteFilter vignetteFilter = new GPUImageVignetteFilter();

    public LiGPUImageFilter() {
        addFilter(new GPUImageFilter());
        addFilter(this.bcsFilter);
        addFilter(this.vignetteFilter);
    }

    public void setFilter(GPUImageFilter filter, int filterTag) {
        reset();
        getFilters().set(0, filter);
        updateMergedFilters();
        if (this.imageForeGround != null) {
            this.imageForeGround.setFilter(this);
        }
        this.filterTag = filterTag;
    }

    public int getBrightness() {
        return this.brightness;
    }

    public int getContrast() {
        return this.contrast;
    }

    public int getSaturation() {
        return this.saturation;
    }

    public int getVignette() {
        return this.vignette;
    }

    public int getFilterTag() {
        return this.filterTag;
    }

    public void setBrightness(int value) {
        this.brightness = value;
        this.bcsFilter.setBrightness(((float) value) * 0.0018f);
        refresh();
    }

    public void setContrast(int value) {
        this.contrast = value;
        this.bcsFilter.setContrast((((float) value) * 0.003f) + 1.0f);
        refresh();
    }

    public void setSaturation(int value) {
        this.saturation = value;
        if (value > 0) {
            this.bcsFilter.setSaturation((((float) value) * 0.005f) + 1.0f);
        } else {
            this.bcsFilter.setSaturation((((float) value) * 0.01f) + 1.0f);
        }
        refresh();
    }

    public void setVignetteCenter(float x, float y) {
        this.vignetteFilter.setVignetteCenter(new PointF(x, y));
    }

    public void setVignetteWidth(float vignetteWidth) {
        this.vignetteFilter.setWignetteWidth(vignetteWidth);
        refresh();
    }

    public void setVignette(int value) {
        this.vignette = value;
        if (value == 0) {
            this.vignetteFilter.setVignetteEnd(0.0f);
        } else if (value > 0) {
            this.vignetteFilter.setVignetteColor(new float[]{1.0f, 1.0f, 1.0f});
            this.vignetteFilter.setVignetteEnd(6.0f - (((float) value) * 0.04f));
        } else {
            this.vignetteFilter.setVignetteColor(new float[]{0.0f, 0.0f, 0.0f});
            this.vignetteFilter.setVignetteEnd((((float) value) * 0.04f) + 6.0f);
        }
        refresh();
    }

    public void reset() {
        this.brightness = 0;
        this.contrast = 0;
        this.saturation = 0;
        this.vignette = 0;
        this.bcsFilter.resetBCS();
        this.vignetteFilter.setVignetteEnd(0.0f);
    }

    public void bind(ImageForeGround imageForeGround) {
        this.imageForeGround = imageForeGround;
        imageForeGround.setFilter(this);
    }

    private void refresh() {
        if (this.imageForeGround != null) {
            this.imageForeGround.requestRender();
        }
    }
}
