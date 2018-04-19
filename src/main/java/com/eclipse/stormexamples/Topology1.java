package com.eclipse.stormexamples;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

public class Topology1 {
	
	public static void main(String[] args) throws InterruptedException {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout1", new Spout1());
		builder.setBolt("bolt1", new Bolt1()).shuffleGrouping("spout1");
		
		Config config = new Config();
	    config.setDebug(true);
		
	    LocalCluster cluster = new LocalCluster();
	    cluster.submitTopology("topology1", config, builder.createTopology());
	    
	    Thread.sleep(40000);
	    cluster.shutdown();
	}
}
