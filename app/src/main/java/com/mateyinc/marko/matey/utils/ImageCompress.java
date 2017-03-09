package com.mateyinc.marko.matey.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import id.zelory.compressor.Compressor;
import imageUtil.Image;
import imageUtil.ImageLoader;

/**
 * Used for compressing image files
 */
public class ImageCompress {
    private static final String TAG = ImageCompress.class.getSimpleName();
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_SIZE_IN_KB = 1024;

    public static File compressImageToFile(File file, Context context) {

        if (!isImage(file)) {
            Log.e(TAG, "Cannot compress non image file.");
            return file;
        }

        long fileSize = getFileSize(file);
        if (fileSize < MAX_SIZE_IN_KB)
            return file;

        File compressedImage = new Compressor.Builder(context)
                .setMaxWidth(MAX_WIDTH)
                .setMaxHeight(MAX_WIDTH)
                .setQuality(97)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath())
                .build()
                .compressToFile(file);

        // If file size is bigger than 1MB, compress again
        if (getFileSize(compressedImage) > MAX_SIZE_IN_KB) {
//            int quality = calculateQuality(getFileSize(compressedImage));
            Log.d(TAG, String.format("Compressing file %s again.", compressedImage.getPath()));
            compressedImage = compressImageToFile(compressedImage, context);
        }

        Log.d(TAG, String.format("Compression finished. From %dKB to %dKB.", fileSize, getFileSize(compressedImage)));

        return compressedImage;
    }

    private static boolean isImage(File file) {
        String filePath = file.getPath().toLowerCase();
        return filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png");
    }

    private static long getFileSize(File file) {
        return file.length()/(1024);
    }

    private static float calculateQuality(long fileSize) {
        return (float)Math.ceil((100*MAX_SIZE_IN_KB)/fileSize);
    }

    private static String getFileName(File file) {
        String path = file.getPath();

        return path.substring(path.lastIndexOf("/") + 1);
    }
}
