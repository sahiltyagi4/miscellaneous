package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CPUProcessing {
	public static void main(String[] args) throws IOException {
		Map<Long, Double> cpumap = new TreeMap<Long, Double>();
		File dir = new File("/Users/sahiltyagi/Desktop/riotreuse/cpufiles");
		///home/dreamlab/Desktop/cpu/cpureusecase
		File[] files = dir.listFiles();
		BufferedWriter wrtr1 = new BufferedWriter(new OutputStreamWriter(new 
				FileOutputStream("/Users/sahiltyagi/Desktop/cpuData.txt")));
		for(File file : files) {
			System.out.println("### going with file:" + file.getAbsoluteFile());
			BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			double cpu=0.0;
			while((line = bfrdr.readLine()) != null) {

//				if(!line.contains("KiB") && !line.contains("%Cpu") && line.contains("storm") && !line.contains("Tasks") && !line.contains("top") && 
//						!line.contains("%MEM") && line.trim().split("\\s+").length > 10) {
				if(line.contains("storm") && line.trim().split("\\s+").length > 10) {
					String[] arr = line.trim().split("\\s+");
					long time = Long.parseLong(arr[0])*1000;
					//System.out.println(line.trim());
					cpu = Double.parseDouble(arr[9]);
					
					if(cpumap.containsKey(time)) {
						//System.out.println("same timestamp!!");
						cpu = cpu + cpumap.get(time);
						cpumap.put(time, cpu);
					} else {
						cpumap.put(time, cpu);
					}
										
				}
			}
			bfrdr.close();
			System.out.println("done reading a file ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		}
		
		BufferedReader bfrdr1 = new BufferedReader(new InputStreamReader(new 
				FileInputStream("/Users/sahiltyagi/Desktop/riotreuse/logicalcount.txt")));
		long ts1=0L, ts2=0L;
		List<Long> tslist = new LinkedList<Long>();
		List<Integer> counterlist = new LinkedList<Integer>();
		String line;
		while((line = bfrdr1.readLine()) != null) {
			counterlist.add(Integer.parseInt(line.split(",")[1]));
			//when doing without reuse
			//counterlist.add(Integer.parseInt(line.split(",")[0]));
			tslist.add(Long.parseLong(line.split(",")[line.split(",").length -1]));
		}
		bfrdr1.close();
		System.out.println("logic file read.......................................................");
		
		int index=0;
		while(index < tslist.size()) {
			int numrec=0;
			double cpusum=0.0;
			ts1 = tslist.get(index);
			if((index +1) < tslist.size()) {
				ts2 = tslist.get(index +1);
				System.out.println("ts1 is: " + ts1 + " and ts2 is: " + ts2);
			} else {
				ts2 = 0L;
			}
			
			//compute values in cpumap based on condition
			Set<Long> mapset = cpumap.keySet();
			Iterator<Long> itr = mapset.iterator();
			while(itr.hasNext()) {
				long ts = itr.next();
				if(ts2 !=0L) {
					if((ts >=ts1) && ts <ts2) {
						numrec++;
						cpusum = cpusum + cpumap.get(ts);
					}
				} else if(ts2 ==0L) {
					if(ts >ts1 || ts ==ts1) {
						numrec++;
						cpusum = cpusum + cpumap.get(ts);
					}
				}
			}
			index++;
			
			wrtr1.write(index + "," + ((cpusum/numrec)*2) + "\n");
		}
		
		System.out.println("size of cpu map:" + cpumap.size());
		wrtr1.close();
		System.out.println("complete.");
		
	}
}