package com.eclipse.streamapp;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import in.dream_lab.bm.stream_iot.tasks.ITask;

public class AppBolt extends DfBolt {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AppBolt(String boltName, String boltID) {
		super(boltName, boltID);
		
	}

	@Override
	public void execute(Tuple input) {
		
		if(!this.pause || this.forward) {
			
			Map<String, String> map = new HashMap<String, String>();
			System.out.println("###map is:" + map + " and " + map.size());
			Float res = this.task.doTask(map);
			Object resMap =  this.task.getLastResult();
			if(resMap == null && res == null )
			{	
				//System.out.println("..emitting null, null .........."+this.boltName);
				super.emit(new Values("Dummy text", "Dummy text")); 
			}
			if(resMap != null && res == null )
			{	
				//System.out.println("..emitting not null, null .........."+this.boltName);
				
				super.emit(new Values(resMap.toString().getBytes(), "Dummy text")); 
			}
			if(resMap == null && res != null )
			{	
				//System.out.println("..emitting null, not null .........."+this.boltName);
				
				super.emit(new Values("null", res.toString().getBytes())); 
			}
			if(resMap != null && res != null )
			{	
				//System.out.println("..emitting not null, not null .........."+this.boltName);
				
				super.emit(new Values(resMap.toString().getBytes(), res.toString().getBytes())); 
			}
		}
		
	}

}
