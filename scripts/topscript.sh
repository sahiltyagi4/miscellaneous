#!/bin/sh

pid=$1
cpuLogFile=$2
echo $pid
echo $cpuLogfile
top -b -n 1 -d 2 | grep $pid >> $cpuLogFile
