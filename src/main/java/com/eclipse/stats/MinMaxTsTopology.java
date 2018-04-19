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
import java.util.List;
import java.util.Map;

import com.eclipse.stormdataflows.DAGUtils;

public class MinMaxTsTopology {
	static BufferedWriter writer;
	static Map<String, Long> tsmap;
	
	public static void main(String[] args) {
		String filename = "spout-top-";
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/topologyTime.csv")));
			tsmap = new HashMap<String, Long>();
			
			for(int i=1; i<74; i++) {
				getMinMaxTimestamps(filename+i+".csv", i, "reuse");
			}
			
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
			String line;
			while((line=rdr.readLine()) != null) {
				String key = line.split(",")[1]+"_"+line.split(",")[3];
				if(tsmap.containsKey(key)) {
					long ts = tsmap.get(key);
					writer.write(line.split(",")[2] + "," + line.split(",")[3] + "," + ts + "\n");
				}
			}
			
			writer.close();
			rdr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void getMinMaxTimestamps(String fileName, int id, String option) throws IOException {
		File dir = new File("/home/sahil/latency-files/");
		File[] allfiles = dir.listFiles();
		List<Long> tslist = new ArrayList<Long>();
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
								if(str.contains("###### SPOUT_MSG_ID,")) {
									long ts = Long.parseLong(str.split(",")[2]);
									tslist.add(ts);
								}
							}
							rdr.close();
						}
					}
				}
			}
		}
		
		if(tslist.size() > 0) {
			long minTs = Collections.min(tslist);
			tsmap.put("A_"+id, minTs);
			long maxTs = Collections.max(tslist);
			tsmap.put("R_"+id, maxTs);
		}
	}
}