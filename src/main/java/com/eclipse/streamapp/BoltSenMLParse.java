package com.eclipse.streamapp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipse.stormdataflows.DAGUtils;
import com.eclipse.stormdataflows.DataFlowBolt;

import in.dream_lab.bm.stream_iot.tasks.parse.SenMLParse;

public class BoltSenMLParse extends DataFlowBolt {
	private static final long serialVersionUID = 1L;
	private static String boltName, boltId;
	private SenMLParse senMl;
	private static Logger l;
	private Properties p;
	
	public static void initLogger(Logger l_) { l = l_; }
	
	public BoltSenMLParse(String boltID, String boltName)
	{
		super(boltID);
		this.boltName = boltName;
		this.boltId = boltId;
		try 
		{
			p = new Properties();
			initLogger(LoggerFactory.getLogger("APP"));
			String taskPropFilename = DAGUtils.propertiesFile;
			InputStream input = new FileInputStream(taskPropFilename);
			p.load(input);
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			l.error("Exception in senMl parse bolt");
		}
	}


	@Override
	public void execute(Tuple input) 
	{
		senMl = new SenMLParse();
		senMl.setup(l,p);
		//remove compute method from DataflowBolt
	   if(!this.pause || this.forward) 
		{
		   Map<String, String> map = new HashMap<String, String>();
		   senMl.doTask(map);
		   Map<String, String>  resMap = senMl.getLastResult();
		   StringBuilder str = new StringBuilder();
		   for(String key : resMap.keySet())
		   {
			   str.append(key);
			   str.append(":");
			   str.append(resMap.get(key) +",");
		   }	 
		   super.emit(new Values(str.toString()));
		   System.out.println("...............................................................................................................................size of result map: " + resMap.size());
		   System.out.println("$$$$$$"+str.toString());
		}
		
	}
	
}