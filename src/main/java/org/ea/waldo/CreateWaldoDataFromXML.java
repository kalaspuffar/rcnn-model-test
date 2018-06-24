package org.ea.waldo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tensorflow.example.Example;
import org.tensorflow.example.Features;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class CreateWaldoDataFromXML extends CreateWaldoCommon {

    /*

<annotation>
	<folder>waldo_images</folder>
	<filename>waldo_2.jpg</filename>
	<path>C:\github\rcnn-model-test\waldo_images\waldo_2.jpg</path>
	<source>
		<database>Unknown</database>
	</source>
	<size>
		<width>1599</width>
		<height>1230</height>
		<depth>3</depth>
	</size>
	<segmented>0</segmented>
	<object>
		<name>waldo</name>
		<pose>Unspecified</pose>
		<truncated>0</truncated>
		<difficult>0</difficult>
		<bndbox>
			<xmin>746</xmin>
			<ymin>248</ymin>
			<xmax>775</xmax>
			<ymax>290</ymax>
		</bndbox>
	</object>
</annotation>
     */

    private static String getTextFromTag(Document doc, String tagName) {
        Element filenameEl = (Element)doc.getElementsByTagName(tagName).item(0);
        return filenameEl.getTextContent();
    }

    public static void runFile(Document doc, TFRecordWriter tfWriter) throws Exception {

        String filename = getTextFromTag(doc, "filename");

        BufferedImage bi = ImageIO.read(new File(getTextFromTag(doc, "path")));

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

        Long xminL = Long.parseLong(getTextFromTag(doc, "xmin"));
        Long xmaxL = Long.parseLong(getTextFromTag(doc, "xmax"));
        Long yminL = Long.parseLong(getTextFromTag(doc, "ymin"));
        Long ymaxL = Long.parseLong(getTextFromTag(doc, "ymax"));

        label.add(1l);
        label_txt.add(getTextFromTag(doc, "name"));

        difficult.add(Long.parseLong(getTextFromTag(doc, "difficult")));
        //truncated.add(Long.parseLong(getTextFromTag(doc, "truncated")));
        truncated.add(1l);
        view.add(getTextFromTag(doc, "pose"));

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

            File dir = new File("waldo_images");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            int i = 0;
            for(File f : dir.listFiles()) {
                if(!f.isFile() || !f.getName().endsWith(".xml")) continue;
                Document doc = db.parse(f);
                if(i > 30) {
                    runFile(doc, tfWriterEval);
                } else {
                    runFile(doc, tfWriterTrain);
                }
                i++;
            }

            dosTrain.close();
            dosEval.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
