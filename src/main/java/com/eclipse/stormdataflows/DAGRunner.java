package com.eclipse.stormdataflows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DAGRunner {
	
	public static void main(String[] args) {
		
		Set<String> dataflowset = new HashSet<String>();
		Set<String> controlset = new HashSet<String>();
		GenerateTopology gentopology = new GenerateTopology();
		BufferedReader bfrdr;
		ControlPub ctrlpub = new ControlPub();
		
		Map<Long, File> dataflowmap = new TreeMap<Long, File>();
		Map<Long, File> controlmap = new TreeMap<Long, File>();
		
		File stormlogdir = new File(DAGUtils.stormlogs);
		if(!stormlogdir.exists() || !stormlogdir.isDirectory()) {
			stormlogdir.mkdir();
		}
		
		File tasklogdir = new File(DAGUtils.tasklogs);
		if(!tasklogdir.exists() || !tasklogdir.isDirectory()) {
			tasklogdir.mkdir();
		}
		
		while(true) {
			List<File> datafiles1 = new LinkedList<File>();
			List<File> datafiles2 = new LinkedList<File>();
			List<File> controlfiles1 = new LinkedList<File>();
			List<File> controlfiles2 = new LinkedList<File>();
			File directory = new File(DAGUtils.dagdirectory);
			File[] allfiles = directory.listFiles();
		
			for(File file : allfiles) {			
				if(file.getName().startsWith("dataflow-")) {
					dataflowmap.put(new Long(file.getName().substring(file.getName().lastIndexOf("-"), file.getName().lastIndexOf("."))), file);
				}
				
				if(file.getName().startsWith("control-")) {
					controlmap.put(new Long(file.getName().substring(file.getName().lastIndexOf("-"), file.getName().lastIndexOf("."))), file);
				}
			}
			
			Set<Long> dftimestamp = dataflowmap.keySet();
			Iterator<Long> tsitr = dftimestamp.iterator();
			while(tsitr.hasNext()) {
				datafiles2.add(dataflowmap.get(tsitr.next()));
			}
			
			int dfsize = datafiles2.size();
			while(dfsize > 0) {
				datafiles1.add(datafiles2.get(dfsize -1));
				dfsize--;
			}
			datafiles2.clear();
			
			Iterator<File> itr = datafiles1.iterator();
			while(itr.hasNext()) {
				File datafile = itr.next();
				String dataflowID = null;
				try {
					 dataflowID = getDataflowID(datafile);
				} catch(ParseException parse) {
					parse.printStackTrace();
				} catch(IOException io) {
					io.printStackTrace();
				}
				
				if(!dataflowset.contains(datafile.getName())) {
					String outputyaml = datafile.getParent() + "/sample-" + dataflowID + ".yaml";
					System.out.println("read data flow file: " + datafile.getName());
					List<String> taskIDs = null;
					//List<String> boltIDs = null;
					try {
						taskIDs = gentopology.generateFluxfile(datafile.getAbsolutePath(), outputyaml);
						//taskIDs = gentopology.getProxySpoutIds();
						//boltIDs = gentopology.getBoltIds();
						//System.out.println("proxy list size: " + proxyspoutIDs.size());
						//System.out.println("bolt list size: " + boltIDs.size());
						
					} catch(ParseException parse) {
						parse.printStackTrace();
					} catch(IOException io) {
						io.printStackTrace();
					}
					
					ProcessBuilder builder = new ProcessBuilder("storm","jar","/home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar","org.apache.storm.flux.Flux","--local",outputyaml);
					builder.redirectOutput(new File(DAGUtils.stormlogs + "logger-" + dataflowID + ".txt"));
					Process p;
					try {
						p = builder.start();
					} catch(IOException io) {
						io.printStackTrace();
					}
					
					//waiting for the topology to get activated
					Iterator<String> taskitr = taskIDs.iterator();
					while(taskitr.hasNext()) {
						boolean waitflag = true;
						String tid = taskitr.next();
						try {
							while(waitflag) {
								File taskfile = new File(DAGUtils.tasklogs + "tasklog-" + tid + ".txt");
								if(taskfile.exists()) {
									//System.out.println("yes the file exists...............");
									BufferedReader taskfilerdr = new BufferedReader(new InputStreamReader(new FileInputStream(taskfile)));
									String taskline = taskfilerdr.readLine();
									if(taskline != null) {
										//System.out.println("...............................................    line is: " + taskline);
										if(taskline.equalsIgnoreCase("Complete.")) {
											waitflag = false;
										}
										taskfilerdr.close();
									}
									
								}
							}
								
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}
				
				String ctrlstring = "control-" + dataflowID;
				Set<Long> ctrltimestamp = controlmap.keySet();
				tsitr = ctrltimestamp.iterator();
				while(tsitr.hasNext()) {
					controlfiles2.add(controlmap.get(tsitr.next()));
				}
				
				int ctrlsize = controlfiles2.size();
				while(ctrlsize > 0) {
					controlfiles1.add(controlfiles2.get(ctrlsize -1));
					ctrlsize = ctrlsize -1;
				}
				controlfiles2.clear();
			
				Iterator<File> ctritr = controlfiles1.iterator();
				try {
					while(ctritr.hasNext()) {
						File ctrlfile = ctritr.next();
						//System.out.println("control string: " + ctrlstring + " and control file: " + ctrlfile.getName());
						if(ctrlstring.matches(ctrlfile.getName().substring(0, ctrlfile.getName().lastIndexOf("-"))) && !controlset.contains(ctrlfile.getName())) {
							bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(ctrlfile.getPath())));
							StringBuilder controlbuilder = new StringBuilder();
							String str;
							while((str = bfrdr.readLine()) != null) {
								controlbuilder.append(str);
							}
							
							bfrdr.close();
							ctrlpub.publishControlMessages(ctrlfile.getPath(), dataflowID);
							controlset.add(ctrlfile.getName());
							
						}
					}
					
					dataflowset.add(datafile.getName());
					
					//add a part to delete all files in spoutlogs directory
					
				} catch(IOException e) {
					e.printStackTrace();
				} catch(MqttException mqttex) {
					mqttex.printStackTrace();
				} catch(ParseException parse) {
					parse.printStackTrace();
				}
			}
		}	
	}
	
	private static String getDataflowID(File file) throws IOException, ParseException {
		BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		StringBuilder stringBuilder = new StringBuilder("");
		String str;
		while((str = bfrdr.readLine()) != null) {
			stringBuilder = stringBuilder.append(str);	
		}
		bfrdr.close();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(stringBuilder.toString());
		JSONObject jsonObject = (JSONObject)obj;
		
		return jsonObject.get("did").toString();
	}
	
}
