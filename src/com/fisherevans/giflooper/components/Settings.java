package com.fisherevans.giflooper.components;

import java.io.Serializable;

public class Settings implements Serializable {
	private static final long serialVersionUID = 4765516304297443905L;
	
	public boolean clearEachFrame, aa, graphicsInterpolation;
    public double accuracy, speed;
	
	public Settings() {
		clearEachFrame = false;
		aa = true;
		graphicsInterpolation = true;
        accuracy = 4;
        speed = 1;
	}
}
