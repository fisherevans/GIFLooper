package com.fisherevans.giflooper.components;

import java.io.Serializable;

public class Settings implements Serializable {
	private static final long serialVersionUID = 4765516304297443905L;
	
	public boolean clearEachFrame, antialiasing, graphicsInterpolation;
	public int accuracy;
	
	public Settings() {
		clearEachFrame = false;
		antialiasing = true;
		graphicsInterpolation = true;
	}
}
