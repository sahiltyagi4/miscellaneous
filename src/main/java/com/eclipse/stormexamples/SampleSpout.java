package com.eclipse.stormexamples;

import java.util.Map;
import java.util.UUID;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

public class SampleSpout extends BaseRichSpout 
{
	private SpoutOutputCollector spoutcollector;
	@Override
	public void nextTuple()
	{
		try 
		{
			Thread.sleep(1000);
			spoutcollector.emit(new Values(5));
			String msgId = UUID.randomUUID().toString();
			long ts = System.currentTimeMillis();
	        System.out.println(ts+","+msgId);
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) 
	{
		this.spoutcollector = arg2;
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) 
	{
		arg0.declare(new Fields("msgid"));
	}
	
}