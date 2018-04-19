package com.eclipse.stormdataflows;

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

public class GenerateTopology {
	
	//List<String> boltIDs = new ArrayList<String>();
	
	/*public static void main(String[] args) throws Exception {
	
		GenerateTopology top = new GenerateTopology();
		top.generateFluxfile("/home/dreamlab/Documents/1/data-1.txt", "/home/dreamlab/Documents/qwerty.yaml");
		
		ProcessBuilder builder = new ProcessBuilder("storm","jar","/home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar","org.apache.storm.flux.Flux","--local","/home/dreamlab/Documents/qwerty.yaml");
		builder.redirectOutput(new File("/home/dreamlab/Documents/redirectSTORM.txt"));
		builder.redirectErrorStream(true);
		Process p = builder.start();
		System.out.println("going to sleep...");
		Thread.sleep(15000);
		
	}*/
	
	@SuppressWarnings("unchecked")
	public List<String> generateFluxfile(String datafilepath, String outputfile) throws ParseException, IOException {
		List<String> taskIDs = new ArrayList<String>();
		//List<String> proxyspouts = new ArrayList<String>();
		//List<String> boltlist = new ArrayList<String>();
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
		
		writer.write("name: \"topology-" + jsonObject.get("did").toString() + "\"\nconfig:\n  topology.workers: 1\n\n");
		StringBuilder spoutbuilder = new StringBuilder("# spout definition\nspouts:\n");
		StringBuilder boltstrbuilder = new StringBuilder("# bolt definitions\nbolts:\n");
		StringBuilder streambuilder = new StringBuilder("# stream definitions\nstreams:\n");
		JSONArray edgesarr = (JSONArray)jsonObject.get("edges");
		itr = edgesarr.iterator();
		while(itr.hasNext()) {
			JSONObject obj1 = itr.next();
			srcdest.add(obj1.get("src").toString()+"_"+obj1.get("des").toString());
		}
		
		JSONArray tasksarr = (JSONArray)jsonObject.get("tasks");
		itr = tasksarr.iterator();
		while(itr.hasNext()) {
			JSONObject obj1 = itr.next();
			if(obj1.containsKey("sas") && obj1.containsKey("spout")) {
				String sas = obj1.get("sas").toString();
				String spoutStatus = obj1.get("spout").toString();
				if(!sas.equals("null") && spoutStatus.equals("true")) {
					spoutbuilder.append("  - id: \"" + "proxyspout-" + obj1.get("tid").toString() + "\"\n    className: \"" + "com.eclipse.stormdataflows.ProxySpout" + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + sas.split(":")[1] + "\"\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "proxyspout-" + obj1.get("tid").toString());
					//proxyspouts.add(obj1.get("tid").toString());
					System.out.println("!!!!!!!!!!!!!!!! going to add:"+obj1.get("tid").toString());
					taskIDs.add(obj1.get("tid").toString());
				}
				
				if(spoutlist.contains(obj1.get("tid").toString()) && spoutStatus.equals("false") && sas.equals("null")) {
					spoutbuilder.append("  - id: \"" + "spout-" + obj1.get("tid").toString() + "\"\n    className: \"" + obj1.get("type").toString() + "\"\n    "
							+ "parallelism: 1\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "spout-" + obj1.get("tid").toString());
				}
				
				if(sas.equals("null") && spoutStatus.equals("false") && !spoutlist.contains(obj1.get("tid").toString())) {
					boltstrbuilder.append("  - id: \"" + "bolt-" + obj1.get("tid").toString() + "\"\n    className: \"" + obj1.get("type").toString() + "\"\n    "
							+ "parallelism: 1\n    constructorArgs:\n      - \"" + obj1.get("tid").toString() + "\"\n\n");
					
					nodemapping.put(obj1.get("tid").toString(), "bolt-" + obj1.get("tid").toString());
					System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ going to add:"+obj1.get("tid").toString());
					//taskIDs.add(obj1.get("tid").toString());
				}
			}
		}
		
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
						System.out.println("key: "+ srckey+"_"+destkey);
						streambuilder.append("  - name: \"" + nodemapping.get(srckey) + " --> " + nodemapping.get(destkey) +"\"\n    from: \"" 
								+ nodemapping.get(srckey) + "\"\n    to: \"" + nodemapping.get(destkey) + "\"\n    grouping:\n      type: SHUFFLE\n\n");
					}
					
					if(!nodemapping.get(srckey).substring(0, 4).equals(nodemapping.get(destkey).substring(0, 4)) && !nodemapping.get(srckey).startsWith("spout")) {
						System.out.println("key1: "+ srckey+"_"+destkey);
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