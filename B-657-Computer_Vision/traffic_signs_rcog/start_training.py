# USAGE
# python train.py --dataset gtsrb-german-traffic-sign --model output/trafficsignnet.model --plot output/plot.png

import matplotlib
matplotlib.use("Agg")
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import BatchNormalization
from tensorflow.keras.layers import Conv2D
from tensorflow.keras.layers import MaxPooling2D
from tensorflow.keras.layers import Activation
from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Dropout
from tensorflow.keras.layers import Dense
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.optimizers import Adam
from tensorflow.keras.utils import to_categorical
from sklearn.metrics import classification_report
from skimage import transform
from skimage import exposure
from skimage import io
import matplotlib.pyplot as plt
import numpy as np
import argparse
import random
import os

#hyperparameters
epoch = 3
lr = 1e-3
batch_size = 32

parser = argparse.ArgumentParser()
parser.add_argument("-d", "--dataset", default="/Users/sahiltyagi/Downloads/archive", required=True)
parser.add_argument("-m", "--model", required=True)
parser.add_argument("-p", "--plot", type=str, default="metrics.png")
args = vars(parser.parse_args())

def model_function(width, height, depth, classes):
	model = Sequential()
	inputShape = (height, width, depth)
	channel = -1

	model.add(Conv2D(8, (5, 5), padding="same",input_shape=inputShape))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=channel))
	model.add(MaxPooling2D(pool_size=(2, 2)))

	model.add(Conv2D(16, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=channel))
	model.add(Conv2D(16, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=channel))
	model.add(MaxPooling2D(pool_size=(2, 2)))

	model.add(Conv2D(32, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=channel))
	model.add(Conv2D(32, (3, 3), padding="same"))
	model.add(Activation("relu"))
	model.add(BatchNormalization(axis=channel))
	model.add(MaxPooling2D(pool_size=(2, 2)))

	model.add(Flatten())
	model.add(Dense(128))
	model.add(Activation("relu"))
	model.add(BatchNormalization())
	model.add(Dropout(0.5))

	model.add(Flatten())
	model.add(Dense(128))
	model.add(Activation("relu"))
	model.add(BatchNormalization())
	model.add(Dropout(0.5))

	model.add(Dense(classes))
	model.add(Activation("softmax"))

	return model


def parse_input(basePath, csvPath):
	data = []
	labels = []
	file = open(csvPath).read().strip().split("\n")[1:]
	random.shuffle(file)

	for (i, line) in enumerate(file):
		if i > 0 and i % 1000 == 0:
			print("[INFO] processed {} total images".format(i))

		(label, imagePath) = line.strip().split(",")[-2:]

		imagePath = os.path.sep.join([basePath, imagePath])
		image = io.imread(imagePath)

		image = transform.resize(image, (32, 32))
		image = exposure.equalize_adapthist(image, clip_limit=0.1)

		data.append(image)
		labels.append(int(label))

	print('successfully read input data for training/testing phases...')

	data = np.array(data)
	labels = np.array(labels)

	return (data, labels)


labels = open("/Users/sahiltyagi/Documents/IUB/miscellaneous/B-657-Computer_Vision/traffic_signs_rcog/labels.txt").read().strip().split("\n")[1:]
labels = [l.split(",")[1] for l in labels]

trainPath = os.path.sep.join([args["dataset"], "Train.csv"])
testPath = os.path.sep.join([args["dataset"], "Test.csv"])

print("going to load training and testing data...")
(trainX, trainY) = parse_input(args["dataset"], trainPath)
(testX, testY) = parse_input(args["dataset"], testPath)

trainX = trainX.astype("float32") / 255.0
testX = testX.astype("float32") / 255.0

numLabels = len(np.unique(trainY))
trainY = to_categorical(trainY, numLabels)
testY = to_categorical(testY, numLabels)

classTotals = trainY.sum(axis=0)
classWeight = classTotals.max() / classTotals

aug = ImageDataGenerator(rotation_range=20, zoom_range=0.2, width_shift_range=0.15, height_shift_range=0.15, shear_range=0.2, 
						horizontal_flip=False, vertical_flip=False, fill_mode="nearest")

opt = Adam(lr=lr, decay=lr / (epoch * 0.5))
model = model_function(width=32, height=32, depth=3, classes=numLabels)
model.compile(loss="categorical_crossentropy", optimizer=opt, metrics=["accuracy"])

print("going to start training...")
fitting = model.fit_generator(aug.flow(trainX, trainY, batch_size=batch_size), validation_data=(testX, testY), steps_per_epoch=trainX.shape[0] // batch_size, 
							epochs=epoch, class_weight=classWeight, verbose=1)

print("going to evaluate model...")
predictions = model.predict(testX, batch_size=batch_size)
print(classification_report(testY.argmax(axis=1),
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