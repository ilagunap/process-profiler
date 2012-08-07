package com.ilaguna.metrics;

public class Tests {
	
	int k = 0;
	
	void useCPU() {
		int s = 1;
		int f = 2000;
		for (int i=0; i < f; i++)
			for (int j=0; j < f; j++)
				for (int k=0; k < f; k++)
					s = s * 2;
		k = s;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String pid = new ProcessInfo().getPid();
		
		MetricsCollector mc = new MetricsCollector(pid);
		mc.collectMetrics();
		mc.collectMetrics();
		
		new Tests().useCPU();
		
		mc.collectMetrics();
		mc.printMetrics();
		
		mc.saveData();

	}

}
