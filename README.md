
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


Train
```
python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=/home/danielp/github/rcnn-model-test/waldo_model/waldo_model.config \
    --train_dir=/home/danielp/github/rcnn-model-test/waldo_model/train
```


python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=/home/danielp/github/rcnn-model-test/model/mymodel.config \
    --train_dir=/home/danielp/github/rcnn-model-test/model/train

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
    --pipeline_config_path /home/danielp/github/rcnn-model-test/waldo_model/waldo_model.config \
    --trained_checkpoint_prefix /home/danielp/github/rcnn-model-test/waldo_model/train/model.ckpt-74 \
    --output_directory /home/danielp/github/rcnn-model-test/waldo_model/export
```

```
python object_detection/export_inference_graph.py \
    --input_type image_tensor \
    --pipeline_config_path /home/danielp/github/rcnn-model-test/model/mymodel.config \
    --trained_checkpoint_prefix /home/danielp/github/rcnn-model-test/model/train/model.ckpt-910 \
    --output_directory /home/danielp/github/rcnn-model-test/model/export
```


Keepers
```
python object_detection/train.py \
    --logtostderr \
    --pipeline_config_path=/home/danielp/github/rcnn-model-test/mymodel/mymodel.config \
    --train_dir=/home/danielp/github/rcnn-model-test/mymodel/train
```