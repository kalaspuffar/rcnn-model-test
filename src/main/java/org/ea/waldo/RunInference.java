package org.ea.waldo;

import com.google.protobuf.ByteString;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;
import org.tensorflow.example.Int64List;
import org.tensorflow.types.UInt8;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

public class RunInference {

    public static final String[] labels = new String[] {
            "background",
            "aeroplane",
            "bicycle",
            "bird",
            "boat",
            "bottle",
            "bus",
            "car",
            "cat",
            "chair",
            "cow",
            "diningtable",
            "dog",
            "horse",
            "motorbike",
            "person",
            "pottedplant",
            "sheep",
            "sofa",
            "train",
            "tvmonitor"
    };

    public static void main(String[] args) {
        System.out.println( "Hello World! I'm using tensorflow version " + TensorFlow.version() );

        try {
            SavedModelBundle smb = SavedModelBundle.load("./waldo_model/export/saved_model", "serve");
            Session s = smb.session();

            BufferedImage orgImg = ImageIO.read(new File(args[0]));

            BufferedImage bi = new BufferedImage(orgImg.getWidth(), orgImg.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = (Graphics2D)bi.getGraphics();
            g.drawImage(orgImg, 0, 0, null);

            System.out.println(bi.getType());

            int w = bi.getWidth();
            int h = bi.getHeight();
            int bufferSize = w * h * 3;

            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            for (int i = 0; i < h; i++) {
                for (int j = 0; j < w; j++) {
                    int pixel = bi.getRGB(j, i);

                    byteBuffer.put((byte)((pixel >> 16) & 0xFF));
                    byteBuffer.put((byte)((pixel >> 8) & 0xFF));
                    byteBuffer.put((byte)((pixel) & 0xFF));
                }
            }
            byteBuffer.rewind();

            Tensor inputTensor = Tensor.create(UInt8.class, new long[] {1, bi.getHeight(), bi.getWidth(), 3}, byteBuffer);

            List<Tensor<?>> result = s.runner()
                    .feed("image_tensor", inputTensor)
                    .fetch("detection_boxes")
                    .fetch("detection_scores")
                    .fetch("detection_classes")
                    .fetch("num_detections")
                    .run();

            int numMaxClasses = 300;

            float[][][] boxes = new float[1][numMaxClasses][4];
            float[][][] detection_boxes = result.get(0).copyTo(boxes);
            float[][] scores = new float[1][numMaxClasses];
            float[][] detection_scores = result.get(1).copyTo(scores);
            float[][] classes = new float[1][numMaxClasses];
            float[][] detection_classes = result.get(2).copyTo(classes);
            float[] n = new float[1];
            float[] numDetections = result.get(3).copyTo(n);

            int numDet = Math.round(numDetections[0]);
            System.out.println("Number of detected: " + numDet);

            for(int i=0; i<3; i++) {
                System.out.println("-----------------------------------");

                int ymin = Math.round(detection_boxes[0][i][0] * h);
                int xmin = Math.round(detection_boxes[0][i][1] * w);
                int ymax = Math.round(detection_boxes[0][i][2] * h);
                int xmax = Math.round(detection_boxes[0][i][3] * w);

                System.out.println("X1 " + xmin + " Y1 " + ymin + " X2 " + xmax + " Y2 " + ymax);
                System.out.println("Score " + detection_scores[0][i]);
                System.out.println("Predicted " + detection_classes[0][i]);

                g.setColor(Color.RED);
                g.drawRect(xmin, ymin, xmax - xmin, ymax - ymin);
                g.drawString(labels[Math.round(detection_classes[0][i])], xmin, ymin);
            }

            ImageIO.write(bi, "PNG", new File(args[1]));
/*
            for(int i=0; i<numDet; i++) {
                System.out.println("Score "+detection_scores[0][i]);
            }
*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
