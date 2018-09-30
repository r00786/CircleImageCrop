package circlecrop.rohit.image;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import circlecrop.rohit.image.gpuimage.CircleOverlayView;
import circlecrop.rohit.image.gpuimage.FileUtil;
import circlecrop.rohit.image.gpuimage.ImageForeGround;
import circlecrop.rohit.image.gpuimage.LIConstants;
import circlecrop.rohit.image.gpuimage.PermissionUtils;

public class CircleCropActivity extends AppCompatActivity {
    public static final String CROPPED_IMAGE_KEY = "C_P_K";
    private static String filePath;
    private CircleOverlayView circleOverlayView;
    private ImageForeGround imageForeGround;
    private TextView tvSave, tvCancel;
    private static CroppedImageCallbacks croppedImageCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_crop_main);
        circleOverlayView = findViewById(R.id.overlay);
        imageForeGround = findViewById(R.id.iv_main_image);
        tvSave = findViewById(R.id.tv_save);
        tvCancel = findViewById(R.id.tv_cancel);
        setup();
        initClicks();
    }

    private void initClicks() {
        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bmp = null;
                try {
                    bmp = imageForeGround.captureCroppedWithPadding(circleOverlayView.getPaddingLeft(), circleOverlayView.getPaddingTop());
                    if (croppedImageCallbacks != null) {
                        croppedImageCallbacks.setCroppedImage(bmp);
                    }
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void setup() {
        Intent intent = getIntent();

        imageForeGround.setImage(getBitmap(intent.getStringExtra(LIConstants.IMAGE_PATH)));
        circleOverlayView.setImageForeGround(imageForeGround);
        circleOverlayView.setHighlightMode(intent.getBooleanExtra(LIConstants.SHOW_GRID, false));
        imageForeGround.setEditMode(true);

    }

    /**
     * if the file is too large to be loaded into memory
     *
     * @param pathOfFileToBeCropped
     */
    public static void openCircleCropActivityWithFilePath(Activity context, String pathOfFileToBeCropped,
                                                          boolean showGrid) {
        if (checkForPermission(context)) {
            croppedImageCallbacks = (CroppedImageCallbacks) context;
            Intent intent = new Intent(context, CircleCropActivity.class);
            intent.putExtra(LIConstants.IMAGE_PATH, pathOfFileToBeCropped);

            intent.putExtra(LIConstants.SHOW_GRID, showGrid);
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "app doesnt have read write permissions", Toast.LENGTH_SHORT).show();
        }
    }

    public static void openCircleCropActivityWithBitmap(Activity context, Bitmap bitmap,
                                                        boolean showGrid) {
        if (checkForPermission(context)) {
            saveImage(bitmap, context);
            croppedImageCallbacks = (CroppedImageCallbacks) context;
            Intent intent = new Intent(context, CircleCropActivity.class);
            intent.putExtra(LIConstants.IMAGE_PATH, filePath);
            intent.putExtra(LIConstants.SHOW_GRID, showGrid);

            context.startActivity(intent);

        } else {
            Toast.makeText(context, "app doesnt have read write permissions", Toast.LENGTH_SHORT).show();
        }

    }

    static boolean checkForPermission(Activity context) {
        return PermissionUtils.hasPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) &&
                PermissionUtils.hasPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (imageForeGround != null) {
            imageForeGround.onResume();

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (imageForeGround != null) {
            imageForeGround.onPause();
        }
    }


    static void saveImage(Bitmap bitmap, Context context) {
        File myDir = new File(FileUtil.getPath(context));
        myDir.mkdirs();

        String fname = "temp.jpg";
        File file = new File(myDir, fname);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            filePath = file.getAbsolutePath();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Bitmap getBitmap(String path) {
        try {
            Bitmap bitmap = null;
            File f = new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public interface CroppedImageCallbacks {
        void setCroppedImage(Bitmap bitmap);
    }

}
