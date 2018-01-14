package com.example.dovydas.projectneural;

import android.graphics.Bitmap;
import android.graphics.PixelFormat;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Dovydas on 2017.12.29.
 */

public class ImageTransformer {
    private float[][] pixels = new float[200][200];

    public float[][] getMatrixFromImage()
    {
        return this.pixels;
    }

    public Bitmap convertToGrayscale(Bitmap img)
    {
        int width = img.getWidth();
        int height = img.getHeight();

        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int p = img.getPixel(x,y);

                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                int avg = (r+g+b)/3;

                p = (a<<24) | (avg<<16) | (avg<<8) | avg;

                this.pixels[y][x] = (float)p;
                img.setPixel(x, y, p);
            }
        }
        return img;
    }

    public Bitmap resizedImage(Bitmap originalImage)
    {
        Bitmap resizedImagee = Bitmap.createScaledBitmap(originalImage, 200, 200, true);

        return resizedImagee;
    }
}
