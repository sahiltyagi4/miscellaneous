package com.eclipse.stormdataflows;

import java.io.File;
import java.io.IOException;

public class Processbuildertest {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String outputyaml = "/home/dreamlab/Documents/compiled/sample-1.yaml";
		ProcessBuilder builder = new ProcessBuilder("storm", "jar", "/home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar", "org.apache.storm.flux.Flux", "--local", outputyaml);
		//ProcessBuilder builder = new ProcessBuilder("storm", "jar", "/home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar", "com.eclipse.stormexamples.Topology3");
		//Process process = Runtime.getRuntime().exec("storm jar /home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar org.apache.storm.flux.Flux --local /home/dreamlab/Documents/compiled/sample-1.yaml");
		//Process process = Runtime.getRuntime().exec("storm jar /home/dreamlab/Documents/compiled/target/compiled-v1-jar-with-dependencies.jar com.eclipse.stormexamples.Topology3");
		System.out.println("starting storm topology...");
		//Process process = Runtime.getRuntime().exec("/home/dreamlab/Documents/fluxscript.sh");
		builder.redirectOutput(new File("/home/dreamlab/Documents/out2.txt"));
		Process p = builder.start();
		Thread.sleep(30000);
		
	}
}
