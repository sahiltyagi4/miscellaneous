package com.eclipse.stormdataflows;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class ProxyBolt extends DataFlowBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ProxyBolt(String boltID) {
		super(boltID);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		super.emit(new Values(tuple.getValues()));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		
	}
}
