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

public class MemoryNodewise {
	public static void main(String[] args) throws IOException {
		Map<Integer, String> filemap = new TreeMap<Integer, String>();
		File dir = new File("/Users/sahiltyagi/Documents/IISc/cpufiles/");
		File[] files = dir.listFiles();
		BufferedWriter wrtr1 = new BufferedWriter(new OutputStreamWriter(new 
				FileOutputStream("/Users/sahiltyagi/Desktop/memoryNodeData.txt")));
		for(File file : files) {
			//File file = new File("C:/Users/Sahil Tyagi/Desktop/ad hoc data/cpu/cpuLog2.txt");
			Map<Long, Double> cpumap = new TreeMap<Long, Double>();
			System.out.println("### going with file:" + file.getAbsoluteFile());
			BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while((line = bfrdr.readLine()) != null) {
				if(!line.contains("KiB") && !line.contains("%Cpu") && line.contains("sahil") && !line.contains("Tasks") && !line.contains("top") && 
					!line.contains("%MEM") && line.trim().split("\\s+").length > 10) {
					String[] arr = line.trim().split("\\s+");
					long time = Long.parseLong(arr[0])*1000;
					double mem=0.0;
					//System.out.println(line);
					mem = Double.parseDouble(arr[10]);
					if(cpumap.containsKey(time)) {
						mem = mem + cpumap.get(time);
						cpumap.put(time, mem);
					} else {
						cpumap.put(time, mem);
					}
				}
			}
			bfrdr.close();
			System.out.println("done reading a file ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			
			BufferedReader bfrdr1 = new BufferedReader(new InputStreamReader(new 
			FileInputStream("/Users/sahiltyagi/Documents/IISc/logicalcount.txt")));
			long ts1=0L, ts2=0L;
			List<Long> tslist = new LinkedList<Long>();
			List<Integer> counterlist = new LinkedList<Integer>();
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
				double memsum=0.0;
				ts1 = tslist.get(index);
				//System.out.println("ts1 is: " + ts1);
				if((index +1) < tslist.size()) {
					ts2 = tslist.get(index +1);
					//System.out.println("ts2 is: " + ts2);
				}
				
				//compute values in cpumap based on condition
				Set<Long> mapset = cpumap.keySet();
				Iterator<Long> itr = mapset.iterator();
				while(itr.hasNext()) {
					long ts = itr.next();
					if(ts2 !=0L) {
						if((ts >ts1 || ts ==ts1) && ts <ts2) {
							numrec++;
							memsum = memsum + cpumap.get(ts);
						}
					} else if(ts2 ==0L) {
						if(ts >ts1 || ts ==ts1) {
							numrec++;
							memsum = memsum + cpumap.get(ts);
						}
					}
				}
				
				if(filemap.containsKey(counterlist.get(index))) {
					filemap.put(counterlist.get(index), filemap.get(counterlist.get(index)) + "," + String.valueOf(memsum/numrec));
				} else {
					filemap.put(counterlist.get(index), String.valueOf(memsum/numrec));
				}
				
				index++;
			}	
			
			cpumap.clear();
		}
		
		//read file map and write to file
		for(Map.Entry<Integer, String> set : filemap.entrySet()) {
			wrtr1.write(set.getKey() +","+ set.getValue() + "\n");
		}
		wrtr1.close();
		System.out.println("complete.");
		
		/*writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/dreamlab/Documents/cpuMAP.txt")));
		for(Map.Entry<Long, Double> entry : cpumap.entrySet()) {
			writer.write(entry.getKey() + "," + entry.getValue() + "\n");
		}
		writer.close();*/
		
	}
}