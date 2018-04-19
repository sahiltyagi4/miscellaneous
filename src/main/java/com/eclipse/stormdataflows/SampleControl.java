package com.eclipse.stormdataflows;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SampleControl {
	
	public static void main(String[] args) {
		try {
			MqttClient client;
			MqttConnectOptions conn = new MqttConnectOptions();
			conn.setCleanSession(true);
			conn.setUserName("admin");
			conn.setPassword("password".toCharArray());
			String topic = args[0];
			String msg = args[1];
			
			client = new MqttClient("tcp://192.168.0.10:61613", MqttClient.generateClientId());
			//client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
			client.connect(conn);
			//client.connect();
			
			MqttMessage message = new MqttMessage();
			//String msg = "SET_FORWARD";
			//String topic = c-a , "d-O1", 
			message.setPayload(msg.getBytes());
			client.publish(topic, message);
			System.out.println("published message: " + msg + " to topic: " + topic);
			
			client.disconnect();
			System.out.println("done.");
			
		} catch(MqttException ex) {
			ex.printStackTrace();
		}
	}
}
