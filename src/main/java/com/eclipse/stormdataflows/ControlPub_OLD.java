package com.eclipse.stormdataflows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ControlPub_OLD {
	MqttClient client;
	static Map<String, String> ffmap = new HashMap<String, String>();
	static Map<String, String> pfmap = new HashMap<String, String>();
	BufferedWriter bfrwritr;
	
	public ControlPub_OLD() {
		// TODO Auto-generated constructor stub
	}
	
	@SuppressWarnings("unchecked")
	public void publishControlMessages(String controlfile, String dataflowID) throws MqttException, IOException, ParseException {
		
		MqttConnectOptions conn = new MqttConnectOptions();
		conn.setCleanSession(true);
		conn.setUserName("admin");
		conn.setPassword("password".toCharArray());
		
		client = new MqttClient("tcp://0.0.0.0:61613", MqttClient.generateClientId());
		client.connect(conn);
		
		BufferedReader bfrdr = new BufferedReader(new InputStreamReader(new FileInputStream(controlfile)));
		StringBuilder builder = new StringBuilder();
		String str;
		while((str = bfrdr.readLine()) != null) {
			builder.append(str);
		}
		
		bfrdr.close();
		
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(builder.toString());
		JSONArray jsonarr = (JSONArray) obj;
		
		Iterator<JSONObject> itr = jsonarr.iterator();
		while(itr.hasNext()) {
			JSONObject jsonob = itr.next();
			String taskID = jsonob.get("tid").toString();
			String ff;
			String pf;
			if(jsonob.containsKey("pf"))
				pf = jsonob.get("pf").toString();
			else
				pf = "false";
			
			if(jsonob.containsKey("ff"))
				ff = jsonob.get("ff").toString();
			else
				ff = "false";
			
			String forwardFlag = "";
			String pauseFlag = "";
			String topic = "c-" + taskID;
			
			if(ff.equals("true"))
				forwardFlag = "SET_FORWARD";
			else if(ff.equals("false"))
				forwardFlag = "UNSET_FORWARD";
			
			if(pf.equals("true"))
				pauseFlag = "SET_PAUSE";
			else if(pf.equals("false"))
				pauseFlag = "UNSET_PAUSE";
			
			
			FileWriter fw = null;
			try {
				fw = new FileWriter(DAGUtils.ctrlogs, true);
			} catch(IOException e) {
				e.printStackTrace();
			}

			if(ffmap.get(taskID) != forwardFlag) {
				ffmap.put(taskID, forwardFlag);
				msgpub(topic, ffmap.get(taskID));
			}
			
			if(pfmap.get(taskID) != pauseFlag) {
				pfmap.put(taskID, pauseFlag);
				msgpub(topic, pfmap.get(taskID));
			}
			
			bfrwritr = new BufferedWriter(fw);
			bfrwritr.write(dataflowID + "," + taskID + "," + ffmap.get(taskID) + "," + pfmap.get(taskID) + "," + System.currentTimeMillis() + "\n");
			bfrwritr.close();
		}
		
		client.disconnect();
	}
	
	public void msgpub(String topic, String msg) throws MqttException {
		MqttMessage message = new MqttMessage();
		message.setPayload(msg.getBytes());
		client.publish(topic, message);
		
	}
}
