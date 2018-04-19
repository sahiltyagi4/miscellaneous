package com.eclipse.bluntstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MergeFiles {
	
	public static void main(String[] args) {
		String src = args[0];
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/"+src+"-cumulative.txt")));
			for(int i=1; i<8; i++) {
				BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/sahil/"+src+"/"+src+"-top-"+i+".csv")));
				String line;
				while((line = rdr.readLine()) != null) {
					writer.write(line.split(",")[1] +","+ line.split(",")[2]+"\n");
				}
				rdr.close();
			}
			
			writer.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}