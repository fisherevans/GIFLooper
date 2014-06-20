package com.fisherevans.giflooper.test;

import java.awt.Rectangle;

public class Blah {
	public static void main(String[] args) {
		Rectangle a = new Rectangle(0, 0, 10, 10);
		Rectangle b = new Rectangle(2, 2, 5, 5);
		Rectangle c = new Rectangle(5, 5, 20, 30);

		System.out.println("A " + a);
		System.out.println("B " + b);
		System.out.println("C " + c);
		
		a.add(b);
		System.out.println("A+B " + a);
		c.add(b);
		System.out.println("C+B " + c);
	}
}
