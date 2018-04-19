package com.eclipse.dagtesting;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

import com.eclipse.stormdataflows.ProxySpout;

public class Topology2 {
	
	public static void main(String[] args) throws Exception {
		
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout1", new ProxySpout(args[0], args[1]));
		builder.setBolt("bolt1", new a("7")).shuffleGrouping("spout1");
		builder.setBolt("bolt2", new b("8")).shuffleGrouping("bolt1");
		builder.setBolt("bolt3", new c("9")).shuffleGrouping("bolt2");
		builder.setBolt("bolt4", new d("10")).shuffleGrouping("bolt3");
		builder.setBolt("bolt5", new e("11")).shuffleGrouping("bolt4");
		builder.setBolt("bolt6", new O1_old()).shuffleGrouping("bolt5");
		
		Config config = new Config();
	    config.setDebug(true);
		
	    LocalCluster cluster = new LocalCluster();
	    cluster.submitTopology("topology_2", config, builder.createTopology());
	    
	    Thread.sleep(400000);
	    cluster.shutdown();
		
	}
}
