package com.eclipse.stormexamples;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

public class BenchmarkTopo {
	
	public static void main(String[] args)  {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout", new SampleSpout());
		builder.setBolt("bolt", new SampleBolt()).shuffleGrouping("spout");
		builder.setBolt("sink", new SampleSink()).shuffleGrouping("bolt");
		Config config = new Config();
	    config.setDebug(false);
		
	    StormTopology stormTopology = builder.createTopology();
	    
	    try {
			StormSubmitter.submitTopology("bm-topo", config, stormTopology);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Exception "+e.getMessage());
		}
	    
	}
}