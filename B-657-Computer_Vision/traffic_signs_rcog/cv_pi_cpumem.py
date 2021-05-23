import os
import matplotlib.pyplot as plt

# file = open(os.path.join('/Users/sahiltyagi/Documents/IUB/miscellaneous/B-657-Computer_Vision/traffic_signs_rcog', 'cpumem100epoch.txt'), 'r')
file = open(os.path.join('/Users/sahiltyagi/Desktop', 'macbook_rsc.txt'), 'r')

# outfile = open(os.path.join('/Users/sahiltyagi/Desktop/', 'pi_cpumem.txt'), 'w')
outfile = open(os.path.join('/Users/sahiltyagi/Desktop/', 'server_cpumem.txt'), 'w')

cpu = 0.0
mem = 0.0
ctr = 0
cpumap = {}
memmap = {}
# for raspberry pi
# for line in file:
# 	if '(B[m[1m 3974' in line and len(line.split()[11].split(':')) == 2:
# 		ctr += 1
# 		cpu = float(line.split()[9])
# 		mem = float(line.split()[10])*2
# 		outfile.write(str(cpu)+','+str(mem)+'\n')
# 		cpumap[ctr] = cpu
# 		if mem < 100:
# 			memmap[ctr] = mem

# for server
for line in file:
	if 'Python' in line and float(line.split()[2]) > 0.0:
		cpu = float(line.split()[2])
		print(line.split()[7])
		mem = float(line.split()[7].replace('M','').replace('+','').replace('-',''))/16000
		ctr += 1
		outfile.write(str(cpu)+','+str(mem)+'\n')
		cpumap[ctr] = cpu
		if mem < 100:
			memmap[ctr] = mem

file.close()
outfile.close()

cpuctr = cpumap.keys()
cpulist = cpumap.values()

memctr = memmap.keys()
memlist = memmap.values()

plt.plot(cpuctr, cpulist, label='CPU')
plt.plot(memctr, memlist, label='Memory')
plt.legend(loc='best')
plt.ylabel('Resource util (%)')
#plt.yticks([100,200,300,400])

# plt.savefig('/Users/sahiltyagi/Desktop/pi_rsc.png')
plt.savefig('/Users/sahiltyagi/Desktop/server_rsc.png')