package com.eclipse.OPMW;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.simple.parser.ParseException;

import com.eclipse.stormdataflows.ControlPub;
import com.eclipse.stormdataflows.DAGUtils;

public class OPMWRunner {
	public static int logicalctr = 0;
	private static BufferedWriter graphwritr = null, controldfwrtr=null;
	//private static StringBuilder graphbuildr = new StringBuilder();
	
	public static void main(String[] args) {
		Set<String> allctrlfiles = new HashSet<String>();
		OPMWRunner runner = new OPMWRunner();
		GenerateOPMWYaml opmwyaml = new GenerateOPMWYaml();
		ControlPub ctrlpub = new ControlPub();
		//FileWriter fw = null;
		
		//delete logical count file if it exists here
		
		Map<Long, File> controlmap = new TreeMap<Long, File>();
		List<File> ctrllist1 = new LinkedList<File>();
		List<File> ctrllist2 = new LinkedList<File>();
		
		/*File stormlogdir = new File(DAGUtils.stormlogs);
		if(!stormlogdir.exists() || !stormlogdir.isDirectory()) {
			stormlogdir.mkdir();
		}
		
		File tasklogdir = new File(DAGUtils.tasklogs);
		if(!tasklogdir.exists() || !tasklogdir.isDirectory()) {
			tasklogdir.mkdir();
		}*/
		
		try {
			graphwritr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.logicalfile)));
			controldfwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.controldflogs)));
		} catch(FileNotFoundException filex) {
			filex.printStackTrace();
		}
		
		File directory = new File(DAGUtils.dagdirectory);
		File[] allfiles = directory.listFiles();
		
		//map of timestamp, file object
		for(File file : allfiles) {
			if(file.getName().startsWith("control-")) {
				controlmap.put(new Long(file.getName().substring(file.getName().lastIndexOf("-"), file.getName().lastIndexOf("."))), file);
			}
		}
			
		Set<Long> mapctrlset = controlmap.keySet();
			
		//reverse the order of the set
		Iterator<Long> ctrlitr = mapctrlset.iterator();
		while(ctrlitr.hasNext()) {
			ctrllist1.add(controlmap.get(ctrlitr.next()));
		}
			
		int ctrlsize = ctrllist1.size();
		while(ctrlsize > 0) {
			ctrllist2.add(ctrllist1.get(ctrlsize -1));
			ctrlsize--;
		}
		ctrllist1.clear();
			
		Iterator<File> fileitr = ctrllist2.iterator();
		while(fileitr.hasNext()) {
			File filectrl = fileitr.next();
			Long ts = Long.parseLong(filectrl.getName().substring(filectrl.getName().lastIndexOf("-") +1, filectrl.getName().indexOf(".")));
			//System.out.println("timestamp is: " + ts);
			System.out.println("going to execute the following control file: " + filectrl.getAbsolutePath());
			try {
				controldfwrtr.write(filectrl.getName() + "\n");
			} catch(IOException e) {
				e.printStackTrace();
			}
				
			if(!allctrlfiles.contains(filectrl.getAbsolutePath())) {
				Long dataflowID = Long.parseLong(filectrl.getName().substring(filectrl.getName().indexOf("-") +1, filectrl.getName().lastIndexOf("-")));
				File dffile = new File(filectrl.getParent() + "/dataflow-" + dataflowID + "-" + ts + ".txt");
				System.out.println("dataflow file is: " + dffile.getAbsolutePath());
				if(dffile.exists()) {
					try {
						controldfwrtr.write(dffile.getName() + "\n");
					} catch(IOException e) {
							e.printStackTrace();
					}
					System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$  the dataflow file exists.............");
					//a dataflow file corresponding to this control file ts exists so generate a yaml file and launch storm topology
					//String outputyaml = dffile.getParent() + "/storm-" + dataflowID + ".yaml";
					//System.out.println("this df file exists...");
					String outputyaml = DAGUtils.yamlfiles + "/storm-" + dataflowID + ".yaml";
					List<String> taskIDs = null;
					try {
						taskIDs = opmwyaml.generateFluxfile(dffile.getAbsolutePath(), outputyaml);
						System.out.println("OPMW_SS,"+"A,"+dataflowID+","+System.currentTimeMillis());
						ProcessBuilder builder = new ProcessBuilder(DAGUtils.storm, "jar", DAGUtils.compiledJAR, "org.apache.storm.flux.Flux", DAGUtils.mode, outputyaml);
						builder.redirectOutput(new File(DAGUtils.stormlogs + "logger-" + dataflowID + ".txt"));
						Process p = builder.start();
							
						System.out.println("going to sleep while waiting for topology to get activated..............");
						Thread.sleep(15000);
							
					} catch(IOException e) {
						e.printStackTrace();
					} catch(ParseException p) {
						p.printStackTrace();
					} catch(InterruptedException e) {
							e.printStackTrace();
					}
				}
					
				//now execute the control file here and increment logical timestamp
				try {
					ctrlpub.publishControlMessages(filectrl.getPath(), String.valueOf(dataflowID));
					logicalctr++;
					long timestamp = System.currentTimeMillis();
					runner.processGraphs(logicalctr, timestamp);
					System.out.println("goint to sleep...");
					Thread.sleep(45000);
						
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			allctrlfiles.add(filectrl.getAbsolutePath());
			System.out.println("loop................. " + filectrl.getAbsolutePath());
		}
			
		try {
			graphwritr.close();
			controldfwrtr.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("TTT TOTAL CORE:" + GenerateOPMWYaml.globalcounter);
		System.out.println("+++++++++++++++++++++++++++++++++++++ reached the end of loop ++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("written the logical count file..............................");
		System.out.println("done.");
	}
	
	private void processGraphs(int logicalTimestamp, long actualTimestamp) {
		int active = 0, inactive = 0;
		Set<String> alltaskIDs = ControlPub.pfmap.keySet();
		Iterator<String> itr = alltaskIDs.iterator();
		while(itr.hasNext()) {
			String taskId = itr.next();
			if(ControlPub.pfmap.get(taskId).equals("UNSET_PAUSE") || ControlPub.ffmap.get(taskId).equals("SET_FORWARD")) {
				active++;
			}
				
			if(ControlPub.pfmap.get(taskId).equals("SET_PAUSE") && ControlPub.ffmap.get(taskId).equals("UNSET_FORWARD")) {
				inactive++;
			}
		}
		
		try {
			graphwritr.write("SS," + logicalTimestamp + "," + active + "," + inactive + "," + actualTimestamp + "\n");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("values so far: " + active + "," + inactive + " at logic timestamp: " + logicalTimestamp);
	}

}
