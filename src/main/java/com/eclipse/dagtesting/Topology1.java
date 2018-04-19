package com.eclipse.dagtesting;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import com.eclipse.OPMW.OPMWBolt;
import com.eclipse.OPMW.OPMWSpout;
import com.eclipse.OPMW.SinkBolt;

public class Topology1 {
	
	public static void main(String[] args) throws Exception {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("s1", new OPMWSpout("1"));
		builder.setBolt("b1", new OPMWBolt("b1", "2")).shuffleGrouping("s1");
		builder.setBolt("b2", new SinkBolt("b2", "3")).shuffleGrouping("b1");
		
		Config config = new Config();
	    config.setDebug(true);
		
	    LocalCluster cluster = new LocalCluster();
	    cluster.submitTopology("topology_1", config, builder.createTopology());
	    
	    Thread.sleep(500000);
	    cluster.shutdown();
		
	}
}
