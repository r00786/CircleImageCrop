# CircleImageCrop

This is a Circle Image Cropper library in which you can move the image around the circle and crop the desired part


![alt text](https://raw.githubusercontent.com/r00786/CircleImageCrop/master/1.png)
![alt text](https://raw.githubusercontent.com/r00786/CircleImageCrop/master/2.png)
![alt text](https://raw.githubusercontent.com/r00786/CircleImageCrop/master/3.png)
![alt text](https://raw.githubusercontent.com/r00786/CircleImageCrop/master/4.png)

  # In Project level build file
  
  
  
  
  ```groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  ```

# In Moule level build file
  
  
  
  
  ```groovy
	dependencies {
	        implementation 'com.github.r00786:CircleImageCrop:Tag'
	}
  ```
  
  # How to Use
  
  //implement the CircleCropActivity.CroppedImageCallbacks in your activity
  public class MainActivity extends AppCompatActivity implements CircleCropActivity.CroppedImageCallbacks{

private CircleImageView ivCrop;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivCrop=findViewById(R.id.iv_crop);
	// Use this codde to open Circle Crop first parameter is context second is bitmap to be cropped and third is whether you want           //grid lines or not
        CircleCropActivity.openCircleCropActivityWithBitmap(this, BitmapFactory.decodeResource(getResources(),R.drawable.photo),true);
    }


     //After saving your bitmap you will receive the bitmap in this method
    @Override
    public void setCroppedImage(Bitmap bitmap) {
            ivCrop.setImageBitmap(bitmap);

    }
}
