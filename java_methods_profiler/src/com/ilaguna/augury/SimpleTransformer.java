package com.ilaguna.augury;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.*;

public class SimpleTransformer implements ClassFileTransformer {
	
	ClassPool pool;
	
	public SimpleTransformer() {
		super();
		pool = ClassPool.getDefault();
	}
 
	@Override
	public byte[] transform(
			final ClassLoader loader, 
			final String className, 
			final Class<?> redefiningClass,
			final ProtectionDomain domain, 
			byte[] bytes) 
	throws IllegalClassFormatException {
		
		System.out.println("[Instrumenting class: " + className + 
				" (" + bytes.length + " bytes)]");
		
		// Java-assist code
		pool.insertClassPath(new ByteArrayClassPath(className, bytes));
		try {
			String name = className.replace("/",".");
			CtClass cc = pool.get(name);
			
			if (cc.isInterface() == false) {
				for (CtMethod m : cc.getDeclaredMethods()) {
					try {
						//System.out.println("Instrumenting: " + m.toString());
						m.insertBefore("{ System.out.println(\"---Hello---\"); }");
					} catch (CannotCompileException ce) {
						
					}
				}
				//cc.writeFile("./bin");
				
				bytes = cc.toBytecode();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return bytes;
	}
}
