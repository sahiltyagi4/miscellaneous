package com.eclipse.bluntstream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.eclipse.stormdataflows.DAGUtils;

public class RandomWalkRunner {
	public static void main(String[] args) throws Exception {
		Map<String, String> spoutidmap = new HashMap<String, String>();
		Map<String, String> boltidmap = new HashMap<String, String>();
		Map<Long, Integer> dfcores = new HashMap<Long, Integer>();
		Set<Long> dfIdlist = new HashSet<Long>();
		
		int globalcorectr=0;
		Set<Long> dfidset = new HashSet<Long>();
		
		//dec 18 addition
		JSONArray jsonAR = new JSONArray();
		
		//BufferedReader randomrdr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/dreamlab/Desktop/random-walk-seq.txt")));
		BufferedReader randomrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
		BufferedWriter jsonwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.jsonwrtr)));
		//BufferedWriter timelog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.timelog)));
		String s1=null;
		StringBuilder seqbuilder = new StringBuilder();
		while((s1=randomrdr.readLine()) != null) {
			String[] s1arr = s1.split(",");
			seqbuilder.append(s1arr[1]+":"+s1arr[3]+",");
		}
		randomrdr.close();
		seqbuilder = new StringBuilder(seqbuilder.toString().substring(0, seqbuilder.toString().length() -1));
		String seq = seqbuilder.toString();
		String[] seqarr = seq.split(",");
		BufferedWriter writr=null;
		BufferedWriter graphwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.graphsfile)));
		//BufferedWriter topidwritr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.topidfile)));
		int logicalctr=0, taskPerCore=8;
		List<String> srcdest = new LinkedList<>();
		List<String> spoutlist = new ArrayList<String>();
		HashMap<Long, JSONObject> dfmap = new HashMap<Long, JSONObject>();
		
		BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.opmwfile)));
		StringBuilder stringBuilder = new StringBuilder("");
		String str;
		while((str = bfrdr.readLine()) != null) {
			stringBuilder = stringBuilder.append(str);	
		}
		bfrdr.close();
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(stringBuilder.toString());
		JSONObject jsonObject = (JSONObject)obj;
		JSONArray dfarr = (JSONArray)jsonObject.get("dataflows");
		Iterator<JSONObject> itr1 = dfarr.iterator();
		while(itr1.hasNext()) {
			JSONObject jsonob = itr1.next();
			Long dfId = (Long)jsonob.get("id");
			dfmap.put(dfId, jsonob);
		}
		
		for(String op : seqarr) {
			Set<String> spoutset = new HashSet<String>();
			Set<String> boltset = new HashSet<String>();
			String[] oparr = op.split(":");
			Long did = Long.parseLong(oparr[1]);
			JSONObject dfobj = dfmap.get(did);
			logicalctr++;
			
			if(oparr[0].equals("A")) {
				//regular spout, regular bolt
				int rs=0, rb=0, taskcounter=0,workers=0;
				//submit topology
				StringBuilder spoutbuilder = new StringBuilder("# spout definition\nspouts:\n");
				StringBuilder boltstrbuilder = new StringBuilder("# bolt definitions\nbolts:\n");
				StringBuilder streambuilder = new StringBuilder("# stream definitions\nstreams:\n");
				Map<String, String> nodemapping = new HashMap<String, String>();
				
				JSONArray spoutarr = (JSONArray)dfobj.get("inputs");
				Iterator<JSONObject> itr2 = spoutarr.iterator();
				while(itr2.hasNext()) {
					JSONObject idobj = itr2.next();
					//System.out.println(idobj.get("id").toString());
					spoutlist.add(idobj.get("id").toString());
					spoutset.add(idobj.get("id").toString());
				}
				
				JSONArray edgearr = (JSONArray)dfobj.get("edges");
				itr2 = edgearr.iterator();
				while(itr2.hasNext()) {
					JSONObject edgeobj = itr2.next();
					//System.out.println(edgeobj.get("src").toString() + "_" + edgeobj.get("des").toString());
					srcdest.add(edgeobj.get("src").toString() + "_" + edgeobj.get("des").toString());
					if(!spoutset.contains(edgeobj.get("src").toString())) {
						boltset.add(edgeobj.get("src").toString());
					}
					if(!spoutset.contains(boltset.add(edgeobj.get("des").toString()))) {
						boltset.add(edgeobj.get("des").toString());
					}
				}
				
				//add to respective hash maps to get id dump file
				StringBuilder spoutidbuilder = new StringBuilder();
				StringBuilder boltidbuilder = new StringBuilder();
				Iterator<String> iditr = spoutset.iterator();
				while(iditr.hasNext()) {
					spoutidbuilder.append(iditr.next()+",");
				}
				spoutidbuilder = new StringBuilder(spoutidbuilder.toString().substring(0, spoutidbuilder.toString().length()-1));
				spoutidmap.put("topology-"+did, spoutidbuilder.toString());
				
				iditr = boltset.iterator();
				while(iditr.hasNext()) {
					boltidbuilder.append(iditr.next()+",");
				}
				boltidbuilder = new StringBuilder(boltidbuilder.toString().substring(0, boltidbuilder.toString().length()-1));
				boltidmap.put("topology-"+did, boltidbuilder.toString());
				
				System.out.println("no is: " + did);
				writr= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.yamlfiles + "/storm-" + did + ".yaml")));
				//System.out.println(dfId);
				
				JSONArray taskarr = (JSONArray)dfobj.get("tasks");
				//Integer workers = (int)Math.ceil((double)taskarr.size()/taskPerCore);
				itr2 = taskarr.iterator();
				
				//dec 18 addition
				JSONObject jobj = new JSONObject();
				JSONArray arr1 = new JSONArray();
				
				while(itr2.hasNext()) {
					JSONObject taskObj = (JSONObject)itr2.next();
					Long taskId = (Long)taskObj.get("id");
					String taskType = (String)taskObj.get("type");
//					if(spoutlist.contains(String.valueOf(taskId))) {
//						//spout
//						spoutbuilder.append("  - id: \"" + "spout-" + taskId + "\"\n    className: \"" + "com.eclipse.OPMW.OPMWSpout" + "\"\n    "
//											+ "parallelism: 1\n    constructorArgs:\n      - \"" + taskId + "\"\n\n");
//						nodemapping.put(String.valueOf(taskId), "spout-" + taskId);
//						rs++;
//					} else {
//						//bolt
//						boltstrbuilder.append("  - id: \"" + "bolt-" + taskId + "\"\n    className: \"" + "com.eclipse.OPMW.OPMWBolt" + "\"\n    "
//								+ "parallelism: 1\n    constructorArgs:\n      - \"" + "bolt-" + taskId + "\"\n      - \"" + taskId + "\"\n\n");
//						nodemapping.put(String.valueOf(taskId), "bolt-" + taskId);
//						rb++;
//					}
					
					if(spoutlist.contains(String.valueOf(taskId))) {
						//spout
						spoutbuilder.append("  - id: \"" + "spout-" + taskId + "\"\n    className: \"" + "com.eclipse.OPMW.OPMWSpout" + "\"\n    "
											+ "parallelism: 1\n    constructorArgs:\n      - \"" + taskId + "\"\n\n");
						nodemapping.put(String.valueOf(taskId), "spout-" + taskId);
						rs++;
					} else if(!spoutlist.contains(String.valueOf(taskId)) && !taskObj.get("config").toString().equals("sink")) {
						//bolt
						boltstrbuilder.append("  - id: \"" + "bolt-" + taskId + "\"\n    className: \"" + "com.eclipse.OPMW.OPMWBolt" + "\"\n    "
								+ "parallelism: 1\n    constructorArgs:\n      - \"" + "bolt-" + taskId + "\"\n      - \"" + taskId + "\"\n\n");
						nodemapping.put(String.valueOf(taskId), "bolt-" + taskId);
						rb++;
					} else {
						//sink bolt
						boltstrbuilder.append("  - id: \"" + "sink-" + taskId + "\"\n    className: \"" + "com.eclipse.OPMW.SinkBolt" + "\"\n    "
								+ "parallelism: 1\n    constructorArgs:\n      - \"" + "sink-" + taskId + "\"\n      - \"" + taskId + "\"\n\n");
						nodemapping.put(String.valueOf(taskId), "sink-" + taskId);
						rb++;
					}
					
				
					//dec 18 addition
					if(!dfIdlist.contains(did)) {
						JSONObject ob1 = new JSONObject();
						ob1.put("id", taskId);
						ob1.put("type", taskType);
						ob1.put("config", taskObj.get("config"));
						
						arr1.add(ob1);
					}
					
				}
				
				if(!dfIdlist.contains(did)) {
					jobj.put("tasks", arr1);
					jobj.put("id", did);
					
					jsonAR.add(jobj);	
				}
				
				taskcounter = rs + rb;
				//System.out.println("task counter: "+ taskcounter);
				workers = (int)Math.ceil((double)taskcounter/taskPerCore);
				dfcores.put(did, workers);
				System.out.println("TT,"+did+","+taskcounter+","+workers);
				//globalcounter = globalcounter + workers;
				if(!dfidset.contains(did)) {
					globalcorectr = globalcorectr + workers;
					System.out.println("TTT,"+did+","+taskcounter+","+workers);
					dfidset.add(did);
				}
				writr.write("name: \"topology-" + did + "\"\nconfig:\n  topology.workers: " + workers + "\n\n");
				writr.write(spoutbuilder.toString());
				writr.write(boltstrbuilder.toString());
			
				Set<String> idset = nodemapping.keySet();
				Iterator<String> itr3 = idset.iterator();
				while(itr3.hasNext()) {
					String srckey = itr3.next();
					Set<String> innerset = nodemapping.keySet();
					Iterator<String> itr4 = innerset.iterator();
					while(itr4.hasNext()) {
						String destkey = itr4.next();
						//System.out.println("val in iteration: " + srckey+"_"+destkey);
						if(srcdest.contains(srckey+"_"+destkey)) {
							streambuilder.append("  - name: \"" + nodemapping.get(srckey) + " --> " + nodemapping.get(destkey) +"\"\n    from: \"" 
									+ nodemapping.get(srckey) + "\"\n    to: \"" + nodemapping.get(destkey) + "\"\n    grouping:\n      type: SHUFFLE\n\n");
						}
					}
					
				}
				
				streambuilder = new StringBuilder(streambuilder.toString().substring(0, streambuilder.toString().length() -2));
				writr.write(streambuilder.toString());
				writr.close();
				
				String outputyaml = DAGUtils.yamlfiles + "/storm-" + did + ".yaml";
				ProcessBuilder builder = new ProcessBuilder(DAGUtils.storm, "jar", DAGUtils.compiledJAR, "org.apache.storm.flux.Flux", DAGUtils.mode, outputyaml);
				builder.redirectOutput(new File(DAGUtils.stormlogs + "logger-" + did + ".txt"));
				//Process p = builder.start();
				
				graphwrtr.write(logicalctr + "," + "abc,xyz," + System.currentTimeMillis() + "\n");
				System.out.println("going to sleep for 1 minute to let topology do it's thing...");
				//long t1 = System.currentTimeMillis();
				//Thread.sleep(60000);
				//long t2 = System.currentTimeMillis();
				//timelog.write("A,"+t1+","+t2+"\n");
				
				dfIdlist.add(did);
				
				
			} else if(oparr[0].equals("R")) {
				//kill topology
				
				int corecount = dfcores.get(did);
				globalcorectr = globalcorectr - corecount;
				
				System.out.println("!!! going to kill topology-" + did);
				ProcessBuilder builder = new ProcessBuilder(DAGUtils.storm, "kill", "topology-" + did);
				//before killing this, grab all spout and bolt ids for this topology
				String spoutstr = spoutidmap.get("topology-"+did);
				String boltstr = boltidmap.get("topology-"+did);
//				if(spoutstr != null) {
//					for(String str1: spoutstr.split(",")) {
//						topidwritr.write("OPMW_SS,"+System.currentTimeMillis()+",RS,spout-"+str1+",true,false\n");
//					}
//				}
				
//				if(boltstr != null) {
//					for(String str1: boltstr.split(",")) {
//						topidwritr.write("OPMW_SS,"+ System.currentTimeMillis()+",BT,bolt-"+str1+",true,false\n");
//					}
//				}
				
				//Process p = builder.start();
				graphwrtr.write(logicalctr + "," + "abc,xyz," + System.currentTimeMillis() + "\n");
				//long t1 = System.currentTimeMillis();
				//Thread.sleep(60000);
				//long t2 = System.currentTimeMillis();
				//timelog.write("R,"+t1+","+t2+"\n");
				
			}
			
		}
		
		//dec 18 addition
		JSONObject fnlobj = new JSONObject();
		fnlobj.put("dataflows", jsonAR);
		jsonwrtr.write(fnlobj.toJSONString());
		
		System.out.println("dfid list size:" + dfIdlist.size());
		System.out.println("TTT GLOBAL:"+globalcorectr);
		//topidwritr.close();
		graphwrtr.close();
		jsonwrtr.close();
		//timelog.close();
		System.out.println("done...");
	}
}