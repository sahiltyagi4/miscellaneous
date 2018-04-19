package com.eclipse.dagtesting;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class a1 extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	OutputCollector collector;

	@Override
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		collector.emit(new Values(arg0.getString(0)));
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
		this.collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("letter_a"));
	}

}
