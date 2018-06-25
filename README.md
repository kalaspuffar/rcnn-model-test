# RCNN Model testing

This repository is a simple example using the tensorflow Object detection library experimenting with usage and learning 
patterns to use in order to run it smoothly.

### Creating data for training

This could be done by annotating the data using [labelImg](https://github.com/tzutalin/labelImg) for annotating the images in either YOLO or PascalVOC format. Then you can use the java classes provided here to create TFRecords from either XML or JSON format.

### Training model

After you've followed the install guide at the [Tensorflow Object Detection](https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/installation.md) page you can start your training.

Running the command below supplying new directories for the training configuration and training output dir will train your model.
```
python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=[CONFIGURATION] \
    --train_dir=[TRAINING OUTPUT DIR]
```

### Evaluating model

After the model is trained you may validate it by using the set of examples you've saved for evaluating.

Running the command below specifying configuration file, training output directory and the evaluation output directory where the script will put the result of the evaluation.
```
python object_detection/eval.py \
    --logtostderr \
    --pipeline_config_path=[CONFIGURATION] \
    --checkpoint_dir=[TRAINING OUTPUT DIR] \
    --eval_dir=[EVALUATION OUTPUT DIR]
```

### Tensorboard

After you've trained your model you may take a closer look at it using the visualizing tool called tensorboard that you can install
alongside the tensorflow installation. 

Running the command below specifying the training output directory will start a webserver you can visit to look at the training values of
your model.
```
tensorboard --logdir=[TRAINING OUTPUT DIR]
```

### Exporting model

Last step before you can run inference on your model is to export it to a few different formats that you can use to read your model.

The command below will give you a frozen model, a saved bundle and also a checkpoint save that you may load in any other program.
The frozen model may also be used to create a TFLite model.

Run the command below replacing the configuration. training output directory, checkpoint file and the export output directory. 
The checkpoint file will have some prefix and end with the training step number. Example model.ckpt-200000
```
python object_detection/export_inference_graph.py \
    --input_type image_tensor \
    --pipeline_config_path [CONFIGURATION] \
    --trained_checkpoint_prefix [TRAINING OUTPUT DIR]/[CHECKPOINT FILE] \
    --output_directory [EXPORT OUTPUT DIR]
```

### Known bugs and needed updates for Python 3

Known bug update ```models/research/object_detection/utils/learning_schedules.py```
```
rate_index = tf.reduce_max(tf.where(tf.greater_equal(global_step, boundaries),
                                      range(num_boundaries),
                                      [0] * num_boundaries))
```

Add list around the range like this:
```
rate_index = tf.reduce_max(tf.where(tf.greater_equal(global_step, boundaries),
                                     list(range(num_boundaries)),
                                      [0] * num_boundaries))
```

Also in ```models/research/object_detection/setup.py``` we need to add the matlabplot library in order to run in a cloud
setup.
```
REQUIRED_PACKAGES = ['Pillow>=1.0', 'matplotlib']
```

### Notes

Train
```
python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=/home/woden/github/rcnn-model-test/waldo_model/waldo_model.config \
    --train_dir=/home/woden/github/rcnn-model-test/waldo_model/train
```

```
python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=/home/woden/github/rcnn-model-test/model/mymodel.config \
    --train_dir=/home/woden/github/rcnn-model-test/model/train
```

```
python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=/home/woden/github/rcnn-model-test/mymodel/mymodel.config \
    --train_dir=/home/woden/github/rcnn-model-test/mymodel/train
```


Evaluate
```
python object_detection/eval.py \
    --logtostderr \
    --pipeline_config_path=/home/danielp/github/rcnn-model-test/waldo_model/waldo_model.config \
    --checkpoint_dir=/home/danielp/github/rcnn-model-test/waldo_model/train \
    --eval_dir=/home/danielp/github/rcnn-model-test/waldo_model/eval
```

Tensorboard
```
tensorboard --logdir=/home/danielp/github/rcnn-model-test/waldo_model/train
```

Export
```
python object_detection/export_inference_graph.py \
    --input_type image_tensor \
    --pipeline_config_path /home/woden/github/rcnn-model-test/waldo_model/waldo_model.config \
    --trained_checkpoint_prefix /home/woden/github/rcnn-model-test/waldo_model/train/model.ckpt-200000 \
    --output_directory /home/woden/github/rcnn-model-test/waldo_model/export
```

```
python object_detection/export_inference_graph.py \
    --input_type image_tensor \
    --pipeline_config_path /home/woden/github/rcnn-model-test/model/mymodel.config \
    --trained_checkpoint_prefix /home/woden/github/rcnn-model-test/model/train/model.ckpt-146987 \
    --output_directory /home/woden/github/rcnn-model-test/model/export
```

Cloud
```
gcloud ml-engine jobs submit training object_detection_`date +%s` \
    --runtime-version 1.7 \
    --job-dir=gs://mloutput/workdata/model/train \
    --packages dist/object_detection-0.1.tar.gz,slim/dist/slim-0.1.tar.gz \
    --module-name object_detection.train \
    --region us-central1 \
    --config /home/woden/github/rcnn-model-test/gcp_train.yaml \
    -- \
    --train_dir=gs://mloutput/workdata/model/train \
    --pipeline_config_path=gs://mloutput/workdata/model/mymodel.config
```
