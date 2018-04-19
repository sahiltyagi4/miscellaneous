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

public class RandomWalkSampling {

	public static void main(String[] args) throws Exception {
		
		List<String> resubmit = new ArrayList<String>();
		List<String> dummytop = new ArrayList<String>();
		//opmw seq
		String[] arr = {"19","54","72","16","56","37","50","6"};
		//opmw random1
		//String[] arr = {"6","20","19","18","56","72","37"};
		//opmw random2
		//String[] arr = {"50","18","37","56","16","54","65","72"};
		//riot random1...no dummy topology
		//String[] arr = {""};
		//riot random2
		//String[] arr = {""};
		dummytop.addAll(Arrays.asList(arr));
		
		Map<Double, Integer> strtmap = new HashMap<Double, Integer>();
		List<Double> liststrt = new LinkedList<Double>();
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Downloads/deptime-start-cdf.csv")));
		String s;
		while((s=rdr.readLine()) != null) {
			
			strtmap.put(Double.parseDouble(s.split(",")[1]), Integer.parseInt(s.split(",")[0]));
			liststrt.add(Double.parseDouble(s.split(",")[1]));
		}
		rdr.close();
		
		rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Desktop/resubmit.txt")));
		while((s=rdr.readLine()) !=null) {
			
			resubmit.add(s.trim());
		}
		rdr.close();
		
		Random rn = new Random(19);
		rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Desktop/seq-opmw-entire.txt")));
		BufferedWriter wrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/dreamlab/Desktop/timecost.txt")));
		while((s=rdr.readLine()) !=null) {
			
			if(s.split(",")[1].equals("R")) {
				
				wrtr.write(s.split(",")[2] + ",0\n");
				
			} else if(s.split(",")[1].equals("A")) {
				
				if(dummytop.contains(s.split(",")[s.split(",").length -1])) {
					
					wrtr.write(s.split(",")[2] + ",0\n");
					
				} else {
					
					if(!resubmit.contains(s.split(",")[2])) {
						
						wrtr.write(s.split(",")[2] + ",0\n");
					} else {
						
						double val = rn.nextFloat();
						int index=0;
						
						while(index != (liststrt.size()-1)) {
							
							if(val > liststrt.get(index) && val < liststrt.get(index+1)) {
								wrtr.write(s.split(",")[2] + "," + strtmap.get(liststrt.get(index+1)) + "\n");
							}
							
							index++;
						} 
						
						if(index == (liststrt.size()-1) && val > liststrt.get(index)) {
							System.out.println("encounter...................");
							wrtr.write(s.split(",")[2] + "," + strtmap.get(liststrt.get(index)) + "\n");
						}
						
					}
					
				}
				
			}
			
		}
		
		rdr.close();
		wrtr.close();
		
	}
}
