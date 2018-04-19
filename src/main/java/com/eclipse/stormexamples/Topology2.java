package com.eclipse.stormexamples;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

import com.eclipse.streamapp.AppBolt;
import com.eclipse.streamapp.SpoutApp;

public class Topology2 {
	
	public static void main(String[] args) throws InterruptedException {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout2", new SpoutApp("s1", "123"));
		builder.setBolt("bolt2", new AppBolt("BlobUpload", "456")).shuffleGrouping("spout2");
		
		Config config = new Config();
	    config.setDebug(true);
		
//	    LocalCluster cluster = new LocalCluster();
//	    cluster.submitTopology("topology2", config, builder.createTopology());
//	    
//	    Thread.sleep(40000);
//	    cluster.shutdown();
	    
	    StormTopology stormTopology = builder.createTopology();
	    
	    try {
			StormSubmitter.submitTopology("top2", config, stormTopology);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception "+e.getMessage());
		}
	    
	}
}
