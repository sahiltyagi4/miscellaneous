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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.eclipse.stormdataflows.ControlPub;
import com.eclipse.stormdataflows.DAGUtils;

public class GenerateOPMWYaml {
	public static int globalcounter = 0;
	public static int taskPerCore =8;
	public static int overallTasks =0;
	public static TreeMap<Integer, String> dfcount = new TreeMap<Integer, String>();
	int overallSlotCount=0;
	static int proxyid=5000;
	
//	public static void main(String[] args) throws Exception {
//	
//		GenerateOPMWYaml top = new GenerateOPMWYaml();
//		File dir = new File(DAGUtils.dagdirectory);
//		File[] files = dir.listFiles();
//		for(File file : files) {
//			if(file.getName().startsWith("dataflow-")) {
//				Long dataflowID = Long.parseLong(file.getName().substring(file.getName().indexOf("-") +1, file.getName().lastIndexOf("-")));
//				top.generateFluxfile(file.getAbsolutePath(), "/home/dreamlab/Desktop/withreuse/storm-" + dataflowID +".yaml");
//			}
//		}
//		
//		System.out.println("done...");
//	}
	
	@SuppressWarnings("unchecked")
	public List<String> generateFluxfile(String datafilepath, String outputfile) throws ParseException, IOException {
		//int spoutcount=0;
		int taskcounter=0;
		
		//dec 18 addition
		JSONObject jObj = new JSONObject();
		JSONArray jtasks = new JSONArray();
		boolean flag = false;
		
		List<String> taskIDs = new ArrayList<String>();
		List<String> srcdest = new LinkedList<>();
		List<String> spoutlist = new ArrayList<String>();
		Map<String, String> nodemapping = new HashMap<String, String>();
		Map<String, String> dummyMap = new HashMap<String, String>();
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
		int ps=0,rb=0,rs=0, rsink=0;
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
					dummyMap.put("proxyspout-" + obj1.get("tid").toString(), "proxyspout-" + obj1.get("tid").toString());
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
					
					spoutbuilder.append("  - id: \"" + "spout-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.OPMW.OPMWSpout" + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
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
					boltstrbuilder.append("  - id: \"" + "bolt-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.OPMW.OPMWBolt" + "\"\n    "
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
					
					rsink++;
					
					//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ SINK IS HERE!!!!!!!!!!!!!!!!!!!!!!!!");
					//to keep state if the dataflow file contains an empty control file
					ControlPub.pfmap.put(obj1.get("tid").toString(), "UNSET_PAUSE");
					ControlPub.ffmap.put(obj1.get("tid").toString(), "UNSET_FORWARD");
	
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
		//applicable for sink case opmw
		if(!(ps>0 && rsink==0)) {
			taskcounter = ps + rs + rb + rsink;
			overallTasks += taskcounter;
			int slot = (int)Math.ceil((double)taskcounter/taskPerCore);
			overallSlotCount +=slot;
			OPMWReuse2.cumulativeslot = overallSlotCount;
			workers = (int)Math.ceil((double)taskcounter/taskPerCore);
			globalcounter = globalcounter + workers;
		}
			
			//RIOT use case
//			if(!(ps>0 && rb==0)) {
//				taskcounter = ps + rs + rb + rsink;
//				overallTasks += taskcounter;
//				int slot = (int)Math.ceil((double)taskcounter/taskPerCore);
//				overallSlotCount +=slot;
//				workers = (int)Math.ceil((double)taskcounter/taskPerCore);
//				globalcounter = globalcounter + workers;
//			}
			
			//for running dfs without sink bolt: 35dfs in eScience
//			taskcounter = ps + rs + rb + rsink;
//			overallTasks += taskcounter;
//			int slot = (int)Math.ceil((double)taskcounter/taskPerCore);
//			overallSlotCount +=slot;
//			workers = (int)Math.ceil((double)taskcounter/taskPerCore);
//			globalcounter = globalcounter + workers;
			
			if (ps>0 && rs==0 && rb==0 && rsink==0) {
				
				OPMWReuse2.dummytopswrtr.write(OPMWReuse2.logicalctr + "," + jsonObject.get("did").toString() + "\n");
			
			}
			
//			if (ps>0 && rs==0 && rb==0) {
//				
//				OPMWReuse2.slotwrtr.write(OPMWReuse2.logicalctr + "," + OPMWReuse2.previousSlot + "\n");
//			
//			}
			
			writer.write("name: \"topology-" + jsonObject.get("did").toString() + "\"\nconfig:\n  topology.workers: " + workers + "\n\n");
			//dfcount.put(Integer.parseInt(jsonObject.get("did").toString()), "\"" + String.valueOf(spoutcount)+"|"+String.valueOf(totalcount) + "\"");
			dfcount.put(Integer.parseInt(jsonObject.get("did").toString()), String.valueOf(taskcounter));
			
			Set<String> idset = nodemapping.keySet();
			Iterator<String> itr1 = idset.iterator();
			while(itr1.hasNext()) {
				String srckey = itr1.next();
				Set<String> innerset = nodemapping.keySet();
				Iterator<String> itr2 = innerset.iterator();
				while(itr2.hasNext()) {
					String destkey = itr2.next();
					
					int stream=0;
					if(srcdest.contains(srckey+"_"+destkey)) {
						String source = nodemapping.get(srckey);
						String destination = nodemapping.get(destkey);
						
						if(source.startsWith("spout") || source.startsWith("bolt") || source.startsWith("proxyspout") && !destination.startsWith("proxyspout") && !destination.startsWith("spout")) {
							streambuilder.append("  - name: \"" + source + " --> " + destination +"\"\n    from: \"" 
									+ source + "\"\n    to: \"" + destination + "\"\n    grouping:\n      type: SHUFFLE\n\n");
							
							stream++;
						}
					}
					
				}
		//	}
			
			//streambuilder = new StringBuilder(streambuilder.toString().substring(0, streambuilder.toString().length() -2));
		}
		
			//s..OPMWReuse2.resubmit.write(OPMWReuse2.logicalctr + "\n");	
		
		
		if(flag) {
			jObj.put("tasks", jtasks);
			OPMWReuse2.jarr.add(jObj);
		}
			
		writer.write(spoutbuilder.toString());
		writer.write(boltstrbuilder.toString());
		writer.write(streambuilder.toString());
		writer.close();
		
		return taskIDs;
	}

}