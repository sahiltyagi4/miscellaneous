package com.eclipse.stormexamples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

public class Bolt1 extends BaseRichBolt {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;
	private List<String> list;

	@Override
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		String alphabet = arg0.getString(0);
		list.add(alphabet);
		//System.out.println("in execute method the alphabet is: " + alphabet);
		//collector.ack(arg0);
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
		this.collector = arg2;
		list = new ArrayList<String>();
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("character"));
	}
	
	@Override
	public void cleanup() {
		Iterator<String> itr = list.iterator();
		while(itr.hasNext()) {
			System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ :" + itr.next()+"_Bolt1");
		}
	}

}
