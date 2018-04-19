package com.eclipse.stormexamples;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;

import com.eclipse.dagtesting.I1;
import com.eclipse.dagtesting.O1;
import com.eclipse.dagtesting.O1_old;
import com.eclipse.dagtesting.a;
import com.eclipse.dagtesting.b;
import com.eclipse.dagtesting.c;

public class Topology3 {
	public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AlreadyAliveException, AuthorizationException {
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("spout3", new I1());
		builder.setBolt("bolt1", new a("a")).shuffleGrouping("spout3");
		builder.setBolt("bolt2", new b("b")).shuffleGrouping("bolt1");
		builder.setBolt("bolt3", new c("c")).shuffleGrouping("bolt2");
		builder.setBolt("bolt4", new O1("O1")).shuffleGrouping("bolt3");
		//builder.setBolt("bolt5", new O1_old()).shuffleGrouping("bolt4");
		
		Config config = new Config();
	    config.setDebug(true);
//		
//	    LocalCluster cluster = new LocalCluster();
//	    cluster.submitTopology("topology3", config, builder.createTopology());
//	    
//	    Thread.sleep(40000);
//	    cluster.shutdown();
		
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
