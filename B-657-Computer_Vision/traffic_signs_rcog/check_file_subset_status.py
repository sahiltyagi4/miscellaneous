import os
import sys

data_dir = str(sys.argv[1])
new_data_dir = str(sys.argv[2])

all_file_names = []
all_images = []

all_trimmed_img_dir = os.path.join(new_data_dir, 'Train')
all_dirs = os.listdir(all_trimmed_img_dir)
for img_dir in all_dirs:
	#print(os.path.join(all_trimmed_img_dir, img_dir))
	if os.path.isdir(os.path.join(all_trimmed_img_dir, img_dir)):
		all_images = os.listdir(os.path.join(all_trimmed_img_dir, img_dir))
		#print(len(all_images))
		
		for img1 in all_images:
			all_file_names.append(img1)

	#print('copied ' + str(img_dir))

print('length of list ' + str(len(all_file_names)))
#print(all_file_names)
valid_images = []
file = open(os.path.join(data_dir, 'Train.csv'), 'r')

out_file = open(os.path.join(new_data_dir, 'Train_trimmed.csv'), 'w')
out_file.write(file.readline())

for line in file:
	img_name = str(line.split(',')[7].split('/')[2].replace('\n', ''))
	print(img_name)
	if img_name in all_file_names:
		print(line)
		valid_images.append(line)

file.close()


for img in valid_images:
	out_file.write(img)

out_file.close()