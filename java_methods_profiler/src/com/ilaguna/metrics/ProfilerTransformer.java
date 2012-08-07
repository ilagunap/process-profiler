package com.ilaguna.metrics;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.io.IOException;
import java.security.ProtectionDomain;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.ByteArrayClassPath;
import javassist.CtMethod;
//import javassist.CtBehavior;
import javassist.NotFoundException;

public class ProfilerTransformer implements ClassFileTransformer
{
	protected static ProfilerTransformer profilerInstance = null;
	protected Instrumentation instrumentation = null;
	protected ClassPool classPool;
	protected MetricsCollector mc;
	protected boolean off = false;
	protected String pattern = null;
	
	public static ProfilerTransformer getInstance()
	{
		return profilerInstance;
	}
	
	public ProfilerTransformer()
	{
		profilerInstance = this;
		classPool = ClassPool.getDefault();
		
		String pid = new ProcessInfo().getPid();
		mc = new MetricsCollector(pid);
		pattern = System.getenv("PROFILE_PATTERN");
		if (pattern == null) {
			System.err.println("Could not find variable 'PROFILE_PATTERN'.");
			System.exit(1);
		} else {
			pattern = pattern.replace(".","/");
		}
		
		System.out.println("*************************************************");
		System.out.println("* In Java profiler");
		System.out.println("* Pattern: " + pattern);
		System.out.println("*************************************************");
	}
	
	public byte[] transform(
			ClassLoader loader, 
			String className, 
			Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, 
			byte[] classfileBuffer)
	throws IllegalClassFormatException
	{
		/*
		 * *******************************************************************
		 * Check for special classes
		 * *******************************************************************
		 */
		/*if (className.contains("java/lang/Shutdown") && !off) {
			//off = true;
			// Save data in file
			//mc.saveData();
		}*/
		
		/*if (off) {
			print("AFTER_SHUTDOWN: " + className);
			return classfileBuffer;
		}*/
			
		/*
		 * Do not instrument the profiler, metric-collector and java classes
		 */
		if (!className.contains(pattern)) {
			return classfileBuffer;
		}
		//if (className.contains("com/ilaguna/metrics") || 
		//		isJavaMethod(className))
		//	return classfileBuffer;
		/*
		 * *******************************************************************
		 */
		
		classPool.insertClassPath(new ByteArrayClassPath(className, 
				classfileBuffer));
		CtClass cc = null;
		try {
			String name = className.replace("/",".");
			cc = classPool.get(name);
		} catch (NotFoundException nfe) {
			System.err.println("NotFoundException: " + nfe.getMessage() + 
					"; transforming class " + className + 
					"; returning uninstrumented class");
			nfe.printStackTrace();
		}
		
		//print("In transformer. class name: " + className);
		if (cc.isInterface() == false) {
			for (CtMethod m : cc.getDeclaredMethods()) {
				try {
					String callName = className + "$" + m.getName();
					m.insertBefore("{ com.ilaguna.metrics.ProfilerTransformer.getInstance().entryCall(\"" + callName + "\"); }");
					m.insertAfter("{ com.ilaguna.metrics.ProfilerTransformer.getInstance().exitCall(\"" + callName + "\"); }");
				} catch (CannotCompileException cce) {
					/*System.err.println("CannotCompileException: " + 
							cce.getMessage() + "; instrumenting method " + 
							m.getLongName() + 
							"; method will not be instrumented");*/
				}
			}
			// return the new bytecode array:
			byte[] newClassfileBuffer = null;
			
			try {
				newClassfileBuffer = cc.toBytecode();
			}
			catch (IOException ioe) {
				System.err.println("IOException: " + ioe.getMessage() + 
						"; transforming class " + className + 
						"; returning uninstrumented class");
				return null;
			}
			catch (CannotCompileException cce) {
				/*System.err.println("CannotCompileException: " + 
						cce.getMessage() + "; transforming class " + 
						className + "; returning uninstrumented class");
				return null;*/
			}
			
			return newClassfileBuffer;
		} else {
			return classfileBuffer;
		}
	}
	
	public void entryCall(String callName) {
		String id = "ENTER-" + 
		//Thread.currentThread().getName() + "-" + 
		callName;
		mc.collectMetrics(id);
		//print("***Wrapper: " + id);
	}
	
	public void exitCall(String callName) {
		String id = "EXIT-" + 
		//Thread.currentThread().getName() + "-" + 
		callName;
		mc.collectMetrics(id);
		//print("***Wrapper: " + id);
	}
	
	/*private void print(String str) {
		System.out.println(str);
	}*/
	
	/*private boolean isJavaMethod(String call) {
		return (call.charAt(0)=='j' && 
				call.charAt(1)=='a' && 
				call.charAt(2)=='v' && 
				call.charAt(3)=='a' &&
				call.charAt(4)=='/');
	}*/
	
	public void saveData() {
		mc.saveData();
	}
}
