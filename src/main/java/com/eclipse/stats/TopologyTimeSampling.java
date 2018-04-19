package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TopologyTimeSampling {
	
	public static void main(String[] args) throws Exception {
		
		List<Integer> ctrlist = new ArrayList<Integer>();
		List<String> dummytop = new ArrayList<String>();
		String[] arr = {"19","54","72","16","56","37","50","6"};
		dummytop.addAll(Arrays.asList(arr));
		
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Desktop/seq-riot-entire.txt")));
		String s;
		while((s=rdr.readLine()) !=null) {
			
			if(dummytop.contains(s.split(",")[s.split(",").length -1])) {
				
				ctrlist.add(Integer.parseInt(s.split(",")[s.split(",").length -2]));
				System.out.println("counters are: " + s.split(",")[s.split(",").length -2] + " for topology: " + s.split(",")[s.split(",").length -1]);
				
			}	
		}
		rdr.close();
		
		Map<Double, Integer> strtmap = new HashMap<Double, Integer>();
		List<Double> liststrt = new LinkedList<Double>();
		//Map<Double, Integer> endmap = new HashMap<Double, Integer>();
		//List<Double> listend = new LinkedList<Double>();
		
		rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Downloads/deptime-start-cdf.csv")));
		BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/dreamlab/Desktop/timecost.txt")));
		while((s=rdr.readLine()) != null) {
			
			strtmap.put(Double.parseDouble(s.split(",")[1]), Integer.parseInt(s.split(",")[0]));
			liststrt.add(Double.parseDouble(s.split(",")[1]));
		}
		rdr.close();
		
//		rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Downloads/deptime-end-cdf.csv")));
//		while((s=rdr.readLine()) != null) {
//			endmap.put(Double.parseDouble(s.split(",")[1]), Integer.parseInt(s.split(",")[0]));
//			listend.add(Double.parseDouble(s.split(",")[1]));
//			
//		}
//		rdr.close();
		
		Random rn = new Random(19);
		//for(int i=1; i<=65; i++) {
		for(int i=1; i<=21; i++) {
			double val = rn.nextFloat();
			int index=0;
			
			if(ctrlist.contains(i)) {
				
				wrtr.write(i+",0\n");
			} else {
				
				while(index != (liststrt.size()-1)) {
					
					if(val > liststrt.get(index) && val < liststrt.get(index+1)) {
						wrtr.write(i+","+strtmap.get(liststrt.get(index+1))+"\n");
					}
					
					index++;
				} 
				
				if(index == (liststrt.size()-1) && val > liststrt.get(index)) {
					System.out.println("encounter...................");
					wrtr.write(i+","+strtmap.get(liststrt.get(index)) +"\n");
				}
				
			}
					
			//System.out.println(i+","+val);
		}
		
		//for(int i=66; i<=130; i++) {
		for(int i=22; i<=42; i++) {
			
			wrtr.write(i+",0\n");
			
//			double val = rn.nextFloat();
//			int index=0;
//			while(index != (listend.size()-1)) {
//				
//				if(val > listend.get(index) && val < listend.get(index+1)) {
//					wrtr.write(i+","+endmap.get(listend.get(index+1)) +"\n");
//				}
//				
//				index++;
//			}
//			
//			if(i==91) {
//				System.out.println("@@@@@@@@@ " + i + " , " + index + " , " + liststrt.size() + " , " + val);
//			}
//			
//			if(val < listend.get(0)) {
//				System.out.println("ho gaya################################");
//				wrtr.write(i+","+endmap.get(listend.get(0))+"\n");
//			}
			
		}
	
		wrtr.close();
	}
	
}
