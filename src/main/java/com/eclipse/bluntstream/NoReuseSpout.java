package com.eclipse.bluntstream;

import java.util.Map;
import java.util.UUID;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import com.eclipse.OPMW.OPMWBolt;

public class NoReuseSpout extends BaseRichSpout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector spoutcollector;
	
	public NoReuseSpout(String spoutId) {}

	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		// TODO Auto-generated method stub
		this.spoutcollector = collector;
	}

	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		//NoReuseBolt.doPiCompute(1600);
		
		String msgId=null;
		String anynum = "5";
		msgId = UUID.randomUUID().toString();
		System.out.println("###### SPOUT_MSG_ID,"+msgId+","+System.currentTimeMillis());
		spoutcollector.emit(new Values(anynum, msgId));
		
		//set input rate to 1 msg/sec
		try {
			Thread.sleep(1000);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("value", "msgid"));
	}

}