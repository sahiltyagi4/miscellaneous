#!/bin/sh

pidfile=$1

#'' > $pidfile
pgrep -U sahil -f 'org.apache.storm.flux.Flux' >> $pidfile
pgrep -U sahil -f 'java -cp target/compiled-v1-jar-with-dependencies.jar com.eclipse.OPMW.OPMWRunner' >> $pidfile
pgrep -U sahil -f 'org.apache.storm.daemon.supervisor' >> $pidfile
