package com.eclipse.stormexamples;

import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;

public class SampleSink extends BaseRichBolt 
{

	    OutputCollector collector; 

	    @Override
	    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
	        this.collector=outputCollector;
	    }

	    @Override
	    public void execute(Tuple input) {
	        String msgId = input.getStringByField("MSGID");
	        long ts = System.currentTimeMillis();
	        System.out.println(ts+","+msgId);
	    }

	    @Override
	    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

	    }
}