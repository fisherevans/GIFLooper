package com.fisherevans.giflooper.components;

import java.io.Serializable;

public class Anchor implements Comparable<Anchor>, Serializable {
	private static final long serialVersionUID = -1070159902604884178L;
	
	public int frameID;
	public double deltaX, deltaY, scaleX, scaleY, radians;
	
	public Anchor(int frameID) {
		this.frameID = frameID;
		deltaX = 0;
		deltaY = 0;
		scaleX = 1;
		scaleY = 1;
		radians = 0;
	}
	
	public Anchor copy(int frameID) {
		Anchor anchor = new Anchor(frameID);
		anchor.deltaX = deltaX;
		anchor.deltaY = deltaY;
		anchor.scaleX = scaleX;
		anchor.scaleY = scaleY;
		anchor.radians = radians;
		return anchor;
	}

	@Override
	public int compareTo(Anchor anchor) {
		return anchor.frameID-frameID;
	}
}
