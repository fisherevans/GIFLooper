package com.fisherevans.giflooper.app.components;

import java.io.Serializable;

public class Anchor implements Comparable<Anchor>, Serializable {
	private static final long serialVersionUID = -1070159902604884178L;
	
	public int frameID;
	public double deltaX, deltaY, scaleX, scaleY, degrees;
	
	public Anchor(int frameID) {
		this.frameID = frameID;
		deltaX = 0;
		deltaY = 0;
		scaleX = 1;
		scaleY = 1;
		degrees = 0;
	}
	
	public Anchor copy(int frameID) {
		Anchor anchor = new Anchor(frameID);
		anchor.deltaX = deltaX;
		anchor.deltaY = deltaY;
		anchor.scaleX = scaleX;
		anchor.scaleY = scaleY;
		anchor.degrees = degrees;
		return anchor;
	}

	@Override
	public int compareTo(Anchor anchor) {
		return frameID-anchor.frameID;
	}
}
