package com.ilaguna.test;

import java.util.ArrayList;

public class TestShapes {
	
	public static void printSizes(Shape s) {
		s.printSize();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<Shape> listOfShapes = new ArrayList<Shape>();
		
		Shape s1 = new Circle(5);
		listOfShapes.add(s1);
		
		Shape s2 = new Circle(10);
		s2.setSize(3);
		listOfShapes.add(s2);
		
		Shape s3 = new Square(7);
		listOfShapes.add(s3);
		
		for (Shape s : listOfShapes) {
			printSizes(s);
		}
	}

}
