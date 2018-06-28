Machine Learning
Object detection
Part 1 - Create training data
---
TFRecord writing class
---
```java
/**
  * TFRecord format:
  * uint64 length
  * uint32 masked_crc32_of_length
  * byte   data[length]
  * uint32 masked_crc32_of_data
  */
```
---
```java
public class TFRecordWriter {
    private static final int MASK_DELTA = 0xa282ead8;
    private final DataOutput output;

    public TFRecordWriter(DataOutput output) {
        this.output = output;
    }

    ...
```
---
```java
public void write(byte[] record, int offset, int length)
    throws IOException {

    byte[] len = toInt64LE(length);
    output.write(len);
    output.write(toInt32LE(maskedCrc32c(len)));
    output.write(record, offset, length);
    output.write(toInt32LE(maskedCrc32c(record, offset, length)));

}
```
@[4]
@[5-6]
@[7-8]
Cyclic redundancy check
---
```java
private byte[] toInt64LE(long data) {
    byte[] buff = new byte[8];
    ByteBuffer bb = ByteBuffer.wrap(buff);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.putLong(data);
    return buff;
}
```
---
```java
public static int maskedCrc32c(byte[] data,
        int offset, int length) {

    PureJavaCrc32C crc32c = new PureJavaCrc32C();
    crc32c.update(data, offset, length);
    int crc = (int)crc32c.getValue();
    return ((crc >>> 15) | (crc << 17)) + MASK_DELTA;

}
```
@[4]
@[5-6]
@[7]
---
Writer application
---
```java
public static void main(String[] args) {
    try {
        FileOutputStream fosTrain =
            new FileOutputStream("file.tfrecords");
        DataOutputStream dosTrain =
            new DataOutputStream(fosTrain);
        TFRecordWriter tfWriterTrain =
            new TFRecordWriter(dosTrain);
    ...
```
---
```java
JSONArray waldoJSON = (JSONArray)
    JSONValue.parse(new FileReader(new File("waldo.json")));

for(Object obj : waldoJSON) {
    JSONObject waldoEnt = (JSONObject)obj;
    Long id = (Long)waldoEnt.get("id");
    if(id <= 20) {
        runFile(waldoEnt, tfWriterTrain);
    } else {
        runFile(waldoEnt, tfWriterEval);
    }
}
```
@[6-7]
---
The runFile function
---
```java
public static void runFile(JSONObject jsonObject, TFRecordWriter tfWriter) throws Exception {

    Long id = (Long)jsonObject.get("id");
    String filename = "waldo_"+id+".jpg";

    BufferedImage bi = ImageIO.read(
        new File("waldo_images/" + filename));

    int width = bi.getWidth();
    int height = bi.getHeight();
    ...
```
---
```java
    ...
    List<Long> label = new ArrayList<>();
    List<String> label_txt = new ArrayList<>();
    List<Float> xmin = new ArrayList<>();
    List<Float> xmax = new ArrayList<>();
    List<Float> ymin = new ArrayList<>();
    List<Float> ymax = new ArrayList<>();
    ...
```
---
```java
    ...
    Long xminL = (Long)jsonObject.get("xmin");
    Long xmaxL = (Long)jsonObject.get("xmax");
    Long yminL = (Long)jsonObject.get("ymin");
    Long ymaxL = (Long)jsonObject.get("ymax");
    ...
```
---
```java
    ...
    label.add(1l);
    label_txt.add("waldo");

    xmin.add((float)xminL / (float)width);
    xmax.add((float)xmaxL / (float)width);
    ymin.add((float)yminL / (float)height);
    ymax.add((float)ymaxL / (float)height);
    ...
```
---
```java
Features features = Features.newBuilder()
    .putFeature("image/width", getIntFeature(width))
    .putFeature("image/height", getIntFeature(height))
    .putFeature("image/filename", getStringFeature(filename))
    .putFeature("image/source_id", getStringFeature(filename))
    .putFeature("image/encoded", getImageFeature(bi))
    .putFeature("image/format", getStringFeature("jpeg"))
    .putFeature("image/object/bbox/xmin", getFloatFeature(xmin))
    .putFeature("image/object/bbox/xmax", getFloatFeature(xmax))
    .putFeature("image/object/bbox/ymin", getFloatFeature(ymin))
    .putFeature("image/object/bbox/ymax", getFloatFeature(ymax))
    .putFeature("image/object/class/text", getStringListFeature(label_txt))
    .putFeature("image/object/class/label", getIntListFeature(label))
    .build();
```
@[2-3]
@[4-5]
@[6-7]
@[8-11]
@[12-13]
---
```java
Example example = Example.newBuilder()
    .setFeatures(features).build();
tfWriter.write(example.toByteArray());
```
---
```java
private static Feature getIntFeature(int val) {
    Int64List int64List = Int64List.newBuilder()
        .addValue(val).build();
    Feature intFeature = Feature.newBuilder()
        .setInt64List(int64List).build();
    return intFeature;
}
```
@[2-3]
@[4-5]
---
```java
private static Feature getIntListFeature(List<Long> val) {
    Int64List int64List = Int64List.newBuilder()
        .addAllValue(val).build();
    Feature intFeature = Feature.newBuilder()
        .setInt64List(int64List).build();
    return intFeature;
}
```
@[3]
---
```java
private static Feature getStringFeature(String val) {
    ByteString byteString =
        ByteString.copyFromUtf8(val);
    BytesList bytesList = BytesList.newBuilder()
        .addValue(byteString).build();
    Feature text = Feature.newBuilder()
        .setBytesList(bytesList).build();
    return text;
}
```
@[2-3]
@[4-5]
---
```java
private static Feature getStringListFeature(List<String> val) {
    BytesList.Builder bBuilder = BytesList.newBuilder();
    for(String s : val) {
        ByteString byteString = ByteString
            .copyFromUtf8(s);
        bBuilder.addValue(byteString);
    }
    Feature text = Feature.newBuilder()
        .setBytesList(bBuilder.build()).build();
    return text;
}
```
@[2]
@[3-7]
@[8-10]
---
```java
private static Feature getImageFeature(BufferedImage orgImg)
    throws Exception {
    BufferedImage bi = new BufferedImage(
        orgImg.getWidth(),
        orgImg.getHeight(),
        BufferedImage.TYPE_INT_RGB
    );
    Graphics2D g = (Graphics2D)bi.getGraphics();
    g.drawImage(orgImg, 0, 0, null);
    ...
```
@[3-7]
@[8-9]
---
```java
    ...
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bi, "jpeg", baos);
    baos.flush();

    ByteString byteString = ByteString.copyFrom(baos.toByteArray());
    baos.close();
    BytesList bytesList = BytesList.newBuilder()
        .addValue(byteString).build();

    Feature text = Feature.newBuilder()
        .setBytesList(bytesList).build();
    return text;
}
```
@[2-4]
@[6-7]
@[8-9]
@[11-13]
Machine Learning<br>
Object detection<br>
Part 2 - Training
---
Configuration file
```json
model {
  faster_rcnn {
    num_classes: 1
    image_resizer {
      keep_aspect_ratio_resizer {
        min_dimension: 600
        max_dimension: 1024
      }
    }
    feature_extractor {
      type: 'faster_rcnn_resnet152'
      first_stage_features_stride: 16
    }
    ...
```
---
```json
    ...
    first_stage_anchor_generator {
      grid_anchor_generator {
        scales: [0.25, 0.5, 1.0, 2.0]
        aspect_ratios: [0.5, 1.0, 2.0]
        height_stride: 16
        width_stride: 16
      }
    }
    ...
```
---
```json
    ...
    first_stage_box_predictor_conv_hyperparams {
      op: CONV
      regularizer {
        l2_regularizer {
          weight: 0.0
        }
      }
      initializer {
        truncated_normal_initializer {
          stddev: 0.01
        }
      }
    }
    ...
```
---
```json
    ...
    first_stage_nms_score_threshold: 0.0
    first_stage_nms_iou_threshold: 0.2
    first_stage_max_proposals: 100
    first_stage_localization_loss_weight: 2.0
    first_stage_objectness_loss_weight: 1.0
    initial_crop_size: 14
    maxpool_kernel_size: 2
    maxpool_stride: 2
    ...
```
---
```json
    ...
    second_stage_box_predictor {
      mask_rcnn_box_predictor {
        use_dropout: false
        dropout_keep_probability: 1.0
        fc_hyperparams {
          op: FC
          regularizer {
            l2_regularizer {
              weight: 0.0
            }
          }
          ...
```
---
```json
          ...
          initializer {
            variance_scaling_initializer {
              factor: 1.0
              uniform: true
              mode: FAN_AVG
            }
          }
        }
      }
    }
    ...
```
---
```json
    ...
    second_stage_post_processing {
      batch_non_max_suppression {
        score_threshold: 0.0
        iou_threshold: 0.3
        max_detections_per_class: 10
        max_total_detections: 10
      }
      score_converter: SOFTMAX
    }
    second_stage_localization_loss_weight: 2.0
    second_stage_classification_loss_weight: 1.0
  }
}
```
---
```json
train_config: {
  batch_size: 1
  optimizer {
    momentum_optimizer: {
      learning_rate: {
        manual_step_learning_rate {
          ...
```
---
```json
          ...
          initial_learning_rate: 0.0001
          schedule {
            step: 900000
            learning_rate: .00001
          }
          schedule {
            step: 1200000
            learning_rate: .000001
          }
          ...
```
---
```json
          ...
        }
      }
      momentum_optimizer_value: 0.9
    }
    use_moving_average: false
  }
  ...
```
---
```json
  ...
  gradient_clipping_by_norm: 10.0
  fine_tune_checkpoint: "[OUTPUT_PATH]/model.ckpt"
  from_detection_checkpoint: false
  num_steps: 200000
  data_augmentation_options {
    random_horizontal_flip {
    }
  }
}
```
---
```json
train_input_reader: {
  tf_record_input_reader {
    input_path: "[DATA_PATH]/[TRAIN_FILE].tfrecords"
  }
  label_map_path: "[DATA_PATH]/[LABEL_FILE].pbtxt"
  shuffle: true
  num_readers: 1
}
```
---
```json
eval_config: {
  num_examples: 1
  max_evals: 1
}
```
---
```json
eval_input_reader: {
  tf_record_input_reader {
    input_path: "[DATA_PATH]/[EVAL_FILE].tfrecords"
  }
  label_map_path: "[DATA_PATH]/[LABEL_FILE]waldo.pbtxt"
  shuffle: true
  num_readers: 1
}
```
---
Label file
```json
item {
  id: 1
  name: 'waldo'
}
```