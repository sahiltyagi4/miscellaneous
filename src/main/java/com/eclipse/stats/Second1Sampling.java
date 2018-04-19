package com.eclipse.stats;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Second1Sampling {
	
	public static void main(String[] args) {
		try {
			BufferedReader rdr;
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1])));
			String line;
			long ts1 = 1503926877646L, ts2 = 1503927545363L, ts3=0L;
			//for(int i=1; i< 61; i++) {
			while(ts1 < ts2) {
				rdr = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
				long msgcount=0;
				while((line=rdr.readLine()) != null) {
					ts3=ts1+1000;
					if(ts3 < ts2) {
						long ts = Long.parseLong(line.split(",")[1]);
						if(ts>ts1 && ts<ts3) {
							msgcount++;
						}
					}
				}
				
				ts1=ts3;
				writer.write(msgcount+"\n");
				System.out.println("in loop....");
			}
			
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}