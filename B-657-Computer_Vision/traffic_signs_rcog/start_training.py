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

start_time = time.time()
print('start time ' + str(start_time))
#hyperparameters
epoch = 100
lr = 1e-3
batch_size = 64

parser = argparse.ArgumentParser()
parser.add_argument("-d", "--dataset", required=True)
parser.add_argument("-m", "--model", required=True)
parser.add_argument("-p", "--plot", type=str, default="metrics.png")
parser.add_argument("-l", "--labels", type=str)
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


def parse_input(basePath, csvPath):
	data = []
	labels = []
	file = open(csvPath).read().strip().split("\n")[1:]
	random.shuffle(file)

	for (i, line) in enumerate(file):
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

trainPath = os.path.sep.join([args["dataset"], "Train.csv"])
testPath = os.path.sep.join([args["dataset"], "Test.csv"])

print("going to load training and testing data...")
(train_x, train_y) = parse_input(args["dataset"], trainPath)
(test_x, test_y) = parse_input(args["dataset"], testPath)

train_x = train_x.astype("float32") / 255.0
test_x = test_x.astype("float32") / 255.0

numLabels = len(np.unique(train_y))
train_y = to_categorical(train_y, numLabels)
test_y = to_categorical(test_y, numLabels)

classTotals = train_y.sum(axis=0)
classWeight = classTotals.max() / classTotals

augment = ImageDataGenerator(rotation_range=20, zoom_range=0.2, width_shift_range=0.15, height_shift_range=0.15, shear_range=0.2, 
						horizontal_flip=False, vertical_flip=False, fill_mode="nearest")

opt = Adam(lr=lr, decay=lr / (epoch * 0.5))


model = model_function(width=32, height=32, depth=3, classes=numLabels)
model.compile(loss="categorical_crossentropy", optimizer=opt, metrics=["accuracy"])

print("going to start training...")
fitting = model.fit_generator(augment.flow(train_x, train_y, batch_size=batch_size), validation_data=(test_x, test_y), steps_per_epoch=train_x.shape[0] // batch_size, 
							epochs=epoch, class_weight=classWeight, verbose=1)

print("going to evaluate model...")
predictions = model.predict(test_x, batch_size=batch_size)
print(classification_report(test_y.argmax(axis=1), predictions.argmax(axis=1), target_names=labels))

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
plt.legend(loc="best")
plt.savefig(args["plot"])
end_time = time.time()
print('end time is ' + str(end_time))
print('total time taken ' + str(end_time - start_time))