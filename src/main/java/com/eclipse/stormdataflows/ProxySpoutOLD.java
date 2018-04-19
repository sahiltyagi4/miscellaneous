package com.eclipse.stormdataflows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ProxySpoutOLD extends BaseRichSpout implements MqttCallback {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector outputCollector;
	private MqttClient mqttClient;
	private String dataTopic;
	//private ConcurrentLinkedQueue<Values> nbqueue;
	private ConcurrentLinkedQueue<byte[]> nbqueue;
	
	public ProxySpoutOLD(String boltId) {
		this.dataTopic = "d-" + boltId;
	}
	
	public static Object deserialize(byte[] data) {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
		ObjectInputStream objectInputStream;
		Object object = null;
		try {
			objectInputStream = new ObjectInputStream(inputStream);
			object =  objectInputStream.readObject();
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch(ClassNotFoundException c) {
			c.printStackTrace();
		}
		return object;
	}
	
	public void nextTuple() {
		// TODO Auto-generated method stub
		byte[] arr = nbqueue.poll();
		if(arr != null) {
			Values val = new Values(deserialize(arr));
			if(val != null) {
				System.out.println("############################################################################################## msg: " + val.get(0));
				outputCollector.emit(val);
				
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void open(Map map, TopologyContext context, SpoutOutputCollector spoutCollector) {
		// TODO Auto-generated method stub
		this.outputCollector = spoutCollector;
		nbqueue = new ConcurrentLinkedQueue<byte[]>();
		try {
			mqttClient = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
			mqttClient.connect();
			mqttClient.setCallback(this);
			mqttClient.subscribe(dataTopic);
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
		
	}

	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("message"));
	}
	
	public void connectionLost(Throwable throwable) {
		// TODO Auto-generated method stub
		System.out.println("lost connection to mqtt server because: " + throwable.getMessage());
	}

	public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
		// TODO Auto-generated method stub
		System.out.println("status message for the message delivery: " + deliveryToken.isComplete());
	}

	public void messageArrived(String str, MqttMessage mqttMessage) throws Exception {
		// TODO Auto-generated method stub
		//nbqueue.add(new Values(mqttMessage.getPayload()));
		nbqueue.add(mqttMessage.getPayload());
	}
}
