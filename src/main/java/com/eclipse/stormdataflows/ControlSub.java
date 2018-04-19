package com.eclipse.stormdataflows;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ControlSub implements MqttCallback{
	static MqttClient client;
	static String topic;
	
	public static void main(String[] args) throws MqttException {
		topic = args[0];
		MqttConnectOptions conn = new MqttConnectOptions();
		conn.setCleanSession(true);
		conn.setUserName("admin");
		conn.setPassword("password".toCharArray());
		
		client = new MqttClient("tcp://0.0.0.0:61613", MqttClient.generateClientId());
		client.connect(conn);
		
		ControlSub obj = new ControlSub();
		obj.msgsub();
	}
	
	public void msgsub() throws MqttException {
		client.setCallback(this);
		client.subscribe(topic);
		
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("connection lost to mqtt server");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		System.out.println("delivered message..");
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		String msg = new String(arg1.getPayload());
		System.out.println("message is: " + msg);
		
	}
	
}
