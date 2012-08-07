package com.ilaguna.metrics;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.io.*;

public class MetricsCollector {
	
	protected ArrayList<String> data;
	protected String PID;
	protected File dir;
	protected BufferedWriter outFile;
	
	/*
	 * Metrics names:
	 * 10: minflt (minor faults) 
	 * 12: majflt (major faults)
	 * 14: utime (user-mode CPU time)
	 * 15: stime (kernel-mode CPU time)
	 * 20: num_threads
	 * 23: vsize (virtual memory size)
	 * 24: rss (RAM memory)
	 * 28: startstack (address of bottom of the stack)
	 * 30: kstkeip (current EIP; instruction pointer)
	 * 39: processor (CPU number last executed on)
	 */
	int[] metricsInStatFile = {10,12,14,15,20,23,24,28,30,39};
	
	public MetricsCollector(String p) {
		PID = p;
		data = new ArrayList<String>();
		openOutputFile();
	}
	
	public void collectMetrics() {
		data.add(gatherData());
	}
	
	public void collectMetrics(String str) {
		data.add(str + "," + gatherData());
	}
	
	private String gatherData() {
		String data = null;
		data = getStatData() + getNumFileDescritors() + "," + getIOStats();
		return data;
	}
	
	private String getStatData() {
		String ret = null;
		String file = "/proc/" + PID + "/stat";
		try {
			BufferedReader input =  new BufferedReader(new FileReader(file));
			try {
				String line = null;
		        while ((line = input.readLine()) != null)
		        	ret = line;
		    } finally {
		    	input.close();
		    }
		} catch (IOException ex) {
			System.out.println("Problem with file: " + file);
			ex.printStackTrace();
		}
		//System.out.println("Metrics for File: " + file);
		String tmp[] = ret.split("[ ]+");
		ret = "";
		for (int i : metricsInStatFile)
			ret = ret.concat(tmp[i-1]) + ",";
		
		return ret;
	}
	
	private String getNumFileDescritors() {
		String ret = null;
		String fdDir = "/proc/" + PID + "/fd";
		int size = new File(fdDir).list().length;
		ret = Integer.toString(size);
		return ret;
	}
	
	private String getIOStats() {
		String ret = null;
		String file = "/proc/" + PID + "/io";
		ArrayList<String> fileData = new ArrayList<String>();
		try {
			BufferedReader input =  new BufferedReader(new FileReader(file));
			try {
				String line = null;
		        while ((line = input.readLine()) != null) {
		        	fileData.add(line);
		        }
		    } finally {
		    	input.close();
		    }
		} catch (IOException ex) {
			System.out.println("Problem with file: " + file);
			ex.printStackTrace();
		}
		
		ret = "";
		ret = fileData.get(0).split("[ ]+")[1] + "," + 
		fileData.get(1).split("[ ]+")[1] + "," + 
		fileData.get(4).split("[ ]+")[1] + "," +
		fileData.get(5).split("[ ]+")[1] + "," + 
		fileData.get(6).split("[ ]+")[1];
		
		return ret;
	}
	
	public void printMetrics() {
		for (int i=0; i < data.size(); ++i)
			System.out.println(Integer.toString(i) + ":" + data.get(i));
	}
	
	private void openOutputFile() {
		try{
			dir = new File("./output");
			dir.mkdirs();
			Calendar cal = new GregorianCalendar();
			
			// Create file 
			FileWriter fstream = new FileWriter("./output/" + 
					Long.toString(cal.getTimeInMillis()) + "_" + PID + ".dat");
			outFile = new BufferedWriter(fstream);
			
			outFile.write(getMetricNames());
			outFile.write("\n");
		} catch (Exception e) { //Catch exception if any
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
		}
	}
	
	public void saveData() {
		try{
			/*File dir = new File("./output");
			dir.mkdirs();
			
			Calendar cal = new GregorianCalendar();
			
			// Create file 
			FileWriter fstream = new FileWriter("./output/" + 
					Long.toString(cal.getTimeInMillis()) + "_" + PID + ".dat");
			BufferedWriter out = new BufferedWriter(fstream);
			
			out.write(getMetricNames());
			out.write("\n");*/
			for (int i=0; i < data.size(); ++i) {
				outFile.write(data.get(i));
				outFile.write("\n");
			}
			
			//Close the output stream
			outFile.close();
		} catch (Exception e) { //Catch exception if any
				System.err.println("Error: " + e.getMessage());
				e.printStackTrace();
		}
	}
	
	private String getMetricNames() {
		String ret = "ID,minflt,majflt,utime,stime,num_threads,vsize,rss," +
				"startstack,kstkeip,processor,num_file_desc,rchar,wchar," +
				"read_bytes,write_bytes,cancelled_write_bytes";
		return ret;
	}
}
