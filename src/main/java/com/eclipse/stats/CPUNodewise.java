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

public class CPUNodewise {
	public static void main(String[] args) throws IOException, Exception {
		Map<Integer, String> filemap = new TreeMap<Integer, String>();
		File dir = new File("/Users/sahiltyagi/Desktop/reusemar30/cpufiles");
		File[] files = dir.listFiles();
		BufferedWriter wrtr1 = new BufferedWriter(new OutputStreamWriter(new 
				FileOutputStream("/Users/sahiltyagi/Desktop/cpuNodeData.txt")));
		for(File file : files) {
			//File file = new File("C:/Users/Sahil Tyagi/Desktop/ad hoc data/cpu/cpuLog2.txt");
			Map<Long, Double> cpumap = new TreeMap<Long, Double>();
			System.out.println("### going with file:" + file.getAbsoluteFile());
			BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			String line;
			while((line = bfrdr.readLine()) != null) {
				if(!line.contains("KiB") && !line.contains("%Cpu") && line.contains("sahil") && !line.contains("Tasks") && !line.contains("top") && 
					!line.contains("%MEM") && line.trim().split("\\s+").length > 10) {
				//if(line.contains("sahil") && line.trim().split("\\s+").length > 10) {
					String[] arr = line.trim().split("\\s+");
					long time = Long.parseLong(arr[0])*1000;
					double cpu=0.0;
					//System.out.println(line);
					//System.out.println(arr[9]);
					cpu = Double.parseDouble(arr[9]);
					if(cpumap.containsKey(time)) {
						cpu = cpu + cpumap.get(time);
						cpumap.put(time, cpu);
					} else {
						cpumap.put(time, cpu);
					}
				}
			}
			bfrdr.close();
			//Thread.sleep(60000);
			System.out.println("done reading a file ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			
			BufferedReader bfrdr1 = new BufferedReader(new InputStreamReader(new 
			FileInputStream("/Users/sahiltyagi/Desktop/reusemar30/logicalcount.txt")));
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
				double cpusum=0.0;
				ts1 = tslist.get(index);
				//System.out.println("ts1 is: " + ts1);
				if((index +1) < tslist.size()) {
					System.out.println("####################################$$$$$$$$$$$$$$$$$$$$$$$$");
					ts2 = tslist.get(index +1);
					//System.out.println("ts2 is: " + ts2);
				} else if((index +1) == tslist.size()) {
					ts2 = 0L;
					System.out.println("value of ts2: " + ts2 + " for index: " + (index+1));
				}
				
				System.out.println("ts1: " +ts1 + " and ts2:" + ts2);
				//compute values in cpumap based on condition
				Set<Long> mapset = cpumap.keySet();
				Iterator<Long> itr = mapset.iterator();
				while(itr.hasNext()) {
					long ts = itr.next();
					if(ts2 !=0L) {
						if((ts >ts1 || ts ==ts1) && ts <ts2) {
							numrec++;
							cpusum = cpusum + cpumap.get(ts);
						} 
					} else if(ts2 ==0L && (ts >ts1)) {
						//System.out.println("ts greater than ts1............");
						numrec++;
						cpusum = cpusum + cpumap.get(ts);
					}
					
				}
				
//				if((index+1) ==130) {
//					System.out.println("cpusum is: " + cpusum + " and numrec: " + numrec);
//				}
				
				if(filemap.containsKey(counterlist.get(index))) {
					filemap.put(counterlist.get(index), filemap.get(counterlist.get(index)) + "," + String.valueOf(cpusum/numrec));
				} else {
					filemap.put(counterlist.get(index), String.valueOf(cpusum/numrec));
				}
				
				//System.out.println("index val: " + index + " and cpuval: " + cpusum +" and num of recs: " + numrec);
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