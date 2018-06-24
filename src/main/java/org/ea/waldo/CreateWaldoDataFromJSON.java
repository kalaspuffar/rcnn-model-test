package org.ea.waldo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tensorflow.example.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CreateWaldoDataFromJSON extends CreateWaldoCommon {

    public static void runFile(JSONObject jsonObject, TFRecordWriter tfWriter) throws Exception {

        Long id = (Long)jsonObject.get("id");
        String filename = "waldo_"+id+".jpg";

        BufferedImage bi = ImageIO.read(new File("waldo_images/" + filename));

        int width = bi.getWidth();
        int height = bi.getHeight();

        List<Long> label = new ArrayList<>();
        List<String> label_txt = new ArrayList<>();
        List<Float> xmin = new ArrayList<>();
        List<Float> xmax = new ArrayList<>();
        List<Float> ymin = new ArrayList<>();
        List<Float> ymax = new ArrayList<>();

        List<Long> difficult = new ArrayList<>();
        List<Long> truncated = new ArrayList<>();
        List<String> view = new ArrayList<>();

        Long xminL = (Long)jsonObject.get("xmin");
        Long xmaxL = (Long)jsonObject.get("xmax");
        Long yminL = (Long)jsonObject.get("ymin");
        Long ymaxL = (Long)jsonObject.get("ymax");

        label.add(1l);
        label_txt.add("waldo");

        difficult.add(0l);
        truncated.add(1l);
        view.add("frontal");

        xmin.add((float)xminL / (float)width);
        xmax.add((float)xmaxL / (float)width);
        ymin.add((float)yminL / (float)height);
        ymax.add((float)ymaxL / (float)height);

        Features features = Features.newBuilder()
                .putFeature("image/width", getIntFeature(width))
                .putFeature("image/height", getIntFeature(height))
                .putFeature("image/filename", getStringFeature(filename))
                .putFeature("image/source_id", getStringFeature(filename))
                .putFeature("image/key/sha256", getImageHash(bi))
                .putFeature("image/encoded", getImageFeature(bi))
                .putFeature("image/format", getStringFeature("jpeg"))
                .putFeature("image/object/bbox/xmin", getFloatFeature(xmin))
                .putFeature("image/object/bbox/xmax", getFloatFeature(xmax))
                .putFeature("image/object/bbox/ymin", getFloatFeature(ymin))
                .putFeature("image/object/bbox/ymax", getFloatFeature(ymax))
                .putFeature("image/object/class/text", getStringListFeature(label_txt))
                .putFeature("image/object/class/label", getIntListFeature(label))
                .putFeature("image/object/difficult", getIntListFeature(difficult))
                .putFeature("image/object/truncated", getIntListFeature(truncated))
                .putFeature("image/object/view", getStringListFeature(view))
                .build();
        Example example = Example.newBuilder().setFeatures(features).build();

        tfWriter.write(example.toByteArray());
    }


    public static void main(String[] args) {

        try {
            FileOutputStream fosTrain = new FileOutputStream("waldo_data/waldo_train.tfrecords");
            DataOutputStream dosTrain = new DataOutputStream(fosTrain);
            TFRecordWriter tfWriterTrain = new TFRecordWriter(dosTrain);
            FileOutputStream fosEval = new FileOutputStream("waldo_data/waldo_eval.tfrecords");
            DataOutputStream dosEval = new DataOutputStream(fosEval);
            TFRecordWriter tfWriterEval = new TFRecordWriter(dosEval);

            JSONArray waldoJSON = (JSONArray) JSONValue.parse(new FileReader(new File("waldo.json")));
            for(Object obj : waldoJSON) {
                JSONObject waldoEnt = (JSONObject)obj;
                Long id = (Long)waldoEnt.get("id");
                if(id <= 20) {
                    runFile(waldoEnt, tfWriterTrain);
                } else {
                    runFile(waldoEnt, tfWriterEval);
                }
            }

            dosTrain.close();
            dosEval.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
