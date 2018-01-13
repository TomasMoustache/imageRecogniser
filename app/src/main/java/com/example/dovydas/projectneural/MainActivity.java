package com.example.dovydas.projectneural;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface2;
import org.tensorflow.types.UInt8;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


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

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void prepareModel() {
        //TensorFlowImageClassifier classifier = new TensorFlowImageClassifier(this); //new TensorFlowImageClassifier(Activity.findViewById(R.id.));
        //prepareEnv();
        this.button = (Button)findViewById(R.id.button111);
        ImageTransformer it = new ImageTransformer();

        this.picture = it.resizedImage(this.picture);
        this.picture = it.convertToGrayscale(this.picture);
        imageView.setImageBitmap(this.picture);

        this.matrix = it.getMatrixFromImage();


        /*byte[] baitai = readBytes("/storage/emulated/0/Documents/model.pb");
        Graph g = new Graph();
        g.importGraphDef(baitai);
        Session session = new Session(g);*/

        /*Tensor tensorImage = Tensor.create(matrix);
        Intent intent = new Intent();*/
        //Bitmap bmp = (Bitmap) intent.getExtras().get("data");
        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
        this.picture.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageBytes = stream.toByteArray();

        List<String> labels = new ArrayList<String>();
        labels.add("0");
        labels.add("1");
        labels.add("2");*/




        loadModel();

        Button btn = (Button)findViewById(R.id.button);

        /*int[] intArray = new int[784];
        this.picture.getPixels(intArray, 0, this.picture.getWidth(), 0, 0, this.picture.getWidth(), this.picture.getHeight());
        float[] floatArray = new float[784];
        for(int i = 0; i < intArray.length; i++) floatArray[i] = (float)intArray[i];*/ //randInt(0,214748364);

        Classification res = (this.mClassifiers.get(0)).recognize(testMetodas());//recognize(floatArray);
        this.button.setText("" + res.getLabel() + " " + res.getConf());
        //btn.setText(""+floatArray[0]+" " + floatArray[1]+ " " + floatArray[2]);



        /*
        try (Tensor<Float> image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {
            float[] labelProbabilities = executeInceptionGraph(baitai, image);
            int bestLabelIdx = maxIndex(labelProbabilities);
            System.out.println(
                    String.format("BEST MATCH: %s (%.2f%% likely)",
                            labels.get(bestLabelIdx),
                            labelProbabilities[bestLabelIdx] * 100f));
        }*/
    }
    public float[] testMetodas()
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmapTest = BitmapFactory.decodeFile("/storage/emulated/0/Documents/bike1.jpg", options);
        ImageTransformer it = new ImageTransformer();
        bitmapTest = it.resizedImage(bitmapTest);
        bitmapTest = it.convertToGrayscale(bitmapTest);
        //bitmapTest = this.picture;

        int width = bitmapTest.getWidth();
        int height = bitmapTest.getHeight();

        // Get 28x28 pixel data from bitmap
        int[] pixels = new int[width * height];
        bitmapTest.getPixels(pixels, 0, width, 0, 0, width, height);

        //float[] retPixels = new float[pixels.length];
        float[] retPixels = new float[width*height];
        /*for (int i = 0; i < pixels.length; ++i) {
            // Set 0 for white and 255 for black pixel
            int pix = pixels[i];
            int b = pix & 0xff;
            retPixels[i] = (float)((0xff - b)/255.0);
        }*/
        for (int i = 0; i < pixels.length; ++i) {
            int val = pixels[i];

            float red = ((val >> 16) & 0xFF) / 255.0f;
            float green = ((val >> 8) & 0xFF) / 255.0f;
            float blue = (val & 0xFF) / 255.0f;

            retPixels[i] = (red+green+blue)/3;
        }

        return retPixels;

        //return floatArray;
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();

        int randomNum = rand.nextInt((max - min) + 1) + min;
        //int posNeg = rand.nextInt((0 - 1) + 1) + 0;
        //if(posNeg == 0) return randomNum;
        return randomNum;
    }

    private void prepareEnv(){
        this.button = (Button)findViewById(R.id.button111);
        ImageTransformer it = new ImageTransformer();

        this.picture = it.resizedImage(this.picture);
        this.picture = it.convertToGrayscale(this.picture);
        imageView.setImageBitmap(this.picture);

        this.matrix = it.getMatrixFromImage();

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
        //The Runnable interface is another way in which you can implement multi-threading other than extending the
        // //Thread class due to the fact that Java allows you to extend only one class. Runnable is just an interface,
        // //which provides the method run.
        // //Threads are implementations and use Runnable to call the method run().


        try {
                    //add 2 classifiers to our classifier arraylist
                    //the tensorflow classifier and the keras classifier
                    mClassifiers.add(
                            TensorFlowClassifier.create(getAssets(), "Keras",
                                    "/storage/emulated/0/Documents/model.pb", "/storage/emulated/0/Documents/labels.txt", 200,
                                    "conv2d_1_input", "dense_2/Softmax", false));//"dense_2/Softmax" :"output_node0"
                } catch (final Exception e) {
                    //if they aren't found, throw an error!
                    throw new RuntimeException("Error initializing classifiers!", e);
                }

    }





    private byte[] readBytes(String path)
    {
        File file = new File(path);
        int size = (int) file.length();
        this.button.setText("" + size);
        byte[] bytes = new byte[size];


        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
            //this.button.setText("Pavyko");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //if(bytes != null) this.button.setText("" + size);
        return bytes;
        //return new byte[8];
    }

    private static int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }
/*
    private static float[] executeInceptionGraph(byte[] graphDef, Tensor<Float> image) {
        try (Graph g = new Graph()) {
            g.importGraphDef(graphDef);
            try (Session s = new Session(g);
                 Tensor<Float> result =
                         s.runner().feed("input", image).fetch("output").run().get(0).expect(Float.class)) {
                final long[] rshape = result.shape();
                if (result.numDimensions() != 2 || rshape[0] != 1) {
                    throw new RuntimeException(
                            String.format(
                                    "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                    Arrays.toString(rshape)));
                }
                int nlabels = (int) rshape[1];
                return result.copyTo(new float[1][nlabels])[0];
            }
        }
    }

    private static Tensor<Float> constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            // Some constants specific to the pre-trained model at:
            // https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
            //
            // - The model was trained with images scaled to 224x224 pixels.
            // - The colors, represented as R, G, B in 1-byte each were converted to
            //   float using (value - Mean)/Scale.
            final int H = 200;
            final int W = 200;
            final float mean = 117f;
            final float scale = 1f;

            // Since the graph is being constructed once per execution here, we can use a constant for the
            // input image. If the graph were to be re-used for multiple input images, a placeholder would
            // have been more appropriate.
            final Output<String> input = b.constant(INPUT_NODE, imageBytes);
            final Output<Float> output =
                    b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 1), Float.class),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[] {H, W})),
                                    b.constant("mean", mean)),
                            b.constant("scale", scale));
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0).expect(Float.class);
            }
        }
    }


    static class GraphBuilder {
        GraphBuilder(Graph g) {
            this.g = g;
        }

        Output<Float> div(Output<Float> x, Output<Float> y) {
            return binaryOp("Div", x, y);
        }

        <T> Output<T> sub(Output<T> x, Output<T> y) {
            return binaryOp("Sub", x, y);
        }

        <T> Output<Float> resizeBilinear(Output<T> images, Output<Integer> size) {
            return binaryOp3("ResizeBilinear", images, size);
        }

        <T> Output<T> expandDims(Output<T> input, Output<Integer> dim) {
            return binaryOp3("ExpandDims", input, dim);
        }

        <T, U> Output<U> cast(Output<T> value, Class<U> type) {
            DataType dtype = DataType.fromClass(type);
            return g.opBuilder("Cast", "Cast")
                    .addInput(value)
                    .setAttr("DstT", dtype)
                    .build()
                    .<U>output(0);
        }

        Output<UInt8> decodeJpeg(Output<String> contents, long channels) {
            return g.opBuilder("DecodeJpeg", "DecodeJpeg")
                    .addInput(contents)
                    .setAttr("channels", channels)
                    .build()
                    .<UInt8>output(0);
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        <T> Output<T> constant(String name, Object value, Class<T> type) {
            try (Tensor<T> t = Tensor.<T>create(value, type)) {
                return g.opBuilder("Const", name)
                        .setAttr("dtype", DataType.fromClass(type))
                        .setAttr("value", t)
                        .build()
                        .<T>output(0);
            }
        }
        Output<String> constant(String name, byte[] value) {
            return this.constant(name, value, String.class);
        }

        Output<Integer> constant(String name, int value) {
            return this.constant(name, value, Integer.class);
        }

        Output<Integer> constant(String name, int[] value) {
            return this.constant(name, value, Integer.class);
        }

        Output<Float> constant(String name, float value) {
            return this.constant(name, value, Float.class);
        }

        private <T> Output<T> binaryOp(String type, Output<T> in1, Output<T> in2) {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
        }

        private <T, U, V> Output<T> binaryOp3(String type, Output<U> in1, Output<V> in2) {
            return g.opBuilder(type, type).addInput(in1).addInput(in2).build().<T>output(0);
        }
        private Graph g;
    }*/

}

