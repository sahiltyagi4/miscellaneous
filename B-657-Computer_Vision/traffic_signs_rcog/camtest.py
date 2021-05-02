import cv2
import argparse
import os
import uuid

ap = argparse.ArgumentParser()
ap.add_argument("-w", "--webcam")
args = vars(ap.parse_args())

webcam_img_directory = args["webcam"]

def capture_image():

	camera = cv2.VideoCapture(0)
	cv2.namedWindow("predict_img")
	ret, frame = camera.read()
	img = os.path.join(webcam_img_directory, 'webcam-'+str(uuid.uuid1())+'.png')
	cv2.imwrite(img, frame)

	camera.release()
	cv2.destroyAllWindows()

capture_image()