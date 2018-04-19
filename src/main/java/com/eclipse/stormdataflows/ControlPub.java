package com.eclipse.stormdataflows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

//import com.eclipse.OPMW.OPMWReuse2;

public class ControlPub implements MqttCallback {
	MqttClient client;
	public static Map<String, String> ffmap = new HashMap<String, String>();
	public static Map<String, String> pfmap = new HashMap<String, String>();
	BufferedWriter bfrwritr;
	
	public static void main(String[] args) {
		
	}
	
	public void publishControlMessages(String controlfile, String dataflowID) throws MqttException, IOException, ParseException {
		MqttConnectOptions conn = DAGUtils.connectToMqtt();		
		
		client = new MqttClient(DAGUtils.mqttconnect, MqttClient.generateClientId());
		client.setCallback(this);
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
		
		//OPMWReuse2.controlflow.write((OPMWReuse2.logicalctr+1) + "," + controlfile);
		
		if(!jsonarr.isEmpty()) {
			Iterator<JSONObject> itr = jsonarr.iterator();
			while(itr.hasNext()) {
				JSONObject jsonob = itr.next();
				String taskID = jsonob.get("tid").toString();
				String ff = null, pf = null;
				String topic = "c-" + taskID;
				
				if(ffmap.get(taskID) == null) {
					ffmap.put(taskID, "UNSET_FORWARD");
				}
				
				if(pfmap.get(taskID) == null) {
					pfmap.put(taskID, "UNSET_PAUSE");
				}
				
				if(jsonob.containsKey("ps")) {
					pf = jsonob.get("ps").toString();
				}
				
				if(jsonob.containsKey("ff")) {
					ff = jsonob.get("ff").toString();
				}
				
				if(ff != null) {
					
					if(ff.equals("true")) {
						//System.out.println("..........................................................................FWD IS TRUE");
					}
					
					if(ff.equals("true") && !ffmap.get(taskID).equals("SET_FORWARD")) {
						ffmap.put(taskID, "SET_FORWARD");
					} else if(ff.equals("false") && !ffmap.get(taskID).equals("UNSET_FORWARD")) {
						ffmap.put(taskID, "UNSET_FORWARD");
					}
					msgpub(topic, ffmap.get(taskID), taskID);
					
					//OPMWReuse2.controlflow.write("," + taskID+"-"+ffmap.get(taskID));
					
				}
				
				if(pf != null) {
					
					if(pf.equals("true") && !pfmap.get(taskID).equals("SET_PAUSE")) {
						pfmap.put(taskID, "SET_PAUSE");
					} else if(pf.equals("false") && !pfmap.get(taskID).equals("UNSET_PAUSE")) {
						pfmap.put(taskID, "UNSET_PAUSE");
					}
					
					msgpub(topic, pfmap.get(taskID), taskID);
					
					//OPMWReuse2.controlflow.write("," + taskID+"-"+pfmap.get(taskID));
					
				}
				
//				FileWriter fw = null;
//				try {
//					fw = new FileWriter(DAGUtils.ctrlogs, true);
//				} catch(IOException e) {
//					e.printStackTrace();
//				}
//				
//				bfrwritr = new BufferedWriter(fw);
//				bfrwritr.write(dataflowID + "," + taskID + "," + ffmap.get(taskID) + "," + pfmap.get(taskID) + "," + System.currentTimeMillis() + "\n");
//				bfrwritr.close();
			}
			
		} else {
			//System.out.println("...................................................... the control file is empty!");
		}
		
		//OPMWReuse2.controlflow.write("\n");
	}
	
	public void msgpub(String topic, String msg, String taskId) throws MqttException {
		//System.out.println("MQTT_PUB,"+ System.currentTimeMillis() +",ControlPub"+","+topic+","+msg); // YS
		//try {
			MqttMessage message = DAGUtils.getMqttMessage(msg.getBytes());
			client.publish(topic, message);
			
			//OPMWReuse2.taskstatwrtr.write(System.currentTimeMillis() + ",CTRLPUB_PUBLISH," + msg + "," + taskId + "\n");
			
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("MQTT_CONNECTLOSS,"+ System.currentTimeMillis() +",ControlPub");	
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		//System.out.println("MQTT_SUB,"+ System.currentTimeMillis() +",ControlPub"+","+message.getPayload());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
//		try {
//			//System.out.println("MQTT_PUB_DELIVERED,"+ System.currentTimeMillis() +",ControlPub"+","+deliveryToken.getMessage()+","+String.join(":", deliveryToken.getTopics())+","+deliveryToken.isComplete()+","+deliveryToken.getException());
//			if(deliveryToken.getException() != null) deliveryToken.getException().printStackTrace();
//			if(deliveryToken.getMessage() != null) 	System.out.println("MQTT_PUB_DELIVERED_MSG,CONTROLPUB,"+ System.currentTimeMillis() +",ControlPub"+",MSG_VAL"+deliveryToken.getMessage().getPayload());
//			
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}		
		
	}
	
}