package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class BillingJSON {
	
	public static void main(String[] args) throws Exception {
		File dir = new File("/Users/sahiltyagi/Desktop/experiments/riotseqreuse/billing");
		File[] files = dir.listFiles();
		String str;
		Set<String> alltasks = new HashSet<String>();
		
		Map<String, String> machineMapping = new HashMap<String, String>();
		Map<String, String> dfIdRecord = new HashMap<String, String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for(File file : files) {
			
			BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			JSONArray arr2 = new JSONArray();
			//System.out.println("going for file: " + file.getName());
			while((str=bfrdr.readLine()) != null) {
				if(str.contains("2018-01-13") && !str.contains("__acker") && !str.contains("__metricsorg.apache.hadoop.metrics2.sink.storm.StormTimelineMetricsSink") 
						&& !str.contains("__system")) {
					
					String key1 = str.split(":")[0];
					String key = key1.split("/")[5].split("-")[1];
					String val = str.split("\\[INFO]")[1];
					
					dfIdRecord.put(key, key1);
					
					String taskid = val.replaceAll("Prepared bolt bolt-", "").replaceAll("Prepared bolt sink-", "").replaceAll("Opened spout spout-", "")
									.replaceAll("Opened spout proxyspout-", "").split(":")[0].trim();
					
					machineMapping.put(taskid, file.getName().split("-")[1].substring(0, file.getName().split("-")[1].indexOf(".")));
					
					if(map.containsKey(key)) {
						
						List<String> list = map.get(key);
						list.add(val);
						map.put(key, list);
						
					} else {
						
						List<String> list = new ArrayList<String>();
						list.add(val);
						map.put(key, list);
						
					}
					
				}
				
			}
			
		}
		
		//checking m/c mapping
		for(Map.Entry<String, String> sample : machineMapping.entrySet()) {
			System.out.println("machine mapping result: " + sample.getKey() + "," + sample.getValue());
		}
		
		JSONArray jsonarr = new JSONArray();
		
		for(Map.Entry<String, List<String>> set : map.entrySet()) {
			
			JSONArray arr = new JSONArray();
			JSONObject obj1 = new JSONObject();
			
			String k = set.getKey();
			List<String> v = set.getValue();
			
			
			Iterator<String> itr = v.iterator();
			while(itr.hasNext()) {
				
				String s = itr.next();
				
				if(s.contains("Prepared bolt") || s.contains("Opened spout")) {
					
					//String topId = k.split("/")[5].split("-")[1];
					obj1.put("topId", k);
					
					JSONObject obj2 = new JSONObject();
					String toprecord = dfIdRecord.get(k);
					String slot = toprecord.split("/")[6];
					obj2.put("slot", slot);
					
					String taskId = s.replaceAll("Prepared bolt bolt-", "").replaceAll("Prepared bolt sink-", "").replaceAll("Opened spout spout-", "")
									.replaceAll("Opened spout proxyspout-", "").split(":")[0].trim();
					obj2.put("id", taskId);
					
					String mac = machineMapping.get(taskId);
					obj2.put("mac", mac);
					
					if(alltasks.contains(taskId)) {
						System.out.println("task encountered second time: " + taskId);
					}
					
					alltasks.add(taskId);
					
					arr.add(obj2);
					
				}
				
			}
			
			obj1.put("tasks", arr);
			
			jsonarr.add(obj1);
			
		}
		
		JSONObject object = new JSONObject();
		object.put("mappings", jsonarr);
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Desktop/billing.json")));
		writer.write(object.toJSONString());
		writer.close();
		
		System.out.println("size of all tasks: " + alltasks.size());
		
	}
	
}
