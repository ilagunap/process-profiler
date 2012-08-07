package com.ilaguna.metrics;

//import java.util.regex.Pattern;


public class SomeTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String str = "java/util/regex/Pattern$Curly";
		//String p = "^j[\\w|/]+$";
		String p = "java";
		
		//if (Pattern.matches(p, str)) {
		if (str.matches(p)) {
			System.out.println("Matches.");
		} else {
			System.out.println("DOES NOT Match!");
		}
	}
}
