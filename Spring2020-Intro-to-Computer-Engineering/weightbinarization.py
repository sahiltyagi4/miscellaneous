f = open('/Users/sahiltyagi/Desktop/ice_project/weights/dense1.txt', 'r')
out = open('/Users/sahiltyagi/Desktop/ice_project/binary_weights/binary_dense1.txt', 'w')
for line in f:
	print(line.strip())
	weight = float(line.strip())
	if weight >= 0:
		out.write('1' + '\n')
	elif weight <= 0:
		out.write('-1' + '\n')

f.close()
out.close()