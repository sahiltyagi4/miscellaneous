package com.eclipse.stormdataflows;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

public class ProxyTopology {
	public static void main(String[] args) throws InterruptedException {
		TopologyBuilder builder = new TopologyBuilder();
		//builder.setSpout("proxyspout", new ProxySpout("bolt3"));
		builder.setSpout("proxyspout", new ProxySpout("bolt3", "xxx"));
		builder.setBolt("proxybolt", new ProxyBolt("bolt4")).shuffleGrouping("proxyspout");
	
		Config config = new Config();
	    config.setDebug(true);
		
	    LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("proxytopology", config, builder.createTopology());
		
		Thread.sleep(400000);
		cluster.shutdown();
	}
}
