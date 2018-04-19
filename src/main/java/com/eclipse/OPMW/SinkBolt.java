package com.eclipse.OPMW;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import com.eclipse.stormdataflows.LatencyDFBolt;

public class SinkBolt extends LatencyDFBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String boltName, boltId;

	public SinkBolt(String boltName, String boltId) {
		super(boltId);
		this.boltName = boltName;
		this.boltId = boltId;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		//System.out.println("######STARWARS7 SINK_MSG_ID,"+tuple.getStringByField("msgid") +","+ System.currentTimeMillis());
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		//s.. declarer.declare(new Fields("value", "msgid"));
		declarer.declare(new Fields("msgid"));
	}
	
}