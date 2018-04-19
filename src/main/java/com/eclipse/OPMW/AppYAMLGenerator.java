package com.eclipse.OPMW;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.eclipse.stormdataflows.ControlPub;

public class AppYAMLGenerator {
	public static int globalcounter = 0;
	static int proxyid=5000;
	
	public static void main(String[] args) throws Exception {
	
		GenerateOPMWYaml top = new GenerateOPMWYaml();
		File dir = new File("/home/dreamlab/Documents/csntos-stream-reuse/src/main/resources/sharedfolder");
		File[] files = dir.listFiles();
		for(File file : files) {
			if(file.getName().startsWith("dataflow-")) {
				Long dataflowID = Long.parseLong(file.getName().substring(file.getName().indexOf("-") +1, file.getName().lastIndexOf("-")));
				top.generateFluxfile(file.getAbsolutePath(), "/home/dreamlab/Desktop/withreuse/storm-" + dataflowID +".yaml");
			}
		}
		
		System.out.println("done...");
	}
	
	@SuppressWarnings("unchecked")
	public List<String> generateAppFluxfile(String datafilepath, String outputfile) throws ParseException, IOException {
		int taskPerCore = 8;
		
		//dec 18 addition
		JSONObject jObj = new JSONObject();
		JSONArray jtasks = new JSONArray();
		boolean flag = false;
		
		List<String> taskIDs = new ArrayList<String>();
		List<String> srcdest = new LinkedList<>();
		List<String> spoutlist = new ArrayList<String>();
		Map<String, String> nodemapping = new HashMap<String, String>();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputfile)));
		BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(datafilepath)));
		StringBuilder stringBuilder = new StringBuilder("");
		String str;
		while((str = bfrdr.readLine()) != null) {
			stringBuilder = stringBuilder.append(str);	
		}
		bfrdr.close();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(stringBuilder.toString());
		JSONObject jsonObject = (JSONObject)obj;
		
		JSONArray spoutsarr = (JSONArray) jsonObject.get("inputs");
		Iterator<JSONObject> itr = spoutsarr.iterator();
		while(itr.hasNext()) {
			JSONObject jsonobj = itr.next();
			spoutlist.add(jsonobj.get("tid").toString());
		}
		
		//dec 18 addition
		jObj.put("id", jsonObject.get("did"));
		
		JSONArray tasksarr = (JSONArray)jsonObject.get("tasks");
		//System.out.println("INFO................ number of tasks: " + tasksarr.size() + " for dataflow id: " + jsonObject.get("did").toString());
		//Integer workers = (int)Math.ceil((double)tasksarr.size()/taskPerCore);
		StringBuilder spoutbuilder = new StringBuilder("# spout definition\nspouts:\n");
		StringBuilder boltstrbuilder = new StringBuilder("# bolt definitions\nbolts:\n");
		StringBuilder streambuilder = new StringBuilder("# stream definitions\nstreams:\n");
		JSONArray edgesarr = (JSONArray)jsonObject.get("edges");
		itr = edgesarr.iterator();
		while(itr.hasNext()) {
			JSONObject obj1 = itr.next();
			srcdest.add(obj1.get("src").toString()+"_"+obj1.get("des").toString());
		}
		
		//total task counter, proxy spout, regular bolt, regular spout
		int taskcounter = 0, ps=0,rb=0,rs=0;
		itr = tasksarr.iterator();
		while(itr.hasNext()) {
			
			//dec 18 addition
			JSONObject taskObj = new JSONObject();
			
			JSONObject obj1 = itr.next();
			if(obj1.containsKey("sas") && obj1.containsKey("spout")) {
				String sas = obj1.get("sas").toString();
				String spoutStatus = obj1.get("spout").toString();
				if(!sas.equals("null") && spoutStatus.equals("true")) {
				//	System.out.println("spout builder...");
					spoutbuilder.append("  - id: \"" + "proxyspout-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.stormdataflows.ProxySpout" + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + sas.split(":")[1] + "\"\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "proxyspout-" + obj1.get("tid").toString());
				//	System.out.println("!!!!!!!!!!!!!!!! going to add:"+obj1.get("tid").toString());
					taskIDs.add(obj1.get("tid").toString());
					ps++;
					
					//dec 18 addition
					taskObj.put("id", proxyid);
					taskObj.put("type", "proxy");
					taskObj.put("config", "empty");
					jtasks.add(taskObj);
					proxyid++;
					
				}
				
				if(spoutlist.contains(obj1.get("tid").toString()) && spoutStatus.equals("false") && sas.equals("null")) {
					
					spoutbuilder.append("  - id: \"" + "spout-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.streamapp.SpoutApp" + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + obj1.get("type").toString() + "\"\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "spout-" + obj1.get("tid").toString());
					rs++;
					
					ControlPub.pfmap.put(obj1.get("tid").toString(), "UNSET_PAUSE");
					ControlPub.ffmap.put(obj1.get("tid").toString(), "UNSET_FORWARD");
					
					//dec 18 addition
					taskObj.put("id", obj1.get("tid"));
					taskObj.put("type", obj1.get("type"));
					taskObj.put("config", obj1.get("config"));
					jtasks.add(taskObj);
					flag=true;
					
				}
				
				if(sas.equals("null") && spoutStatus.equals("false") && !spoutlist.contains(obj1.get("tid").toString()) && !obj1.get("config").toString().equals("sink")) {
					boltstrbuilder.append("  - id: \"" + "bolt-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.streamapp.AppBolt" + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + 
							obj1.get("type").toString() + "\"\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "bolt-" + obj1.get("tid").toString());
					
					//to keep state if the dataflow file contains an empty control file
					ControlPub.pfmap.put(obj1.get("tid").toString(), "UNSET_PAUSE");
					ControlPub.ffmap.put(obj1.get("tid").toString(), "UNSET_FORWARD");
					rb++;
					
					//dec 18 addition
					taskObj.put("id", obj1.get("tid"));
					taskObj.put("type", obj1.get("type"));
					taskObj.put("config", obj1.get("config"));
					jtasks.add(taskObj);
					flag=true;
					
				}
				
				if(sas.equals("null") && spoutStatus.equals("false") && !spoutlist.contains(obj1.get("tid").toString()) && obj1.get("config").toString().equals("sink")) {
					boltstrbuilder.append("  - id: \"" + "sink-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.OPMW.SinkBolt" + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + 
							obj1.get("type").toString() + "\"\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "sink-" + obj1.get("tid").toString());
					
					//to keep state if the dataflow file contains an empty control file
					ControlPub.pfmap.put(obj1.get("tid").toString(), "UNSET_PAUSE");
					ControlPub.ffmap.put(obj1.get("tid").toString(), "UNSET_FORWARD");
					rb++;
					
					//dec 18 addition
					taskObj.put("id", obj1.get("tid"));
					taskObj.put("type", obj1.get("type"));
					taskObj.put("config", obj1.get("config"));
					jtasks.add(taskObj);
					flag=true;
					
				}
			}
		}
		
		int workers=0;
//		if(!(ps==1 && rs==0 && rb==0)) {
//			taskcounter = ps + rs + rb;
//			workers = (int)Math.ceil((double)taskcounter/8);
//			System.out.println("TTT,"+jsonObject.get("did").toString()+","+taskcounter+ ","+workers);
//			globalcounter = globalcounter + workers;
//		}
		
		//RIOT use case
		if(!(ps>0 && rb==0)) {
			taskcounter = ps + rs + rb;
			//overallTasks += taskcounter;
			int slot = (int)Math.ceil((double)taskcounter/taskPerCore);
			//overallSlotCount +=slot;
			workers = (int)Math.ceil((double)taskcounter/taskPerCore);
			globalcounter = globalcounter + workers;
		}
		
		//dec 18 addition
		if(flag) {
			jObj.put("tasks", jtasks);
			AppRunner.jarr.add(jObj);
		}
		
		writer.write("name: \"topology-" + jsonObject.get("did").toString() + "\"\nconfig:\n  topology.workers: " + workers + "\n\n");
		System.out.println("for dataflow ID " + jsonObject.get("did").toString() + ", # workers: " + workers);
		
		writer.write(spoutbuilder.toString());
		writer.write(boltstrbuilder.toString());
		Set<String> idset = nodemapping.keySet();
		Iterator<String> itr1 = idset.iterator();
		while(itr1.hasNext()) {
			String srckey = itr1.next();
			Set<String> innerset = nodemapping.keySet();
			Iterator<String> itr2 = innerset.iterator();
			while(itr2.hasNext()) {
				String destkey = itr2.next();
				if(srcdest.contains(srckey+"_"+destkey)) {
					if(!nodemapping.get(srckey).startsWith("proxyspout") && !nodemapping.get(destkey).startsWith("proxyspout")) {
					//	System.out.println("key: "+ srckey+"_"+destkey);
						streambuilder.append("  - name: \"" + nodemapping.get(srckey) + " --> " + nodemapping.get(destkey) +"\"\n    from: \"" 
								+ nodemapping.get(srckey) + "\"\n    to: \"" + nodemapping.get(destkey) + "\"\n    grouping:\n      type: SHUFFLE\n\n");
					}
					
					if(!nodemapping.get(srckey).substring(0, 4).equals(nodemapping.get(destkey).substring(0, 4)) && !nodemapping.get(srckey).startsWith("spout")) {
					//	System.out.println("key1: "+ srckey+"_"+destkey);
						streambuilder.append("  - name: \"" + nodemapping.get(srckey) + " --> " + nodemapping.get(destkey) +"\"\n    from: \"" 
								+ nodemapping.get(srckey) + "\"\n    to: \"" + nodemapping.get(destkey) + "\"\n    grouping:\n      type: SHUFFLE\n\n");
					}
				}
			}
		}
		
		streambuilder = new StringBuilder(streambuilder.toString().substring(0, streambuilder.toString().length() -2));
		writer.write(streambuilder.toString());
		writer.close();
		
		return taskIDs;
	}
}