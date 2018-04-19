package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AnalyzeCPUIssue {

	public static void main(String[] args) throws Exception {
		
		Map<String, String> macflags = new HashMap<String, String>();
		Map<String, Long> macTime = new HashMap<String, Long>();
		Map<String, String> taskflags = new HashMap<String, String>();
		Map<String, Long> taskTime = new HashMap<String, Long>();
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Documents/IISc/tasksMac.txt")));
		String str;
		while((str=rdr.readLine()) != null) {
			
			Long time = Long.parseLong(str.split("\\[INFO]")[1].split(",")[0].trim());
			String flag = str.split("\\[INFO]")[1].split(",")[2].trim();
			String taskid = str.split("\\[INFO]")[1].split(",")[3].trim();
			
			macflags.put(taskid, flag);
			macTime.put(taskid, time);
		}
		rdr.close();
		
		rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/Users/sahiltyagi/Documents/IISc/taskStatusFlag.txt")));
		while((str=rdr.readLine()) != null) {
			
			Long time = Long.parseLong(str.split(",")[0].trim());
			String flag = str.split(",")[2].trim();
			String taskid = str.split(",")[3].trim();
			
			taskflags.put(taskid, flag);
			taskTime.put(taskid, time);
		}
		rdr.close();
		
		System.out.println("taskmac size:" + macflags.size());
		System.out.println("taskstatus flag:" + taskflags.size());
		
		for(Map.Entry<String, String> entry: taskflags.entrySet()) {
			
			String taskid = entry.getKey();
			if(!entry.getValue().equals(macflags.get(taskid))) {
				System.out.println("...........no matching in flags for task:" + taskid);
			} else {
				int diff = (int)(taskTime.get(taskid) - macTime.get(taskid));
				System.out.println(taskid + "," + macflags.get(taskid) + "," + taskflags.get(taskid) + "," + diff);
			}
			
		}
		
		System.out.println("execution complete.");
	}
	
}
