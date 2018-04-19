package com.eclipse.OPMW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NewLatencyStats {
	static BufferedWriter wrtr;
	static Map<String, Integer> msgcount = new HashMap<String, Integer>();
	
	private static void processLatency(String option) throws Exception {
		
		BufferedReader timelogrdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/sahil/timelog" + option + ".txt")));
		List<String> listtime = new LinkedList<String>();
		String str;
		while((str = timelogrdr.readLine()) != null) {
			String[] arr = str.split(",");
			listtime.add(arr[1]+","+arr[2]);
		}
		timelogrdr.close();
		
		
		HashMap<String, Long> spout = new HashMap<String, Long>();
		HashMap<String, List<Long>> sink = new HashMap<String, List<Long>>();
		File dir = new File("/home/sahil/latency-files/");
		//list all orion$id$ directories
		File[] allfiles = dir.listFiles();
		for(File orion : allfiles) {
			if(orion.isDirectory()) {
				
				//go inside each orion directory and fetch all file names
				File[] latencyfiles = orion.listFiles();
				for(File file : latencyfiles) {
					System.out.println(file.getName());
					System.out.println("is dir:" + file.isDirectory());
					
					File[] f1 = file.listFiles();
					for(File f2 : f1) {
						BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f2.getAbsolutePath())));
						String str1;
						while((str1 = rdr.readLine()) != null) {
							if(str1.contains("###### SPOUT_MSG_ID,")) {
								String msgId = str1.split(",")[1];
								long ts = Long.parseLong(str1.split(",")[2]);
								spout.put(msgId, ts);
								
							} else if(str1.contains("###### SINK_MSG_ID,")) {
								String msgId = str1.split(",")[1];
								long ts = Long.parseLong(str1.split(",")[2]);
									
								if(!sink.containsKey(msgId)) {
									List<Long> tslist = new LinkedList<Long>();
									tslist.add(ts);
									sink.put(msgId, tslist);
									msgcount.put(msgId, 1);
								} else {
									List<Long> list = sink.get(msgId);
									list.add(ts);
									sink.put(msgId, list);
									msgcount.put(msgId, msgcount.get(msgId) +1);
								}
								
							}
						}
						
						rdr.close();
					}
				}
				
			}
		}
		
		
		//Iterator<String> timeitr = listtime.iterator();
		int i=0;
		while(i < listtime.size()) {
			String s = listtime.get(i);
			long start = Long.parseLong(s.split(",")[0]);
			long end = Long.parseLong(s.split(",")[1]);
			StringBuilder bldr = new StringBuilder();
			for(Map.Entry entry : spout.entrySet()) {
				String key = entry.getKey().toString();
				long value = Long.parseLong(entry.getValue().toString());
				
				if(sink.containsKey(key)) {
					Iterator<Long> itr = sink.get(key).iterator();
					while(itr.hasNext()) {
						long ts = itr.next();
						if(ts > start && ts < end) {
							bldr.append((ts - value)+",");
						}
						
					}
				}
			}
			
			//bldr = new StringBuilder(bldr.toString().substring(0, bldr.toString().length()-1));
			wrtr.write((i+1) + "," + bldr.toString() + "\n");
			System.out.println("done processing iteration:" + (i+1));
			i++;
			
		}
		
		wrtr.close();
		spout.clear();
		sink.clear();
		
	}
	
	
	public static void getMessageCount(String option) throws IOException {
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/selectivity" + option + ".txt")));
		Map<Integer, Integer> msgMap = new TreeMap<Integer, Integer>();
		
		for(Map.Entry<String, Integer> entry : msgcount.entrySet()) {
			int val = entry.getValue();
			if(!msgMap.containsKey(val)) {
				msgMap.put(val, 1);
			} else {
				msgMap.put(val, msgMap.get(val) + 1);
			}
		}
		
		for(Map.Entry<Integer, Integer> entry : msgMap.entrySet()) {
			writer.write(entry.getKey() + "," + entry.getValue() + "\n");
		}
		
		writer.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		String exp = args[0];
		wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/latencyBucket" + exp + ".csv")));
		processLatency(exp);
		getMessageCount(exp);
		
		System.out.println("complete.");
	}
}
