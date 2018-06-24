package org.ea.waldo;

import com.google.protobuf.ByteString;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FloatList;
import org.tensorflow.example.Int64List;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.List;

public class CreateWaldoCommon {

    private static final String    HEXES    = "0123456789ABCDEF";

    protected static String getHex(byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    protected static Feature getIntFeature(int val) {
        Int64List int64List = Int64List.newBuilder().addValue(val).build();
        Feature intFeature = Feature.newBuilder().setInt64List(int64List).build();
        return intFeature;
    }

    protected static Feature getStringFeature(String val) {
        ByteString byteString = ByteString.copyFromUtf8(val);
        BytesList bytesList = BytesList.newBuilder().addValue(byteString).build();
        Feature text = Feature.newBuilder().setBytesList(bytesList).build();
        return text;
    }


    protected static Feature getIntListFeature(List<Long> val) {
        Int64List int64List = Int64List.newBuilder().addAllValue(val).build();
        Feature intFeature = Feature.newBuilder().setInt64List(int64List).build();
        return intFeature;
    }


    protected static Feature getFloatFeature(List<Float> val) {
        FloatList floatList = FloatList.newBuilder().addAllValue(val).build();
        Feature floatFeature = Feature.newBuilder().setFloatList(floatList).build();
        return floatFeature;
    }

    protected static Feature getStringListFeature(List<String> val) {
        BytesList.Builder bBuilder = BytesList.newBuilder();
        for(String s : val) {
            ByteString byteString = ByteString.copyFromUtf8(s);
            bBuilder.addValue(byteString);
        }
        Feature text = Feature.newBuilder().setBytesList(bBuilder.build()).build();
        return text;
    }

    protected static Feature getImageHash(BufferedImage orgImg) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage bi = new BufferedImage(orgImg.getWidth(), orgImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();
        g.drawImage(orgImg, 0, 0, null);

        ImageIO.write(bi, "jpeg", baos);
        baos.flush();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(baos.toByteArray());
        baos.close();

        byte[] imageDigest = md.digest();

        ByteString byteString = ByteString.copyFrom(getHex(imageDigest), "UTF-8");
        BytesList bytesList = BytesList.newBuilder().addValue(byteString).build();
        Feature text = Feature.newBuilder().setBytesList(bytesList).build();

        return text;
    }

    protected static Feature getImageFeature(BufferedImage orgImg) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage bi = new BufferedImage(orgImg.getWidth(), orgImg.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D)bi.getGraphics();
        g.drawImage(orgImg, 0, 0, null);

        ImageIO.write(bi, "jpeg", baos);
        baos.flush();
        ByteString byteString = ByteString.copyFrom(baos.toByteArray());
        baos.close();
        BytesList bytesList = BytesList.newBuilder().addValue(byteString).build();
        Feature text = Feature.newBuilder().setBytesList(bytesList).build();

        return text;
    }
}
