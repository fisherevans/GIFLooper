package com.fisherevans.giflooper.components;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Project implements Serializable {
	private static final long serialVersionUID = -2028594137369529380L;
	
	public int frameCount;
	public List<Anchor> _anchors;
	public Map<Pair, Transition> _transitions;
	public Settings settings;
	
	public Project(int frameCount) {
		this.frameCount = frameCount;
		_anchors = new ArrayList<Anchor>();
		_transitions = new HashMap<Pair, Transition>();
		loadDefaults();
		settings = new Settings();
	}
	
	private void loadDefaults() {
		_anchors.add(new Anchor(0));
		_anchors.add(new Anchor(frameCount-1));
		_transitions.put(new Pair(0, 1), new Transition());
	}
	
	private class Pair implements Serializable {
		private static final long serialVersionUID = -4946865563702264409L;
		
		public int x, y;
		
		public Pair(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Pair) {
				Pair pair = (Pair) obj;
				return pair.x == x && pair.y == y;
			} else
				return false;
		}
	}
}
