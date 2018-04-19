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
import org.apache.storm.tuple.Values;

public class SampleBolt extends BaseRichBolt 
{
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;

	@Override
	public void execute(Tuple arg0) 
	{
		String msgid = (String) arg0.getValueByField("msgid");
		doPiCompute(1600);
		collector.emit(new Values(msgid));
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
		this.collector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("msgid"));
	}
	
	@Override
	public void cleanup() {
		
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