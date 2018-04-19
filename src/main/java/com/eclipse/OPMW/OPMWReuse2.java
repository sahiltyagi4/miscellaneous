package com.eclipse.OPMW;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.eclipse.stormdataflows.ControlPub;
import com.eclipse.stormdataflows.DAGUtils;

public class OPMWReuse2 {
	public static int logicalctr = 0;
	public static int cumulativeslot=0;
	public static int did=0;
	public static BufferedWriter logicwrtr = null, jsonwrtr = null, dummytopswrtr = null, slotwrtr =null;
	public static JSONObject jobj = new JSONObject();
	public static JSONArray jarr = new JSONArray();
	//controldfwrtr=null, timelog=null, taskstatwrtr=null, dummytopswrtr=null, controlflow=null;

	public static void main(String[] args) {
		//startTime = System.currentTimeMillis();
		Set<String> allctrlfiles = new HashSet<String>();
		OPMWReuse2 runner = new OPMWReuse2();
		GenerateOPMWYaml opmwyaml = new GenerateOPMWYaml();
		ControlPub ctrlpub = new ControlPub();
		
		Map<Long, File> controlmap = new TreeMap<Long, File>();
		List<File> ctrllist1 = new LinkedList<File>();
		List<File> ctrllist2 = new LinkedList<File>();
		
		try {
			
//			controlflow = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.ctrlflow)));
//			taskstatwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.pauseFlag)));
			dummytopswrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.dummytops)));
//			timelog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.timelog)));
			logicwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.logicalfile)));
			jsonwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.jsonwrtr)));
			slotwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.slotfile)));
//			controldfwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.controldflogs)));
		} catch(FileNotFoundException filex) {
			filex.printStackTrace();
		}
		
		File directory = new File(DAGUtils.dagdirectory);
		File[] allfiles = directory.listFiles();
		
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
		int dfid=0;
		Iterator<File> fileitr = ctrllist2.iterator();
		while(fileitr.hasNext()) {
			File filectrl = fileitr.next();
			Long ts = Long.parseLong(filectrl.getName().substring(filectrl.getName().lastIndexOf("-") +1, filectrl.getName().indexOf(".")));
//			try {
//				
//				controldfwrtr.write(filectrl.getName() + "\n");
//				
//			} catch(IOException e) {
//				e.printStackTrace();
//			}
				
			if(!allctrlfiles.contains(filectrl.getAbsolutePath())) {
				Long dataflowID = Long.parseLong(filectrl.getName().substring(filectrl.getName().indexOf("-") +1, filectrl.getName().lastIndexOf("-")));
				dfid = Integer.parseInt(filectrl.getName().substring(filectrl.getName().indexOf("-") +1, filectrl.getName().lastIndexOf("-")));
				File dffile = new File(filectrl.getParent() + "/dataflow-" + dataflowID + "-" + ts + ".txt");
				//System.out.println("dataflow file is: " + dffile.getAbsolutePath());
				
				if(dffile.exists()) {
//					try {
//						controldfwrtr.write(dffile.getName() + "\n");
//					} catch(IOException e) {
//							e.printStackTrace();
//					}
					
					String outputyaml = DAGUtils.yamlfiles + "/storm-" + dataflowID + ".yaml";
					List<String> taskIDs = null;
					try {
						taskIDs = opmwyaml.generateFluxfile(dffile.getAbsolutePath(), outputyaml);
						System.out.println("going to deploy df file:" + dffile.getName());
						ProcessBuilder builder = new ProcessBuilder(DAGUtils.storm, "jar", DAGUtils.compiledJAR, "org.apache.storm.flux.Flux", DAGUtils.mode, outputyaml);
						builder.redirectOutput(new File(DAGUtils.stormlogs + "logger-" + dataflowID + ".txt"));
						System.out.println("going to sleep while waiting for topology to get activated..............");
						//Process p = builder.start();
						
						//Thread.sleep(30000);
							
					} catch(IOException e) {
						e.printStackTrace();
					} catch(ParseException p) {
						p.printStackTrace();
//					} catch(InterruptedException e) {
//						e.printStackTrace();
					}
				}
				
				
				try {
					//ctrlpub.publishControlMessages(filectrl.getPath(), String.valueOf(dataflowID));
					logicalctr++;
					long timestamp = System.currentTimeMillis();
					//runner.processGraphs(logicalctr, timestamp);
					
					System.out.println("goint to sleep following control file execution...");
					//Thread.sleep(30000);
					
				} catch(Exception e) {
					e.printStackTrace();
				}
				
//				try {
//					timelog.write(logicalctr+","+System.currentTimeMillis()+"," + GenerateOPMWYaml.dfcount.get(dfid) + "\n");
//				} catch(IOException io) {
//					io.printStackTrace();
//				}
					
			}
			
			allctrlfiles.add(filectrl.getAbsolutePath());
			try {
				OPMWReuse2.slotwrtr.write(OPMWReuse2.logicalctr + "," + OPMWReuse2.cumulativeslot + "\n");
			} catch(IOException io) {
				io.printStackTrace();
			}
			
		}
			
		try {
			
			//dec 18 addition
			jobj.put("dataflows", jarr);
			jsonwrtr.write(jobj.toJSONString());
			
//			controlflow.close();
//			taskstatwrtr.close();
			dummytopswrtr.close();
//			timelog.close();
			logicwrtr.close();
			jsonwrtr.close();
//			controldfwrtr.close();
			slotwrtr.close();
			
		} catch(IOException e) {
			e.printStackTrace();
		}
	
		System.out.println("TOTAL CORE:" + GenerateOPMWYaml.globalcounter);
		System.out.println("TOTAL REUSE TASK COUNT:" + GenerateOPMWYaml.overallTasks);
		System.out.println("toal logical ctr:" + logicalctr);
		System.out.println("+++++++++++++++++++++++++++++++++++++ reached the end of loop ++++++++++++++++++++++++++++++++++++++++++++++");
		
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
			logicwrtr.write("SS," + logicalTimestamp + "," + active + "," + inactive + "," + actualTimestamp + "\n");
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("values so far: " + active + "," + inactive + " at logic timestamp: " + logicalTimestamp);
	}

}