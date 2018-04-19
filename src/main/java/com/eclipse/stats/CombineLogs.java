package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;

public class CombineLogs {
	
	public static void main(String[] args) throws Exception {
		
		Map<String, String> m1 = new TreeMap<String, String>();
		Map<String, String> m2 = new TreeMap<String, String>();
		Map<String, String> m3 = new TreeMap<String, String>();
		Map<String, String> m4 = new TreeMap<String, String>();
		
		File dir = new File("/home/dreamlab/Downloads/slots");
		if(dir.isDirectory()) {
			
			File[] files = dir.listFiles();
			for(File file : files) {
				
				if(file.getName().equals("opmw-seq")) {
					
					File[] arr = file.listFiles();
					for(File f : arr) {
						
						BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
						String str;
						while((str=rdr.readLine()) != null) {
							
							if(f.getName().equals("merge.csv")) {
								m1.put(str.split(",")[1], str.split(",")[2]);
							}
							
							if(f.getName().equals("demerge.csv")) {
								m2.put(str.split(",")[1], str.split(",")[2]);
							}
							
							if(f.getName().equals("withoutreuse.csv")) {
								m3.put(str.split(",")[1], str.split(",")[2]);
							}
							
							if(f.getName().equals("deployed.csv")) {
								m4.put(str.split(",")[0], str.split(",")[1]);
							}
							
						}
						
						rdr.close();
						
					}
					
				}
				
			}
			
		}
		
		
		BufferedWriter writr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/dreamlab/Desktop/fused.csv")));
		writr.write("counter,merge,demerge,withoutreuse,deployed\n");
		for(int i=1; i<131; i++) {
			
			String s1 = String.valueOf(i);
			writr.write(i + "," + m1.get(s1) + "," + m2.get(s1) + "," + m3.get(s1) + "," + m4.get(s1) + "\n");
			
		}
		
		writr.close();
		
	}
	
}
