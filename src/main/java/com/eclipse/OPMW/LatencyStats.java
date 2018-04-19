package com.eclipse.OPMW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LatencyStats {
	//static BufferedWriter bfrwrtr;
	static HashMap<String, Long> globalspout = new HashMap<String, Long>();
	static HashMap<String, List<Long>> globalsink = new HashMap<String, List<Long>>();
	static HashMap<Integer, Integer> finalmap = new HashMap<Integer, Integer>();
	
	private static void processLatency(String fileName, int id, String option) throws Exception {
		
		BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/" + option+ "/latency-"+id+".csv")));
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
					File[] f1 = file.listFiles();
					for(File f2 : f1) {
						if(f2.getName().matches(fileName)) {
							
							BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f2.getAbsolutePath())));
							String str;
							while((str = rdr.readLine()) != null) {
								if(str.contains("###### SPOUT_MSG_ID,") && !str.contains("STDERR") && str.split(",").length ==3) {
									String msgId = str.split(",")[1];
									long ts = Long.parseLong(str.split(",")[2]);
									spout.put(msgId, ts);
									
								} else if(str.contains("###### SINK_MSG_ID,") && !str.contains("STDERR") && str.split(",").length ==3) {
									String msgId = str.split(",")[1];
									long ts = Long.parseLong(str.split(",")[2]);
									
									if(!sink.containsKey(msgId)) {
										List<Long> tslist = new LinkedList<Long>();
										tslist.add(ts);
										sink.put(msgId, tslist);
									} else {
										List<Long> list = sink.get(msgId);
										list.add(ts);
										sink.put(msgId, list);
									}
								
								}
							}
						
							rdr.close();
						}
					}
				}
				
			}
		}
		
		for(Map.Entry entry : globalspout.entrySet()) {
			String key = entry.getKey().toString();
			long value = Long.parseLong(entry.getValue().toString());
			if(sink.containsKey(key)) {
				Iterator<Long> itr = sink.get(key).iterator();
				while(itr.hasNext()) {
					long ts = itr.next();
					//System.out.println("writing something here..................");
					wrtr.write(key+","+(ts - value)+"\n");
				}
			}
			
		}
		
		System.out.println(id + "," + sink.size());
		
		System.out.println(id+"," + spout.size() + "," + sink.size());
		
		spout.clear();
		sink.clear();
		wrtr.close();
	}
	
	public static void loadSpoutSinkMap() throws IOException {
		File dir = new File("/home/sahil/latency-files/");
		//list all orion$id$ directories
		File[] allfiles = dir.listFiles();
		for(File orion : allfiles) {
			if(orion.isDirectory()) {
				//go inside each orion directory and fetch all file names
				File[] latencyfiles = orion.listFiles();
				for(File file : latencyfiles) {
					File[] f1 = file.listFiles();
					for(File f2 : f1) {							
						BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f2.getAbsolutePath())));
						System.out.println("going to read: " + f2.getAbsolutePath());
						String str;
						while((str = rdr.readLine()) != null) {
//							if(str.contains("###### SPOUT_MSG_ID,") && !str.contains("STDERR") && str.split(",").length ==3) {
//								String msgId = str.split(",")[1];
//								long ts = Long.parseLong(str.split(",")[2]);
//								if(!globalspout.containsKey(msgId)) {
//									globalspout.put(msgId, ts);
//								}
//								
//							} else if(str.contains("###### SINK_MSG_ID,") && !str.contains("STDERR") && str.split(",").length ==3) {
//								String msgId = str.split(",")[1];
//								long ts = Long.parseLong(str.split(",")[2]);
//								if(!globalsink.containsKey(msgId)) {
//									List<Long> tslist = new ArrayList<Long>();
//									tslist.add(ts);
//									globalsink.put(msgId, tslist);
//								} else {
//									List<Long> list = globalsink.get(msgId);
//									list.add(ts);
//									globalsink.put(msgId, list);
//								}
//								
//							}
							
							if(str.contains("###### SPOUT_MSG_ID,")) {
								String msgId = str.split(",")[1];
								long ts = Long.parseLong(str.split(",")[2]);
								if(!globalspout.containsKey(msgId)) {
									globalspout.put(msgId, ts);
								}
							}
							
						}
						
						rdr.close();	
					}
				}
			}	
		}
		
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@ SPOUT SIZE: " + globalspout.size());
	}
	
//	public static void writeMsgAndTimestamp(String task, String option) throws IOException {
//		BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/" + task + option + ".txt")));
//		
//		if(task.equals("spout")) {
//			for(Map.Entry<String, Long> entry : globalspout.entrySet()) {
//				wrtr.write(entry.getKey() + "," + entry.getValue() + "\n");
//			}
//			
//		} else  if(task.equals("sink")) {
//			for(Map.Entry<String, List<Long>> entry : globalsink.entrySet()) {
//				String msgid = entry.getKey();
//				Iterator<Long> itr  = entry.getValue().iterator();
//				while(itr.hasNext()) {
//					wrtr.write(msgid + "," + itr.next() + "\n");
//				}
//				
//			}
//			
//			globalsink.clear();
//		}
//		
//		wrtr.close();
//	}
	
	public static void main(String[] args) throws Exception {
		String filename = "latency-top-";
		String exp = args[0];
		loadSpoutSinkMap();
		//writeMsgAndTimestamp("spout", exp);
		//writeMsgAndTimestamp("sink", exp);
		//Integer[] arr = {1,2,3,4,5,6,7,8,9,10,20,30,40,50,60,70,80,90,100,200,300,400,500,600,700,800,900};
		
		for(int i=1; i<74; i++) {
		//for(int i : arr) {
			processLatency(filename+i+".csv", i, exp);
		}
		
		System.out.println("going for no. of messages with a particular count..");
		System.out.println("complete.");
	}
}
