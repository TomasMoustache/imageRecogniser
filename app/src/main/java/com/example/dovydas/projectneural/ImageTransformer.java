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
        //get image width and height
        int width = img.getWidth();
        int height = img.getHeight();

        //convert to grayscale
        for(int y = 0; y < height; y++){
            for(int x = 0; x < width; x++){
                int p = img.getPixel(x,y);

                int a = (p>>24)&0xff;
                int r = (p>>16)&0xff;
                int g = (p>>8)&0xff;
                int b = p&0xff;

                //calculate average
                int avg = (r+g+b)/3;

                //replace RGB value with avg
                p = (a<<24) | (avg<<16) | (avg<<8) | avg;

                this.pixels[y][x] = (float)p;
                img.setPixel(x, y, p);
            }
        }
        return img;
    }

    public Bitmap resizedImage(Bitmap originalImage)
    {
        //Bitmap resizedImage = new Bitmap(200, 200);
        //Bitmap resizedImage = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
        /*Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, 200, 200, null);
        g.dispose();*/
        Bitmap resizedImagee = Bitmap.createScaledBitmap(originalImage, 200, 200, true);

        return resizedImagee;
    }

    public void saveImage(Bitmap originalImage, String filename)
    {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            originalImage.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
