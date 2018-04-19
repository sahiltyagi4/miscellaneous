package com.eclipse.stormexamples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

public class Bolt3S extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<String> list;
	private OutputCollector collector;

	@Override
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		String str = arg0.getString(0);
		list.add(str);
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
		arg0.declare(new Fields("data"));
	}
	
	@Override
	public void cleanup() {
		Iterator<String> itr = list.iterator();
		while(itr.hasNext()) {
			System.out.println("************************************************************************************************** value: " + itr.next()+"_Bolt3S");
		}
	}

}
