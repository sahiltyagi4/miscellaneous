package com.eclipse.stormexamples;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.eclipse.stormdataflows.DataFlowBolt;

public class Bolt3 extends DataFlowBolt {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Bolt3(String boltID) {
		// TODO Auto-generated constructor stub
		super(boltID);
	}

	@Override
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		super.emit(new Values(arg0.getValues()));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("no."));
	}

}
