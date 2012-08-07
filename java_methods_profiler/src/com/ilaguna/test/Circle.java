package com.ilaguna.test;

public class Circle extends Shape {
	
	private int size;
	
	public Circle(int s) {
		this.size = s;
	}

	public void printSize() {
		System.out.println("Size of circle is " + Integer.toString(size));
	}
	
	public void setSize(int s) {
		this.size = s;
	}
}
