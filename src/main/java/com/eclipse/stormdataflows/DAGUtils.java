package com.eclipse.stormdataflows;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class DAGUtils {
	
//	public static String compiledJAR = "/home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar";
//	public static String mode = "--remote";
//	public static String storm = "storm";
//	public static String mqttconnect = "tcp://0.0.0.0:61613";
//	public static String stormlogs = "/home/dreamlab/Documents/1/stormlogs/";
//	public static String tasklogs = "/home/dreamlab/Documents/1/tasklogs/";
//	public static String graphsfile = "/home/dreamlab/Desktop/logicalcount.txt";
//	public static String topfile = "/home/dreamlab/Desktop/topfile.txt";
//	public static  String dagdirectory = "/home/dreamlab/Desktop/sharedfolder";
//	public static String topscript = "/home/dreamlab/Documents/topscript.sh";
//	public static String yamlfiles = "/home/dreamlab/Documents/1";
//	public static String cpuredirect = "/home/dreamlab/Documents/cpuredirect.txt";
//	public static String pidfile = "/home/dreamlab/Desktop/cpuPID.txt";
//	public static String cpuscript = "/home/dreamlab/Documents/cpu_storm.sh";
//	public static String cpuLog = "/home/dreamlab/Documents/cpuLog.txt";
//	public static String ctrlogs = "/home/dreamlab/Documents/topologylogs.csv";
//	public static String cpuAvgfile = "/home/dreamlab/Desktop/cpuAvg.csv";
//	public static String topidfile = "/home/dreamlab/Desktop/topologyids.csv";
//	public static String controldflogs = "/home/dreamlab/Desktop/controlDf.csv";
//	public static String propertiesFile = "/home/dreamlab/Documents/compiled/src/main/resources/tasks.properties";
//	public static String opmwfile = "/home/dreamlab/Desktop/opmw-top-65.json";
//	public static String seqOPMWfile = "/home/dreamlab/Desktop/seq-opmw-entire.txt";
//	//public static String seqOPMWfile = "/home/dreamlab/Desktop/ran1-seq-entire.txt";
//	public static String timelog = "/home/dreamlab/Desktop/timelog.txt";
//	public static String pauseFlag = "/home/dreamlab/Desktop/pauseFlag.txt";
	
	public static String compiledJAR = "/home/sahil/compiled/target/compiled-v1-jar-with-dependencies.jar";
	public static String mode = "--remote";
	public static String storm = "/home/sahil/nodes/rigel/apache-storm-1.0.2/bin/storm";
	public static String mqttconnect = "tcp://192.168.0.10:61613";
	public static String stormlogs = "/home/sahil/stormlogs/";	
	public static String tasklogs = "/home/sahil/tasklogs/";
	public static String logicalfile = "/home/sahil/logicalcount.txt";
	public static String graphsfile = "/home/sahil/logicalcount.txt";
	public static  String dagdirectory = "/home/sahil/sharedfolder/";
	public static String topscript = "/home/sahil/compiled/scripts/topscript.sh";
	public static String yamlfiles = "/home/sahil/yamls";
	public static String ctrlogs = "/home/sahil/topologylogs.csv";
	public static String cpuAvgfile = "/stormCPU/cpuAvg.csv";
	public static String topidfile = "/home/sahil/top-ids.csv";
	public static String controldflogs = "/home/sahil/controlDf.csv";
	public static String propertiesFile = "/home/sahil/tasks_rigel.properties";
	public static String opmwfile = "/home/sahil/opmw-top-65.json";
	public static String riotfile = "/home/sahil/app_final.json";
	public static String seqOPMWfile = "/home/sahil/seq-opmw.txt";
	public static String seqRIOTfile = "/home/sahil/seq-riot.txt";
	public static String timelog = "/home/sahil/timelog.txt";
	public static String pauseFlag = "/home/sahil/taskStatusFlag.txt";
	public static String dummytops = "/home/sahil/dummytopology.txt";
	public static String ctrlflow = "/home/sahil/controlflow.txt";
	public static String jsonwrtr = "/home/sahil/df.json";
	public static String slotfile = "/home/sahil/cumulativeslot.txt";
	
	
//	public static String compiledJAR = "/Users/sahiltyagi/Documents/IISc/compiled/target/compiled-v1-jar-with-dependencies.jar";
//	public static String mode = "--remote";
//	public static String storm = "storm";
//	public static String mqttconnect = "tcp://0.0.0.0:61613";
//	public static String stormlogs = "/Users/sahiltyagi/Documents/IISc/experiments/stormlogs/";	
//	public static String tasklogs = "/Users/sahiltyagi/Documents/IISc/experiments/tasklogs/";
//	public static String logicalfile = "/Users/sahiltyagi/Documents/IISc/experiments/logicalcount.txt";
//	public static String graphsfile = "/Users/sahiltyagi/Documents/IISc/experiments/logicalcount.txt";
//	public static String dagdirectory = "/Users/sahiltyagi/Desktop/sharedfolder/";
//	public static String topscript = "/Users/sahiltyagi/Documents/IISc/compiled/scripts/topscript.sh";
//	public static String yamlfiles = "/Users/sahiltyagi/Documents/IISc/experiments/yamls";
//	public static String ctrlogs = "/Users/sahiltyagi/Documents/IISc/experiments/topologylogs.csv";
//	public static String cpuAvgfile = "/Users/sahiltyagi/Documents/IISc/experiments/cpuAvg.csv";
//	public static String topidfile = "/Users/sahiltyagi/Documents/IISc/experiments/top-ids.csv";
//	public static String controldflogs = "/Users/sahiltyagi/Documents/IISc/experiments/controlDf.csv";
//	public static String propertiesFile = "/Users/sahiltyagi/Documents/IISc/experiments/tasks_rigel.properties";
//	public static String opmwfile = "/Users/sahiltyagi/Documents/IISc/experiments/opmw-top-65.json";
//	public static String riotfile = "/Users/sahiltyagi/Documents/IISc/experiments/app_final.json";
//	public static String seqOPMWfile = "/Users/sahiltyagi/Documents/IISc/seq-opmw.txt";
//	public static String seqRIOTfile = "/Users/sahiltyagi/Documents/IISc/seq-riot.txt";
//	public static String timelog = "/Users/sahiltyagi/Documents/IISc/experiments/timelog.txt";
//	public static String pauseFlag = "/Users/sahiltyagi/Documents/IISc/experiments/taskStatusFlag.txt";
//	public static String dummytops = "/Users/sahiltyagi/Documents/IISc/experiments/dummytopology.txt";
//	public static String ctrlflow = "/Users/sahiltyagi/Documents/IISc/experiments/dfcontrolflow.txt";
//	public static String slotfile = "/Users/sahiltyagi/Desktop/cumulativeslot.txt";
//	public static String jsonwrtr = "/Users/sahiltyagi/Desktop/df.json";
	
	public static MqttConnectOptions connectToMqtt() {
		
		MqttConnectOptions conn = new MqttConnectOptions();
		conn.setAutomaticReconnect(true); // YS
		conn.setCleanSession(false); // YS
		//conn.setCleanSession(true); // sahil
		conn.setConnectionTimeout(30); // YS
		//conn.setConnectionTimeout(120); //sahil
		//conn.setKeepAliveInterval(120); //sahil
		conn.setKeepAliveInterval(30);		 // YS	
		conn.setUserName("admin");
		conn.setPassword("password".toCharArray());
		
		return conn;
	}
	
	public static MqttMessage getMqttMessage(byte[] arr) {
		MqttMessage message = new MqttMessage();
		message.setQos(2); //YS
		message.setPayload(arr);
		
		return message;
	}
	
	
}
