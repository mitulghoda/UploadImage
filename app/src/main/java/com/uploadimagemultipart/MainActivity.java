package com.uploadimagemultipart;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.uploadimagemultipart.SimpleMessageStatusResponse.SimpleMessageStatusResponse;
import com.uploadimagemultipart.WebServices.ApiHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class MainActivity extends AppCompatActivity {


    TextView chosephoto;
    private Uri fileUri;
    private String selectedImagePath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    //  Intent for Choose Image from Gallery
    String imagePath;
    private static final int REQ_CODE_PICK_IMAGE = 2;
    private int WRITE_STORAGE_PERMISSION_CODE = 23;
    private int READ_STORAGE_PERMISSION_CODE = 22;
    private int CAMERA_PERMISSION_CODE = 21;
    ImageView iv_userprofile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        idMapping();
        setonClick();
    }
    private void idMapping()
    {
        chosephoto =(TextView)findViewById(R.id.chosephoto);
        iv_userprofile = (ImageView)findViewById(R.id.iv_userprofile);

    }
    private void setonClick()
    {
        chosephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogForSelectPicture();
            }
        });
    }

    private void showDialogForSelectPicture() {


        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Include dialog.xml file
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.layout_select_picture);
        dialog.show();

        TextView txt_camera = (TextView) dialog.findViewById(R.id.txt_camera);
        TextView txt_gallery = (TextView) dialog.findViewById(R.id.txt_gallery);


        txt_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
                if (isCameraAllowed()) {
                    dispatchTakePictureIntent(REQUEST_IMAGE_CAPTURE);
                } else {
                    requestCameraPermission();
                }
            }
        });
        txt_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog

                dialog.dismiss();
                if (isReadStorageAllowed()) {
                    if (isWriteStorageAllowed()) {
                        selectPictureFromGallery();
                    } else {
                        requestWriteStoragePermission();
                    }
                } else {
                    requestReadStoragePermission();
                }

            }
        });

    }



    private void dispatchTakePictureIntent(int requestImageCapture) {
        isDeviceSupportCamera(this);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = UIUtil.createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                fileUri = Uri.fromFile(photoFile);
//                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//                startActivityForResult(takePictureIntent, requestImageCapture);

                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, requestImageCapture);
            }
        }
    }

    private void selectPictureFromGallery() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQ_CODE_PICK_IMAGE);
    }
    // Checking Device Support Camera or not
    private boolean isDeviceSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // This device has a camera
            return true;
        } else {
            // no camera on this device
            Toast.makeText(context, "This device does not have a camera.", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
    }

    public String getPath(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        return filePath;

    }
    public void Image_Selecting_Task(Intent data,ImageView imageView) {
        Uri selectedImage = data.getData();
        selectedImagePath = getPath(selectedImage);
        Bitmap bitmap = BitmapLoader.downSampleBitmap(selectedImagePath,imageView);
        int imageAngle = UIUtil.getImageAngle(selectedImagePath);
        Bitmap rotateBitMap = UIUtil.rotateImage(bitmap, imageAngle);

        imageView.setImageBitmap(rotateBitMap);

    }


    private Bitmap setFullImageFromFilePath(ImageView imageView, String imageSelectedPath) {
        selectedImagePath=imageSelectedPath;
        Bitmap bitmap = BitmapLoader.downSampleBitmap(selectedImagePath, imageView);
        int imageAngle = UIUtil.getImageAngle(selectedImagePath);
        Bitmap rotateBitMap = UIUtil.rotateImage(bitmap, imageAngle);
        imageView.setImageBitmap(rotateBitMap);

        return bitmap;
    }
    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestReadStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},READ_STORAGE_PERMISSION_CODE);
    }




    private boolean isWriteStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestWriteStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_STORAGE_PERMISSION_CODE);
    }


    private boolean isCameraAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestCameraPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == READ_STORAGE_PERMISSION_CODE || requestCode == CAMERA_PERMISSION_CODE|| requestCode == WRITE_STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast

            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(MainActivity.this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }




    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        Log.e("requestCode",""+requestCode);
        super.onActivityResult(requestCode, resultCode, data);


            if(resultCode == Activity.RESULT_OK && resultCode!= Activity.RESULT_CANCELED) {




                switch (requestCode) {
                    case REQ_CODE_PICK_IMAGE:
                        Image_Selecting_Task(data,(ImageView)this.findViewById(R.id.iv_userprofile));
                        callApiProfile();

                        break;
                    case REQUEST_IMAGE_CAPTURE:



                        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

                        File destination = new File(Environment.getExternalStorageDirectory(),
                                System.currentTimeMillis() + ".jpg");

                        selectedImagePath = destination.getAbsolutePath();

                        FileOutputStream fo;
                        try {
                            destination.createNewFile();
                            fo = new FileOutputStream(destination);
                            fo.write(bytes.toByteArray());
                            fo.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
              if (!(selectedImagePath != null && !selectedImagePath.equals(""))) {

                            Toast.makeText(MainActivity.this, "Please Select Any Image", Toast.LENGTH_SHORT).show();
                        } else {

                            callApiProfile();
                        }



                        break;
                    default:
                        break;

                }




            }







    }



    private void callApiProfile() {

        if (!UIUtil.checkNetwork(MainActivity.this)) {

            return;
        }


        ApiHandler.getApiService().uploadPhoto((new TypedFile("image/*", userUploadedImage())), geteimagemap(), new Callback<SimpleMessageStatusResponse>() {
            @Override
            public void success(SimpleMessageStatusResponse editProfileMainResponse, Response response) {

                if (editProfileMainResponse == null) {
                    Toast.makeText(MainActivity.this, "Something went wrong.Please try again later.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editProfileMainResponse == null) {
                    Toast.makeText(MainActivity.this, "Something went wrong.Please try again later.", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (editProfileMainResponse.getStatus() == null) {
                    Toast.makeText(MainActivity.this, "Something went wrong.Please try again later.", Toast.LENGTH_SHORT).show();

                    return;
                }

                if (!editProfileMainResponse.getStatus().equalsIgnoreCase("success")) {

                    Toast.makeText(MainActivity.this, "" + editProfileMainResponse.getMessage(), Toast.LENGTH_SHORT).show();

                    return;
                }
                if (editProfileMainResponse.getStatus().equalsIgnoreCase("success")) {


                }


            }

            @Override
            public void failure(RetrofitError error) {


            }
        });

    }


    private File userUploadedImage() {
        if(selectedImagePath!=null && !selectedImagePath.isEmpty()) {
            File image = new File(selectedImagePath);
            return image;
        }
        return  null;
    }

    private Map<String,String> geteimagemap() {
        Map<String, String> map = new HashMap<>();



        map.put("id", "99");




        Log.e("getMap", "" + map);

        return map;
    }
    }