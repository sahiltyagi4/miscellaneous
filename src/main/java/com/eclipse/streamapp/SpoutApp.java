package com.eclipse.streamapp;

import org.apache.storm.tuple.Values;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class SpoutApp extends GenericSpout {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sampleData; 
	public SpoutApp(String spoutName, String spoutId) {
		super(spoutName, spoutId);
		sampleData = "{\"e\":[{\"u\":\"string\",\"n\":\"source\",\"sv\":\"ci4lr75sl000802ypo4qrcjda23\"},{\"v\":\"6.1668213\",\"u\":\"lon\",\"n\":\"longitude\"},{\"v\":\"46.1927629\",\"u\":\"lat\",\"n\":\"latitude\"},{\"v\":\"8\",\"u\":\"far\",\"n\":\"temperature\"},{\"v\":\"53.7\",\"u\":\"per\",\"n\":\"humidity\"},{\"v\":\"0\",\"u\":\"per\",\"n\":\"light\"},{\"v\":\"411.02\",\"u\":\"per\",\"n\":\"dust\"},{\"v\":\"140\",\"u\":\"per\",\"n\":\"airquality_raw\"}],\"bt\":1422748800000}";
	}

	@Override
	public void nextTuple() {
		String msg=null;
		if(!this.pause) {
			this.spoutcollector.emit(new Values(sampleData));
		}
		
		if(forward) {
			this.mqttmsg = new MqttMessage();
			this.mqttmsg.setPayload(sampleData.getBytes());
			
			try {
				this.mqtt.publish(this.datatopic, this.mqttmsg);
				
			} catch(MqttException e) {
				e.printStackTrace();
			}
			
		}
		
		//set input rate to 100 msg/sec
		try {
			Thread.sleep(10);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}