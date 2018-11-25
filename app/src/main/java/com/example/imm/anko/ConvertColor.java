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


    public static Bitmap convertImage(Bitmap b) {

        int width = b.getWidth();
        int height = b.getHeight();
        int pixel, A, R , G, B;
        Bitmap bmOut = Bitmap.createScaledBitmap(b, width, height, false);;

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = b.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2989 * R + 0.5870 * G + 0.1140 * B);
                // use 128 as threshold, above -> white, below -> black
                if (gray > 150) {
                    gray = 255;
                }
                else{
                    gray = 0;
                }
                // set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));
            }
        }

        return bmOut;
    }

    /*public static float[] convertNew(int[] intValues) {
        float[] a = new float[32*32];
        int imageMean = 128;
        float imageStd = 128.0f;

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            a[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
            a[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
            a[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
        }

        return a;
    }
*/


    public static int convertTfToPixel(float pixel) {
        int gray = (int) (255*pixel);
        gray = 255-gray;
        return Color.argb(255, gray, gray, gray);
    }
}
