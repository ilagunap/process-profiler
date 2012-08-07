package com.ilaguna.test;

public class Square extends Shape {
	
	private int size;
	
	public Square(int s) {
		this.size = s;
	}

	public void printSize() {
		System.out.println("Size of square is " + Integer.toString(size));
		printArea();
	}
	
	public void setSize(int s) {
		this.size = s;
	}
	
	public void printArea() {
		int a = size * size;
		System.out.println("Area of square is " + Integer.toString(a));
	}
}
