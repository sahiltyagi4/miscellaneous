import os
input = open('/Users/sahiltyagi/Desktop/dense1.txt', 'r')
out = open('/Users/sahiltyagi/Desktop/dense1_parsed.txt', 'w')
for line in input:
    line = ' '.join(line.split()).replace('][', ' ').replace('[',' ').replace(']',' ')
    for l in line.split(' '):
        out.write(l + '\n')