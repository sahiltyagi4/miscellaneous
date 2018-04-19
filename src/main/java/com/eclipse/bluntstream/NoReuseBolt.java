package com.eclipse.bluntstream;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class NoReuseBolt extends BaseRichBolt {
	private OutputCollector collector;

	public NoReuseBolt(String boltName, String boltId) {}
	
	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		String anynum = tuple.getStringByField("value");
		String msgId = tuple.getStringByField("msgid");
		
		doPiCompute(1600);
		collector.emit(new Values(anynum, msgId));
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
		this.collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("value", "msgid"));
	}

	public static float doPiCompute(int n) {
		double i, j; // Number of iterations and control variables
		double f; // factor that repeats
		double pi = 1;
		for (i = n; i > 1; i--) {
			f = 2;
			for (j = 1; j < i; j++) {
				f = 2 + Math.sqrt(f);
			}
			f = Math.sqrt(f);
			pi = pi * f / 2;
		}
		pi *= Math.sqrt(2) / 2;
		pi = 2 / pi;
		
		return (float) pi;
	}
	
	
}