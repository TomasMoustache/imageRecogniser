//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.tensorflow.contrib.android;

import android.content.res.AssetManager;
import android.os.Trace;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.Tensors;
import org.tensorflow.Session.Run;
import org.tensorflow.Session.Runner;
import org.tensorflow.contrib.android.RunStats;
import org.tensorflow.types.UInt8;

public class TensorFlowInferenceInterface2 {
    private static final String TAG = "TensorFlowInferenceInterface";
    private static final String ASSET_FILE_PREFIX = "file:///android_asset/";
    private final String modelName;
    private final Graph g;
    private final Session sess;
    private Runner runner;
    private List<String> feedNames = new ArrayList();
    private List<Tensor<?>> feedTensors = new ArrayList();
    private List<String> fetchNames = new ArrayList();
    private List<Tensor<?>> fetchTensors = new ArrayList();
    private RunStats runStats;

    public TensorFlowInferenceInterface2(AssetManager var1, String var2) {
        this.prepareNativeRuntime();
        this.modelName = var2;
        this.g = new Graph();
        this.sess = new Session(this.g);
        this.runner = this.sess.runner();
        boolean var3 = var2.startsWith("file:///android_asset/");
        Object var4 = null;

        try {
            String var5 = var3?var2.split("file:///android_asset/")[1]:var2;
            var4 = var1.open(var5);
        } catch (IOException var9) {
            if(var3) {
                throw new RuntimeException("Failed to load model from \'" + var2 + "\'", var9);
            }

            try {
                var4 = new FileInputStream(var2);
            } catch (IOException var8) {
                throw new RuntimeException("Failed to load model from \'" + var2 + "\'", var9);
            }
        }

        try {
            if(VERSION.SDK_INT >= 18) {
                Trace.beginSection("initializeTensorFlow");
                Trace.beginSection("readGraphDef");
            }

            byte[] var10 = new byte[((InputStream)var4).available()];//((InputStream)var4).available()
            int var6 = ((InputStream)var4).read(var10);
            if(var6 != var10.length) {
                throw new IOException("read error: read only " + var6 + " of the graph, expected to read " + var10.length);
            } else {
                if(VERSION.SDK_INT >= 18) {
                    Trace.endSection();
                }

                this.loadGraph(var10, this.g);
                ((InputStream)var4).close();
              //  Log.i("TensorFlowInferenceInterface", "Successfully loaded model from \'" + var2 + "\'");
                if(VERSION.SDK_INT >= 18) {
                    Trace.endSection();
                }

            }
        } catch (IOException var7) {
            throw new RuntimeException("Failed to load model from \'" + var2 + "\'", var7);
        }
    }

    public TensorFlowInferenceInterface2(InputStream var1) {
        this.prepareNativeRuntime();
        this.modelName = "";
        this.g = new Graph();
        this.sess = new Session(this.g);
        this.runner = this.sess.runner();

        try {
            if(VERSION.SDK_INT >= 18) {
                Trace.beginSection("initializeTensorFlow");
                Trace.beginSection("readGraphDef");
            }

            int var2 = var1.available() > 16384?var1.available():16384;
            ByteArrayOutputStream var3 = new ByteArrayOutputStream(var2);
            byte[] var5 = new byte[16384];

            int var4;
            while((var4 = var1.read(var5, 0, var5.length)) != -1) {
                var3.write(var5, 0, var4);
            }

            byte[] var6 = var3.toByteArray();
            if(VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }

            this.loadGraph(var6, this.g);
            //Log.i("TensorFlowInferenceInterface", "Successfully loaded model from the input stream");
            if(VERSION.SDK_INT >= 18) {
                Trace.endSection();
            }

        } catch (IOException var7) {
            throw new RuntimeException("Failed to load model from the input stream", var7);
        }
    }

    public TensorFlowInferenceInterface2(Graph var1) {
        this.prepareNativeRuntime();
        this.modelName = "";
        this.g = var1;
        this.sess = new Session(var1);
        this.runner = this.sess.runner();
    }

    public void run(String[] var1) {
        this.run(var1, false);
    }

    public void run(String[] var1, boolean var2) {
        this.closeFetches();
        String[] var3 = var1;
        int var4 = var1.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String var6 = var3[var5];
            this.fetchNames.add(var6);
            TensorFlowInferenceInterface2.TensorId var7 = TensorFlowInferenceInterface2.TensorId.parse(var6);
            this.runner.fetch(var7.name, var7.outputIndex);
        }

        try {
            if(var2) {
                Run var13 = this.runner.setOptions(RunStats.runOptions()).runAndFetchMetadata();
                this.fetchTensors = var13.outputs;
                if(this.runStats == null) {
                    this.runStats = new RunStats();
                }

                this.runStats.add(var13.metadata);
            } else {
                this.fetchTensors = this.runner.run();
            }
        } catch (RuntimeException var11) {
            //Log.e("TensorFlowInferenceInterface", "Failed to run TensorFlow inference with inputs:[" + TextUtils.join(", ", this.feedNames) + "], outputs:[" + TextUtils.join(", ", this.fetchNames) + "]");
            throw var11;
        } finally {
            this.closeFeeds();
            this.runner = this.sess.runner();
        }

    }

    public Graph graph() {
        return this.g;
    }

    public Operation graphOperation(String var1) {
        Operation var2 = this.g.operation(var1);
        if(var2 == null) {
            throw new RuntimeException("Node \'" + var1 + "\' does not exist in model \'" + this.modelName + "\'");
        } else {
            return var2;
        }
    }

    public String getStatString() {
        return this.runStats == null?"":this.runStats.summary();
    }

    public void close() {
        this.closeFeeds();
        this.closeFetches();
        this.sess.close();
        this.g.close();
        if(this.runStats != null) {
            this.runStats.close();
        }

        this.runStats = null;
    }

    protected void finalize() throws Throwable {
        try {
            this.close();
        } finally {
            super.finalize();
        }

    }

    public void feed(String var1, boolean[] var2, long... var3) {
        byte[] var4 = new byte[var2.length];

        for(int var5 = 0; var5 < var2.length; ++var5) {
            var4[var5] = (byte)(var2[var5]?1:0);
        }

        this.addFeed(var1, Tensor.create(Boolean.class, var3, ByteBuffer.wrap(var4)));
    }

    public void feed(String var1, float[] var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, FloatBuffer.wrap(var2)));
    }

    public void feed(String var1, int[] var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, IntBuffer.wrap(var2)));
    }

    public void feed(String var1, long[] var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, LongBuffer.wrap(var2)));
    }

    public void feed(String var1, double[] var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, DoubleBuffer.wrap(var2)));
    }

    public void feed(String var1, byte[] var2, long... var3) {
        this.addFeed(var1, Tensor.create(UInt8.class, var3, ByteBuffer.wrap(var2)));
    }

    public void feedString(String var1, byte[] var2) {
        this.addFeed(var1, Tensors.create(var2));
    }

    public void feedString(String var1, byte[][] var2) {
        this.addFeed(var1, Tensors.create(var2));
    }

    public void feed(String var1, FloatBuffer var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, var2));
    }

    public void feed(String var1, IntBuffer var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, var2));
    }

    public void feed(String var1, LongBuffer var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, var2));
    }

    public void feed(String var1, DoubleBuffer var2, long... var3) {
        this.addFeed(var1, Tensor.create(var3, var2));
    }

    public void feed(String var1, ByteBuffer var2, long... var3) {
        this.addFeed(var1, Tensor.create(UInt8.class, var3, var2));
    }

    public void fetch(String var1, float[] var2) {
        this.fetch(var1, FloatBuffer.wrap(var2));
    }

    public void fetch(String var1, int[] var2) {
        this.fetch(var1, IntBuffer.wrap(var2));
    }

    public void fetch(String var1, long[] var2) {
        this.fetch(var1, LongBuffer.wrap(var2));
    }

    public void fetch(String var1, double[] var2) {
        this.fetch(var1, DoubleBuffer.wrap(var2));
    }

    public void fetch(String var1, byte[] var2) {
        this.fetch(var1, ByteBuffer.wrap(var2));
    }

    public void fetch(String var1, FloatBuffer var2) {
        this.getTensor(var1).writeTo(var2);
    }

    public void fetch(String var1, IntBuffer var2) {
        this.getTensor(var1).writeTo(var2);
    }

    public void fetch(String var1, LongBuffer var2) {
        this.getTensor(var1).writeTo(var2);
    }

    public void fetch(String var1, DoubleBuffer var2) {
        this.getTensor(var1).writeTo(var2);
    }

    public void fetch(String var1, ByteBuffer var2) {
        this.getTensor(var1).writeTo(var2);
    }

    private void prepareNativeRuntime() {
        //Log.i("TensorFlowInferenceInterface", "Checking to see if TensorFlow native methods are already loaded");

        try {
            new RunStats();
            //Log.i("TensorFlowInferenceInterface", "TensorFlow native methods already loaded");
        } catch (UnsatisfiedLinkError var4) {
           // Log.i("TensorFlowInferenceInterface", "TensorFlow native methods not found, attempting to load via tensorflow_inference");

            try {
                System.loadLibrary("tensorflow_inference");
               // Log.i("TensorFlowInferenceInterface", "Successfully loaded TensorFlow native methods (RunStats error may be ignored)");
            } catch (UnsatisfiedLinkError var3) {
                throw new RuntimeException("Native TF methods not found; check that the correct native libraries are present in the APK.");
            }
        }

    }

    private void loadGraph(byte[] var1, Graph var2) throws IOException {
        long var3 = System.currentTimeMillis();
        if(VERSION.SDK_INT >= 18) {
            Trace.beginSection("importGraphDef");
        }

        try {
            var2.importGraphDef(var1);
        } catch (IllegalArgumentException var7) {
            throw new IOException("Not a valid TensorFlow Graph serialization: " + var7.getMessage());
        }

        if(VERSION.SDK_INT >= 18) {
            Trace.endSection();
        }

        long var5 = System.currentTimeMillis();
       // Log.i("TensorFlowInferenceInterface", "Model load took " + (var5 - var3) + "ms, TensorFlow version: " + TensorFlow.version());
    }

    private void addFeed(String var1, Tensor<?> var2) {
        TensorFlowInferenceInterface2.TensorId var3 = TensorFlowInferenceInterface2.TensorId.parse(var1);
        this.runner.feed(var3.name, var3.outputIndex, var2);
        this.feedNames.add(var1);
        this.feedTensors.add(var2);
    }

    private Tensor<?> getTensor(String var1) {
        int var2 = 0;

        for(Iterator var3 = this.fetchNames.iterator(); var3.hasNext(); ++var2) {
            String var4 = (String)var3.next();
            if(var4.equals(var1)) {
                return (Tensor)this.fetchTensors.get(var2);
            }
        }

        throw new RuntimeException("Node \'" + var1 + "\' was not provided to run(), so it cannot be read");
    }

    private void closeFeeds() {
        Iterator var1 = this.feedTensors.iterator();

        while(var1.hasNext()) {
            Tensor var2 = (Tensor)var1.next();
            var2.close();
        }

        this.feedTensors.clear();
        this.feedNames.clear();
    }

    private void closeFetches() {
        Iterator var1 = this.fetchTensors.iterator();

        while(var1.hasNext()) {
            Tensor var2 = (Tensor)var1.next();
            var2.close();
        }

        this.fetchTensors.clear();
        this.fetchNames.clear();
    }

    private static class TensorId {
        String name;
        int outputIndex;

        private TensorId() {
        }

        public static TensorFlowInferenceInterface2.TensorId parse(String var0) {
            TensorFlowInferenceInterface2.TensorId var1 = new TensorFlowInferenceInterface2.TensorId();
            int var2 = var0.lastIndexOf(58);
            if(var2 < 0) {
                var1.outputIndex = 0;
                var1.name = var0;
                return var1;
            } else {
                try {
                    var1.outputIndex = Integer.parseInt(var0.substring(var2 + 1));
                    var1.name = var0.substring(0, var2);
                } catch (NumberFormatException var4) {
                    var1.outputIndex = 0;
                    var1.name = var0;
                }

                return var1;
            }
        }
    }
}
