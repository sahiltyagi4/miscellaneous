package com.eclipse.stormexamples;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class Bolt2 extends BaseRichBolt {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector collector;

	@Override
	public void execute(Tuple arg0) {
//		// TODO Auto-generated method stub
//		String msg = arg0.getString(0);
//		message = new MqttMessage();
//		message.setPayload(msg.getBytes());
//		try {
//			mqtt.publish(publish_topic, message);
//		} catch(MqttException e) {
//			e.printStackTrace();
//		}
		
		System.out.println(arg0.getString(0));
	}

	@Override
	public void prepare(Map arg0, TopologyContext arg1, OutputCollector arg2) {
		// TODO Auto-generated method stub
//		this.collector = arg2;
//		queue = new ConcurrentLinkedQueue<String>();
//		try {
//			mqtt = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId());
//			mqtt.connect();
//		} catch(MqttException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("mqtt_message"));
	}

	

}
