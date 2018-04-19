package com.eclipse.stormexamples;

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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Spout1 extends BaseRichSpout implements MqttCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector collector;
	private MqttClient mqttClient;
	private String topic = "spout1";
	private ConcurrentLinkedQueue<String> queue;
	
	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		/*String[] arr = {"a","b","c","d","e"};
		for(int i=0; i<arr.length; i++) {
			collector.emit(new Values(arr[i]));
		}*/
		
		String msg = queue.poll();
		if(msg != null) {
			collector.emit(new Values(msg+"_Spout1"));
		}
		
	}

	@Override
	public void open(Map arg0, TopologyContext arg1, SpoutOutputCollector arg2) {
		// TODO Auto-generated method stub
		this.collector = arg2;
		queue = new ConcurrentLinkedQueue<String>();
		try {
			mqttClient = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
			mqttClient.connect();
			mqttClient.setCallback(this);
			mqttClient.subscribe(topic);
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("alphabet"));
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		System.out.println("lost connection to mqtt server because: " + arg0.getMessage());
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		System.out.println("status message for the message delivery: " + arg0.isComplete());
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		queue.add(new String(arg1.getPayload()));
	}

}
