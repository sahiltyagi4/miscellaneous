package com.eclipse.stormdataflows;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public abstract class DataFlowSpout2 extends BaseRichSpout implements MqttCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector collector;
	private String boltId;
	private String selfId;
	private String dataTopic;
	private MqttClient mqttClient;
	private ConcurrentLinkedQueue<byte[]> nbqueue;
	BufferedWriter writer;

	public DataFlowSpout2(String boltId, String selfId) {
		// TODO Auto-generated constructor stub
		this.boltId = boltId;
		this.selfId = selfId;
		this.dataTopic = "d-" + boltId;
	}
	
	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		byte[] arr = nbqueue.poll();
		if(arr != null) {
			Values val = new Values(deserialize(arr));
			if(val != null) {
				//System.out.println("################################################# msg: " + val.get(0).toString());
				collector.emit(val);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void open(Map map, TopologyContext context, SpoutOutputCollector collector) {
		// TODO Auto-generated method stub
		this.collector = collector;
		nbqueue = new ConcurrentLinkedQueue<byte[]>();
		try {
			MqttConnectOptions conn = new MqttConnectOptions();
			conn.setCleanSession(true);
			conn.setUserName("admin");
			conn.setPassword("password".toCharArray());
			
			mqttClient = new MqttClient("tcp://0.0.0.0:61613", MqttClient.generateClientId());
			
			mqttClient.connect(conn);
			mqttClient.setCallback(this);
			mqttClient.subscribe(dataTopic);
			
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(DAGUtils.tasklogs + "proxyspout-" + selfId + ".txt")));
			writer.write("Complete.");
			writer.close();
			
		} catch(MqttException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
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
		nbqueue.add(mqttMessage.getPayload());
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
}