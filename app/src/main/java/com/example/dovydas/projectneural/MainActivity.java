package com.example.dovydas.projectneural;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.tensorflow.Graph;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private float matrix[][];

    private static final String INPUT_NODE = "conv2d_1_input";
    private static final String OUTPUT_NODE = "output_node0";
    private Button button;
    Bitmap picture;
    Graph g;


    private List<Classifier> mClassifiers = new ArrayList<>();
    private TensorFlowInferenceInterface inferenceInterface;


    static {
        System.loadLibrary("tensorflow_inference");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.imageView = (ImageView)this.findViewById(R.id.imageView);
        Button photoButton = (Button) this.findViewById(R.id.button111);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        loadModel();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void prepareModel() {

        this.button = (Button)findViewById(R.id.button111);
        ImageTransformer it = new ImageTransformer();

        this.picture = it.resizedImage(this.picture);
        this.picture = it.convertToGrayscale(this.picture);
        imageView.setImageBitmap(this.picture);

        this.matrix = it.getMatrixFromImage();

        Button btn = (Button)findViewById(R.id.button);

        Classification res = (this.mClassifiers.get(0)).recognize(testMetodas());//recognize(floatArray);
        if (res.getLabel() == "0")
        {
            this.button.setText("" + "bicycle" + " " + res.getConf());
        }
        else
        {
            if (res.getLabel() == "1")
            {
                this.button.setText("" + "bird" + " " + res.getConf());
            }
            else
            {
                this.button.setText("" + "car" + " " + res.getConf());
            }
        }
    }

    public float[] testMetodas()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmapTest = BitmapFactory.decodeFile("/storage/emulated/0/Documents/bike0.jpg", options);
        //ImageTransformer it = new ImageTransformer();
        //bitmapTest = it.resizedImage(bitmapTest);
        //bitmapTest = it.convertToGrayscale(bitmapTest);
        bitmapTest = this.picture;

        int width = bitmapTest.getWidth();
        int height = bitmapTest.getHeight();

        int[] pixels = new int[width * height];
        bitmapTest.getPixels(pixels, 0, width, 0, 0, width, height);

        float[] retPixels = new float[width*height];

        for (int i = 0; i < pixels.length; ++i) {
            int val = pixels[i];

            float red = ((val >> 16) & 0xFF) / 255.0f;
            float green = ((val >> 8) & 0xFF) / 255.0f;
            float blue = (val & 0xFF) / 255.0f;

            retPixels[i] = (red+green+blue)/3;
        }

        return retPixels;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            this.picture = photo;
            imageView.setImageBitmap(photo);

            prepareModel();
        }
    }

    private void loadModel() {

        try {
                    mClassifiers.add(
                            TensorFlowClassifier.create(getAssets(), "Keras",
                                    "/storage/emulated/0/Documents/model.pb", "/storage/emulated/0/Documents/labels.txt", 200,
                                    "conv2d_1_input", "dense_2/Softmax", false));//"dense_2/Softmax" :"output_node0"
                } catch (final Exception e) {

                    throw new RuntimeException("Error initializing classifiers!", e);
                }
    }
}

