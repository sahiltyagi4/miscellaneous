package com.eclipse.dagtesting;

import java.util.Map;
import java.util.Random;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class I3 extends BaseRichSpout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	SpoutOutputCollector spoutcollector;

	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		String[] arr = {"I31","I32","I33","I34","I35","I36"};
		Random random = new Random();
		spoutcollector.emit(new Values(arr[random.nextInt(arr.length)]));
		
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		// TODO Auto-generated method stub
		this.spoutcollector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("letter_I1"));
	}

}
