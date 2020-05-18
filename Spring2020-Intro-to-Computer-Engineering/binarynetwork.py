import tensorflow as tf
from tensorflow import keras
import tensorflow_datasets as tfds
import numpy as np

model = tf.keras.models.Sequential([
		tf.keras.layers.Dense(128, activation='relu', input_shape=(28*28,)),
    	tf.keras.layers.Dense(10)])


# d0 = []
# f = open('/Users/sahiltyagi/Desktop/ice_project/weights/dense0.txt', 'r')
# for line in f:
# 	d0.append(float(line.strip()))
# f.close()

# d1 = []
# f = open('/Users/sahiltyagi/Desktop/ice_project/weights/dense1.txt', 'r')
# for line in f:
# 	d1.append(float(line.strip()))
# f.close()

# b0 = []
# f = open('/Users/sahiltyagi/Desktop/ice_project/weights/bias0.txt', 'r')
# for line in f:
# 	b0.append(float(line.strip()))
# f.close()

# b1 = []
# f = open('/Users/sahiltyagi/Desktop/ice_project/weights/bias1.txt', 'r')
# for line in f:
# 	b1.append(float(line.strip()))
# f.close()

d0 = []
f = open('/Users/sahiltyagi/Desktop/ice_project/binary_weights/binary_dense0.txt', 'r')
for line in f:
	d0.append(float(line.strip()))
f.close()

d1 = []
f = open('/Users/sahiltyagi/Desktop/ice_project/binary_weights/binary_dense1.txt', 'r')
for line in f:
	d1.append(float(line.strip()))
f.close()

b0 = []
f = open('/Users/sahiltyagi/Desktop/ice_project/binary_weights/binary_bias0.txt', 'r')
for line in f:
	b0.append(float(line.strip()))
f.close()

b1 = []
f = open('/Users/sahiltyagi/Desktop/ice_project/binary_weights/binary_bias1.txt', 'r')
for line in f:
	b1.append(float(line.strip()))
f.close()

np_d0 = np.asarray(d0, dtype=np.float32)
np_d1 = np.asarray(d1, dtype=np.float32)
np_b0 = np.asarray(b0, dtype=np.float32)
np_b1 = np.asarray(b1, dtype=np.float32)

np_d0 = np_d0.reshape(784, 128)
np_d1 = np_d1.reshape(128, 10)
np_b0 = np_b0.reshape(128,)
np_b1 = np_b1.reshape(10,)


layer1 = [np_d0, np_b0]
layer2 = [np_d1, np_b1]

model.layers[0].set_weights(layer1)
model.layers[1].set_weights(layer2)

# for i in range(0, len(model.layers)):
# 	print(model.layers[i])
# 	print(model.layers[i].get_weights())
# 	print(model.layers[i].get_weights()[0].shape)
# 	print(model.layers[i].get_weights()[1].shape)

# mnist_builder = tfds.builder("mnist")
# mnist_builder.download_and_prepare()
# ds_test = mnist_builder.as_dataset(split="test")
# print(type(ds_test))


model.compile(optimizer=tf.train.AdamOptimizer(learning_rate=1e-4),
              loss=keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['sparse_categorical_accuracy'])


(_, _), (x_test, y_test) = keras.datasets.mnist.load_data()

for j in range(0,1):
	x_test = x_test.reshape(10000, 784).astype('float32') / 255
	y_test = y_test.astype('float32')
	results = model.evaluate(x_test, y_test, batch_size=128)
	
	one_input = x_test.reshape(10000, 784)[0,:] / 255
	print(one_input)
	output_digit = y_test.reshape(10000,1)[0,]
	print(output_digit)
	np.savetxt('/Users/sahiltyagi/Desktop/ice_project/one_input.txt', one_input)
	print('test loss, test acc:', results)