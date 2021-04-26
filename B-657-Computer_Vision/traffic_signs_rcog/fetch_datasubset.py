import os
import sys
import random
import shutil

data_dir = str(sys.argv[1])
new_data_dir = str(sys.argv[2])

training_data = os.path.join(data_dir, 'Train')
all_dirs = os.listdir(training_data)

trimmed_data_dir = os.path.join(new_data_dir, 'Train')

for train_dir in all_dirs:
	train_dir_name = train_dir
	print(train_dir_name)
	train_dir = os.path.join(training_data, train_dir)
	if os.path.isdir(train_dir):
		per_label_images = os.listdir(train_dir)
		random.shuffle(per_label_images)
		images_to_consider = len(per_label_images) // 4

		os.makedirs(os.path.join(trimmed_data_dir, train_dir_name))
		
		for ix in range(images_to_consider):
			image = per_label_images[ix]
			src_label_dir = os.path.join(os.path.join(training_data, train_dir), image)
			dest_label_dir = os.path.join(os.path.join(trimmed_data_dir, train_dir_name), image)
			shutil.copy(src_label_dir, dest_label_dir)

	print('done for directory ' + str(train_dir))