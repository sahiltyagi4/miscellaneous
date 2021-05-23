import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dropout
from tensorflow.keras.layers import BatchNormalization
from tensorflow.keras.layers import Dense
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.utils import to_categorical
from sklearn.metrics import classification_report
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Activation
from tensorflow.keras.layers import Flatten
from skimage import exposure
from skimage import transform
from skimage import io
import numpy as np
import argparse
import random
import os
import time
import psutil

#hyperparameters
epoch = 100
lr = 1e-3
batch_size = 64

parser = argparse.ArgumentParser()
parser.add_argument("-d", "--dataset", required=True)
parser.add_argument("-m", "--model", required=True)
parser.add_argument("-p", "--plot", type=str, required=True)
parser.add_argument("-l", "--labels", type=str, required=True)
args = vars(parser.parse_args())

def model_function(width, height, depth, classes):
	model = Sequential()
	inputShape = (height, width, depth)
	chan = -1

	model.add(Conv2D(8, (5, 5), padding="same",input_shape=inputShape))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=chan))
	model.add(MaxPooling2D(pool_size=(2, 2)))
	model.add(Conv2D(16, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=chan))
	model.add(Conv2D(16, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=chan))
	model.add(MaxPooling2D(pool_size=(2, 2)))
	model.add(Conv2D(32, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=chan))
	model.add(Conv2D(32, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=chan))
	model.add(MaxPooling2D(pool_size=(2, 2)))
	model.add(Flatten())
	model.add(Dense(128))
	model.add(Activation("relu"))
	model.add(BatchNormalization())
	model.add(Dropout(0.4))
	model.add(Flatten())
	model.add(Dense(256))
	model.add(Activation("relu"))
	model.add(BatchNormalization())
	model.add(Dropout(0.4))
	model.add(Dense(classes))
	model.add(Activation("softmax"))

	return model

def resource_utilization():
	return psutil.virtual_memory()[2]


def parse_input(basePath, csvPath):
	data = []
	labels = []
	file = open(csvPath).read().strip().split("\n")[1:]
	random.shuffle(file)

	for (i, line) in enumerate(file):
		# train on smaller dataset when training on raspberry pi due to memory constraints
		if i > 999 and i // 1000 == 0:
			print('parsed input batch of ' + str(i) + ' images so far...')

		if resource_utilization() > 90:
			print('EXCEEDED RESOURCE UTILIZATION SO GOING TO STOP LOADING MORE INPUT DATA!')
			break

		(label, imgpath) = line.strip().split(",")[-2:]

		imgpath = os.path.sep.join([basePath, imgpath])
		image = io.imread(imgpath)

		image = transform.resize(image, (32, 32))
		image = exposure.equalize_adapthist(image, clip_limit=0.1)

		data.append(image)
		labels.append(int(label))

	print('successfully read input data for training/testing phases...')

	data = np.array(data)
	labels = np.array(labels)

	return (data, labels)

labels = open(args["labels"]).read().strip().split("\n")[1:]
labels = [l.split(",")[1] for l in labels]

trainpath = os.path.sep.join([args["dataset"], "Train.csv"])
testpath = os.path.sep.join([args["dataset"], "Test.csv"])

print("going to load training and testing data...")
(train_x, train_y) = parse_input(args["dataset"], trainpath)
(test_x, test_y) = parse_input(args["dataset"], testpath)

train_x = train_x.astype("float32") / 255.0
test_x = test_x.astype("float32") / 255.0

labelnum = len(np.unique(train_y))
train_y = to_categorical(train_y, labelnum)
test_y = to_categorical(test_y, labelnum)

total_class = trainY.sum(axis=0)
class_weights = total_class.max() / total_class

aug = ImageDataGenerator(rotation_range=20, zoom_range=0.2, width_shift_range=0.15, height_shift_range=0.15, shear_range=0.2, 
						horizontal_flip=False, vertical_flip=False, fill_mode="nearest")

opt = Adam(lr=lr, decay=lr / (epoch * 0.5))
model = model_function(width=32, height=32, depth=3, classes=labelnum)
model.compile(loss="categorical_crossentropy", optimizer=opt, metrics=["accuracy"])

print("going to start training...")
fitting = model.fit_generator(aug.flow(train_x, train_y, batch_size=batch_size), validation_data=(test_x, test_y), steps_per_epoch=train_x.shape[0] // batch_size, 
							class_weight=class_weights, verbose=1, epochs=epoch)

print("going to evaluate model...")
predictions = model.predict(test_x, batch_size=batch_size)
print(classification_report(test_y.argmax(axis=1),
	predictions.argmax(axis=1), target_names=labels))

print('going to save trained model to local directory...')
model.save(args["model"])

epoch_list = np.arange(0, epoch)
plt.style.use("ggplot")
plt.figure()
plt.plot(epoch_list, fitting.history["loss"], label="training loss")
plt.plot(epoch_list, fitting.history["val_loss"], label="validation loss")
plt.plot(epoch_list, fitting.history["acc"], label="training accuracy")
plt.plot(epoch_list, fitting.history["val_acc"], label="validation accuracy")
plt.title("Train loss + Accuracy on GTSRB")
plt.xlabel("Epoch #")
plt.ylabel("Loss/Accuracy")
plt.legend(loc="lower left")
plt.savefig(args["plot"])