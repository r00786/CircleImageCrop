package circlecrop.rohit.image.gpuimage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import circlecrop.rohit.image.R;


public class CircleOverlayView extends View {
    private ImageForeGround imageForeGround;
    private boolean highlightMode;
    private LiGPUImageFilter liGPUImageFilter;
    private Paint paint = new Paint(1);
    private PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(Mode.CLEAR);
    private PorterDuffXfermode porterDuffXfermodeOver = new PorterDuffXfermode(Mode.OVERLAY);

    public CircleOverlayView(Context context) {
        super(context);
    }

    public CircleOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircleOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageForeGround(ImageForeGround imageForeGround) {
        this.imageForeGround = imageForeGround;
    }

    public void setLiGPUImageFilter(LiGPUImageFilter liGPUImageFilter) {
        this.liGPUImageFilter = liGPUImageFilter;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int paddingTop = getPaddingTop();
        int paddingSide = Math.max((width - ((height - ViewUtils.convertDpToPx(getContext(), 195)) - (paddingTop * 2))) / 2, getResources().getDimensionPixelSize(R.dimen.ad_item_spacing_6));
        setPadding(paddingSide, paddingTop, paddingSide, getPaddingBottom());
        if (this.imageForeGround != null) {
            this.imageForeGround.setTransformOffsetLimit((float) paddingSide, (float) paddingSide, (float) paddingTop, (float) getCircleOffsetBottom(), (float) width, (float) height);
        }
        if (this.liGPUImageFilter != null) {
            this.liGPUImageFilter.setVignetteCenter(((float) width) / 2.0f, (((float) height) / 2.0f) + (((float) (getCircleOffsetBottom() - paddingTop)) / 2.0f));
            this.liGPUImageFilter.setVignetteWidth((float) ((width - (paddingSide * 2)) / 2));
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setColor(getResources().getColor(this.highlightMode ? R.color.ad_black_70 : R.color.ad_black_solid));
        this.paint.setStyle(Style.FILL);
        this.paint.setAntiAlias(true);

        canvas.drawPaint(this.paint);
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getWidth() - getPaddingRight();
        int bottom = top + (right - left);
        this.paint.setXfermode(this.porterDuffXfermode);
        canvas.drawCircle((float) ((left + right) / 2), (float) ((top + bottom) / 2), (float) ((right - left) / 2), this.paint);
        if (this.highlightMode) {
            this.paint.setXfermode(this.porterDuffXfermodeOver);
            this.paint.setColor(getResources().getColor(R.color.ad_white_solid));
            this.paint.setStyle(Style.STROKE);
            this.paint.setStrokeWidth((float) ViewUtils.convertDpToPx(getContext(), 2));
            canvas.drawCircle((float) ((left + right) / 2), (float) ((top + bottom) / 2), (float) ((right - left) / 2), this.paint);
            this.paint.setColor(getResources().getColor(R.color.ad_white_55));
            this.paint.setStrokeWidth((float) ViewUtils.convertDpToPx(getContext(), 1));
            float radius = ((float) (right - left)) / 2.0f;
            float lineStartX = ((float) left) + (0.05719f * radius);
            float lineEndX = ((float) right) - (0.05719f * radius);
            float lineY = ((float) top) + (((float) (bottom - top)) / 3.0f);
            canvas.drawLine(lineStartX, lineY, lineEndX, lineY, this.paint);
            lineY = ((float) bottom) - (((float) (bottom - top)) / 3.0f);
            canvas.drawLine(lineStartX, lineY, lineEndX, lineY, this.paint);
            float lineStartY = ((float) top) + (0.05719f * radius);
            float lineEndY = ((float) bottom) - (0.05719f * radius);
            float lineX = ((float) left) + (((float) (right - left)) / 3.0f);
            canvas.drawLine(lineX, lineStartY, lineX, lineEndY, this.paint);
            lineX = ((float) right) - (((float) (right - left)) / 3.0f);
            canvas.drawLine(lineX, lineStartY, lineX, lineEndY, this.paint);
        }
        this.paint.reset();
    }

    public void setHighlightMode(boolean highlightMode) {
        this.highlightMode = highlightMode;
        invalidate();
    }

    private int getCircleOffsetBottom() {
        return (getMeasuredHeight() - getPaddingTop()) - ((getMeasuredWidth() - getPaddingLeft()) - getPaddingRight());
    }
}
