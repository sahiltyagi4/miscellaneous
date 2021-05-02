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
import uuid

ap = argparse.ArgumentParser()
ap.add_argument("-m", "--model", required=True)
ap.add_argument("-l", "--labels", required=True)
ap.add_argument("-s", "--stream", required=True)
args = vars(ap.parse_args())

print("loading model now...")
model = load_model(args["model"])
webcam_img_directory = args["stream"]

def capture_image():

	camera = cv2.VideoCapture(0)
	cv2.namedWindow("predict_img")
	ret, frame = camera.read()
	img = os.path.join(webcam_img_directory, 'wbcam-'+str(uuid.uuid1())+'.png')
	cv2.imwrite(img, frame)

	camera.release()
	cv2.destroyAllWindows()
	return img


def do_inference_on_streaming(streaming_image, labels):

	image = io.imread(streaming_image)
	image = transform.resize(image, (32, 32))
	image = exposure.equalize_adapthist(image, clip_limit=0.1)

	image = image.astype("float32") / 255.0
	image = np.expand_dims(image, axis=0)

	preds = model.predict(image)
	j = preds.argmax(axis=1)[0]
	label = labels[j]

	image = cv2.imread(streaming_image)
	image = imutils.resize(image, width=128)
	cv2.putText(image, label, (5, 15), cv2.FONT_HERSHEY_SIMPLEX,0.45, (0, 0, 255), 2)

	cv2.imwrite(streaming_image, image)


def main():
	labels = open(args["labels"]).read().strip().split("\n")[1:]
	labels = [l.split(",")[1] for l in labels]

	while True:
		cmd = input('Press y to capture/classify an image....')
		if cmd == 'y':
			streaming_img = capture_image()
			do_inference_on_streaming(streaming_img, labels)

		else:
			print('waiting for another inference request....')

if __name__ == "__main__": 
	main()



