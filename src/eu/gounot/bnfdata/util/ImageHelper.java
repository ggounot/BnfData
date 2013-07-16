package eu.gounot.bnfdata.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import eu.gounot.bnfdata.R;

public class ImageHelper {

    private static final String TAG = "ImageHelper";

    public static Bitmap getResizedImage(Context context, String imageUrl) {
        Bitmap image = downloadImage(imageUrl);
        if (image != null) {
            image = resizeImage(context, image);
        }

        return image;
    }

    public static Bitmap downloadImage(String imageUrl) {
        Bitmap bitmap = null;

        try {
            InputStream inputStream = new URL(imageUrl).openStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            // 404 happens. bitmap remains null and the default image will be displayed.
        } catch (MalformedURLException e) {
            Log.e(TAG, e.toString(), e);
        } catch (IOException e) {
            Log.e(TAG, e.toString(), e);
        }

        return bitmap;
    }

    public static Bitmap resizeImage(Context context, Bitmap bitmap) {
        Resources res = context.getResources();

        // Retrieve the maximum dimensions that the image should have.
        int maxWidth = res.getDimensionPixelSize(R.dimen.object_image_max_width);
        int maxHeight = res.getDimensionPixelSize(R.dimen.object_image_max_height);

        // Get the original bitmap's width and height.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate the ratio to apply to resize the bitmap
        // so it doesn't exceed the maximum dimensions.
        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);

        // Apply the ratio to the original dimensions to get the new dimensions.
        width = Math.round(width * ratio);
        height = Math.round(height * ratio);

        // Resize the bitmap with the new dimensions.
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

}
