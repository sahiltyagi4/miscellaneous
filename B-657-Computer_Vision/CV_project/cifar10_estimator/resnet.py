# Copyright 2017 The TensorFlow Authors. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ==============================================================================
"""ResNet model for classifying images from CIFAR-10 dataset.

Support single-host training with one or multiple devices.

ResNet as proposed in:
Kaiming He, Xiangyu Zhang, Shaoqing Ren, Jian Sun
Deep Residual Learning for Image Recognition. arXiv:1512.03385

CIFAR-10 as in:
http://www.cs.toronto.edu/~kriz/cifar.html


"""
from __future__ import division
from __future__ import print_function

import argparse
import functools
import itertools
import os
import json
import time

import cifar10
import cifar10_model
import cifar10_utils
import numpy as np
import six
from six.moves import xrange  # pylint: disable=redefined-builtin
import tensorflow as tf

tf.logging.set_verbosity(tf.logging.INFO)


def get_model_fn(num_gpus, variable_strategy, num_workers):
  """Returns a function that will build the resnet model."""

  def _resnet_model_fn(features, labels, mode, params):
    """Resnet model body.

    Support single host, one or more GPU training. Parameter distribution can
    be either one of the following scheme.
    1. CPU is the parameter server and manages gradient updates.
    2. Parameters are distributed evenly across all GPUs, and the first GPU
       manages gradient updates.

    Args:
      features: a list of tensors, one for each tower
      labels: a list of tensors, one for each tower
      mode: ModeKeys.TRAIN or EVAL
      params: Hyperparameters suitable for tuning
    Returns:
      A EstimatorSpec object.
    """
    tf.logging.info('check to see if model fn not called on every switch input fn!!!!!!!!!')
    is_training = (mode == tf.estimator.ModeKeys.TRAIN)
    weight_decay = params.weight_decay
    momentum = params.momentum

    tower_features = features
    tower_labels = labels
    tower_losses = []
    tower_gradvars = []
    tower_preds = []

    # channels first (NCHW) is normally optimal on GPU and channels last (NHWC)
    # on CPU. The exception is Intel MKL on CPU which is optimal with
    # channels_last.
    data_format = params.data_format
    if not data_format:
      if num_gpus == 0:
        data_format = 'channels_last'
      else:
        data_format = 'channels_first'

    if num_gpus == 0:
      num_devices = 1
      device_type = 'cpu'
    else:
      num_devices = num_gpus
      device_type = 'gpu'


    consolidation_device = '/gpu:0' if variable_strategy == 'GPU' else '/cpu:0'
    with tf.device(consolidation_device):

      learning_rate = tf.compat.v1.train.exponential_decay(learning_rate=0.1, global_step=tf.train.get_global_step(), decay_steps=2000, decay_rate=0.0133)
      optimizer = tf.train.AdamOptimizer(learning_rate=learning_rate)
      train_hooks = []

    for i in range(num_devices):
      worker_device = '/{}:{}'.format(device_type, i)
      if variable_strategy == 'CPU':
        device_setter = cifar10_utils.local_device_setter(
            worker_device=worker_device)
      elif variable_strategy == 'GPU':
        device_setter = cifar10_utils.local_device_setter(worker_device=worker_device)

      with tf.variable_scope('resnet', reuse=bool(i != 0)):
        with tf.name_scope('tower_%d' % i) as name_scope:
          with tf.device(device_setter):
            loss, gradvars, preds, compgrad_op = _tower_fn(
                is_training, weight_decay, tower_features[i], tower_labels[i],
                data_format, params.num_layers, params.batch_norm_decay,
                params.batch_norm_epsilon, optimizer)
            
            tower_losses.append(loss)
            tower_gradvars.append(gradvars)
            tower_preds.append(preds)
            if i == 0:
              # Only trigger batch_norm moving mean and variance update from
              # the 1st tower. Ideally, we should grab the updates from all
              # towers but these stats accumulate extremely fast so we can
              # ignore the other stats from the other towers without
              # significant detriment.
              update_ops = tf.get_collection(tf.GraphKeys.UPDATE_OPS,
                                             name_scope)


    # Now compute global loss and gradients.
    with tf.name_scope('gradient_averaging'):
      all_grads = {}
      for grad, var in itertools.chain(*tower_gradvars):
        if grad is not None:
          all_grads.setdefault(var, []).append(grad)
      for var, grads in six.iteritems(all_grads):
        # Average gradients on the same device as the variables
        # to which they apply.
        with tf.device(var.device):
          if len(grads) == 1:
            avg_grad = grads[0]
          else:
            avg_grad = tf.multiply(tf.add_n(grads), 1. / len(grads))

    # Device that runs the ops to apply global gradient updates.
    consolidation_device = '/gpu:0' if variable_strategy == 'GPU' else '/cpu:0'
    with tf.device(consolidation_device):
      # Create single grouped train op
      loss = tf.reduce_mean(tower_losses, name='loss')

      examples_sec_hook = cifar10_utils.ExamplesPerSecondHook(params.train_batch_size, every_n_steps=100)

      train_hooks.append(examples_sec_hook)
      op_test1 = optimizer.apply_gradients(gradvars, global_step=tf.train.get_global_step())
      train_op = [op_test1]
      
      train_op.extend(update_ops)
      train_op = tf.group(*train_op)
      compgrad_op = tf.group(*compgrad_op)

      predictions = {'classes': tf.concat([p['classes'] for p in tower_preds], axis=0),
                      'probabilities':tf.concat([p['probabilities'] for p in tower_preds], axis=0)}
      stacked_labels = tf.concat(labels, axis=0)
      accuracy = tf.metrics.accuracy(stacked_labels, predictions['classes'])
      metrics = {'accuracy': accuracy}

      tensors_to_log = {'worker_train_accuracy': accuracy[1], 'learning_rate': learning_rate, 'loss': loss, 'global_step': tf.train.get_global_step()}
      logging_hook = tf.train.LoggingTensorHook(tensors=tensors_to_log, every_n_iter=40)
      train_hooks.append(logging_hook)
      
      return tf.estimator.EstimatorSpec(mode=mode,
          predictions=predictions,
          loss=loss,
          train_op=train_op,
          training_hooks=train_hooks,
          eval_metric_ops=metrics)   

  return _resnet_model_fn


def _tower_fn(is_training, weight_decay, feature, label, data_format,
              num_layers, batch_norm_decay, batch_norm_epsilon, optimizer):
  """Build computation tower (Resnet).

  Args:
    is_training: true if is training graph.
    weight_decay: weight regularization strength, a float.
    feature: a Tensor.
    label: a Tensor.
    data_format: channels_last (NHWC) or channels_first (NCHW).
    num_layers: number of layers, an int.
    batch_norm_decay: decay for batch normalization, a float.
    batch_norm_epsilon: epsilon for batch normalization, a float.

  Returns:
    A tuple with the loss for the tower, the gradients and parameters, and
    predictions.

  """
  model = cifar10_model.ResNetCifar10(
      num_layers,
      batch_norm_decay=batch_norm_decay,
      batch_norm_epsilon=batch_norm_epsilon,
      is_training=is_training,
      data_format=data_format)
  logits = model.forward_pass(feature, input_data_format='channels_last')
  tower_pred = {
      'classes': tf.argmax(input=logits, axis=1),
      'probabilities': tf.nn.softmax(logits)
  }

  tower_loss = tf.losses.sparse_softmax_cross_entropy(
      logits=logits, labels=label)
  tower_loss = tf.reduce_mean(tower_loss)

  model_params = tf.trainable_variables()
  tower_loss += weight_decay * tf.add_n(
      [tf.nn.l2_loss(v) for v in model_params])

  compgrad_tower = [optimizer.compute_gradients(tower_loss, model_params)]
  og_grads = [grad[0] for grad in compgrad_tower[0]]

  for new_g, new_v in zip(new_grads, model_params):
    tf.summary.histogram(new_v.name + '_MODIFIED', new_g) #last_gradient = new_g

  return tower_loss, zip(og_grads, model_params), tower_pred, compgrad_tower


def input_fn(data_dir,
             subset,
             num_shards,
             run_config,
             batch_size=128,
             use_distortion_for_training=True):
  """Create input graph for model.

  Args:
    data_dir: Directory where TFRecords representing the dataset are located.
    subset: one of 'train', 'validate' and 'eval'.
    num_shards: num of towers participating in data-parallel training.
    batch_size: total batch size for training to be divided by the number of
    shards.
    use_distortion_for_training: True to use distortions.
  Returns:
    two lists of tensors for features and labels, each of num_shards length.
  """
  #Is this called on every batch? make_batch is called here.

  # if subset == 'train':
  #   batch_size = run_config.get_node_batch_size

  tf.logging.info(">>> Num shards: " + str(num_shards))
  
  with tf.device('/cpu:0'):
    use_distortion = subset == 'train' and use_distortion_for_training
    dataset = cifar10.Cifar10DataSet(data_dir, subset, use_distortion)
    #XXX This is where the sharding needs to happen based on the worker?
    image_batch, label_batch = dataset.make_batch(int(batch_size))
    if num_shards <= 1:
      # No GPU available or only 1 GPU.
      return [image_batch], [label_batch]

    # Note that passing num=batch_size is safe here, even though
    # dataset.batch(batch_size) can, in some cases, return fewer than batch_size
    # examples. This is because it does so only when repeating for a limited
    # number of epochs, but our dataset repeats forever.
    # XXX repeats forever: what does that mean!?!?
    
    image_batch = tf.unstack(image_batch, num=batch_size, axis=0)
    label_batch = tf.unstack(label_batch, num=batch_size, axis=0)
    feature_shards = [[] for i in range(num_shards)]
    label_shards = [[] for i in range(num_shards)]
    for i in xrange(batch_size):
      idx = i % num_shards
      feature_shards[idx].append(image_batch[i])
      label_shards[idx].append(label_batch[i])
    feature_shards = [tf.parallel_stack(x) for x in feature_shards]
    label_shards = [tf.parallel_stack(x) for x in label_shards]
    return feature_shards, label_shards

def main(job_dir, data_dir, num_gpus, variable_strategy,
         use_distortion_for_training, log_device_placement, num_intra_threads,
         **hparams):
  # The env variable is on deprecation path, default is set to off.
  tf.set_random_seed(0)
  np.random.seed(0)

  tf.logging.info(">>> Num intra threads = " + str(num_intra_threads))

  sess_config = tf.ConfigProto(
      allow_soft_placement=True,
      log_device_placement=log_device_placement,
      intra_op_parallelism_threads=num_intra_threads,
      gpu_options=tf.GPUOptions(force_gpu_compatible=True))

  tf_config = json.loads(os.environ['TF_CONFIG'])

  run_config = tf.estimator.RunConfig(session_config=sess_config, model_dir=job_dir, save_checkpoints_steps=100, keep_checkpoint_max=7)

  hparams=tf.contrib.training.HParams(is_chief=run_config.is_chief, **hparams)
  warm_start = hparams.warm_start
  tf.logging.info('##################### value of warm-start is: ' + str(warm_start))
  tf.logging.info("##################### is the run_config chief: " + str(run_config.is_chief))
  tf.logging.info("##################### hparams.train_batch_size " + str(hparams.train_batch_size))
  tf.logging.info("##################### hparams.eval_batch_size " + str(hparams.eval_batch_size))
  tf.logging.info("##################### hparams.train_steps " + str(hparams.train_steps))
  tf.logging.info("##################### value of replicas " + str(run_config.num_worker_replicas))

  train_input_fn = functools.partial(
      input_fn,
      data_dir,
      subset='train',
      num_shards=num_gpus,
      run_config=run_config,
      batch_size=128,
      use_distortion_for_training=use_distortion_for_training)

  eval_input_fn = functools.partial(
      input_fn,
      data_dir,
      subset='eval',
      run_config=run_config,
      num_shards=num_gpus,
      batch_size=hparams.eval_batch_size)

  num_eval_examples = cifar10.Cifar10DataSet.num_examples_per_epoch('eval')
  if num_eval_examples % hparams.eval_batch_size != 0:
      raise ValueError('validation set size must be multiple of eval_batch_size')

  train_steps = hparams.train_steps
  eval_steps=50
  
  classifier = tf.estimator.Estimator(model_fn=get_model_fn(num_gpus,variable_strategy,run_config.num_worker_replicas or 1), 
    config=run_config, params=hparams)

  acc_threshold = tf.estimator.experimental.stop_if_higher_hook(estimator=classifier, metric_name="accuracy", threshold=0.90)
  train_spec = tf.estimator.TrainSpec(input_fn=train_input_fn, hooks= [acc_threshold])
  eval_spec = tf.estimator.EvalSpec(input_fn=eval_input_fn, steps=eval_steps)
  tf.estimator.train_and_evaluate(classifier, train_spec, eval_spec)



if __name__ == '__main__':
  parser = argparse.ArgumentParser()
  parser.add_argument(
      '--data-dir',
      type=str,
      required=True,
      help='The directory where the CIFAR-10 input data is stored.')
  parser.add_argument(
      '--job-dir',
      type=str,
      required=True,
      help='The directory where the model will be stored.')
  parser.add_argument(
      '--variable-strategy',
      choices=['CPU', 'GPU'],
      type=str,
      default='CPU',
      help='Where to locate variable operations')
  parser.add_argument(
      '--num-gpus',
      type=int,
      default=1,
      help='The number of gpus used. Uses only CPU if set to 0.')
  parser.add_argument(
      '--num-layers',
      type=int,
      default=20,
      help='The number of layers of the model.')
  parser.add_argument(
      '--train-steps',
      type=int,
      default=80000,
      help='The number of steps to use for training.')
  parser.add_argument(
      '--train-batch-size',
      type=int,
      default=128,
      help='Batch size for training.')
  parser.add_argument(
      '--eval-batch-size',
      type=int,
      default=100,
      help='Batch size for validation.')
  parser.add_argument(
      '--momentum',
      type=float,
      default=0.995,
      help='Momentum for MomentumOptimizer.')
  parser.add_argument(
      '--weight-decay',
      type=float,
      default=5e-4,
      help='Weight decay for convolutions.')
  parser.add_argument(
      '--learning-rate',
      type=float,
      default=0.1,
      help="""\
      This is the inital learning rate value. The learning rate will decrease
      during training. For more details check the model_fn implementation in
      this file.\
      """)
  parser.add_argument(
      '--use-distortion-for-training',
      type=bool,
      default=True,
      help='If doing image distortion for training.')
  parser.add_argument(
      '--sync',
      action='store_true',
      default=False,
      help="""\
      If present when running in a distributed environment will run on sync mode.\
      """)
  parser.add_argument(
      '--num-intra-threads',
      type=int,
      default=0,
      help="""\
      Number of threads to use for intra-op parallelism. When training on CPU
      set to 0 to have the system pick the appropriate number or alternatively
      set it to the number of physical CPU cores.\
      """)
  parser.add_argument(
      '--num-inter-threads',
      type=int,
      default=0,
      help="""\
      Number of threads to use for inter-op parallelism. If set to 0, the
      system will pick an appropriate number.\
      """)
  parser.add_argument(
      '--data-format',
      type=str,
      default=None,
      help="""\
      If not set, the data format best for the training device is used. 
      Allowed values: channels_first (NCHW) channels_last (NHWC).\
      """)
  parser.add_argument(
      '--warm-start',
      type=str,
      default='false')
  parser.add_argument(
      '--log-device-placement',
      action='store_true',
      default=True,
      help='Whether to log device placement.')
  parser.add_argument(
      '--batch-norm-decay',
      type=float,
      default=0.997,
      help='Decay for batch norm.')
  parser.add_argument(
      '--batch-norm-epsilon',
      type=float,
      default=2e-5,
      help='Epsilon for batch norm.')
  args = parser.parse_args()

  if args.num_gpus > 0:
    assert tf.test.is_gpu_available(), "Requested GPUs but none found."
  if args.num_gpus < 0:
    raise ValueError(
        'Invalid GPU count: \"--num-gpus\" must be 0 or a positive integer.')
  if args.num_gpus == 0 and args.variable_strategy == 'GPU':
    raise ValueError('num-gpus=0, CPU must be used as parameter server. Set'
                     '--variable-strategy=CPU.')
  if (args.num_layers - 2) % 6 != 0:
    raise ValueError('Invalid --num-layers parameter.')
  if args.num_gpus != 0 and args.train_batch_size % args.num_gpus != 0:
    raise ValueError('--train-batch-size must be multiple of --num-gpus.')
  if args.num_gpus != 0 and args.eval_batch_size % args.num_gpus != 0:
    raise ValueError('--eval-batch-size must be multiple of --num-gpus.')

  main(**vars(args))