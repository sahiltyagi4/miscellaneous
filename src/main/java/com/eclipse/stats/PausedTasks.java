package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PausedTasks {
	
	public static void main(String[] args) throws Exception {
		
		List<String> sahil = new ArrayList<String>();
		
		//BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Desktop/pauseFlag.txt")));
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Downloads/log-paused.csv")));
		String s;
		while((s=rdr.readLine()) != null) {
			
			sahil.add(s.split(",")[2]);
		}
		rdr.close();
		
		//rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Downloads/log-paused.csv")));
		rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Desktop/pauseFlag.txt")));
		while((s=rdr.readLine()) != null) {
			
			String taskId = s.split(",")[3];
			
			if(!sahil.contains(taskId)) {
				System.out.println(taskId);
			}
			
			
		}
		rdr.close();
		
		System.out.println("done.........");
		
	}
	
}
