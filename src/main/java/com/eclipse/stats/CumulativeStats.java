package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class CumulativeStats {
	public static void main(String[] args) throws Exception {
		BufferedWriter wrtr1 = new BufferedWriter(new OutputStreamWriter(new 
		FileOutputStream("/Users/sahiltyagi/Desktop/cpuData.txt")));
		
		File file = new File("/Users/sahiltyagi/Desktop/cpuNodeData.txt");
		BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String str=null;
		while((str = bfrdr.readLine()) != null) {
			String[] arr = str.split(",");
			double val=0.0;
			for(int i=1; i<arr.length; i++) {
				if(!arr[i].equals("NaN")) {
					val = val + Double.parseDouble(arr[i]);
				}
			}
			
			wrtr1.write(arr[0]+","+val+"\n");
		}
		
		wrtr1.close();
		System.out.println("done...");
	}
}