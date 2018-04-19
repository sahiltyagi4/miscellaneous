package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eclipse.stormdataflows.DAGUtils;

public class MinMaxTopReuse {
	static HashMap<String, Long> globalspout = new HashMap<String, Long>();
	static Map<String, Long> tsmap;
	
	public static void loadspoutmap() {
		try {
			File dir = new File("/home/sahil/latency-files/");
			File[] allfiles = dir.listFiles();
			for(File orion : allfiles) {
				if(orion.isDirectory()) {
					File[] latencyfiles = orion.listFiles();
					for(File file : latencyfiles) {
						File[] f1 = file.listFiles();
						for(File f2 : f1) {
							BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f2.getAbsolutePath())));
							String str;
							while((str = rdr.readLine()) != null) {
								if(str.contains("###### SPOUT_MSG_ID,") && !str.contains("STDERR") && str.split(",").length ==3) {
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
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void processMinMaxTimestamps(String fileName, int id, String option) {
		try {
			long sinkmin=0, sinkmax=0, spoutmin=0, spoutmax=0;
			long minTs=0, maxTs=0;
			List<Long> timestamps = new ArrayList<Long>();
			HashMap<String, List<Long>> sink = new HashMap<String, List<Long>>();
			File dir = new File("/home/sahil/latency-files/");
			File[] allfiles = dir.listFiles();
			for(File orion : allfiles) {
				if(orion.isDirectory()) {
					File[] latencyfiles = orion.listFiles();
					for(File file : latencyfiles) {
						File[] f1 = file.listFiles();
						System.out.println(file.getName());
						for(File f2 : f1) {
							if(f2.getName().matches(fileName)) {
								BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f2.getAbsolutePath())));
								String str;
								while((str = rdr.readLine()) != null) {
									if(str.contains("###### SINK_MSG_ID,") && !str.contains("STDERR") && str.split(",").length ==3) {
										System.out.println("line is:" + str);
										String msgId = str.split(",")[1];
										long ts = Long.parseLong(str.split(",")[2]);
										if(!sink.containsKey(msgId)) {
											List<Long> tslist = new LinkedList<Long>();
											tslist.add(ts);
											sink.put(msgId, tslist);
										} else if(sink.containsKey(msgId)) {
											List<Long> list = sink.get(msgId);
											list.add(ts);
											sink.put(msgId, list);
										}
									}
								}
							}
						}
					}
				}
			}
			
			System.out.println("size of sink:"+sink.size());
			for(Map.Entry<String, List<Long>> entry : sink.entrySet()) {
				List<Long> list = entry.getValue();
				Iterator<Long> itr = list.iterator();
				while(itr.hasNext()) {
					timestamps.add(itr.next());
				}
			}
			
			//System.out.println("size of timestamp list: " + timestamps.size());
			if(timestamps.size() >0) {				
				sinkmin = Collections.min(timestamps);
				sinkmax = Collections.max(timestamps);
			}
			
			timestamps.clear();
			String msg;
			List<String> sinkmsgs = new ArrayList<String>();
			
			for(Map.Entry<String, List<Long>> entry : sink.entrySet()) {
				List<Long> list = entry.getValue();
				if(list.contains(sinkmin)) {
					msg = entry.getKey();
					sinkmsgs.add(msg);
				}
				
				if(list.contains(sinkmax)) {
					msg = entry.getKey();
					sinkmsgs.add(msg);
				}
			}
			
			List<Long> spoutTs = new ArrayList<Long>();
			Iterator<String> itr = sinkmsgs.iterator();
			while(itr.hasNext()) {
				spoutTs.add(globalspout.get(itr.next()));
			}
			
			//System.out.println("size of spout timestamps list:"+spoutTs.size());
			if(spoutTs.size()>0) {
				spoutmin = Collections.min(spoutTs);
				tsmap.put("A_"+id, spoutmin);
				spoutmax = Collections.max(spoutTs);
				tsmap.put("R_"+id, spoutmax);
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} 
	}
	
	public static void main(String[] args) {
		String filename = "latency-top-";
		loadspoutmap();
		tsmap = new HashMap<String, Long>();
		for(int i=1; i<74; i++) {
			processMinMaxTimestamps(filename+i+".csv", i, "reuse");
		}
		
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/topologyTime.csv")));
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
			String line;
			while((line=rdr.readLine()) != null) {
				String key = line.split(",")[1]+"_"+line.split(",")[3];
				if(tsmap.containsKey(key)) {
					long ts = tsmap.get(key);
					writer.write(line.split(",")[2] + "," + line.split(",")[3] + "," + ts + "\n");
				}
			}
			
			System.out.println("size of tsmap:"+tsmap.size());
			
//			System.out.println("opening up the ts hashmap...");
//			for(Map.Entry<String, Long> entry : tsmap.entrySet()) {
//				System.out.println(entry.getKey()+","+entry.getValue());
//			}
			
			rdr.close();
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}