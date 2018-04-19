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

public class Costfunction {
	static Map<Integer, Long> ctrTime = new HashMap<Integer, Long>();
	static Map<Integer, Integer> ctrTasks = new HashMap<Integer, Integer>();
	static List<Long> sinklist = new ArrayList<Long>();
	
	private static long msgtimestamps(String topologyId, String operation) throws IOException {
		sinklist.clear();
		File dir = new File("/home/sahil/latency-files/");
		File[] allfiles = dir.listFiles();
		for(File orion : allfiles) {
			//System.out.println("orion: " + orion.getAbsolutePath());
			if(orion.isDirectory()) {
				File[] latencyfiles = orion.listFiles();
				for(File file : latencyfiles) {
						//System.out.println("file f2 is: " + file.getName());
						if(file.getName().matches("noreuse-"+topologyId+".csv")) {
							
							BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(file.getAbsolutePath())));
							String str;
							while((str=rdr.readLine()) != null) {
								
								if(str.contains("######ASDR2 SINK_MSG_ID,")) {
									
									//added time of every msgid in each topology
									sinklist.add(Long.parseLong(str.split(",")[2]));
									
								}
								
							}
							
						}
					//}
				}
			}
		}
		
		System.out.println("size of sink list for topology-" + topologyId + ": " + sinklist.size());
		if(operation.equals("min")) {
			
			if(sinklist.size() > 0) {
				return Collections.min(sinklist);
			} else
				return 0;
			
		}
		else
			if(sinklist.size() > 0)
				return Collections.max(sinklist);
			else
				return 0;
	}
	
	public static void main(String[] args) {
		try {
			
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/topologyTime9.csv")));
			
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.timelog)));
			String str;
			while((str=rdr.readLine()) != null) {
				
				ctrTime.put(Integer.parseInt(str.split(",")[0]), Long.parseLong(str.split(",")[1]));
				
				if(!str.split(",")[2].equals("null"))
					ctrTasks.put(Integer.parseInt(str.split(",")[0]), Integer.parseInt(str.split(",")[2]));
				
			}
			
			rdr.close();
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
			while((str=rdr.readLine()) != null) {
				
				//logic counter
				int ctr = Integer.parseInt(str.split(",")[2]);
				//dag ID
				String dfID = str.split(",")[3];
				long deployTime = 0L;
				//ctr 1 to 65 is submission seq in sequential run
				if(ctr <= 65) {
					
					System.out.println("on counter " + ctr + " in submission phase");
					deployTime = msgtimestamps(dfID, "min");
					
					//add on for no reuse
					writer.write(ctr + "," + (deployTime - ctrTime.get(ctr)) + "," + ctrTasks.get(ctr) + "\n");
					
				} else {
					
					System.out.println("on counter " + ctr + " in removal phase");
					deployTime = msgtimestamps(dfID, "max");
					
					//no reuse
					if(!ctrTasks.get(ctr).equals(null)) {
						
						writer.write(ctr + "," + (deployTime - ctrTime.get(ctr)) + "," + ctrTasks.get(ctr) + "\n");
					
					} else {
						
						writer.write(ctr + "," + (deployTime - ctrTime.get(ctr)) + ",-1" + "\n");
					}
						
					
				}
				
				System.out.println("counter value for write op: " + ctr);
				System.out.println("ctr time: " + ctrTime.get(ctr));
				System.out.println("value of deploy time: " + deployTime);
				
				System.out.println("ctr val is: " + ctr);
//				if(ctrTasks.get(ctr).equals(null))
//					writer.write(ctr + "," + (deployTime - ctrTime.get(ctr)) + ",-1" + "\n");
//				else
//					writer.write(ctr + "," + (deployTime - ctrTime.get(ctr)) + "," + ctrTasks.get(ctr) + "\n");
				
				
			}
			
			rdr.close();
			writer.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}