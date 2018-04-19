package com.eclipse.streamapp;

import java.util.Map;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.eclipse.stormdataflows.DAGUtils;

public abstract class GenericSpout extends BaseRichSpout implements MqttCallback {
	private static final long serialVersionUID = 1L;
	public SpoutOutputCollector spoutcollector;
	String spoutId, controltopic, datatopic;
	boolean pause, forward;
	boolean pf=false, ff=false;
	public MqttClient mqtt;
	public MqttMessage mqttmsg;
	
	public GenericSpout(String spoutName, String spoutId) {
		this.spoutId = spoutId;
		this.controltopic = "c-" + spoutId;
		this.datatopic = "d-" + spoutId;
		setPause(false);
		setForward(false);
	}
	
	@Override
	public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
		// TODO Auto-generated method stub
		this.spoutcollector = collector;
		
		MqttConnectOptions conn = DAGUtils.connectToMqtt();
		
		try {
			mqtt = new MqttClient(DAGUtils.mqttconnect, MqttClient.generateClientId());
			mqtt.connect(conn);
			mqtt.setCallback(this);
			mqtt.subscribe(controltopic);
			long tid = Thread.currentThread().getId();
			System.out.println("OPMW_SS," + System.currentTimeMillis() + ",RS,spout-" + spoutId + "," + pause + "," + forward);
			System.out.println("TT_ID,"+tid);
		} catch(MqttException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("opmwspout"));
	}

	@Override
	public void connectionLost(Throwable throwable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub
	
	}	

	@Override
	public void messageArrived(String str, MqttMessage mqttMessage) throws Exception {
		// TODO Auto-generated method stub
		//mqttMessage.setQos(2);
		String msg = new String(mqttMessage.getPayload());
		System.out.println("MQTT_SUB,"+ System.currentTimeMillis() +",spout-"+spoutId+","+msg);
		switch(msg) {
		case "SET_FORWARD":
			//System.out.println("******************************************************************************************************************************* got:" + msg);
			this.setForward(true);
			break;
			
		case "UNSET_FORWARD":
			//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ got:" + msg);
			this.setForward(false);
			break;
			
		case "SET_PAUSE":
			//System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ PAUSE IS TRUE ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
			this.setPause(true);
			break;
			
		case "UNSET_PAUSE":
			this.setPause(false);
			break;
			
		default:
			//System.out.println("Error msg: " + msg);
		}
		
		//for logging state changes on each worker
		if(pf != isPause()) {
			System.out.println("OPMW_SS," + System.currentTimeMillis() + ",RS," + "spout-" + spoutId + "," + pause + "," + forward);
			pf = isPause();
		}
		
		if(ff != isForward()) {
			System.out.println("OPMW_SS," + System.currentTimeMillis() + ",RS," + "spout-" + spoutId + "," + pause + "," + forward);	
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