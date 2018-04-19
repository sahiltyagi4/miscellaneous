package com.eclipse.stormdataflows;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.List;
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
import org.eclipse.paho.client.mqttv3.MqttTopic;

public abstract class DataFlowSpout extends BaseRichSpout implements MqttCallback {

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
	//BufferedWriter writer;

	public DataFlowSpout(String boltId, String selfId) {
		// TODO Auto-generated constructor stub
		this.boltId = boltId;
		this.selfId = selfId;
		this.dataTopic = "d-" + boltId;
	}
	
	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		// FIXME: Need to subscribe to control topic, and pause/unsubscribe when downstream bolt is paused.
		byte[] arr = nbqueue.poll();
		if(arr != null) {
			String msgid = new String(arr);
			Values val = new Values(msgid);
			if(!msgid.equals(null)) {
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
			MqttConnectOptions conn = DAGUtils.connectToMqtt();
			
			mqttClient = new MqttClient(DAGUtils.mqttconnect, MqttClient.generateClientId());
			
			mqttClient.setCallback(this);
			mqttClient.connect(conn);
			mqttClient.subscribe(dataTopic, 2);
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("msgid"));
	}
	
	public void connectionLost(Throwable throwable) {
		// TODO Auto-generated method stub
		System.out.println("MQTT_CONNECTLOSS,DATAFLOWSPOUT,"+ System.currentTimeMillis() +",proxyspout-"+selfId);
		//throwable.printStackTrace();
	}

	public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
//		try {
//			
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		
	}

	public void messageArrived(String str, MqttMessage mqttMessage) throws Exception {
		// TODO Auto-generated method stub
		byte[] arr = mqttMessage.getPayload();
		nbqueue.add(arr);
	}
	
}
