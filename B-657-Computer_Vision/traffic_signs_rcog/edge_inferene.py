# python edge_inference.py --model output/trafficsignnet.model --testdata gtsrb-german-traffic-sign/Test --predictions examples

from tensorflow.keras.models import load_model
from skimage import transform
from skimage import exposure
from skimage import io
from imutils import paths
import numpy as np
import argparse
import imutils
import random
import cv2
import os

ap = argparse.ArgumentParser()
ap.add_argument("-m", "--model", required=True)
ap.add_argument("-t", "--testdata", required=True)
ap.add_argument("-p", "--predictions", required=True)
args = vars(ap.parse_args())

print("loading model now...")
model = load_model(args["model"])

labelNames = open("labels.txt").read().strip().split("\n")[1:]
labelNames = [l.split(",")[1] for l in labelNames]

print("going to shuffle images....")
imagePaths = list(paths.list_images(args["testdata"]))
print('total size of all images to test ' + str(len(imagePaths)))
random.shuffle(imagePaths)
imagePaths = imagePaths[:25]

for (i, imagePath) in enumerate(imagePaths):
	#resize image to 32x32 and apply contrast limited adaptive histogram equalization (CLAHE)
	image = io.imread(imagePath)
	image = transform.resize(image, (32, 32))
	image = exposure.equalize_adapthist(image, clip_limit=0.1)

	image = image.astype("float32") / 255.0
	image = np.expand_dims(image, axis=0)

	preds = model.predict(image)
	print('predicted image number ' + str(i))
	j = preds.argmax(axis=1)[0]
	label = labelNames[j]

	image = cv2.imread(imagePath)
	image = imutils.resize(image, width=128)
	cv2.putText(image, label, (5, 15), cv2.FONT_HERSHEY_SIMPLEX,
		0.45, (0, 0, 255), 2)

	p = os.path.sep.join([args["predictions"], "{}.png".format(i)])
	cv2.imwrite(p, image)