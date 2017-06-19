package com.uploadimagemultipart;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

/**
 * Created by LENOVO on 17-04-2015.
 */
public class BitmapLoader {
    public static int getScale(int originalWidth, int originalHeight,ImageView imageView) {
        // Get the dimensions of the View
        int requiredWidth = imageView.getWidth();
        UIUtil.log("Imageview Width" + requiredWidth);
        int requiredHeight = imageView.getHeight();
        UIUtil.log("Imageview Height" + requiredHeight);

        //a scale of 1 means the original dimensions
        //of the image are maintained
        int scale = 1;

        //calculate scale only if the height or width of
        //the image exceeds the required value.
        if ((originalWidth > requiredWidth) || (originalHeight > requiredHeight)) {
            //calculate scale with respect to
            //the smaller dimension
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) originalHeight
                    / (float) requiredHeight);
            final int widthRatio = Math.round((float) originalWidth / (float) requiredWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return scale;
    }

    public static BitmapFactory.Options getOptions(String filePath, ImageView imageView) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        //setting inJustDecodeBounds to true
        //ensures that we are able to measure
        //the dimensions of the image,without
        //actually allocating it memory

        //The image won't be loaded into memory.
        //But the outheight and outwidth properties of BitmapFactory.Options will contain the actual size params of the image specified.
        // Calculate how much u want to subsample it. i.e. 1/2 or 1/4 or 1/8 etc. and assign 2/4/8 etc.
        // accordingly to the inSampleSize.
        options.inJustDecodeBounds = true;

        //decode the file for measurement
        BitmapFactory.decodeFile(filePath, options);

        //obtain the inSampleSize for loading a
        //scaled down version of the image.
        //options.outWidth and options.outHeight
        //are the measured dimensions of the
        //original image
        options.inSampleSize = getScale(options.outWidth,
                options.outHeight, imageView);

        //set inJustDecodeBounds to false again
        //so that we can now actually allocate the
        //bitmap some memory
        // Again, analysis images using the acquired inSampleSize value
        // Now set inJustDecodeBounds to FALSE

        options.inJustDecodeBounds = false;

        return options;

    }


    public static Bitmap downSampleBitmap(String filePath, ImageView imageView) {
        BitmapFactory.Options options = getOptions(filePath, imageView);
        //  call BitmapFactory.decodeFile() to load the image of the exact size as calculated above.
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static Bitmap downSampleBitmap1(String filePath, ImageView imageView) {
        BitmapFactory.Options options = getOptions(filePath, imageView);
        return BitmapFactory.decodeFile(filePath, options);
    }


}
