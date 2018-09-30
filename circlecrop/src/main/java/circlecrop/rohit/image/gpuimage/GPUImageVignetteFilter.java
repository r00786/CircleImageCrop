package circlecrop.rohit.image.gpuimage;

import android.graphics.PointF;
import android.opengl.GLES20;

public class GPUImageVignetteFilter extends GPUImageFilter {
    private PointF mVignetteCenter;
    private int mVignetteCenterLocation;
    private float[] mVignetteColor;
    private int mVignetteColorLocation;
    private float mVignetteEnd;
    private int mVignetteEndLocation;
    private float mVignetteStart;
    private int mVignetteStartLocation;
    private float mVignetteWidth;
    private int mVignetteWidthLocation;

    public GPUImageVignetteFilter() {
        this(new PointF(), new float[]{0.0f, 0.0f, 0.0f}, 0.0f, 0.0f);
    }

    public GPUImageVignetteFilter(PointF vignetteCenter, float[] vignetteColor, float vignetteStart, float vignetteEnd) {
        super("attribute vec4 position;\nattribute vec4 inputTextureCoordinate;\n \nvarying vec2 textureCoordinate;\n \nvoid main()\n{\n    gl_Position = position;\n    textureCoordinate = inputTextureCoordinate.xy;\n}", " uniform sampler2D inputImageTexture;\n varying highp vec2 textureCoordinate;\n \n uniform lowp vec2 vignetteCenter;\n uniform lowp vec3 vignetteColor;\n uniform highp float vignetteStart;\n uniform highp float vignetteEnd;\n uniform highp float vignetteWidth;\n \n void main()\n {\n     \n     lowp vec3 rgb = texture2D(inputImageTexture, textureCoordinate).rgb;\n     lowp float d = distance(gl_FragCoord.xy, vignetteCenter)/vignetteWidth;\n     if (d > vignetteEnd) {\n         gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n     } else {\n         lowp float percent = smoothstep(vignetteStart, vignetteEnd, d);\n         gl_FragColor = vec4(mix(rgb.x, vignetteColor.x, percent), mix(rgb.y, vignetteColor.y, percent), mix(rgb.z, vignetteColor.z, percent), 1.0);\n     }\n }");
        this.mVignetteWidth = 100.0f;
        this.mVignetteCenter = vignetteCenter;
        this.mVignetteColor = vignetteColor;
        this.mVignetteStart = vignetteStart;
        this.mVignetteEnd = vignetteEnd;
    }

    public void onInit() {
        super.onInit();
        this.mVignetteCenterLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteCenter");
        this.mVignetteColorLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteColor");
        this.mVignetteStartLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteStart");
        this.mVignetteEndLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteEnd");
        this.mVignetteWidthLocation = GLES20.glGetUniformLocation(getProgram(), "vignetteWidth");
        setVignetteCenter(this.mVignetteCenter);
        setVignetteColor(this.mVignetteColor);
        setVignetteStart(this.mVignetteStart);
        setVignetteEnd(this.mVignetteEnd);
        setWignetteWidth(this.mVignetteWidth);
    }

    public void setWignetteWidth(float vignetteWidth) {
        this.mVignetteWidth = vignetteWidth;
        setFloat(this.mVignetteWidthLocation, this.mVignetteWidth);
    }

    public void setVignetteCenter(PointF vignetteCenter) {
        this.mVignetteCenter = vignetteCenter;
        setPoint(this.mVignetteCenterLocation, this.mVignetteCenter);
    }

    public void setVignetteColor(float[] vignetteColor) {
        this.mVignetteColor = vignetteColor;
        setFloatVec3(this.mVignetteColorLocation, this.mVignetteColor);
    }

    public void setVignetteStart(float vignetteStart) {
        this.mVignetteStart = vignetteStart;
        setFloat(this.mVignetteStartLocation, this.mVignetteStart);
    }

    public void setVignetteEnd(float vignetteEnd) {
        this.mVignetteEnd = vignetteEnd;
        setFloat(this.mVignetteEndLocation, this.mVignetteEnd);
    }
}
