package com.eclipse.bluntstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class LatencyProxy {
//	static Map<String, Integer> msgcount = new HashMap<String, Integer>();
	
	private static void processLatency(String fileName, int id, String option) throws Exception {
		
		Set<String> source = new HashSet<String>();
		Set<String> sink = new HashSet<String>();
		
		File dir = new File("/home/sahil/latency-files/");
		//list all orion$id$ directories
		File[] allfiles = dir.listFiles();
		for(File orion : allfiles) {
			if(orion.isDirectory()) {
				File[] latencyfiles = orion.listFiles();
				for(File file : latencyfiles) {
					File[] f1 = file.listFiles();
					for(File f2 : f1) {
						if(f2.getName().matches(fileName)) {
							BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f2.getAbsolutePath())));
							String str;
							while((str = rdr.readLine()) != null) {
								String msgId = str.split(",")[1];
								
								if(str.contains("###### SPOUT_MSG_ID,")) {
									source.add(msgId);
								} else if(str.contains("###### PROXY_MSG_ID,")) {
									source.add(msgId);
								} else if(str.contains("###### SINK_MSG_ID,")) {
									sink.add(msgId);
//									if(!msgcount.containsKey(msgId)) {
//										msgcount.put(msgId, 1);
//									} else {
//										msgcount.put(msgId, msgcount.get(msgId) +1);
//									}
									
								}
								
							}
						}
					}
				}
			}
		}
		
		System.out.println(id+","+source.size()+","+sink.size());
		
	}
	
//	public static void getSelectivity(String option) throws IOException {
//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/ProxySelectivity" + option + ".txt")));
//		Map<Integer, Integer> msgMap = new TreeMap<Integer, Integer>();
//		
//	}
	
	public static void main(String[] args) throws Exception {
		String filename = "latency-top-";
		String exp = args[0];
		for(int i=1; i<74; i++) {
			processLatency(filename+i+".csv", i, exp);
		}
		
	}
}
