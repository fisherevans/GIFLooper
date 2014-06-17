package com.fisherevans.giflooper.components;

import java.io.Serializable;

public class Transition implements Serializable {
	private static final long serialVersionUID = 7747538564144000653L;
	
	public double speed;
	public InterploationType interpolationType;
	
	public Transition() {
		speed = 1;
		interpolationType = InterploationType.Linear;
	}
}
