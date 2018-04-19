package com.eclipse.stormdataflows;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.eclipse.OPMW.OPMWBolt;
import com.eclipse.OPMW.OPMWwithLatency;

public abstract class LatencyDFBolt extends BaseRichBolt implements MqttCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private MqttClient mqtt;
	private MqttMessage message;
	private String dataTopic, controlTopic, boltID;
	public boolean pause, forward;
	boolean pf =false, ff =false;
	
	public LatencyDFBolt(String boltID) {
		this.dataTopic = "d-"+boltID;
		this.controlTopic = "c-"+boltID;
		this.boltID = boltID;
		setPause(false);
		setForward(false);
	}

	@SuppressWarnings("rawtypes")
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.outputCollector = collector;
		try {
			
			MqttConnectOptions conn = DAGUtils.connectToMqtt();		

			mqtt = new MqttClient(DAGUtils.mqttconnect, MqttClient.generateClientId());
			mqtt.setCallback(this);
			mqtt.connect(conn);
			mqtt.subscribe(controlTopic, 2);
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
	}
	
	protected void emit(Values values) {
		
		String msgid = values.get(0).toString();
		
		if(!pause || forward) {
			OPMWBolt.doPiCompute(1600);
		}
			
		if(!pause) {
			outputCollector.emit(values);
		}
		
		if(forward) {
			pubMessageID(dataTopic, msgid);
		}
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("msgid"));
	}
	
//	 private static byte[] castMSGID(String msgid) {
//	         ByteArrayOutputStream out = null;
//	         try {
//                	 out = new ByteArrayOutputStream();
//        	         ObjectOutputStream objectstream = new ObjectOutputStream(out);
//	                 objectstream.writeUTF(msgid);
//                	 objectstream.flush();
//        	         //objectstream.writeObject(parameters);
//	                 //objectstream.close();
//                	 //out.close();
//         	} catch(IOException e) {
//                	 e.printStackTrace();
//         	}
//         	return out.toByteArray();
//	 }

	public void pubMessageID(String dataTopic, String msgid) {
		byte[] data = msgid.getBytes();
		message = DAGUtils.getMqttMessage(data);
		try {
		    mqtt.publish(dataTopic, message);
        } catch(MqttException e) {
                e.printStackTrace();
        }
	}

//	private void publishMessages(String topic, Values values) {
//		byte[] arr = castMessage(values);
//		message = DAGUtils.getMqttMessage(arr);
//		
//		try {
//			mqtt.publish(topic, message);
//		} catch(MqttException e) {
//			e.printStackTrace();
//		}
//	}
	
//	private static byte[] castMessage(List<Object> parameters) {
//		ByteArrayOutputStream out = null;
//		try {
//			out = new ByteArrayOutputStream();
//			ObjectOutputStream objectstream = new ObjectOutputStream(out);
//			objectstream.writeObject(parameters);
//			objectstream.close();
//			out.close();
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
//		return out.toByteArray();
//	}
	
	public void connectionLost(Throwable throwable) {
		// TODO Auto-generated method stub
		System.out.println("MQTT_CONNECTLOSS,"+ System.currentTimeMillis() +",bolt-"+boltID);
		//throwable.printStackTrace();
	}

	public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
		// YS
//		try{
//			if(deliveryToken.getException() != null) deliveryToken.getException().printStackTrace();
//			if(deliveryToken.getMessage() != null) {}	//System.out.println("MQTT_PUB_DELIVERED_MSG,DATAFLOWBOLT,"+ System.currentTimeMillis() +",bolt-"+boltID+",MSG_VAL"+deliveryToken.getMessage().getPayload());
//
//		}catch(Exception ex){
//			ex.printStackTrace();
//		}
	}

	public void messageArrived(String str, MqttMessage mqttMessage) {
		// TODO Auto-generated method stub
		String msg = new String(mqttMessage.getPayload());
		
		switch(msg) {
		case "SET_FORWARD":
			this.setForward(true);
			break;
			
		case "UNSET_FORWARD":
			this.setForward(false);
			break;
			
		case "SET_PAUSE":
			this.setPause(true);
			break;
			
		case "UNSET_PAUSE":
			this.setPause(false);
			break;
			
		default:
			System.out.println("Error msg in latencyDF bolt: " + msg);
		}
		
		
		//OPMW task status logging
		//System.out.println(System.currentTimeMillis() + ",TASK_ARRIVED_11," + msg + "," + boltID + "\n");
		
		
		if(pf != isPause()) {
			pf = isPause();
		}
		
		if(ff != isForward()) {
			ff = isForward();
		}
	}
	
	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		this.pause = pause;
	}

	public boolean isForward() {
		return forward;
	}

	public void setForward(boolean forward) {
		this.forward = forward;
	}

}
