package com.ilaguna.metrics;

public class ProcessInfo {
	
	public native String getPid();
	
	public ProcessInfo() {
		try {
			System.loadLibrary("processinfo");
		} catch (Exception e) {
			System.err.println("Could not load 'processinfo' library");
			e.printStackTrace();
		}
	}
	
	/**
	 * This method should not be called!
	 * @param args
	 */
	public static void main(String[] args) {	
	}
}
