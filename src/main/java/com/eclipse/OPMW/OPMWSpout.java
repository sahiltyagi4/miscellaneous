package com.eclipse.OPMW;

import java.util.Map;
import java.util.UUID;

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

import com.eclipse.stormdataflows.DAGUtils;

public class OPMWSpout extends BaseRichSpout implements MqttCallback {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpoutOutputCollector spoutcollector;
	String spoutId, controltopic, datatopic;
	boolean pause, forward;
	boolean pf=false, ff=false;
	private MqttClient mqtt;
	private MqttMessage mqttmsg;
	
	public OPMWSpout(String spoutId) {
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
			mqtt.setCallback(this);
			mqtt.connect(conn);
			mqtt.subscribe(controltopic, 2); //YS
			//long tid = Thread.currentThread().getId();
			//System.out.println("WORKER_PORT,"+context.getThisWorkerPort()+",COMPONENT," + context.getThisComponentId() + ",TASK," + context.getThisTaskId() + ",SPOUT_ID,spout-" + spoutId + ",TT_ID,"+tid);
		} catch(MqttException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void nextTuple() {
		// TODO Auto-generated method stub
		
		String msgId="a";
		if(!pause) {
			//msgId = UUID.randomUUID().toString();
			spoutcollector.emit(new Values(msgId));
		}
		
		if(forward) {
			
			if(msgId != null) {
				mqttmsg = DAGUtils.getMqttMessage(msgId.getBytes());
				try {
					mqtt.publish(datatopic, mqttmsg);
					
				} catch(MqttException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		//set input rate to 5 msg/sec
		try {
			Thread.sleep(200);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		declarer.declare(new Fields("msgid"));
	}

	@Override
	public void connectionLost(Throwable throwable) {
		System.out.println("MQTT_CONNECTLOSS,"+ System.currentTimeMillis() +",spout-"+spoutId+",REGULARSPOUT");
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
//		try {
//			//System.out.println("MQTT_PUB_DELIVERED,"+ System.currentTimeMillis() +",spout-"+spoutId+","+deliveryToken.getMessage()+","+String.join(":", deliveryToken.getTopics())+","+deliveryToken.isComplete()+","+deliveryToken.getException());
//			if(deliveryToken.getException() != null) deliveryToken.getException().printStackTrace();
//			if(deliveryToken.getMessage() != null) 	{} //System.out.println("MQTT_PUB_DELIVERED_MSG,REGULARSPOUT,"+ System.currentTimeMillis() +",spout-"+spoutId+",MSG_VAL"+deliveryToken.getMessage().getPayload());
//
//		} catch (MqttException e) {
//			e.printStackTrace();
//		}
	
	}	

	@Override
	public void messageArrived(String str, MqttMessage mqttMessage) throws Exception {
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
			//System.out.println("Error msg: " + msg);
		}
		
		//OPMW task status logging
		//System.out.println(System.currentTimeMillis() + ",TASK_ARRIVED_11," + msg + "," + spoutId + "\n");
		
		
		//for logging state changes on each worker
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
