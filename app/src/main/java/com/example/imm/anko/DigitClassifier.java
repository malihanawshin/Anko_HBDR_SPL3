package com.example.imm.anko;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DigitClassifier {

    private String input_name;
    private String output_name;
    private int input_size;
    private float[] output;
    private String[] output_names;
    private List<String> labels;

    private static final int total_classes = 10;
    private TensorFlowInferenceInterface tf;

    private static final String TAG = "all classes";
    private static final float threshold = 0.1f;

    public static DigitClassifier create(AssetManager assets, String model_path, String label_path, int input_size, String input_name, String output_name) throws IOException {

        DigitClassifier digitClassifier = new DigitClassifier();
        String label_file = label_path.split("file:///android_asset/")[1];

        digitClassifier.input_name = input_name;
        digitClassifier.output_name = output_name;
        digitClassifier.input_size = input_size;
        digitClassifier.labels = getLabels(assets,label_file);
        digitClassifier.output_names = new String[]{output_name};
        digitClassifier.output = new float[total_classes];

        digitClassifier.tf = new TensorFlowInferenceInterface(assets,model_path);

        return digitClassifier;
    }

    private static List<String> getLabels(AssetManager assets, String label_file) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(assets.open(label_file)));
        List<String> labels = new ArrayList<>();
        String line;

        while((line = br.readLine())!= null){
            labels.add(line);
        }
        br.close();

        return labels;
    }


    public DigitClassification recognize(float[] pixels){

        tf.feed(input_name, pixels, 1, input_size, input_size, 1);
        tf.run(output_names);
        tf.fetch(output_name,output);

        DigitClassification digit = new DigitClassification();

        for(int i=0;i<output.length;i++){
            Log.d(TAG, String.format("Class: %s Confidence: %f", labels.get(i), output[i]));
            if (output[i] > threshold && output[i] > digit.getConfidence()) {
                digit.update(labels.get(i),output[i]);
            }
        }

        return digit;
    }



}
