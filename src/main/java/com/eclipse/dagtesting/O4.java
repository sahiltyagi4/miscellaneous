package com.eclipse.dagtesting;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import com.eclipse.stormdataflows.DataFlowBolt;

public class O4 extends DataFlowBolt {

	public O4(String boltID) {
		super(boltID);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		super.emit(new Values(arg0.getValues()));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("letter"));
	}

}