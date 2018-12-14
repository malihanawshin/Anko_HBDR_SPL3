package com.example.imm.anko;

import android.graphics.Bitmap;
import android.graphics.Color;

public class ConvertColor {
    public static float[] convertForTensorflow(int[] argbPixels) {

        float[] arr = new float[argbPixels.length];

        for (int i = 0; i < argbPixels.length; i++) {
            int argbPixel = argbPixels[i];
            int alpha = (argbPixel >> 24) & 0xff;
            int r = (argbPixel >> 16) & 0xff;
            int g = (argbPixel >> 8) & 0xff;
            int b = argbPixel & 0xff;

            if (alpha == 0) {
                arr[i] = 0.0f;
                continue;
            }

            int avg = (r+g+b)/3;
            float grayscaled = avg/255.0f;
            grayscaled = grayscaled*(alpha/255.0f);
            arr[i] = 1.0f-grayscaled;
        }

        return arr;
    }



    public static int convertTfToPixel(float pixel) {
        int gray = (int) (255*pixel);
        gray = 255-gray;
        return Color.argb(255, gray, gray, gray);
    }
}
