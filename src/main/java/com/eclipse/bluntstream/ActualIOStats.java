package com.eclipse.bluntstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eclipse.stormdataflows.DAGUtils;

public class ActualIOStats {

	public static void main(String[] args) {
		try {
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/sahil/timeduration.csv")));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1])));
			String line;
			Map<Integer, Long> durationmap = new HashMap<Integer, Long>();
			while((line = rdr.readLine()) != null) {
				durationmap.put(Integer.parseInt(line.split(",")[0]), Long.parseLong(line.split(",")[2]));
			}
			rdr.close();
			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
			line = rdr.readLine();
			long ts1 = Long.parseLong(line.split(",")[1]);
			rdr.close();
			long ts2;
			for(int i=1; i< 65; i++) {
				long msgcount=0;
				rdr = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
				ts2 = ts1 + durationmap.get(i);
				while((line = rdr.readLine()) != null) {
					long ts = Long.parseLong(line.split(",")[1]);
					
					if(ts>=ts1 && ts<ts2) {
						msgcount++;
					}
				}
			
				System.out.println("counter: "+i+" timestamp: "+ts1+" and "+ts2);
				System.out.println("bucket size for index " + i + ": " + durationmap.get(i));
				writer.write(i+","+msgcount+"\n");
				ts1 = ts2;
				rdr.close();
			}
		
			writer.close();
		} catch(IOException io) {
			io.printStackTrace();
		}
	}
	
//	try {
//		BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.timelog)));
//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1])));
//		String line;
//		List<String> timelist = new LinkedList<String>();
//		while((line=rdr.readLine()) != null) {
//			timelist.add(line.split(",")[1]+"_"+line.split(",")[2]);
//		}
//		rdr.close();
//		
//		Iterator<String> itr = timelist.iterator();
//		int ctr=0;
//		while(itr.hasNext()) {
//			long msgcount=0;
//			ctr++;
//			String val = itr.next();
//			long ts1 = Long.parseLong(val.split("_")[0]);
//			long ts2 = Long.parseLong(val.split("_")[1]);
//			
//			rdr = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
//			while((line = rdr.readLine()) != null) {
//				long ts = Long.parseLong(line.split(",")[1]);
//				if(ctr == 64) {
//					if(ts >= ts1) {
//						msgcount++;
//					} 
//				} else {
//					if(ts>=ts1 && ts<ts2) {
//						msgcount++;
//					}
//				}
//				
//			}
//			
//			rdr.close();
//			System.out.println("computed for index: " + ctr);
//			writer.write(ctr+","+msgcount+"\n");
//		}
//		
//		writer.close();
//	} catch(IOException e) {
//		e.printStackTrace();
//	}
	
}