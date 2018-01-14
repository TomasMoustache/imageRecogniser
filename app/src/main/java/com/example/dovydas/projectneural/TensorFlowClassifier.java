package com.example.dovydas.projectneural;

/**
 * Created by Dovydas on 2018.01.09.
 */

import android.content.res.AssetManager;
//Reads text from a character-input stream, buffering characters so as to provide for the efficient reading of characters, arrays, and lines.
//for erros
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
//An InputStreamReader is a bridge from byte streams to character streams:
// //It reads bytes and decodes them into characters using a specified charset.
// //The charset that it uses may be specified by name or may be given explicitly, or the platform's default charset may be accepted.
import java.util.ArrayList;
import java.util.List;
//made by google, used as the window between android and tensorflow native C++

public class TensorFlowClassifier implements Classifier {

    private static final float THRESHOLD = 0.1f;

    private TensorFlowInferenceInterface tfHelper;

    private String name;
    private String inputName;
    private String outputName;
    private int inputSize;
    private boolean feedKeepProb;

    private List<String> labels;
    private float[] output;
    private String[] outputNames;

    private static List<String> readLabels(AssetManager am, String fileName) throws IOException {

        List<String> labels = new ArrayList<>();

        labels.add("0");
        labels.add("1");
        labels.add("2");

        return labels;
    }

    public static TensorFlowClassifier create(AssetManager assetManager, String name,
                                              String modelPath, String labelFile, int inputSize, String inputName, String outputName,
                                              boolean feedKeepProb) throws IOException {

        TensorFlowClassifier c = new TensorFlowClassifier();

        c.name = name;

        c.inputName = inputName;
        c.outputName = outputName;

        c.labels = readLabels(assetManager, labelFile);

        c.tfHelper = new TensorFlowInferenceInterface(assetManager, modelPath);
        int numClasses = 3;

        c.inputSize = inputSize;

        c.outputNames = new String[] { outputName };

        c.outputName = outputName;
        c.output = new float[numClasses];

        c.feedKeepProb = feedKeepProb;

        return c;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Classification recognize(final float[] pixels) {

        tfHelper.feed(inputName, pixels, 1, inputSize, inputSize, 1);

        if (feedKeepProb) {
            tfHelper.feed("keep_prob", new float[] { 1 });
        }

        tfHelper.run(outputNames);

        tfHelper.fetch(outputName, output);

        Classification ans = new Classification();
        for (int i = 0; i < output.length; ++i) {

            System.out.println(output[i]);
            System.out.println(labels.get(i));
            if (output[i] > THRESHOLD && output[i] > ans.getConf()) {

                ans.update(output[i], labels.get(i));
            }
        }

        return ans;
    }
}