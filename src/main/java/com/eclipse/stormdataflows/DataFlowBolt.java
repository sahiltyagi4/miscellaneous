package com.eclipse.stormdataflows;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.eclipse.OPMW.OPMWBolt;
import com.eclipse.OPMW.StudentRecordHandler;

public abstract class DataFlowBolt extends BaseRichBolt implements MqttCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private MqttClient mqtt;
	private MqttMessage message;
	private String dataTopic, controlTopic, boltID;
	public boolean pause, forward;
	boolean pf =false, ff =false;
	//BufferedWriter writer;

	public DataFlowBolt(String boltID) {
		this.dataTopic = "d-"+boltID;
		this.controlTopic = "c-"+boltID;
		this.boltID = boltID;
		setPause(false);
		setForward(false);
	}

	@SuppressWarnings("rawtypes")
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.outputCollector = collector;
		try {
			MqttConnectOptions conn = DAGUtils.connectToMqtt();
			mqtt = new MqttClient(DAGUtils.mqttconnect, MqttClient.generateClientId());
			mqtt.setCallback(this);
			mqtt.connect(conn);
			mqtt.subscribe(controlTopic, 2);
			long tid = Thread.currentThread().getId();
			//System.out.println("OPMW_SS," + System.currentTimeMillis() + ",BT,bolt-" + boltID + "," + pause + "," + forward);
			//System.out.println("WORKER_PORT,"+context.getThisWorkerPort()+",COMPONENT," + context.getThisComponentId() + ",TASK," + context.getThisTaskId() + ",BOLT_ID,bolt-" + boltID + ",TT_ID,"+tid);
			
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
	}
	
	protected void emit(Values values) {
		
		if(!pause || forward) {
			OPMWBolt.doPiCompute(1600);
			//OPMWBolt.doXMLparseOp(StudentRecordHandler.xmlstring);
			//OPMWBolt.computeAverage();	
		}
			
		if(!pause) {
			outputCollector.emit(values);
		}
		
		if(forward) {
			publishMessages(dataTopic, values);
		}
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer arg0) {
		// TODO Auto-generated method stub
		arg0.declare(new Fields("dfBolt"));
	}
	
	private void publishMessages(String topic, Values values) {
		System.out.println("MQTT_PUB,"+ System.currentTimeMillis() +",bolt-"+boltID+","+topic+","+values.get(0).toString());
		byte[] arr = castMessage(values);
		message = DAGUtils.getMqttMessage(arr);
		//System.out.println("MQTT_PUB,SERIALIZE_CHECK,"+ System.currentTimeMillis() +",bolt-"+boltID+","+topic+","+new String(arr));
		
		try {
			mqtt.publish(topic, message);
		} catch(MqttException e) {
			e.printStackTrace();
		}
	}
	
	private static byte[] castMessage(List<Object> parameters) {
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			ObjectOutputStream objectstream = new ObjectOutputStream(out);
			objectstream.writeObject(parameters);
			objectstream.close();
			out.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}
	
	public void connectionLost(Throwable throwable) {
		// TODO Auto-generated method stub
		System.out.println("MQTT_CONNECTLOSS,"+ System.currentTimeMillis() +",bolt-"+boltID);
		throwable.printStackTrace();
		//System.out.println("***************************** lost connection to mqtt server");
	}

	public void deliveryComplete(IMqttDeliveryToken deliveryToken) {
		// YS
		try{
			
			System.out.println("MQTT_PUB_DELIVERED,DATAFLOWBOLT,"+ System.currentTimeMillis() +",bolt-"+boltID+","+deliveryToken.getMessage()+","+String.join(":", deliveryToken.getTopics())+","+deliveryToken.isComplete()+","+deliveryToken.getException());
			if(deliveryToken.getException() != null) deliveryToken.getException().printStackTrace();
			if(deliveryToken.getMessage() != null)  {}; //System.out.println("MQTT_PUB_DELIVERED_MSG,DATAFLOWBOLT,"+ System.currentTimeMillis() +",bolt-"+boltID+",MSG_VAL"+deliveryToken.getMessage().getPayload());

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void messageArrived(String str, MqttMessage mqttMessage) {
		// TODO Auto-generated method stub
		//mqttMessage.setQos(2);
		String msg = new String(mqttMessage.getPayload());
		System.out.println("MQTT_SUB,"+ System.currentTimeMillis() +",bolt-"+boltID+","+msg);
		//System.out.println("------------------------------------------------------------------------------------ mqtt message is:"+msg+"__"+this.controlTopic);
		
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
			System.out.println("Error msg: " + msg);
		}
		
		if(pf != isPause()) {
			System.out.println("OPMW_SS," + System.currentTimeMillis() + ",BT,bolt-" + boltID + "," + pause + "," + forward);
			pf = isPause();
		}
		
		if(ff != isForward()) {
			System.out.println("OPMW_SS," + System.currentTimeMillis() + ",BT,bolt-" + boltID + "," + pause + "," + forward);
			ff = isForward();
		}
	}
	
	public boolean isPause() {
		return pause;
	}

	public void setPause(boolean pause) {
		//System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& in pause set true method with value: " + pause);
		this.pause = pause;
	}

	public boolean isForward() {
		return forward;
	}

	public void setForward(boolean forward) {
		this.forward = forward;
	}

}