package com.eclipse.stormexamples;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

import com.eclipse.streamapp.AppBolt;
import com.eclipse.streamapp.SpoutApp;

public class AppTopology {
	public static void main(String[] args) {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("city", new SpoutApp("spout-city", "c1"));
//		builder.setBolt("senML", new AppBolt("SenMLParse", "777")).shuffleGrouping("city");
//		builder.setBolt("dt", new AppBolt("DecisionTree", "777")).shuffleGrouping("senML");
		
//		builder.setBolt("csv", new AppBolt("CsvToSenML", "777")).shuffleGrouping("dt");
		builder.setBolt("acc", new AppBolt("Accumulator", "777")).shuffleGrouping("city");
		builder.setBolt("plot", new AppBolt("Plot", "777")).shuffleGrouping("acc");
		builder.setBolt("azz", new AppBolt("AzureInsert", "777")).shuffleGrouping("acc");
		builder.setBolt("blob", new AppBolt("BlobUpload", "777")).shuffleGrouping("acc");
//		
//		builder.setBolt("kf", new AppBolt("KalmanFilter", "777")).shuffleGrouping("blob");
//		builder.setBolt("bf", new AppBolt("BloomFilter", "777")).shuffleGrouping("kf");
//		builder.setBolt("rf", new AppBolt("RangeFilter", "777")).shuffleGrouping("bf");
//		builder.setBolt("slr", new AppBolt("SLR", "777")).shuffleGrouping("rf");
//		builder.setBolt("dc", new AppBolt("DistinctCount", "777")).shuffleGrouping("slr");
//		builder.setBolt("interpolation", new AppBolt("Interpolation", "777")).shuffleGrouping("dc");
//		builder.setBolt("average", new AppBolt("Average", "777")).shuffleGrouping("interpolation");
//		
//		builder.setBolt("mqtt", new AppBolt("MqttPub", "777")).shuffleGrouping("average");
//		builder.setBolt("table", new AppBolt("AzureInsert", "777")).shuffleGrouping("mqtt");
		Config config = new Config();
	    config.setDebug(true);
	    config.setNumWorkers(2);
		
	   // LocalCluster cluster = new LocalCluster();
	  //  cluster.submitTopology("topology1", config, builder.createTopology());
	    
	  /*  try {
			Thread.sleep(400000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    cluster.shutdown();*/
	    try {
	    	 
	    	StormTopology top = builder.createTopology();
	    	StormSubmitter.submitTopology("top-APP", config, top);
	 	    
	    } catch(AlreadyAliveException alive) {
	    	alive.printStackTrace();
	    } catch(InvalidTopologyException inv) {
	    	inv.printStackTrace();
	    } catch(AuthorizationException auth) {
	    	auth.printStackTrace();
	    }
	    
	}
}
