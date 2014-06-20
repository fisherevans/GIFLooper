package com.fisherevans.giflooper.app.components;

import java.io.Serializable;

public class Settings implements Serializable {
	private static final long serialVersionUID = 4765516304297443905L;
	
	public boolean clearEachFrame, aa, timeCosineInterpolation, graphicsInterpolation;
    public double speed;
	
	public Settings() {
		clearEachFrame = true;
		aa = true;
		graphicsInterpolation = true;
		timeCosineInterpolation = true;
        speed = 1;
	}
}
