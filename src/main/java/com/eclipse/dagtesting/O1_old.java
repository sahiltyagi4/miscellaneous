package com.eclipse.dagtesting;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

public class O1_old extends BaseRichBolt {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	OutputCollector collector;
	List list;
	static BufferedWriter writer;

	@Override
	public void execute(Tuple arg0) {
		// TODO Auto-generated method stub
		//list.add(arg0.getString(0));
		list.add((Values)arg0.getValues());
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ val: " + arg0.getValues().get(0).toString());
		//writeout(arg0);
	}
	
	public static void writeout(Tuple arg0) {
		try {
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% msg: " + arg0);
			writer.write(arg0.getString(0)+ "\n");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
		this.collector = arg2;
		list = new LinkedList();
		//getwriter();
	}
	
	public static void getwriter() {
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/dreamlab/Documents/output1.txt")));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("letter_c"));
	}
	
	@Override
	public void cleanup() {
		Iterator<String> itr = list.iterator();
		while(itr.hasNext()) {
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% output: " + itr.next().toString());
		}
		//closewriter();
		
	}
	
	public static void closewriter() {
		try {
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
}
