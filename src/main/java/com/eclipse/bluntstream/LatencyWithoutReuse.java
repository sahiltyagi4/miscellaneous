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
import java.util.TreeMap;

import org.apache.storm.shade.org.eclipse.jetty.util.ajax.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.eclipse.stormdataflows.DAGUtils;

public class LatencyWithoutReuse {
	//CODE FOR 21 DF DATASET
	public static void main(String[] args) throws Exception {
		Map<String, String> spoutidmap = new HashMap<String, String>();
		Map<String, String> boltidmap = new HashMap<String, String>();
		Map<Long, Integer> dfTasks = new TreeMap<Long, Integer>();
		
		int globalcounter=0;
		
		//dec 18 addition
		JSONArray jsonAR = new JSONArray();
		
		BufferedWriter writr=null;
		//BufferedWriter startTime = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/home/sahil/startTime.csv")));
		//BufferedWriter startTime = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/Users/sahiltyagi/Documents/IISc/experiments/startTime.csv")));
		//BufferedWriter topidwritr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.topidfile)));
		BufferedWriter graphwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.graphsfile)));
		BufferedWriter jsonwrtr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.jsonwrtr)));
		//BufferedWriter timelog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.timelog)));
		int logicalctr=0, taskPerCore=8;
		List<String> srcdest = new LinkedList<>();
		List<String> spoutlist = new ArrayList<String>();
		BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.opmwfile)));
		//BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.riotfile)));
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
		HashMap<Long, JSONObject> dfmap = new HashMap<Long, JSONObject>();
		while(itr1.hasNext()) {
			JSONObject jsonob = itr1.next();
			Long dfId = (Long)jsonob.get("id");
			dfmap.put(dfId, jsonob);
		}
		
		int size=0;
		bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
		while((str = bfrdr.readLine()) != null) {
			size++;
		}
		bfrdr.close();
		
		bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
		stringBuilder = new StringBuilder();
		int ctr=0;
		while((str = bfrdr.readLine()) != null) {
			stringBuilder = stringBuilder.append(str.split(",")[3] + ",");
			ctr++;
			if(ctr ==(size/2)) {
				break;
			}
		}
		bfrdr.close();
		stringBuilder = new StringBuilder(stringBuilder.toString().substring(0, stringBuilder.toString().length() -1));
		System.out.println("ADD SEQ: " + stringBuilder.toString());
		
		String[] dfseqarr = stringBuilder.toString().split(",");
		System.out.println("df seq size: " + dfseqarr.length);
		for(String dfval : dfseqarr) {
			Set<String> spoutset = new HashSet<String>();
			Set<String> boltset = new HashSet<String>();
			
			Long did = Long.parseLong(dfval);
			JSONObject dfobj = dfmap.get(did);
			logicalctr++;
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
			
			//dec 18 addition
			JSONObject jobj = new JSONObject();
			JSONArray jarr = new JSONArray();
			jobj.put("id", did);
			
			JSONArray edgearr = (JSONArray)dfobj.get("edges");
			itr2 = edgearr.iterator();
			while(itr2.hasNext()) {
				JSONObject edgeobj = itr2.next();
				//System.out.println(edgeobj.get("src").toString() + "_" + edgeobj.get("des").toString());
				srcdest.add(edgeobj.get("src").toString() + "_" + edgeobj.get("des").toString());
				if(!spoutset.contains(edgeobj.get("src").toString())) {
					boltset.add(edgeobj.get("src").toString());
				} 
				if(!spoutset.contains(edgeobj.get("des").toString())) {
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
			//System.out.println("spout for topology-"+did+": " + spoutidbuilder.toString());
			spoutidmap.put("topology-"+did, spoutidbuilder.toString());
			
			iditr = boltset.iterator();
			while(iditr.hasNext()) {
				boltidbuilder.append(iditr.next()+",");
			}
			boltidbuilder = new StringBuilder(boltidbuilder.toString().substring(0, boltidbuilder.toString().length()-1));
			//System.out.println("BOLT for topology-"+did+": " + boltidbuilder.toString());
			boltidmap.put("topology-"+did, boltidbuilder.toString());
			
			//System.out.println("no is: " + did);
			writr= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.yamlfiles + "/storm-" + did + ".yaml")));
			//System.out.println(dfId);
			
			int rs=0, rb=0, taskcounter=0, workers=0;
			JSONArray taskarr = (JSONArray)dfobj.get("tasks");
			itr2 = taskarr.iterator();
			while(itr2.hasNext()) {
				
				//dec 18 addition
				JSONObject jsontasks = new JSONObject();
				
				JSONObject taskObj = (JSONObject)itr2.next();
				Long taskId = (Long)taskObj.get("id");
				String taskType = (String)taskObj.get("type");
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
				
				jsontasks.put("id", taskId);
				jsontasks.put("type", taskType);
				jsontasks.put("config", taskObj.get("config"));
				
				jarr.add(jsontasks);
				
			}
			
			jobj.put("tasks", jarr);
			jsonAR.add(jobj);
			
			taskcounter = rs + rb;
			
			dfTasks.put(did, taskcounter);
			
			workers = (int)Math.ceil((double)taskcounter/taskPerCore);
			System.out.println("TT,"+did+","+taskcounter+","+workers);
			
			globalcounter = globalcounter + workers;
			
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
			
			long t1 = System.currentTimeMillis();
			//Process p = builder.start();
			long t2 = System.currentTimeMillis();
			
			//startTime.write(logicalctr + "," + t1 + "," + t2 + "\n");
			//timelog.write(logicalctr + "," + t2 + "," + taskcounter + "\n");
			
			graphwrtr.write(logicalctr + "," + "abc,xyz," + t2 + "\n");

			//Thread.sleep(60000);
			
		}
		
		System.out.println("TT total cores:" + globalcounter);
			
		bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(DAGUtils.seqOPMWfile)));
		stringBuilder = new StringBuilder();
		ctr=0;
		while((str = bfrdr.readLine()) != null) {
			ctr++;
			if(ctr > (size/2)) {
				stringBuilder = stringBuilder.append(str.split(",")[3] + ",");
			}
		}
		bfrdr.close();
		if(stringBuilder.toString().length() > 1) {
			stringBuilder = new StringBuilder(stringBuilder.toString().substring(0, stringBuilder.toString().length() -1));
			System.out.println("REMOVAL SEQ: " + stringBuilder.toString());
		}
			
		String[] seqarr = stringBuilder.toString().split(",");
		System.out.println("removal seq length: " + seqarr.length);
		for(String top : seqarr) {
			logicalctr++;
			System.out.println("!!!!!!! going to kill topology-" + top);
			
			ProcessBuilder builder = new ProcessBuilder(DAGUtils.storm, "kill", "topology-" + top);
			
			//before killing this, grab all spout and bolt ids for this topology
			String spoutstr = spoutidmap.get("topology-"+top);
			String boltstr = boltidmap.get("topology-"+top);
			if(spoutstr != null) {
//				for(String str1: spoutstr.split(",")) {
//					topidwritr.write("OPMW_SS,"+System.currentTimeMillis()+",RS,spout-"+str1+",true,false\n");
//				}
			}
			
			if(boltstr != null) {
//				for(String str1: boltstr.split(",")) {
//					topidwritr.write("OPMW_SS,"+ System.currentTimeMillis()+",BT,bolt-"+str1+",true,false\n");
//				}
			}
				
			long t3 = System.currentTimeMillis();
			//Process p = builder.start();
			long t4 = System.currentTimeMillis();
			
			//startTime.write(logicalctr + "," + t3 + "," + t4 + "\n");
			//timelog.write(logicalctr + "," + t4 + "," + dfTasks.get(Long.parseLong(top)) + "\n");
			graphwrtr.write(logicalctr + "," + "abc,xyz," + t4 + "\n");
			
			//Thread.sleep(60000);

		}
		
		//dec 18 addition
		
		JSONObject fnlobj = new JSONObject();
		fnlobj.put("dataflows", jsonAR);
		jsonwrtr.write(fnlobj.toJSONString());
		
		//startTime.close();
		//timelog.close();
		//topidwritr.close();
		graphwrtr.close();
		jsonwrtr.close();
		System.out.println("done....");
	}
}