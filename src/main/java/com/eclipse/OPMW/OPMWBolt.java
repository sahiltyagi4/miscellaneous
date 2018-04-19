package com.eclipse.OPMW;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.Random;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.xml.sax.InputSource;

import com.eclipse.stormdataflows.DataFlowBolt;
import com.eclipse.stormdataflows.LatencyDFBolt;

public class OPMWBolt extends LatencyDFBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String boltName, boltId;

	public OPMWBolt(String boltName, String boltId) {
		super(boltId);
		this.boltName = boltName;
		this.boltId = boltId;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void execute(Tuple tuple) {
		// TODO Auto-generated method stub
		String msgId = tuple.getStringByField("msgid");
		super.emit(new Values(msgId));	
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("msgid"));
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
	
//	public static int doXMLparseOp(String input) {
//		
//		StudentRecordHandler recordHandler = new StudentRecordHandler();
//		try {
//			SAXParserFactory factory = SAXParserFactory.newInstance();
//			SAXParser saxParser = factory.newSAXParser();
//			saxParser.parse(new InputSource(new StringReader(input)), recordHandler);
//		} catch (Exception e) {
//			e.printStackTrace();			
//		}
//		
//		return recordHandler.valueLength;
//	}

//	public static double computeAverage() {
//		Random random = new Random();
//		int num = 0;
//		int count =0;
//		for(int i=1; i <101; i++) {
//			int anynum = random.nextInt(100);
//			num = num+anynum;
//			count = i;
//		}
//		
//		return (num/count);
//	}
	
}