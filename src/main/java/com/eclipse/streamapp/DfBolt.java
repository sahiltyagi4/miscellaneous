package com.eclipse.streamapp;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipse.OPMW.OPMWBolt;
import com.eclipse.OPMW.StudentRecordHandler;
import com.eclipse.stormdataflows.DAGUtils;

import in.dream_lab.bm.stream_iot.tasks.ITask;
import in.dream_lab.bm.stream_iot.tasks.aggregate.AccumlatorTask;
import in.dream_lab.bm.stream_iot.tasks.aggregate.BlockWindowAverage;
import in.dream_lab.bm.stream_iot.tasks.aggregate.DistinctApproxCount;
import in.dream_lab.bm.stream_iot.tasks.filter.BloomFilterCheck;
import in.dream_lab.bm.stream_iot.tasks.filter.RangeFilterCheck;
import in.dream_lab.bm.stream_iot.tasks.io.AzureBlobUploadTask;
import in.dream_lab.bm.stream_iot.tasks.io.AzureTableInsert;
import in.dream_lab.bm.stream_iot.tasks.io.MQTTPublishTask;
import in.dream_lab.bm.stream_iot.tasks.parse.CsvToSenMLParse;
import in.dream_lab.bm.stream_iot.tasks.parse.SenMLParse;
import in.dream_lab.bm.stream_iot.tasks.predict.DecisionTreeClassify;
import in.dream_lab.bm.stream_iot.tasks.predict.SimpleLinearRegressionPredictor;
import in.dream_lab.bm.stream_iot.tasks.statistics.Interpolation;
import in.dream_lab.bm.stream_iot.tasks.statistics.KalmanFilter;
import in.dream_lab.bm.stream_iot.tasks.visualize.XChartMultiLinePlotTask;



public abstract class DfBolt extends BaseRichBolt implements MqttCallback {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OutputCollector outputCollector;
	private MqttClient mqtt;
	private MqttMessage message;
	private String dataTopic, controlTopic, boltID;
	protected String boltName;
	public boolean pause, forward;
	boolean pf =false, ff =false;
	
	private static Logger l;
	private Properties p;
	public ITask task;

	public DfBolt(String boltName, String boltID) {
		this.dataTopic = "d-"+boltID;
		this.controlTopic = "c-"+boltID;
		this.boltID = boltID;
		this.boltName = boltName;
		setPause(false);
		setForward(false);
	}
	
	public static void initLogger(Logger l_) { l = l_; }

	@SuppressWarnings("rawtypes")
	public void prepare(Map map, TopologyContext context, OutputCollector collector) {
		// TODO Auto-generated method stub
		this.outputCollector = collector;
		try {
			MqttConnectOptions conn = DAGUtils.connectToMqtt();			
			
			mqtt = new MqttClient(DAGUtils.mqttconnect, MqttClient.generateClientId());
			mqtt.setCallback(this);
			mqtt.connect(conn);
			subscribeMessages(controlTopic);
			long tid = Thread.currentThread().getId();
			
		} catch(MqttException e) {
			e.printStackTrace();
		}
		
		
		//logging and props for give bolt
		p = new Properties();
		initLogger(LoggerFactory.getLogger("APP"));
		String taskPropFilename = DAGUtils.propertiesFile;
		if(l == null )
			System.out.println("TTT Logger is null");
		if(taskPropFilename == null )
			System.out.println("TTT task proper is null");
		//System.out.println(" TTT : task properties file "+taskPropFilename);
		InputStream input =null;
		try {
			input = new FileInputStream(taskPropFilename);
			//System.out.println(" TTT : File read " );
			 p.load(input);
			 //System.out.println(" TTT : p loaded" );
			//call fetchITask here
			task = fetchITask(boltName);
			task.setup(l, p);
			
		} catch(IOException e) {
			System.out.println("TASK_PROP_Exception,"+System.currentTimeMillis()+",bolt-"+boltID);
			e.printStackTrace();
		}
		
		
	}
	
	
	private static ITask fetchITask(String boltname) {
		ITask task=null;
		switch(boltname) {
			case "SenMLParse":
				task = new SenMLParse();
				break;
		
			case "KalmanFilter":
				task = new KalmanFilter();
				break;
				
			case "Average":
				task = new BlockWindowAverage();
				break;
				
			case "Accumlator":
				task = new AccumlatorTask();
				break;
			
			case "Accumulator":
				task = new AccumlatorTask();
				break;
				
			case "BloomFilter":
				task = new BloomFilterCheck();
				break;	
				
			case "CsvToSenML":
				task = new CsvToSenMLParse();
				break;	
			
			case "SLR":
				task = new SimpleLinearRegressionPredictor();
				break;
				
			case "RangeFilter":
				task = new RangeFilterCheck();
				break;
				
			case "DistinctCount":
				task = new DistinctApproxCount();
				break;
				
			case "Interpolation":
				task = new Interpolation();
				break;
				
			case "AzureInsert":
				task = new AzureTableInsert();
				break;
				
			case "DecisionTree":
				task = new DecisionTreeClassify();
				break;	
				
			case "BlobUpload":
				task = new AzureBlobUploadTask();
				break;
				
			case "MqttPub":
				task = new MQTTPublishTask();
				break;
				
			case "Plot":
				task = new XChartMultiLinePlotTask();
				break;
				
			default:
				System.out.println("Task select error: " + boltname);
		
		}
		return task;
	}
	
	
	protected void emit(Values values) {
			
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
		arg0.declare(new Fields("map","res"));
	}
	
	private void publishMessages(String topic, Values values) {
		message = new MqttMessage();
		message.setQos(2); // YS
		message.setPayload(castMessage(values));
		
		try {
			mqtt.publish(topic, message);
		} catch(MqttException e) {
			e.printStackTrace();
		}
	}
	
	private void subscribeMessages(String topic) {
		try {
			mqtt.subscribe(topic, 2); // YS
			
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
			
			//System.out.println("MQTT_PUB_DELIVERED,"+ System.currentTimeMillis() +",bolt-"+boltID+","+deliveryToken.getMessage()+","+String.join(":", deliveryToken.getTopics())+","+deliveryToken.isComplete()+","+deliveryToken.getException());
			if(deliveryToken.getException() != null) deliveryToken.getException().printStackTrace();
			//if(deliveryToken.getMessage() != null) 	System.out.println("MQTT_PUB_DELIVERED_MSG,"+ System.currentTimeMillis() +",bolt-"+boltID+",MSG_VAL"+deliveryToken.getMessage().getPayload());

		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void messageArrived(String str, MqttMessage mqttMessage) {
		// TODO Auto-generated method stub
		//mqttMessage.setQos(2);
		String msg = new String(mqttMessage.getPayload());
		//System.out.println("MQTT_SUB,"+ System.currentTimeMillis() +",bolt-"+boltID+","+msg);
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
			//System.out.println("OPMW_SS," + System.currentTimeMillis() + ",BT,bolt-" + boltID + "," + pause + "," + forward);
			pf = isPause();
		}
		
		if(ff != isForward()) {
			//System.out.println("OPMW_SS," + System.currentTimeMillis() + ",BT,bolt-" + boltID + "," + pause + "," + forward);
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